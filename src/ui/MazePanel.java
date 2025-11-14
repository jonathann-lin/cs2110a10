package ui;

import graph.Pathfinding;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Random;
import graph.MazeGraph;
import graph.MazeGraph.MazeVertex;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.List;
import javax.swing.JPanel;
import graph.MazeGraph.Direction;
import graph.MazeGraph.MazeEdge;
import util.ElevationGenerator;
import util.GameMap;
import util.MazeGenerator;
import ui.Tile.TileType;

public class MazePanel extends JPanel {

    /**
     * Tile size (width and height) used to compute the preferred display size of a game board
     * [pixels].
     */
    public static final int PREFERRED_TILE_SIZE = 20;

    /**
     * 2D array of Tile objects that comprise the game's background
     */
    private Tile[][] tileGrid;

    /**
     * The maze graph that is being visualized
     */
    private MazeGraph graph;

    /**
     * The last edge that PacMann traversed prior to his current location.
     */
    private MazeEdge pacMannLastEdge;

    /**
     * The last edge that the ghost traversed prior to his current location.
     */
    private MazeEdge ghostLastEdge;

    /**
     * Used to generate new random mazes.
     */
    private final MazeGenerator mazeGen;

    /**
     * The width (in Tiles) of the maze
     */
    private final int mazeWidth;

    /**
     * The height (in Tiles) of the maze
     */
    private final int mazeHeight;

    /**
     * Used to randomly generate maze layouts, elevations, and chase paths
     */
    private final Random rng;

    /**
     * Whether to render the graph edges for debugging purposes.
     */
    private boolean showGraph;

    /**
     * Whether to render a ghost chase path on the maze.
     */
    private boolean showChase;

    /**
     * Constructs a new maze panel with a random initial maze.
     */
    public MazePanel(int width, int height) {
        setBackground(Color.BLACK);

        mazeWidth = 3 * width + 2;
        mazeHeight = 3 * height + 2;

        showGraph = false;
        showChase = false;

        rng = new Random();
        mazeGen = new MazeGenerator(width, height, rng);

        newMaze();
    }

    /**
     * Updates the panel to contain a new randomly generated maze.
     */
    public void newMaze() {
        MazeGenerator.TileType[][] types = mazeGen.generateMaze();

        double[][] elevations = ElevationGenerator.generateElevations(mazeWidth, mazeHeight, rng);

        tileGrid = new Tile[mazeWidth][mazeHeight];
        for (int i = 0; i < mazeWidth; i++) {
            for (int j = 0; j < mazeHeight; j++) {
                tileGrid[i][j] = new Tile(getTileType(types, i, j), i, j, elevations[i][j]);
            }
        }
        setPreferredSize(new Dimension(PREFERRED_TILE_SIZE * tileGrid.length,
                PREFERRED_TILE_SIZE * tileGrid[0].length));

        graph = new MazeGraph(new GameMap(types, elevations));
        updateChasePath();
    }

    /**
     * Returns a random edge from the current maze graph.
     */
    private MazeEdge randomEdge() {
        MazeVertex v = graph.closestTo(rng.nextInt(mazeWidth), rng.nextInt(mazeHeight));
        ArrayList<MazeEdge> edges = new ArrayList<>();
        for (MazeEdge e : v.outgoingEdges()) {
            edges.add(e);
        }
        return edges.get(rng.nextInt(edges.size()));
    }

    /**
     * Respawns PacMann and the ghost at new random locations and visualizes the new ghost chase path.
     */
    public void updateChasePath() {
        if (showChase) {
            pacMannLastEdge = randomEdge();
            ghostLastEdge = randomEdge();
        }
        repaint();
    }

    /**
     * Toggles whether the graph vertices and edges should be visualized.
     */
    public void toggleGraph() {
        showGraph = !showGraph;
        repaint();
    }

