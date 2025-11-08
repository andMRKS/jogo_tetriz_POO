import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Dimension;

/**
 * A new panel to display the "Next" piece.
 */
public class SidePanel extends JPanel {

    private Board board; // A reference to the main game board

    // We'll set the size of this panel
    private static final int PANEL_WIDTH = 120;
    private static final int PANEL_HEIGHT = 744; // Should match the Tetris window height

    public SidePanel(Board board) {
        this.board = board;
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.DARK_GRAY);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw "NEXT" text
        g.setColor(Color.WHITE);
        g.setFont(g.getFont().deriveFont(18f));
        g.drawString("NEXT", 20, 30);

        // Get the next piece from the board
        Shape nextPiece = board.getNextPiece();
        if (nextPiece == null || nextPiece.getShape() == Tetrominoe.NoShape) {
            return;
        }

        // Draw the next piece
        // We need to calculate a good size and position for it
        int squareSize = 20; // Let's make the preview blocks 20x20
        int drawX = 20; // X position to start drawing the piece
        int drawY = 80; // Y position to start drawing the piece

        // Center the piece. (IShape is long, OShape is wide)
        if (nextPiece.getShape() == Tetrominoe.IShape) {
            drawY -= squareSize;
        }
        if (nextPiece.getShape() == Tetrominoe.OShape) {
            drawX += squareSize / 2;
        }


        for (int i = 0; i < 4; i++) {
            int x = drawX + nextPiece.getX(i) * squareSize;
            int y = drawY - nextPiece.getY(i) * squareSize; // Y-axis is inverted
            drawSquare(g, x, y, squareSize, nextPiece.getShape().color);
        }
    }

    /**
     * A simpler drawSquare method for the preview panel.
     */
    private void drawSquare(Graphics g, int x, int y, int squareSize, Color color) {
        g.setColor(color);
        g.fillRect(x + 1, y + 1, squareSize - 2, squareSize - 2);

        g.setColor(color.brighter());
        g.drawLine(x, y + squareSize - 1, x, y);
        g.drawLine(x, y, x + squareSize - 1, y);

        g.setColor(color.darker());
        g.drawLine(x + 1, y + squareSize - 1, x + squareSize - 1, y + squareSize - 1);
        g.drawLine(x + squareSize - 1, y + squareSize - 1, x + squareSize - 1, y + 1);
    }
}