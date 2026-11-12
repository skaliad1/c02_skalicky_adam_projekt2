package rasterizer;

import java.awt.Rectangle;

public class CohenSutherland {
    private static final int INSIDE = 0; // 0000
    private static final int LEFT = 1;   // 0001
    private static final int RIGHT = 2;  // 0010
    private static final int BOTTOM = 4; // 0100
    private static final int TOP = 8;    // 1000

    private static int computeOutCode(double x, double y, Rectangle clipWindow) {
        int code = INSIDE;
        if (x < clipWindow.x) {
            code |= LEFT;
        } else if (x > clipWindow.x + clipWindow.width) {
            code |= RIGHT;
        }
        if (y < clipWindow.y) {
            code |= TOP;
        } else if (y > clipWindow.y + clipWindow.height) {
            code |= BOTTOM;
        }
        return code;
    }
    public static class ClippedLine {
        public int x1, y1, x2, y2;
        public boolean visible;

        public ClippedLine(int x1, int y1, int x2, int y2, boolean visible) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
            this.visible = visible;
        }
    }
    public static ClippedLine clipLine(double x1, double y1, double x2, double y2, Rectangle clipWindow) {
        int outcode1 = computeOutCode(x1, y1, clipWindow);
        int outcode2 = computeOutCode(x2, y2, clipWindow);
        boolean accept = false;

        double xmin = clipWindow.x;
        double ymin = clipWindow.y;
        double xmax = clipWindow.x + clipWindow.width;
        double ymax = clipWindow.y + clipWindow.height;

        while (true) {
            if ((outcode1 | outcode2) == 0) {
                accept = true;
                break;
            } else if ((outcode1 & outcode2) != 0) {
                break;
            } else {
                double x = 0, y = 0;
                int outcodeOut = (outcode1 != 0) ? outcode1 : outcode2;

                if ((outcodeOut & TOP) != 0) {
                    x = x1 + (x2 - x1) * (ymin - y1) / (y2 - y1);
                    y = ymin;
                } else if ((outcodeOut & BOTTOM) != 0) {
                    x = x1 + (x2 - x1) * (ymax - y1) / (y2 - y1);
                    y = ymax;
                } else if ((outcodeOut & RIGHT) != 0) {
                    y = y1 + (y2 - y1) * (xmax - x1) / (x2 - x1);
                    x = xmax;
                } else if ((outcodeOut & LEFT) != 0) {
                    y = y1 + (y2 - y1) * (xmin - x1) / (x2 - x1);
                    x = xmin;
                }

                if (outcodeOut == outcode1) {
                    x1 = x;
                    y1 = y;
                    outcode1 = computeOutCode(x1, y1, clipWindow);
                } else {
                    x2 = x;
                    y2 = y;
                    outcode2 = computeOutCode(x2, y2, clipWindow);
                }
            }
        }

        if (accept) {
            return new ClippedLine((int) Math.round(x1), (int) Math.round(y1),
                    (int) Math.round(x2), (int) Math.round(y2), true);
        } else {
            return new ClippedLine(0, 0, 0, 0, false);
        }
    }
}