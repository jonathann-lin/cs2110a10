package graph;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import graph.MazeGraph.Direction;
import graph.MazeGraph.MazeEdge;
import graph.MazeGraph.IPair;
import graph.MazeGraph.MazeVertex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import util.GameMap;
import util.MazeGenerator.TileType;

public class MazeGraphTest {

    /* Note, to conform to the precondition of the `MazeGraph` constructor, make sure that any
     * TileType arrays that you construct contain a `PATH` tile at index [2][2] and represent a
     * single, orthogonally connected component of `PATH` tiles. */

    /**
     * Create a game map with tile types corresponding to the letters on each line of `template`.
     * 'w' = WALL, 'p' = PATH, and 'g' = GHOSTBOX.  The letters of `template` must form a rectangle.
     * Elevations will be a gradient from the top-left to the bottom-right corner with a horizontal
     * slope of 2 and a vertical slope of 1.
     */
    GameMap createMap(String template) {
        Scanner lines = new Scanner(template);
        ArrayList<ArrayList<TileType>> lineLists = new ArrayList<>();

        while (lines.hasNextLine()) {
            ArrayList<TileType> lineList = new ArrayList<>();
            for (char c : lines.nextLine().toCharArray()) {
                switch (c) {
                    case 'w' -> lineList.add(TileType.WALL);
                    case 'p' -> lineList.add(TileType.PATH);
                    case 'g' -> lineList.add(TileType.GHOSTBOX);
                }
            }
            lineLists.add(lineList);
        }

        int height = lineLists.size();
        int width = lineLists.getFirst().size();

        TileType[][] types = new TileType[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                types[i][j] = lineLists.get(j).get(i);
            }
        }

