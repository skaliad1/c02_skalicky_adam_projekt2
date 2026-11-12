package rasterizer;

import model.Polygon;
import view.Panel;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScanLineFill {
    private Panel panel;

    public ScanLineFill(Panel panel) {
        this.panel = panel;
    }

    public void fill(Polygon polygon, int fillColor, int borderColor) {
        List<Point> points = polygon.getPoints();
        int size = points.size();
        if (size < 3) return;

        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (Point p : points) {
            if (p.y < minY) minY = p.y;
            if (p.y > maxY) maxY = p.y;
        }

        for (int y = minY; y <= maxY; y++) {
            List<Integer> intersections = new ArrayList<>();

            for (int i = 0; i < size; i++) {
                Point p1 = points.get(i);
                Point p2 = points.get((i + 1) % size);

                if (p1.y == p2.y) continue;

                if (y >= Math.min(p1.y, p2.y) && y < Math.max(p1.y, p2.y)) {
                    double xIntersection = p1.x + (double) (y - p1.y) * (p2.x - p1.x) / (p2.y - p1.y);
                    intersections.add((int) Math.round(xIntersection));
                }
            }

            Collections.sort(intersections);

            for (int i = 0; i < intersections.size() - 1; i += 2) {
                int xStart = intersections.get(i);
                int xEnd = intersections.get(i + 1);

                for (int x = xStart; x <= xEnd; x++) {
                    panel.drawPixel(x, y, fillColor);
                }
            }
        }

        PolygonRasterizer polygonRasterizer = new PolygonRasterizer(new LineRasterizer(panel));
        polygonRasterizer.rasterize(polygon, borderColor);
    }
}