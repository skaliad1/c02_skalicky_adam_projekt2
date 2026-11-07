package rasterizer;

import model.Polygon;
import java.awt.Point;
import java.util.List;

public class PolygonRasterizer {
    private final LineRasterizer lineRasterizer;

    public PolygonRasterizer(LineRasterizer lineRasterizer) {
        this.lineRasterizer = lineRasterizer;
    }

    public void rasterize(Polygon polygon, int color) {
        List<Point> points = polygon.getPoints();
        int size = points.size();

        if (size < 2) return;

        for (int i = 0; i < size - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            lineRasterizer.rasterize(p1.x, p1.y, p2.x, p2.y, color);
        }

        if (size >= 3) {
            Point first = points.get(0);
            Point last = points.get(size - 1);
            lineRasterizer.rasterize(last.x, last.y, first.x, first.y, color);
        }
    }
}