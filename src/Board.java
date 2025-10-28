import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * The game board where all the action happens.
 * Handles game state, rendering, and player input.
 */
public class Board extends JPanel implements ActionListener {

    // --- Constants ---
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 22; // 22 rows (20 visible)
    private static final int GAME_SPEED = 300; // Milliseconds per game tick

    // --- Game State ---
    private Timer timer;                // Controls the game loop
    private boolean isStarted = false;  // Is the game running?
    private boolean isPaused = false;   // Is the game paused?
    private boolean isFallingFinished = false; // Is the current piece done falling?

    private int score = 0;
    private int curX = 0; // Current X position of the piece
    private int curY = 0; // Current Y position of the piece

    private Shape curPiece;             // The active, falling piece
    private Tetrominoe[] board;         // The grid of settled pieces
    private Tetris parent;              // Reference to the main Tetris frame

    public Board(Tetris parent) {
        this.parent = parent;
        setFocusable(true); // Allow the board to receive keyboard input
        setBackground(Color.BLACK);

        curPiece = new Shape();
        timer = new Timer(GAME_SPEED, this); // Game timer
        board = new Tetrominoe[BOARD_WIDTH * BOARD_HEIGHT];
        addKeyListener(new TAdapter());
    }

    /**
     * Starts the game.
     */
    public void start() {
        if (isPaused) {
            return;
        }

        isStarted = true;
        isFallingFinished = false;
        score = 0;
        clearBoard();
        newPiece();
        timer.start();
    }

    /**
     * Pauses/unpauses the game.
     */
    private void pause() {
        if (!isStarted) {
            return;
        }

        isPaused = !isPaused;

        if (isPaused) {
            timer.stop();
            parent.getStatusBar().setText("Paused");
        } else {
            timer.start();
            parent.getStatusBar().setText(" Score: " + score);
        }
        repaint();
    }

