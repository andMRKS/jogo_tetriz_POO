import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Board extends JPanel implements ActionListener {

    // --- Constants ---
    // You set this to 15 in the last step
    private static final int BOARD_WIDTH = 15;
    private static final int BOARD_HEIGHT = 22; // 22 rows (20 visible)

    // --- Game Speed (variable) ---
    private int gameSpeed = 300;

    // --- Game State ---
    private Timer timer;
    private boolean isStarted = false;
    private boolean isPaused = false;
    private boolean isFallingFinished = false;

    private int score = 0;
    private int level = 1;
    private int linesClearedTotal = 0;
    private int curX = 0;
    private int curY = 0;

    private Shape curPiece;
    private Shape nextPiece; // --- NEW: For the "Next Piece" preview ---
    private Tetrominoe[] board;
    private Tetris parent;

    // --- NEW: A color for the "Ghost Piece" ---
    private static final Color GHOST_COLOR = new Color(80, 80, 80, 150);


    public Board(Tetris parent) {
        this.parent = parent;
        setFocusable(true);
        setBackground(Color.BLACK);

        curPiece = new Shape();
        nextPiece = new Shape(); // --- NEW: Initialize nextPiece ---
        timer = new Timer(gameSpeed, this);
        board = new Tetrominoe[BOARD_WIDTH * BOARD_HEIGHT];
        addKeyListener(new TAdapter());
    }

    /**
     * Getter for the SidePanel to use.
     * @return The next Shape that will fall.
     */
    public Shape getNextPiece() {
        return nextPiece;
    }

    private void updateStatusBar() {
        parent.getStatusBar().setText(" Score: " + score + " | Level: " + level);
    }

    public void start() {
        if (isPaused) {
            return;
        }

        isStarted = true;
        isFallingFinished = false;

        score = 0;
        level = 1;
        linesClearedTotal = 0;
        gameSpeed = 300;
        timer.setDelay(gameSpeed);

        clearBoard();

        // --- NEW: Must generate BOTH pieces at the start ---
        curPiece.setRandomShape(); // Generate the first piece
        nextPiece.setRandomShape(); // Generate the "next" piece

        // Position the first piece
        curX = BOARD_WIDTH / 2;
        curY = BOARD_HEIGHT - 1 + curPiece.minY();

        updateStatusBar();
        timer.start();
    }

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
            updateStatusBar();
        }
        repaint();
    }

    private void clearBoard() {
        for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; i++) {
            board[i] = Tetrominoe.NoShape;
        }
    }

    /**
     * Creates a new random piece at the top of the board.
     */
    private void newPiece() {
        // --- NEW: The "next" piece becomes the "current" piece
        curPiece.setShape(nextPiece.getShape());
        // --- NEW: Generate a new "next" piece
        nextPiece.setRandomShape();

        curX = BOARD_WIDTH / 2;
        curY = BOARD_HEIGHT - 1 + curPiece.minY();

        if (!checkMove(curPiece, curX, curY)) {
            // Game Over
            curPiece.setShape(Tetrominoe.NoShape);
            timer.stop();
            isStarted = false;
            parent.getStatusBar().setText("Game Over. Score: " + score);
        }

        // --- NEW: We must also tell the SidePanel to repaint ---
        parent.repaint(); // Repaints the whole frame, including the side panel
    }

    /**
     * --- NEW: A "pure" check function that doesn't change game state. ---
     * This checks if a piece can move to a new position without colliding.
     * @return true if the move is valid, false otherwise.
     */
    private boolean checkMove(Shape piece, int newX, int newY) {
        for (int i = 0; i < 4; i++) {
            int x = newX + piece.getX(i);
            int y = newY - piece.getY(i); // Y-axis is inverted in piece coords

            // Check boundaries
            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) {
                return false;
            }
            // Check for collision with settled pieces
            if (shapeAt(x, y) != Tetrominoe.NoShape) {
                return false;
            }
        }
        return true;
    }

    /**
     * --- UPDATED: This now uses checkMove() ---
     * Attempts to move the current piece.
     * @return true if the move was successful, false otherwise.
     */
    private boolean tryMove(Shape newPiece, int newX, int newY) {
        // Use our new "check" function first
        if (!checkMove(newPiece, newX, newY)) {
            return false;
        }

        // Move is valid. Update the current piece.
        curPiece = newPiece;
        curX = newX;
        curY = newY;
        repaint();
        return true;
    }


    private void oneLineDown() {
        if (!tryMove(curPiece, curX, curY - 1)) {
            pieceDropped();
        }
    }

    private void dropDown() {
        int newY = curY;
        // Use checkMove to find the bottom
        while (newY > 0) {
            if (!checkMove(curPiece, curX, newY - 1)) {
                break;
            }
            newY--;
        }

        // Manually set the new Y and call pieceDropped
        curY = newY;
        repaint();
        pieceDropped();
    }

    private void pieceDropped() {
        // Add piece to the board grid
        for (int i = 0; i < 4; i++) {
            int x = curX + curPiece.getX(i);
            int y = curY - curPiece.getY(i);
            board[y * BOARD_WIDTH + x] = curPiece.getShape();
        }

        removeFullLines();

        if (!isFallingFinished) {
            newPiece();
        }
    }

    private void removeFullLines() {
        int numFullLines = 0;
        for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {
            boolean lineIsFull = true;
            for (int j = 0; j < BOARD_WIDTH; j++) {
                if (shapeAt(j, i) == Tetrominoe.NoShape) {
                    lineIsFull = false;
                    break;
                }
            }
            if (lineIsFull) {
                numFullLines++;
                for (int k = i; k < BOARD_HEIGHT - 1; k++) {
                    for (int j = 0; j < BOARD_WIDTH; j++) {
                        board[k * BOARD_WIDTH + j] = shapeAt(j, k + 1);
                    }
                }
                for (int j = 0; j < BOARD_WIDTH; j++) {
                    board[(BOARD_HEIGHT - 1) * BOARD_WIDTH + j] = Tetrominoe.NoShape;
                }
                i++;
            }
        }

        if (numFullLines > 0) {
            int pointsGained = 0;
            switch (numFullLines) {
                case 1: pointsGained = 100 * level; break;
                case 2: pointsGained = 300 * level; break;
                case 3: pointsGained = 500 * level; break;
                case 4: pointsGained = 800 * level; break;
            }
            score += pointsGained;

            linesClearedTotal += numFullLines;
            int newLevel = (linesClearedTotal / 10) + 1;
            if (newLevel > level) {
                level = newLevel;
                if (gameSpeed > 100) {
                    gameSpeed -= 20;
                    timer.setDelay(gameSpeed);
                }
            }
            updateStatusBar();
            isFallingFinished = true;
            curPiece.setShape(Tetrominoe.NoShape);
            repaint();
        }
    }

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

    private int squareWidth() {
        return (int) getSize().getWidth() / BOARD_WIDTH;
    }

    private int squareHeight() {
        return (int) getSize().getHeight() / BOARD_HEIGHT;
    }

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
                Tetrominoe shape = shapeAt(j, BOARD_HEIGHT - 1 - i);
                if (shape != Tetrominoe.NoShape) {
                    drawSquare(g, j * squareWidth(),
                            boardTop + i * squareHeight(), shape, false);
                }
            }
        }

        // Draw the currently falling piece
        if (curPiece.getShape() != Tetrominoe.NoShape) {

            // --- NEW: Draw the Ghost Piece first! ---
            drawGhostPiece(g, boardTop);

            // Now draw the actual piece
            for (int i = 0; i < 4; i++) {
                int x = curX + curPiece.getX(i);
                int y = curY - curPiece.getY(i);

                int drawX = x * squareWidth();
                int drawY = boardTop + (BOARD_HEIGHT - 1 - y) * squareHeight();

                drawSquare(g, drawX, drawY, curPiece.getShape(), false);
            }
        }
    }

    /**
     * --- NEW: Draws the ghost piece shadow ---
     */
    private void drawGhostPiece(Graphics g, int boardTop) {
        if (curPiece.getShape() == Tetrominoe.NoShape) {
            return;
        }

        int ghostY = curY;
        // Find the lowest possible Y position
        while (checkMove(curPiece, curX, ghostY - 1)) {
            ghostY--;
        }

        // Draw the piece at that ghost position
        for (int i = 0; i < 4; i++) {
            int x = curX + curPiece.getX(i);
            int y = ghostY - curPiece.getY(i); // Use ghostY

            int drawX = x * squareWidth();
            int drawY = boardTop + (BOARD_HEIGHT - 1 - y) * squareHeight();

            drawSquare(g, drawX, drawY, curPiece.getShape(), true); // true = isGhost
        }
    }


    /**
     * --- UPDATED: Now takes an 'isGhost' flag ---
     * Draws a single square of a Tetromino.
     */
    private void drawSquare(Graphics g, int x, int y, Tetrominoe shape, boolean isGhost) {
        Color color = isGhost ? GHOST_COLOR : shape.color;
        g.setColor(color);

        // If it's a ghost, just draw a rectangle outline
        if (isGhost) {
            g.drawRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);
        } else {
            // Otherwise, draw the full, lit block
            g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);

            g.setColor(color.brighter());
            g.drawLine(x, y + squareHeight() - 1, x, y);
            g.drawLine(x, y, x + squareWidth() - 1, y);

            g.setColor(color.darker());
            g.drawLine(x + 1, y + squareHeight() - 1,
                    x + squareWidth() - 1, y + squareHeight() - 1);
            g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1,
                    x + squareWidth() - 1, y + 1);
        }
    }


    // --- Input ---
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