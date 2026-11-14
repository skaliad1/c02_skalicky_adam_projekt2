package rasterizer;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SutherlandHodgman {

    private static class Point2D {
        public double x, y;

        public Point2D(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private static double calculateSignedArea(List<Point> polygon) {
        double area = 0;
        int n = polygon.size();
        for (int i = 0; i < n; i++) {
            Point p1 = polygon.get(i);
            Point p2 = polygon.get((i + 1) % n);
            area += (p2.x - p1.x) * (p2.y + p1.y);
        }
        return area;
    }

    private static boolean isInside(Point2D p, Point2D cp1, Point2D cp2) {
        return (cp2.x - cp1.x) * (p.y - cp1.y) - (cp2.y - cp1.y) * (p.x - cp1.x) >= 0;
    }
    private static Point2D getIntersection(Point2D cp1, Point2D cp2, Point2D p1, Point2D p2) {
        double dcx = cp1.x - cp2.x;
        double dcy = cp1.y - cp2.y;
        double dpx = p1.x - p2.x;
        double dpy = p1.y - p2.y;

        double n1 = cp1.x * cp2.y - cp1.y * cp2.x;
        double n2 = p1.x * p2.y - p1.y * p2.x;
        double denom = dcx * dpy - dcy * dpx;

        if (Math.abs(denom) < 1e-10) {
            return p1;
        }

        double n3 = 1.0 / denom;
        return new Point2D(
                (n1 * dpx - dcx * n2) * n3,
                (n1 * dpy - dcy * n2) * n3
        );
    }
    public static List<Point> clipPolygon(List<Point> subjectPolygon, List<Point> clipPolygon) {
        if (subjectPolygon == null || subjectPolygon.isEmpty() || clipPolygon == null || clipPolygon.size() < 3) {
            return new ArrayList<>();
        }
        List<Point> mutableClip = new ArrayList<>(clipPolygon);

        if (calculateSignedArea(mutableClip) > 0) {
            Collections.reverse(mutableClip);
        }

        List<Point2D> outputList = new ArrayList<>();
        for (Point p : subjectPolygon) {
            outputList.add(new Point2D(p.x, p.y));
        }

        int len = mutableClip.size();
        for (int i = 0; i < len; i++) {
            if (outputList.isEmpty()) break;

            List<Point2D> inputList = new ArrayList<>(outputList);
            outputList.clear();

            Point cp1Point = mutableClip.get(i);
            Point cp2Point = mutableClip.get((i + 1) % len);
            Point2D cp1 = new Point2D(cp1Point.x, cp1Point.y);
            Point2D cp2 = new Point2D(cp2Point.x, cp2Point.y);

            Point2D s = inputList.get(inputList.size() - 1);

            for (Point2D e : inputList) {
                if (isInside(e, cp1, cp2)) {
                    if (!isInside(s, cp1, cp2)) {
                        outputList.add(getIntersection(cp1, cp2, s, e));
                    }
                    outputList.add(e);
                } else if (isInside(s, cp1, cp2)) {
                    outputList.add(getIntersection(cp1, cp2, s, e));
                }
                s = e;
            }
        }
        List<Point> result = new ArrayList<>();
        for (Point2D p2d : outputList) {
            result.add(new Point((int) Math.round(p2d.x), (int) Math.round(p2d.y)));
        }
        return result;
    }
}