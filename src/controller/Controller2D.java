package controller;

import model.Line;
import model.Polygon;
import rasterizer.CohenSutherland;
import rasterizer.LineRasterizer;
import rasterizer.PolygonRasterizer;
import rasterizer.ScanLineFill;
import rasterizer.SeedFill;
import rasterizer.SutherlandHodgman;
import view.Panel;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Controller2D {

    public enum Mode {
        LINE,
        POLYGON,
        FILL_BOUNDARY,
        FILL_COLOR,
        SCAN_LINE,
        EDIT_CLIP,
        CLIP_POLYGON,
        FILL_PATTERN
    }

    private Panel panel;
    private LineRasterizer rasterizer;
    private PolygonRasterizer polygonRasterizer;

    private List<Line> lines;
    private Polygon polygon;
    private Polygon clipPolygon;

    private Rectangle clipWindow = new Rectangle(150, 100, 300, 200);
    private boolean isClippingEnabled = false;
    private boolean isPolygonClippingEnabled = false;

    private Mode currentMode = Mode.LINE;

    private int startX, startY;
    private int dragX, dragY;
    private boolean isDragging = false;
    private boolean isShiftPressed = false;

    private int hoverX, hoverY;
    private final int TOLERANCE = 10;

    private BufferedImage fillSnapshot = null;

    public Controller2D(Panel panel) {
        this.panel = panel;
        this.rasterizer = new LineRasterizer(panel);
        this.polygonRasterizer = new PolygonRasterizer(rasterizer);

        this.lines = new ArrayList<>();
        this.polygon = new Polygon();
        this.clipPolygon = new Polygon();

        panel.clear();
        initListeners();
    }

    private void initListeners() {
        panel.setFocusable(true);
        panel.requestFocus();

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startX = e.getX();
                startY = e.getY();
                dragX = e.getX();
                dragY = e.getY();

                if (currentMode == Mode.POLYGON && polygon.getSize() == 0) {
                    polygon.addPoint(startX, startY);
                } else if (currentMode == Mode.CLIP_POLYGON && clipPolygon.getSize() == 0) {
                    clipPolygon.addPoint(startX, startY);
                }
                isDragging = true;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isDragging = false;
                int endX = e.getX();
                int endY = e.getY();

                if (currentMode == Mode.LINE) {
                    if (isShiftPressed) {
                        Point snapped = Line.snap(startX, startY, endX, endY);
                        endX = snapped.x;
                        endY = snapped.y;
                    }
                    lines.add(new Line(startX, startY, endX, endY, 0xFFFFFF));
                    redrawScene();
                }
                else if (currentMode == Mode.POLYGON) {
                    polygon.addPoint(endX, endY);
                    redrawScene();
                }
                else if (currentMode == Mode.CLIP_POLYGON) {
                    clipPolygon.addPoint(endX, endY);
                    isPolygonClippingEnabled = true;
                    redrawScene();
                }
                else if (currentMode == Mode.EDIT_CLIP) {
                    int x = Math.min(startX, endX);
                    int y = Math.min(startY, endY);
                    int width = Math.abs(endX - startX);
                    int height = Math.abs(endY - startY);

                    if (width > 5 && height > 5) {
                        clipWindow.setBounds(x, y, width, height);
                    }
                    redrawScene();
                }
                else if (currentMode == Mode.FILL_BOUNDARY || currentMode == Mode.FILL_COLOR) {
                    SeedFill seedFill = new SeedFill(panel);
                    if (currentMode == Mode.FILL_BOUNDARY) {
                        seedFill.fillBoundary(endX, endY);
                    } else {
                        seedFill.fillFlood(endX, endY);
                    }
                    fillSnapshot = panel.createSnapshot();
                    panel.repaint();
                    redrawScene();
                }
                else if (currentMode == Mode.FILL_PATTERN) {
                    SeedFill seedFill = new SeedFill(panel);
                    seedFill.fillPatternBoundary(endX, endY);
                    fillSnapshot = panel.createSnapshot();
                    panel.repaint();
                    redrawScene();
                }
                else if (currentMode == Mode.SCAN_LINE) {
                    ScanLineFill scanLineFill = new ScanLineFill(panel);
                    scanLineFill.fill(polygon, 0x00FF00, 0xFFFFFF);
                    fillSnapshot = panel.createSnapshot();
                    panel.repaint();
                    redrawScene();
                }
            }
        });

        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                dragX = e.getX();
                dragY = e.getY();
                redrawScene();
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                hoverX = e.getX();
                hoverY = e.getY();
            }
        });

        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    isShiftPressed = !isShiftPressed;
                    System.out.println("mod Line - svislé/vodorovné/úhlopříčné: " + (isShiftPressed ? "ZAPNUTO" : "VYPNUTO"));
                } else if (e.getKeyCode() == KeyEvent.VK_C) {
                    lines.clear();
                    polygon.clear();
                    clipPolygon.clear();
                    isPolygonClippingEnabled = false;
                    fillSnapshot = null;
                    panel.clear();
                    System.out.println("Plátno vymazáno");
                    panel.repaint();
                } else if (e.getKeyCode() == KeyEvent.VK_L) {
                    currentMode = Mode.LINE;
                    System.out.println("Režim: Úsečka");
                } else if (e.getKeyCode() == KeyEvent.VK_P) {
                    currentMode = Mode.POLYGON;
                    System.out.println("Režim: Polygon");
                } else if (e.getKeyCode() == KeyEvent.VK_O) {
                    isPolygonClippingEnabled = !isPolygonClippingEnabled;
                    currentMode = Mode.CLIP_POLYGON;
                    System.out.println("Ořez polygonem (Sutherland-Hodgman): " + (isPolygonClippingEnabled ? "ZAPNUTO" : "VYPNUTO"));
                    redrawScene();
                } else if (e.getKeyCode() == KeyEvent.VK_F) {
                    currentMode = Mode.FILL_BOUNDARY;
                    System.out.println("Režim: Boundary Fill");
                } else if (e.getKeyCode() == KeyEvent.VK_G) {
                    currentMode = Mode.FILL_COLOR;
                    System.out.println("Režim: Flood Fill");
                } else if (e.getKeyCode() == KeyEvent.VK_B) {
                    currentMode = Mode.FILL_PATTERN;
                    System.out.println("Režim: Vyplnění vzorem (Seed Fill)");
                } else if (e.getKeyCode() == KeyEvent.VK_S) {
                    currentMode = Mode.SCAN_LINE;
                    System.out.println("Režim: Scan Line výplň");
                } else if (e.getKeyCode() == KeyEvent.VK_E) {
                    isClippingEnabled = !isClippingEnabled;
                    currentMode = Mode.EDIT_CLIP;
                    System.out.println("Ořezávací okno (Cohen-Sutherland): " + (isClippingEnabled ? "ZAPNUTO (lze upravit myší)" : "VYPNUTO"));
                    redrawScene();
                } else if (e.getKeyCode() == KeyEvent.VK_X) {
                    deleteElementAt(hoverX, hoverY);
                    System.out.println("Smazaný vrchol");
                    redrawScene();
                }
            }
        });
    }

    private void deleteElementAt(int x, int y) {
        List<Point> points = polygon.getPoints();
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            if (Math.hypot(p.x - x, p.y - y) <= TOLERANCE) {
                polygon.removePointAt(i);
                fillSnapshot = null;
                return;
            }
        }
        List<Point> clipPoints = clipPolygon.getPoints();
        for (int i = 0; i < clipPoints.size(); i++) {
            Point p = clipPoints.get(i);
            if (Math.hypot(p.x - x, p.y - y) <= TOLERANCE) {
                clipPolygon.removePointAt(i);
                return;
            }
        }
        for (int i = 0; i < lines.size(); i++) {
            Line l = lines.get(i);
            if (Math.hypot(l.getX1() - x, l.getY1() - y) <= TOLERANCE ||
                    Math.hypot(l.getX2() - x, l.getY2() - y) <= TOLERANCE) {
                lines.remove(i);
                return;
            }
        }
    }

    private void redrawScene() {
        panel.clear();

        if (fillSnapshot != null) {
            panel.restoreSnapshot(fillSnapshot);
        }
        for (Line line : lines) {
            if (isClippingEnabled) {
                CohenSutherland.ClippedLine clipped = CohenSutherland.clipLine(
                        line.getX1(), line.getY1(), line.getX2(), line.getY2(), clipWindow
                );
                if (clipped.visible) {
                    rasterizer.rasterize(clipped.x1, clipped.y1, clipped.x2, clipped.y2, line.getColor());
                }
            } else {
                rasterizer.rasterize(line.getX1(), line.getY1(), line.getX2(), line.getY2(), line.getColor());
            }
        }

        if (isClippingEnabled) {
            drawClipWindow();
        }

        if (isPolygonClippingEnabled && clipPolygon.getSize() >= 3) {
            List<Point> clippedPoints = SutherlandHodgman.clipPolygon(polygon.getPoints(), clipPolygon.getPoints());
            Polygon clippedPolygonObj = new Polygon();
            for (Point p : clippedPoints) {
                clippedPolygonObj.addPoint(p.x, p.y);
            }
            polygonRasterizer.rasterize(clippedPolygonObj, 0xFFFFFF);
            polygonRasterizer.rasterize(clipPolygon, 0xAAAAAA);
        } else {
            polygonRasterizer.rasterize(polygon, 0xFFFFFF);

            List<Point> clipPoints = clipPolygon.getPoints();
            for (int i = 0; i < clipPoints.size() - 1; i++) {
                Point p1 = clipPoints.get(i);
                Point p2 = clipPoints.get(i + 1);
                rasterizer.rasterize(p1.x, p1.y, p2.x, p2.y, 0xAAAAAA);
            }
        }

        if (isDragging) {
            if (currentMode == Mode.LINE) {
                int endX = dragX;
                int endY = dragY;
                if (isShiftPressed) {
                    Point snapped = Line.snap(startX, startY, dragX, dragY);
                    endX = snapped.x;
                    endY = snapped.y;
                }
                rasterizer.rasterize(startX, startY, endX, endY, 0xFF0000);
            } else if (currentMode == Mode.POLYGON) {
                int size = polygon.getSize();
                if (size >= 1) {
                    Point last = polygon.getPoints().get(size - 1);
                    rasterizer.rasterize(last.x, last.y, dragX, dragY, 0xFF0000);

                    if (size >= 2) {
                        Point first = polygon.getPoints().get(0);
                        rasterizer.rasterize(dragX, dragY, first.x, first.y, 0xFF0000);
                    }
                }
            } else if (currentMode == Mode.CLIP_POLYGON) {
                int size = clipPolygon.getSize();
                if (size >= 1) {
                    Point last = clipPolygon.getPoints().get(size - 1);
                    rasterizer.rasterize(last.x, last.y, dragX, dragY, 0x00FFFF);

                    if (size >= 2) {
                        Point first = clipPolygon.getPoints().get(0);
                        rasterizer.rasterize(dragX, dragY, first.x, first.y, 0x00FFFF);
                    }
                }
            } else if (currentMode == Mode.EDIT_CLIP && isClippingEnabled) {
                int x = Math.min(startX, dragX);
                int y = Math.min(startY, dragY);
                int w = Math.abs(dragX - startX);
                int h = Math.abs(dragY - startY);
                rasterizer.rasterize(x, y, x + w, y, 0xFF0000);
                rasterizer.rasterize(x + w, y, x + w, y + h, 0xFF0000);
                rasterizer.rasterize(x + w, y + h, x, y + h, 0xFF0000);
                rasterizer.rasterize(x, y + h, x, y, 0xFF0000);
            }
        }
        panel.repaint();
    }

    private void drawClipWindow() {
        int x1 = clipWindow.x;
        int y1 = clipWindow.y;
        int x2 = clipWindow.x + clipWindow.width;
        int y2 = clipWindow.y + clipWindow.height;

        int color = 0xAAAAAA;
        rasterizer.rasterize(x1, y1, x2, y1, color);
        rasterizer.rasterize(x2, y1, x2, y2, color);
        rasterizer.rasterize(x2, y2, x1, y2, color);
        rasterizer.rasterize(x1, y2, x1, y1, color);
    }
}