    /**
     * Toggles whether a ghost chase path should be visualized.
     */
    public void toggleChasePath() {
        showChase = !showChase;
        if (showChase && ghostLastEdge == null) { // first chase path
            ghostLastEdge = randomEdge();
            pacMannLastEdge = randomEdge();
        }
        repaint();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        Dimension size = getSize();
        int height = tileGrid.length;
        int width = tileGrid[0].length;

        // Determine the largest fitting tile size, then shift coordinates so that board will be
        //  centered.  Scaling allows shapes to be defined using normalized coordinates.
        int tileDim = Math.min(size.width / width, size.height / height);
        int hOffset = size.width - tileDim * width;
        int vOffset = size.height - tileDim * height;
        g2.translate(hOffset / 2, vOffset / 2);
        g2.scale(tileDim, tileDim);

        g2.setStroke(new BasicStroke(3.0f / tileDim));
        // background tiles
        for (Tile[] row : tileGrid) {
            for (Tile tile : row) {
                tile.paint(g2);
            }
        }

        if (showGraph) {
            for (MazeVertex v : graph.vertices()) {
                paintVertex(v, g2);
            }
        }

        if (showChase) {
            paintChasePaths(g2);
        }
    }

    /**
     * Paints PacMann, the ghost, and the chase path between them.
     */
    void paintChasePaths(Graphics2D g2) {
        paintPath(g2, Color.RED, Pathfinding.shortestNonBacktrackingPath(ghostLastEdge.head(),
                pacMannLastEdge.head(), ghostLastEdge));
        paintGhost(g2, Color.RED);
        paintPacMann(g2);
    }

    /**
     * Paints the given vertex `v` and its outgoing edges.
     */
    void paintVertex(MazeVertex v, Graphics2D g2) {
        g2.setColor(Color.GREEN);
        g2.setStroke(new BasicStroke(0.05f));

        int i = v.loc().i();
        int j = v.loc().j();

        // draw circle for vertex
        g2.draw(new Ellipse2D.Double(i + 0.2, j + 0.2, 0.6, 0.6));

        // draw lines to represent outgoing edges
        if (v.edgeInDirection(Direction.LEFT) != null) {
            MazeVertex v2 = v.edgeInDirection(Direction.LEFT).head();
            int i2 = v2.loc().i();
            int j2 = v2.loc().j();

            if (i > 0) {
                g2.draw(new Line2D.Double(i + 0.2, j + 0.4, i2 + 0.8, j2 + 0.4));
            } else if (i2 == mazeWidth - 1) {
                g2.draw(new Line2D.Double(0.2, j + 0.4, 0, j2 + 0.4));
                g2.draw(new Line2D.Double(mazeWidth, j + 0.4, mazeWidth - 0.2, j2 + 0.4));
            }
        }
        if (v.edgeInDirection(Direction.RIGHT) != null) {
            MazeVertex v2 = v.edgeInDirection(Direction.RIGHT).head();
            int i2 = v2.loc().i();
            int j2 = v2.loc().j();

            if (i < mazeWidth - 1) {
                g2.draw(new Line2D.Double(i + 0.8, j + 0.6, i2 + 0.2, j2 + 0.6));
            } else if (i2 == 0) {
                g2.draw(new Line2D.Double(mazeWidth - 0.2, j + 0.6, mazeWidth, j2 + 0.6));
                g2.draw(new Line2D.Double(0, j + 0.6, 0.2, j2 + 0.6));
            }
        }
        if (v.edgeInDirection(Direction.UP) != null) {
            MazeVertex v2 = v.edgeInDirection(Direction.UP).head();
            int i2 = v2.loc().i();
            int j2 = v2.loc().j();

            if (j > 0) {
                g2.draw(new Line2D.Double(i + 0.4, j + 0.2, i2 + 0.4, j2 + 0.8));
            } else if (j2 == mazeHeight - 1) {
                g2.draw(new Line2D.Double(i + 0.4, 0.2, i2 + 0.4, 0));
                g2.draw(new Line2D.Double(i + 0.4, mazeHeight, i2 + 0.4, mazeHeight - 0.2));
            }
        }
        if (v.edgeInDirection(Direction.DOWN) != null) {
            MazeVertex v2 = v.edgeInDirection(Direction.DOWN).head();
            int i2 = v2.loc().i();
            int j2 = v2.loc().j();

            if (j < mazeHeight - 1) {
                g2.draw(new Line2D.Double(i + 0.6, j + 0.8, i2 + 0.6, j2 + 0.2));
            } else if (j2 == 0) {
                g2.draw(new Line2D.Double(i + 0.6, mazeHeight - 0.2, i2 + 0.6, mazeHeight));
                g2.draw(new Line2D.Double(i + 0.6, 0, i2 + 0.6, 0.2));
            }
        }
    }

