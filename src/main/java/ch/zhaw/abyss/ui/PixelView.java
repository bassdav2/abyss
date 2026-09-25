package ch.zhaw.abyss.ui;

import ch.zhaw.abyss.ui.pixel.Frame;

import javafx.scene.canvas.Canvas;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

/**
 * Bringt den Software-Framebuffer pixelgenau auf eine JavaFX-Zeichenfläche: Nachbarpixel ohne
 * Glättung, seitenrichtiges Letterboxing und optional ein dezenter Röhrenbildschirm-Filter.
 */
final class PixelView {
    private final Canvas canvas;
    private final WritableImage image;
    private final int width, height;
    private double scale = 1, offsetX, offsetY;

    PixelView(Canvas canvas, int width, int height) {
        this.canvas = canvas;
        this.width = width;
        this.height = height;
        this.image = new WritableImage(width, height);
    }

    /**
     * Zeichnet ein Bild.
     *
     * @param frame fertiges Pixelbild
     * @param scanlines Röhrenfilter zeichnen
     */
    void present(Frame frame, boolean scanlines) {
        image.getPixelWriter()
                .setPixels(
                        0,
                        0,
                        width,
                        height,
                        PixelFormat.getIntArgbInstance(),
                        frame.pixels(),
                        0,
                        width);
        double w = canvas.getWidth(), h = canvas.getHeight();
        scale = Math.min(w / width, h / height);
        offsetX = Math.floor((w - width * scale) / 2);
        offsetY = Math.floor((h - height * scale) / 2);
        var g = canvas.getGraphicsContext2D();
        g.setImageSmoothing(false);
        g.setFill(Color.web("#020409"));
        g.fillRect(0, 0, w, h);
        g.drawImage(image, offsetX, offsetY, width * scale, height * scale);
        if (scanlines && scale >= 2) {
            g.setFill(Color.rgb(0, 0, 0, .16));
            double line = Math.max(1, scale * .28);
            for (int row = 1; row <= height; row++)
                g.fillRect(offsetX, offsetY + row * scale - line, width * scale, line);
        }
    }

    /**
     * Rechnet eine Szenenposition in Bildpixel um.
     *
     * @param sceneX Position in der Szene
     * @return Bildpixel x
     */
    double toPixelX(double sceneX) {
        return (sceneX - offsetX) / Math.max(.001, scale);
    }

    /**
     * Rechnet eine Szenenposition in Bildpixel um.
     *
     * @param sceneY Position in der Szene
     * @return Bildpixel y
     */
    double toPixelY(double sceneY) {
        return (sceneY - offsetY) / Math.max(.001, scale);
    }
}
