package view;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Panel extends JPanel {

    private BufferedImage image;

    public Panel(int width, int height) {
        setPreferredSize(new Dimension(width, height));
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        clear();
    }

    public void clear() {
        Graphics g = image.getGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, image.getWidth(), image.getHeight());
        g.dispose();
    }

    public void drawPixel(int x, int y, int color) {
        if (x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight()) {
            image.setRGB(x, y, color);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);
    }

    public BufferedImage getRaster() {
        return image;
    }
    public BufferedImage createSnapshot() {
        BufferedImage snapshot = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        Graphics g = snapshot.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return snapshot;
    }

    public void restoreSnapshot(BufferedImage snapshot) {
        if (snapshot != null) {
            Graphics g = image.getGraphics();
            g.drawImage(snapshot, 0, 0, null);
            g.dispose();
        }
    }
}