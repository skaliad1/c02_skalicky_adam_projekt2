package rasterizer;

import java.awt.image.BufferedImage;

public class RasterLayer {
    private BufferedImage buffer;
    private final int width;
    private final int height;

    public RasterLayer(int width, int height) {
        this.width = width;
        this.height = height;
        this.buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        clear();
    }

    public void clear() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                buffer.setRGB(x, y, 0x000000);
            }
        }
    }

    public BufferedImage getBuffer() {
        return buffer;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}