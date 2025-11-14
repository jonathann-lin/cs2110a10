package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * A window that allows playing an interactive game of PacMann.  Consists of a score/lives label,
 * buttons for pausing the current game and creating a new game, and a game board view that supports
 * keyboard input.
 */
public class A10PacMannVisualization extends JFrame {

    /**
     * The component that visualizes the game's maze and actors.
     */
    private final MazePanel mazePanel;

    /**
     * Create a new window to visualize MazeGraph creation and path finding.
     */
    public A10PacMannVisualization(int width, int height) {
        super("PacMann");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        mazePanel = new MazePanel(width, height);
        add(mazePanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(true);
        buttonPanel.setBackground(Color.BLACK);
        add(buttonPanel, BorderLayout.PAGE_END);

        JButton nextMapButton = new JButton("Next Map");
        nextMapButton.addActionListener(e -> mazePanel.newMaze());
        buttonPanel.add(nextMapButton);

        JCheckBox showGraphOption = new JCheckBox("Show Graph Edges");
        showGraphOption.setForeground(Color.WHITE);
        showGraphOption.addActionListener( e -> mazePanel.toggleGraph());
        buttonPanel.add(showGraphOption);

        JCheckBox showChaseOption = new JCheckBox("Show Chase Path");
        showChaseOption.setForeground(Color.WHITE);
        showChaseOption.addActionListener( e -> mazePanel.toggleChasePath());
        buttonPanel.add(showChaseOption);

        JButton showChasePathOption = new JButton("Next Chase");
        showChasePathOption.addActionListener( e -> mazePanel.updateChasePath());
        buttonPanel.add(showChasePathOption);
        pack();
    }

    /**
     * Process the program arguments and construct/show the GameFrame on the event dispatch thread.
     */
    public static void main(String[] args) {
        // feel free to update these values to test different maze sizes.
        int mazeWidth = 10; // must be >= 4
        int mazeHeight = 10; // must be >= 3

        // Create and start GUI
        SwingUtilities.invokeLater(() -> {
            A10PacMannVisualization frame = new A10PacMannVisualization(mazeWidth, mazeHeight);
            frame.setVisible(true);
        });
    }
}