    /**
     * Paints PacMann at his current position in the maze.
     */
    public void paintPacMann(Graphics2D g2) {
        g2.setColor(Color.YELLOW);

        double i = pacMannLastEdge.head().loc().i() - 0.25;
        double j = pacMannLastEdge.head().loc().j() - 0.25;

        int startAngle = switch (pacMannLastEdge.direction()) {
            case Direction.LEFT -> 210;
            case Direction.RIGHT -> 30;
            case Direction.UP -> 120;
            case Direction.DOWN -> 300;
        };

        g2.fill(new Arc2D.Double(i, j, 1.5, 1.5, startAngle, 300, Arc2D.PIE));
    }

    /**
     * Paints the ghost in the given `color` at its current position in the maze.
     */
    public void paintGhost(Graphics2D g2, Color color) {

        // body
        g2.setColor(color);
        double i = ghostLastEdge.head().loc().i() - 0.25;
        double j = ghostLastEdge.head().loc().j() - 0.25;
        g2.fill(new Arc2D.Double(i + 0.1, j, 1.3, 1.3, 0, 180, Arc2D.PIE));
        Path2D.Double ghostBottom = new Path2D.Double(Path2D.WIND_NON_ZERO);
        ghostBottom.moveTo(i + 0.1, j + 0.65);
        ghostBottom.lineTo(i + 1.4, j + 0.65);
        ghostBottom.lineTo(i + 1.4, j + 1.5);
        ghostBottom.lineTo(i + 1.15, j + 1.25);
        ghostBottom.lineTo(i + 0.95, j + 1.5);
        ghostBottom.lineTo(i + 0.75, j + 1.25);
        ghostBottom.lineTo(i + 0.55, j + 1.5);
        ghostBottom.lineTo(i + 0.35, j + 1.25);
        ghostBottom.lineTo(i + 0.1, j + 1.5);
        ghostBottom.closePath();
        g2.fill(ghostBottom);

        // eye whites
        g2.setColor(Color.WHITE);
        double eyeX = i + switch (ghostLastEdge.direction()) {
            case Direction.LEFT -> 0.2;
            case Direction.RIGHT -> 0.4;
            default -> 0.3;
        };
        double eyeY = j + switch (ghostLastEdge.direction()) {
            case Direction.UP -> 0.2;
            case Direction.DOWN -> 0.4;
            default -> 0.3;
        };

        g2.fill(new Ellipse2D.Double(eyeX, eyeY, 0.4, 0.5));
        g2.fill(new Ellipse2D.Double(eyeX + 0.5, eyeY, 0.4, 0.5));

        // pupils
        g2.setColor(Color.BLUE);
        double pupilX = i + switch (ghostLastEdge.direction()) {
            case Direction.LEFT -> 0.2;
            case Direction.RIGHT -> 0.55;
            default -> 0.375;
        };
        double pupilY = j + switch (ghostLastEdge.direction()) {
            case Direction.UP -> 0.2;
            case Direction.DOWN -> 0.65;
            default -> 0.425;
        };

        g2.fill(new Ellipse2D.Double(pupilX, pupilY, 0.25, 0.25));
        g2.fill(new Ellipse2D.Double(pupilX + 0.5, pupilY, 0.25, 0.25));
    }

    /**
     * Visualizes the given chase `path` on the maze.
     */
    public void paintPath(Graphics2D g2, Color color, List<MazeEdge> path) {
        if (path == null || path.isEmpty()) {
            return;
        }
        Path2D.Double shape = new Path2D.Double();
        var prev = path.getFirst().tail().loc();
        shape.moveTo(prev.i() + 0.5, prev.j() + 0.5);
        for (var e : path) {
            var pt = e.head().loc();
            if (Math.abs(pt.i() - prev.i()) + Math.abs(pt.j() - prev.j()) > 1) {
                shape.moveTo(pt.i() + 0.5, pt.j() + 0.5);
            } else {
                shape.lineTo(pt.i() + 0.5, pt.j() + 0.5);
            }
            prev = pt;
        }
        g2.setColor(color);
        g2.draw(shape);
    }

