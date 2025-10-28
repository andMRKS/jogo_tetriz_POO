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
        // Create the status bar label
        statusBar = new JLabel(" Score: 0");
        add(statusBar, BorderLayout.SOUTH); // Add it to the bottom of the window

        // Create the game board
        Board board = new Board(this);
        add(board); // Add the board to the center

        // Start the game logic in the board
        board.start();

        // Standard window setup
        setTitle("Tetris");
        setSize(300, 600); // Set window size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window
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