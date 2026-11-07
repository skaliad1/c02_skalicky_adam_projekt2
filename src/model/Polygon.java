package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Polygon {

    private final List<Point> points;

    public Polygon() {
        points = new ArrayList<>();
    }

    public void addPoint(int x, int y) {
        points.add(new Point(x, y));
    }

    public void addPoint(Point p) {
        points.add(p);
    }

    public List<Point> getPoints() {
        return Collections.unmodifiableList(points);
    }

    public int getSize() {
        return points.size();
    }

    public void clear() {
        points.clear();
    }

    public void removePointAt(int index) {
        if (index >= 0 && index < points.size()) {
            points.remove(index);
        }
    }
}