    /**
     * Return the type of tile that should be drawn in a particular location. If this is a wall
     * tile, its type determined based on whether the surrounding tiles are paths or walls.
     */
    private TileType getTileType(MazeGenerator.TileType[][] types, int i, int j) {
        if (types[i][j] != MazeGenerator.TileType.WALL) {
            return new TileType(0,0);
        }

        // left tunnel edges
        if (i == 0 && j > 1 && j < mazeHeight - 2) {
            if (types[i][j - 2] != MazeGenerator.TileType.WALL) {
                return new TileType(4, 3);
            } else if (types[i][j - 1] != MazeGenerator.TileType.WALL) {
                return new TileType(3, 2);
            } else if (types[i][j + 1] != MazeGenerator.TileType.WALL) {
                return new TileType(3, 0);
            } else if (types[i][j + 2] != MazeGenerator.TileType.WALL) {
                return new TileType(4, 0);
            }
        }

        // right tunnel edges
        if (i == mazeWidth - 1 && j > 1 && j < mazeHeight - 2) {
            if (types[i][j - 2] != MazeGenerator.TileType.WALL) {
                return new TileType(4, 2);
            } else if (types[i][j - 1] != MazeGenerator.TileType.WALL) {
                return new TileType(3, 2);
            } else if (types[i][j + 1] != MazeGenerator.TileType.WALL) {
                return new TileType(3, 0);
            } else if (types[i][j + 2] != MazeGenerator.TileType.WALL) {
                return new TileType(4, 1);
            }
        }

        // top tunnel edges
        if (j == 0 && i > 1 && i < mazeWidth - 2) {
            if (types[i - 2][j] != MazeGenerator.TileType.WALL) {
                return new TileType(4, 1);
            } else if (types[i - 1][j] != MazeGenerator.TileType.WALL) {
                return new TileType(3, 1);
            } else if (types[i + 1][j] != MazeGenerator.TileType.WALL) {
                return new TileType(3, 3);
            } else if (types[i + 2][j] != MazeGenerator.TileType.WALL) {
                return new TileType(4, 0);
            }
        }

        // bottom tunnel edges
        if (j == mazeHeight - 1 && i > 1 && i < mazeWidth - 2) {
            if (types[i - 2][j] != MazeGenerator.TileType.WALL) {
                return new TileType(4, 2);
            } else if (types[i - 1][j] != MazeGenerator.TileType.WALL) {
                return new TileType(3, 1);
            } else if (types[i + 1][j] != MazeGenerator.TileType.WALL) {
                return new TileType(3, 3);
            } else if (types[i + 2][j] != MazeGenerator.TileType.WALL) {
                return new TileType(4, 3);
            }
        }

        int up = j > 0 && types[i][j - 1] == MazeGenerator.TileType.WALL ? 1 : 0;
        int down = j < mazeHeight - 1 && types[i][j + 1] == MazeGenerator.TileType.WALL ? 1 : 0;
        int left = i > 0 && types[i - 1][j] == MazeGenerator.TileType.WALL ? 1 : 0;
        int right = i < mazeWidth - 1 && types[i + 1][j] == MazeGenerator.TileType.WALL ? 1 : 0;
        int numWalls = up + down + left + right;

        if (numWalls == 2) { // wall cap
            return new TileType(2, down + right + 2 * down * left);
        } else if (numWalls == 3) { // wall side
            return new TileType(3, 2 * down + left - right);
        } else { // wall joint
            if (types[i + 1][j - 1] != MazeGenerator.TileType.WALL) { // northeast
                return new TileType(4, 1);
            } else if (types[i - 1][j - 1] != MazeGenerator.TileType.WALL) { // northwest
                return new TileType(4, 0);
            } else if (types[i + 1][j + 1] != MazeGenerator.TileType.WALL) { // southeast
                return new TileType(4, 2);
            } else { // southwest
                return new TileType(4, 3);
            }
        }
    }
}
