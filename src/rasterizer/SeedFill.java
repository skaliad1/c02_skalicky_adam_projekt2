package rasterizer;

import view.Panel;
import java.awt.Point;
import java.util.Stack;

public class SeedFill {
    private Panel panel;

    private static final int BOUNDARY_FILL_COLOR = 0xB2AC88; //Hnědá výplň
    private static final int FLOOD_FILL_COLOR = 0x0000FF; //Modrá výplň
    private static final int DEFAULT_BOUNDARY_COLOR = 0xFFFFFF; //Bílá hranice

    public SeedFill(Panel panel) {
        this.panel = panel;
    }

    public void fillBoundary(int startX, int startY) {
        fillWithBoundary(startX, startY, BOUNDARY_FILL_COLOR, DEFAULT_BOUNDARY_COLOR);
    }

    public void fillFlood(int startX, int startY) {
        fillWithColor(startX, startY, FLOOD_FILL_COLOR);
    }

    //Omezení barvou hranice
    public void fillWithBoundary(int startX, int startY, int fillColor, int boundaryColor) {
        int width = panel.getRaster().getWidth();
        int height = panel.getRaster().getHeight();

        if (startX < 0 || startX >= width || startY < 0 || startY >= height) return;

        int startColor = panel.getRaster().getRGB(startX, startY) & 0x00FFFFFF;
        int boundMasked = boundaryColor & 0x00FFFFFF;
        int fillMasked = fillColor & 0x00FFFFFF;

        if (startColor == boundMasked || startColor == fillMasked) return;

        Stack<Point> stack = new Stack<>();
        stack.push(new Point(startX, startY));

        while (!stack.isEmpty()) {
            Point p = stack.pop();
            int x = p.x;
            int y = p.y;

            if (x < 0 || x >= width || y < 0 || y >= height) continue;

            int currentPixelColor = panel.getRaster().getRGB(x, y) & 0x00FFFFFF;

            if (currentPixelColor != boundMasked && currentPixelColor != fillMasked) {
                panel.drawPixel(x, y, fillColor);

                stack.push(new Point(x + 1, y));
                stack.push(new Point(x - 1, y));
                stack.push(new Point(x, y + 1));
                stack.push(new Point(x, y - 1));
            }
        }
    }

    // Omezení barvou pozadí
    public void fillWithColor(int startX, int startY, int fillColor) {
        int width = panel.getRaster().getWidth();
        int height = panel.getRaster().getHeight();

        if (startX < 0 || startX >= width || startY < 0 || startY >= height) return;

        int targetColor = panel.getRaster().getRGB(startX, startY) & 0x00FFFFFF;
        int fillMasked = fillColor & 0x00FFFFFF;

        if (targetColor == fillMasked) return;

        Stack<Point> stack = new Stack<>();
        stack.push(new Point(startX, startY));

        while (!stack.isEmpty()) {
            Point p = stack.pop();
            int x = p.x;
            int y = p.y;

            if (x < 0 || x >= width || y < 0 || y >= height) continue;

            int currentPixelColor = panel.getRaster().getRGB(x, y) & 0x00FFFFFF;

            if (currentPixelColor == targetColor) {
                panel.drawPixel(x, y, fillColor);

                stack.push(new Point(x + 1, y));
                stack.push(new Point(x - 1, y));
                stack.push(new Point(x, y + 1));
                stack.push(new Point(x, y - 1));
            }
        }
    }
}