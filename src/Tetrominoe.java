import java.awt.Color;

/**
 * Enum defining the 7 Tetris shapes (and NoShape).
 * Each shape holds its (x, y) coordinate template and its color.
 * The coordinate system is (0,0) as the center of rotation.
 * Y-axis increases *upwards* in this template.
 */
public enum Tetrominoe {
    NoShape(new int[][]{{0, 0}, {0, 0}, {0, 0}, {0, 0}}, new Color(0, 0, 0)),
    ZShape(new int[][]{{0, -1}, {0, 0}, {-1, 0}, {-1, 1}}, new Color(204, 102, 102)),
    SShape(new int[][]{{0, -1}, {0, 0}, {1, 0}, {1, 1}}, new Color(102, 204, 102)),
    IShape(new int[][]{{0, -1}, {0, 0}, {0, 1}, {0, 2}}, new Color(102, 102, 204)),
    TShape(new int[][]{{-1, 0}, {0, 0}, {1, 0}, {0, 1}}, new Color(204, 204, 102)),
    OShape(new int[][]{{0, 0}, {1, 0}, {0, 1}, {1, 1}}, new Color(204, 102, 204)),
    LShape(new int[][]{{-1, -1}, {0, -1}, {0, 0}, {0, 1}}, new Color(102, 204, 204)),
    JShape(new int[][]{{1, -1}, {0, -1}, {0, 0}, {0, 1}}, new Color(218, 170, 0));

    public final int[][] coords;
    public final Color color;

    Tetrominoe(int[][] coords, Color c) {
        this.coords = coords;
        this.color = c;
    }
}