        double[][] elevations = new double[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                elevations[i][j] = (2.0 * i + j);
            }
        }
        return new GameMap(types, elevations);
    }


    @DisplayName("WHEN a GameMap with exactly one path tile in position [2][2] is passed into the "
            + "MazeGraph constructor, THEN a graph with one vertex is created.")
    @Test
    void testOnePathCell() {
        GameMap map = createMap("""
                wwwww
                wwwww
                wwpww
                wwwww
                wwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        assertEquals(1, vertices.size());
        assertTrue(vertices.containsKey(new IPair(2, 2)));
    }

    @DisplayName("WHEN a GameMap with exactly two horizontally adjacent path tiles is passed into "
            + "the MazeGraph constructor, THEN a graph with two vertices is created in which the two "
            + "vertices are connected by two directed edges with weights determined by evaluating "
            + "`MazeGraph.edgeWeight` on their elevations.")
    @Test
    void testTwoPathCellsHorizontal() {
        GameMap map = createMap("""
                wwwww
                wwwww
                wwppw
                wwwww
                wwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        // graph contains two vertices with the correct locations
        assertEquals(2, vertices.size());
        IPair left = new IPair(2, 2);
        IPair right = new IPair(3, 2);
        assertTrue(vertices.containsKey(left));
        assertTrue(vertices.containsKey(right));

        MazeVertex vl = vertices.get(left);
        MazeVertex vr = vertices.get(right);

        // left vertex has one edge to the vertex to its right
        assertNull(vl.edgeInDirection(Direction.LEFT));
        assertNull(vl.edgeInDirection(Direction.UP));
        assertNull(vl.edgeInDirection(Direction.DOWN));
        MazeEdge l2r = vl.edgeInDirection(Direction.RIGHT);
        assertNotNull(l2r);

        // edge from left to right has the correct fields
        double lElev = map.elevations()[2][2];
        double rElev = map.elevations()[3][2];
        assertEquals(vl, l2r.tail());
        assertEquals(vr, l2r.head());
        assertEquals(Direction.RIGHT, l2r.direction());
        assertEquals(MazeGraph.edgeWeight(lElev, rElev), l2r.weight());

        // right vertex has one edge to the vertex to its left with the correct fields
        assertNull(vr.edgeInDirection(Direction.RIGHT));
        assertNull(vr.edgeInDirection(Direction.UP));
        assertNull(vr.edgeInDirection(Direction.DOWN));
        MazeEdge r2l = vr.edgeInDirection(Direction.LEFT);
        assertNotNull(r2l);
        assertEquals(vr, r2l.tail());
        assertEquals(vl, r2l.head());
        assertEquals(Direction.LEFT, r2l.direction());
        assertEquals(MazeGraph.edgeWeight(rElev, lElev), r2l.weight());
    }

    @DisplayName("WHEN a GameMap with exactly two vertically adjacent path tiles is passed into "
            + "the MazeGraph constructor, THEN a graph with two vertices is created in which the two "
            + "vertices are connected by two directed edges with weights determined by evaluating "
            + "`MazeGraph.edgeWeight` on their elevations.")
    @Test
    void testTwoPathCellsVertical() {
        GameMap map = createMap("""
                wwwww
                wwwww
                wwpww
                wwpww
                wwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        // graph contains two vertices with the correct locations
        assertEquals(2, vertices.size());
        IPair top = new IPair(2, 2);
        IPair bottom = new IPair(2, 3);
        assertTrue(vertices.containsKey(top));
        assertTrue(vertices.containsKey(bottom));

        MazeVertex vt = vertices.get(top);
        MazeVertex vb = vertices.get(bottom);

        // top vertex has one edge to the vertex below it
        assertNull(vt.edgeInDirection(Direction.LEFT));
        assertNull(vt.edgeInDirection(Direction.UP));
        assertNull(vt.edgeInDirection(Direction.RIGHT));
        MazeEdge t2b = vt.edgeInDirection(Direction.DOWN);
        assertNotNull(t2b);

        // edge from top to bottom has the correct fields
        double tElev = map.elevations()[2][2];
        double bElev = map.elevations()[2][3];
        assertEquals(vt, t2b.tail());
        assertEquals(vb, t2b.head());
        assertEquals(Direction.DOWN, t2b.direction());
        assertEquals(MazeGraph.edgeWeight(tElev, bElev), t2b.weight());

        // bottom vertex has one edge to the vertex upwards with the correct fields
        assertNull(vb.edgeInDirection(Direction.RIGHT));
        assertNull(vb.edgeInDirection(Direction.DOWN));
        assertNull(vb.edgeInDirection(Direction.LEFT));
        MazeEdge b2t = vb.edgeInDirection(Direction.UP);
        assertNotNull(b2t);
        assertEquals(vb, b2t.tail());
        assertEquals(vt, b2t.head());
        assertEquals(Direction.UP, b2t.direction());
        assertEquals(MazeGraph.edgeWeight(bElev, tElev), b2t.weight());

    }

    @DisplayName("WHEN a GameMap includes two path tiles in the first and last column of the same "
            + "row, THEN (tunnel) edges are created between these tiles with the correct properties.")
    @Test
    void testHorizontalTunnelEdgeCreation() {
        GameMap map = createMap("""
            wwwwww
            wwwwww
            pppwwp
            wwwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        assertEquals(4, vertices.size());
        IPair left = new IPair(0, 2);
        IPair right = new IPair(5, 2);
        assertTrue(vertices.containsKey(left));
        assertTrue(vertices.containsKey(right));

        MazeVertex vl = vertices.get(left);
        MazeVertex vr = vertices.get(right);

        assertNull(vl.edgeInDirection(Direction.UP));
        assertNull(vl.edgeInDirection(Direction.DOWN));
        MazeEdge l2r = vl.edgeInDirection(Direction.LEFT);
        assertNotNull(l2r);

        double lElev = map.elevations()[0][2];
        double rElev = map.elevations()[5][2];

        assertEquals(vl, l2r.tail());
        assertEquals(vr, l2r.head());
        assertEquals(Direction.LEFT, l2r.direction());
        assertEquals(MazeGraph.edgeWeight(lElev, rElev), l2r.weight());

        // right vertex has one edge to the vertex on the left (tunnel)
        assertNull(vr.edgeInDirection(Direction.LEFT));
        assertNull(vr.edgeInDirection(Direction.UP));
        assertNull(vr.edgeInDirection(Direction.DOWN));
        MazeEdge r2l = vr.edgeInDirection(Direction.RIGHT);
        assertNotNull(r2l);

        assertEquals(vr, r2l.tail());
        assertEquals(vl, r2l.head());
        assertEquals(Direction.RIGHT, r2l.direction());
        assertEquals(MazeGraph.edgeWeight(rElev, lElev), r2l.weight());

    }

    @DisplayName("WHEN a GameMap includes a cyclic connected component of path tiles with a "
            + "non-path tiles in the middle, THEN its graph includes edges between all adjacent "
            + "pairs of vertices.")
    @Test
    void testCyclicPaths() {
        GameMap map = createMap("""
                wwwwwww
                wwwwwww
                wwpppww
                wwpwpww
                wwpppww
                wwwwwww""");
        MazeGraph graph = new MazeGraph(map);
        Map<IPair, MazeVertex> vertices = new HashMap<>();
        graph.vertices().forEach(v -> vertices.put(v.loc(), v));

        assertEquals(8, vertices.size());
        IPair p22 = new IPair(2, 2);
        IPair p32 = new IPair(3, 2);
        IPair p42 = new IPair(4, 2);
        IPair p43 = new IPair(4, 3);
        IPair p44 = new IPair(4, 4);
        IPair p34 = new IPair(3, 4);
        IPair p24 = new IPair(2, 4);
        IPair p23 = new IPair(2, 3);
        assertTrue(vertices.containsKey(p22));
        assertTrue(vertices.containsKey(p32));
        assertTrue(vertices.containsKey(p42));
        assertTrue(vertices.containsKey(p43));
        assertTrue(vertices.containsKey(p44));
        assertTrue(vertices.containsKey(p34));
        assertTrue(vertices.containsKey(p24));
        assertTrue(vertices.containsKey(p23));

        MazeVertex v22 = vertices.get(p22);
        MazeVertex v32 = vertices.get(p32);
        MazeVertex v42 = vertices.get(p42);
        MazeVertex v23 = vertices.get(p23);
        MazeVertex v43 = vertices.get(p43);
        MazeVertex v24 = vertices.get(p24);
        MazeVertex v34 = vertices.get(p34);
        MazeVertex v44 = vertices.get(p44);

        double e22 = map.elevations()[2][2];
        double e32 = map.elevations()[3][2];
        double e42 = map.elevations()[4][2];
        double e23 = map.elevations()[2][3];
        double e43 = map.elevations()[4][3];
        double e24 = map.elevations()[2][4];
        double e34 = map.elevations()[3][4];
        double e44 = map.elevations()[4][4];

        // Edges from v22
        MazeEdge e22_32 = v22.edgeInDirection(Direction.RIGHT);
        assertNotNull(e22_32);
        assertEquals(v22, e22_32.tail());
        assertEquals(v32, e22_32.head());
        assertEquals(MazeGraph.edgeWeight(e22, e32), e22_32.weight());

        MazeEdge e22_23 = v22.edgeInDirection(Direction.DOWN);
        assertNotNull(e22_23);
        assertEquals(v22, e22_23.tail());
        assertEquals(v23, e22_23.head());
        assertEquals(MazeGraph.edgeWeight(e22, e23), e22_23.weight());

        assertNull(v22.edgeInDirection(Direction.LEFT));
        assertNull(v22.edgeInDirection(Direction.UP));

        // Edges from v32
        MazeEdge e32_42 = v32.edgeInDirection(Direction.RIGHT);
        assertNotNull(e32_42);
        assertEquals(v32, e32_42.tail());
        assertEquals(v42, e32_42.head());
        assertEquals(MazeGraph.edgeWeight(e32, e42), e32_42.weight());

        MazeEdge e32_22 = v32.edgeInDirection(Direction.LEFT);
        assertNotNull(e32_22);
        assertEquals(v32, e32_22.tail());
        assertEquals(v22, e32_22.head());
        assertEquals(MazeGraph.edgeWeight(e32, e22), e32_22.weight());

        assertNull(v32.edgeInDirection(Direction.DOWN));
        assertNull(v32.edgeInDirection(Direction.UP));

        // Edges from v42
        MazeEdge e42_43 = v42.edgeInDirection(Direction.DOWN);
        assertNotNull(e42_43);
        assertEquals(v42, e42_43.tail());
        assertEquals(v43, e42_43.head());
        assertEquals(MazeGraph.edgeWeight(e42, e43), e42_43.weight());

        MazeEdge e42_32 = v42.edgeInDirection(Direction.LEFT);
        assertNotNull(e42_32);
        assertEquals(v42, e42_32.tail());
        assertEquals(v32, e42_32.head());
        assertEquals(MazeGraph.edgeWeight(e42, e32), e42_32.weight());

        assertNull(v42.edgeInDirection(Direction.RIGHT));
        assertNull(v42.edgeInDirection(Direction.UP));

        // Edges from v43
        MazeEdge e43_44 = v43.edgeInDirection(Direction.DOWN);
        assertNotNull(e43_44);
        assertEquals(v43, e43_44.tail());
        assertEquals(v44, e43_44.head());
        assertEquals(MazeGraph.edgeWeight(e43, e44), e43_44.weight());

        MazeEdge e43_42 = v43.edgeInDirection(Direction.UP);
        assertNotNull(e43_42);
        assertEquals(v43, e43_42.tail());
        assertEquals(v42, e43_42.head());
        assertEquals(MazeGraph.edgeWeight(e43, e42), e43_42.weight());

        assertNull(v43.edgeInDirection(Direction.LEFT));
        assertNull(v43.edgeInDirection(Direction.RIGHT));

        // Edges from v44
        MazeEdge e44_34 = v44.edgeInDirection(Direction.LEFT);
        assertNotNull(e44_34);
        assertEquals(v44, e44_34.tail());
        assertEquals(v34, e44_34.head());
        assertEquals(MazeGraph.edgeWeight(e44, e34), e44_34.weight());

        MazeEdge e44_43 = v44.edgeInDirection(Direction.UP);
        assertNotNull(e44_43);
        assertEquals(v44, e44_43.tail());
        assertEquals(v43, e44_43.head());
        assertEquals(MazeGraph.edgeWeight(e44, e43), e44_43.weight());

        assertNull(v44.edgeInDirection(Direction.RIGHT));
        assertNull(v44.edgeInDirection(Direction.DOWN));

        // Edges from v34
        MazeEdge e34_24 = v34.edgeInDirection(Direction.LEFT);
        assertNotNull(e34_24);
        assertEquals(v34, e34_24.tail());
        assertEquals(v24, e34_24.head());
        assertEquals(MazeGraph.edgeWeight(e34, e24), e34_24.weight());

        MazeEdge e34_44 = v34.edgeInDirection(Direction.RIGHT);
        assertNotNull(e34_44);
        assertEquals(v34, e34_44.tail());
        assertEquals(v44, e34_44.head());
        assertEquals(MazeGraph.edgeWeight(e34, e44), e34_44.weight());

        assertNull(v34.edgeInDirection(Direction.DOWN));
        assertNull(v34.edgeInDirection(Direction.UP));

        // Edges from v24
        MazeEdge e24_23 = v24.edgeInDirection(Direction.UP);
        assertNotNull(e24_23);
        assertEquals(v24, e24_23.tail());
        assertEquals(v23, e24_23.head());
        assertEquals(MazeGraph.edgeWeight(e24, e23), e24_23.weight());

        MazeEdge e24_34 = v24.edgeInDirection(Direction.RIGHT);
        assertNotNull(e24_34);
        assertEquals(v24, e24_34.tail());
        assertEquals(v34, e24_34.head());
        assertEquals(MazeGraph.edgeWeight(e24, e34), e24_34.weight());

        assertNull(v24.edgeInDirection(Direction.LEFT));
        assertNull(v24.edgeInDirection(Direction.DOWN));

        // Edges from v23
        MazeEdge e23_22 = v23.edgeInDirection(Direction.UP);
        assertNotNull(e23_22);
        assertEquals(v23, e23_22.tail());
        assertEquals(v22, e23_22.head());
        assertEquals(MazeGraph.edgeWeight(e23, e22), e23_22.weight());

        MazeEdge e23_24 = v23.edgeInDirection(Direction.DOWN);
        assertNotNull(e23_24);
        assertEquals(v23, e23_24.tail());
        assertEquals(v24, e23_24.head());
        assertEquals(MazeGraph.edgeWeight(e23, e24), e23_24.weight());

        assertNull(v23.edgeInDirection(Direction.LEFT));
        assertNull(v23.edgeInDirection(Direction.RIGHT));


    }

    // TODO 2d: Add at least two additional test cases that cover other distinct scenarios that can
    //  arise in `GameMap`s. It is crucial that your graph is being linked together correctly,
    //  otherwise the later portions of this and the next assignment will break with strange
    //  behaviors.

    @DisplayName("WHEN a GameMap contains vertices in all 4 corners of the game map, their edges are"
            + " correctly connected")
    @Test
    void test() {}
}
