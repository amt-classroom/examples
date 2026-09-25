package ch.heigvd.amt.reflection;

/** Deux champs privés, sans getter : rien que la reflection ne puisse contourner. */
public class Point {

    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
