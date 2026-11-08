package model;

import java.awt.Point;

public class Line {
    private int x1, y1, x2, y2;
    private int color;

    public Line(int x1, int y1, int x2, int y2, int color) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.color = color;
    }

    public static Point snap(int x1, int y1, int x2, int y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;

        if (dx == 0 && dy == 0) {
            return new Point(x1, y1);
        }

        double angle = Math.atan2(dy, dx);
        double octant = Math.round(angle / (Math.PI / 4.0));
        double targetAngle = octant * (Math.PI / 4.0);

        double cos = Math.cos(targetAngle);
        double sin = Math.sin(targetAngle);

        double length = dx * cos + dy * sin;

        int newX = (int) Math.round(x1 + length * cos);
        int newY = (int) Math.round(y1 + length * sin);

        return new Point(newX, newY);
    }

    public int getX1() {
        return x1;
    }
    public int getY1() {
        return y1;
    }
    public int getX2() {
        return x2;
    }
    public int getY2() {
        return y2;
    }
    public int getColor() {
        return color;
    }
}