    /**
     * Clears the board by filling it with NoShape.
     */
    private void clearBoard() {
        for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; i++) {
            board[i] = Tetrominoe.NoShape;
        }
    }

    /**
     * Creates a new random piece at the top of the board.
     */
    private void newPiece() {
        curPiece.setRandomShape();
        curX = BOARD_WIDTH / 2; // Spawn in middle
        curY = BOARD_HEIGHT - 1 + curPiece.minY(); // Spawn just above the top

        if (!tryMove(curPiece, curX, curY)) {
            // Game Over
            curPiece.setShape(Tetrominoe.NoShape);
            timer.stop();
            isStarted = false;
            parent.getStatusBar().setText("Game Over. Score: " + score);
        }
    }

    /**
     * Attempts to move the current piece.
     * @param newPiece The piece (potentially rotated) to move.
     * @param newX The new X coordinate.
     * @param newY The new Y coordinate.
     * @return true if the move was successful, false otherwise.
     */
    private boolean tryMove(Shape newPiece, int newX, int newY) {
        for (int i = 0; i < 4; i++) {
            int x = newX + newPiece.getX(i);
            int y = newY - newPiece.getY(i); // Y-axis is inverted in piece coords

            // Check boundaries
            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) {
                return false;
            }

            // Check for collision with settled pieces
            if (shapeAt(x, y) != Tetrominoe.NoShape) {
                return false;
            }
        }

        // Move is valid. Update the current piece.
        curPiece = newPiece;
        curX = newX;
        curY = newY;
        repaint();
        return true;
    }

    /**
     * Moves the piece down one line.
     */
    private void oneLineDown() {
        if (!tryMove(curPiece, curX, curY - 1)) {
            pieceDropped();
        }
    }

    /**
     * Instantly drops the piece to the bottom.
     */
    private void dropDown() {
        int newY = curY;
        while (newY > 0) {
            if (!tryMove(curPiece, curX, newY - 1)) {
                break;
            }
            newY--;
        }
        pieceDropped();
    }

    /**
     * Called when a piece hits the bottom or another piece.
     * It "stamps" the piece onto the board and checks for line clears.
     */
    private void pieceDropped() {
        // Add piece to the board grid
        for (int i = 0; i < 4; i++) {
            int x = curX + curPiece.getX(i);
            int y = curY - curPiece.getY(i);
            board[y * BOARD_WIDTH + x] = curPiece.getShape();
        }

        // Check for completed lines
        removeFullLines();

        // Spawn the next piece
        if (!isFallingFinished) {
            newPiece();
        }
    }

    /**
     * Checks for and removes any full lines.
     */
    private void removeFullLines() {
        int numFullLines = 0;

        // Iterate from bottom row to top
        for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {
            boolean lineIsFull = true;

            // Check if the line is full
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (shapeAt(j, i) == Tetrominoe.NoShape) {
                    lineIsFull = false;
                    break;
                }
            }

            if (lineIsFull) {
                numFullLines++;
                // Move all lines above this one down
                for (int k = i; k < BOARD_HEIGHT - 1; k++) {
                    for (int j = 0; j < BOARD_WIDTH; j++) {
                        board[k * BOARD_WIDTH + j] = shapeAt(j, k + 1);
                    }
                }
                // The top line becomes empty
                for (int j = 0; j < BOARD_WIDTH; j++) {
                    board[(BOARD_HEIGHT - 1) * BOARD_WIDTH + j] = Tetrominoe.NoShape;
                }
                // Re-check the same row index 'i' since it now contains the row from above
                i++;
            }
        }

        if (numFullLines > 0) {
            score += numFullLines * 100; // Update score
            parent.getStatusBar().setText(" Score: " + score);
            isFallingFinished = true;
            curPiece.setShape(Tetrominoe.NoShape);
            repaint();
        }
    }

    /**
     * Main game loop, triggered by the Timer.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (isFallingFinished) {
            isFallingFinished = false;
            newPiece();
        } else {
            oneLineDown();
        }
    }

    // --- Drawing ---

    /**
     * Calculates the width of one square.
     */
    private int squareWidth() {
        return (int) getSize().getWidth() / BOARD_WIDTH;
    }

    /**
     * Calculates the height of one square.
     */
    private int squareHeight() {
        return (int) getSize().getHeight() / BOARD_HEIGHT;
    }

    /**
     * Gets the settled shape at a given board coordinate.
     */
    private Tetrominoe shapeAt(int x, int y) {
        return board[y * BOARD_WIDTH + x];
    }

    /**
     * Main paint method.
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int boardTop = (int) getSize().getHeight() - BOARD_HEIGHT * squareHeight();

        // Draw all the settled pieces on the board
        for (int i = 0; i < BOARD_HEIGHT; i++) {
            for (int j = 0; j < BOARD_WIDTH; j++) {
                Tetrominoe shape = shapeAt(j, BOARD_HEIGHT - 1 - i); // Draw from bottom up
                if (shape != Tetrominoe.NoShape) {
                    drawSquare(g, j * squareWidth(),
                            boardTop + i * squareHeight(), shape);
                }
            }
        }

        // Draw the currently falling piece
        if (curPiece.getShape() != Tetrominoe.NoShape) {
            for (int i = 0; i < 4; i++) {
                int x = curX + curPiece.getX(i);
                int y = curY - curPiece.getY(i); // Y-axis inverted

                // Calculate pixel coordinates
                int drawX = x * squareWidth();
                int drawY = boardTop + (BOARD_HEIGHT - 1 - y) * squareHeight();

                drawSquare(g, drawX, drawY, curPiece.getShape());
            }
        }
    }

    /**
     * Draws a single square of a Tetromino.
     */
    private void drawSquare(Graphics g, int x, int y, Tetrominoe shape) {
        Color color = shape.color;
        g.setColor(color);
        g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);

        // Draw 3D-like highlights
        g.setColor(color.brighter());
        g.drawLine(x, y + squareHeight() - 1, x, y);
        g.drawLine(x, y, x + squareWidth() - 1, y);

        g.setColor(color.darker());
        g.drawLine(x + 1, y + squareHeight() - 1,
                x + squareWidth() - 1, y + squareHeight() - 1);
        g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1,
                x + squareWidth() - 1, y + 1);
    }

    // --- Input ---

    /**
     * Inner class to handle keyboard input.
     */
    class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            if (!isStarted || curPiece.getShape() == Tetrominoe.NoShape) {
                return;
            }

            int keycode = e.getKeyCode();

            if (keycode == 'p' || keycode == 'P') {
                pause();
                return;
            }

            if (isPaused) {
                return;
            }

            switch (keycode) {
                case KeyEvent.VK_LEFT:
                    tryMove(curPiece, curX - 1, curY);
                    break;
                case KeyEvent.VK_RIGHT:
                    tryMove(curPiece, curX + 1, curY);
                    break;
                case KeyEvent.VK_DOWN:
                    oneLineDown();
                    break;
                case KeyEvent.VK_UP:
                    tryMove(curPiece.rotateRight(), curX, curY);
                    break;
                case KeyEvent.VK_SPACE:
                    dropDown();
                    break;
            }
        }
    }
}