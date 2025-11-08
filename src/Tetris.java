import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.BorderLayout;

/**
 * Main application class.
 * This class creates the main window (JFrame) and adds the game board to it.
 */
public class Tetris extends JFrame {

    private JLabel statusBar;

    public Tetris() {
        // Set the layout for the main window
        setLayout(new BorderLayout());

        // Create the status bar label
        statusBar = new JLabel(" Score: 0 | Level: 1");
        add(statusBar, BorderLayout.SOUTH); // Add it to the bottom of the window

        // Create the game board
        Board board = new Board(this);
        add(board, BorderLayout.CENTER); // Add the board to the center

        // Create and add the side panel
        SidePanel sidePanel = new SidePanel(board);
        add(sidePanel, BorderLayout.EAST); // Add the side panel to the right

        // Start the game logic in the board
        board.start();

        // --- Window Size Update ---
        // We are using the 15-block width (15 * 32 = 480)
        // We add 120 pixels for the new SidePanel (480 + 120 = 600)
        int newWidth = 600;
        int newHeight = 744; // 22 blocks * 32 pixels/block + ~40px for bars

        setTitle("Tetris");
        setSize(newWidth, newHeight); // Set NEW window size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        // Important: pack() and setResizable(false) makes the layout clean
        // pack(); // Calculates the size based on components
        setResizable(false); // Prevents ugly resizing
    }

    /**
     * A getter for the Board class to update the score.
     * @return The JLabel used as a status bar.
     */
    public JLabel getStatusBar() {
        return statusBar;
    }

    public static void main(String[] args) {
        // Run the game on the Swing Event Dispatch Thread (EDT)
        javax.swing.SwingUtilities.invokeLater(() -> {
            Tetris game = new Tetris();
            game.setVisible(true);
        });
    }
}