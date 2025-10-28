import java.util.Random;

/**
 * Represents a single Tetris piece (Tetromino).
 * Handles the piece's coordinates, shape, and rotation.
 */
public class Shape {

    private Tetrominoe pieceShape;
    private int[][] coords; // Coordinates of the 4 blocks [4][2]

    public Shape() {
        coords = new int[4][2];
        setShape(Tetrominoe.NoShape);
    }

    /**
     * Sets the shape of the piece based on a Tetrominoe enum.
     * @param shape The enum constant representing the desired shape.
     */
    public void setShape(Tetrominoe shape) {
        pieceShape = shape;
        // Copy the coordinates from the enum's template
        for (int i = 0; i < 4; i++) {
            System.arraycopy(shape.coords[i], 0, coords[i], 0, 2);
        }
    }

    /**
     * Sets the shape to a new random Tetromino.
     */
    public void setRandomShape() {
        Random r = new Random();
        int x = Math.abs(r.nextInt()) % 7 + 1; // 1 to 7
        Tetrominoe[] values = Tetrominoe.values();
        setShape(values[x]);
    }

    // --- Getters for coordinates ---
    public int getX(int index) {
        return coords[index][0];
    }

    public int getY(int index) {
        return coords[index][1];
    }

    public Tetrominoe getShape() {
        return pieceShape;
    }

    // --- Setters for rotation ---
    private void setX(int index, int x) {
        coords[index][0] = x;
    }

    private void setY(int index, int y) {
        coords[index][1] = y;
    }

    /**
     * @return The minimum Y-coordinate, used for spawning.
     */
    public int minY() {
        int m = coords[0][1];
        for (int i = 1; i < 4; i++) {
            m = Math.min(m, coords[i][1]);
        }
        return m;
    }

    /**
     * Rotates the piece 90 degrees left.
     * Formula: (x, y) -> (y, -x)
     * @return A new Shape object with the rotated coordinates.
     */
    public Shape rotateLeft() {
        if (pieceShape == Tetrominoe.OShape) {
            return this; // O-Shape doesn't rotate
        }

        Shape result = new Shape();
        result.pieceShape = this.pieceShape;

        for (int i = 0; i < 4; i++) {
            result.setX(i, getY(i));
            result.setY(i, -getX(i));
        }
        return result;
    }

    /**
     * Rotates the piece 90 degrees right.
     * Formula: (x, y) -> (-y, x)
     * @return A new Shape object with the rotated coordinates.
     */
    public Shape rotateRight() {
        if (pieceShape == Tetrominoe.OShape) {
            return this; // O-Shape doesn't rotate
        }

        Shape result = new Shape();
        result.pieceShape = this.pieceShape;

        for (int i = 0; i < 4; i++) {
            result.setX(i, -getY(i));
            result.setY(i, getX(i));
        }
        return result;
    }
}