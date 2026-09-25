import ch.zhaw.abyss.application.Cosmetics;
import ch.zhaw.abyss.domain.DiverClass;
import ch.zhaw.abyss.domain.Weapon;
import ch.zhaw.abyss.ui.art.DiverArt;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;

/**
 * Pixel-App-Signet: die Taucherin in einem Messingbullauge, 64 x 64 Pixel, verlustfrei auf alle
 * macOS-Grössen vergrössert. Aufruf mit Klassenpfad der kompilierten Hauptquellen.
 */
class MakeIcon {
    public static void main(String[] args) throws Exception {
        Path root = Path.of(args[0]);
        Path out = root.resolve("build/Abyss.iconset");
        Files.createDirectories(out);
        int n = 64;
        int[] px = new int[n * n];
        for (int y = 0; y < n; y++)
            for (int x = 0; x < n; x++) {
                double dx = Math.max(0, Math.max(6 - x, x - (n - 7))), dy = Math.max(0, Math.max(6 - y, y - (n - 7)));
                if (dx * dx + dy * dy > 36) continue;
                double t = y / (double) n;
                px[y * n + x] = mix(0xFF16263D, 0xFF06101C, t);
            }
        for (int y = 0; y < n; y++)
            for (int x = 0; x < n; x++) {
                double d = Math.hypot(x - 31.5, y - 31.5);
                if (d < 23) px[y * n + x] = mix(0xFF1A6166, 0xFF0B2B2E, (y - 8) / 48.0);
                else if (d < 25) px[y * n + x] = 0xFFE09B3A;
                else if (d < 27) px[y * n + x] = d < 26 ? 0xFFF5C45E : 0xFF94501C;
            }
        for (int k = 0; k < 12; k++) {
            double a = k * Math.PI / 6;
            int x = (int) Math.round(31.5 + Math.cos(a) * 25.5), y = (int) Math.round(31.5 + Math.sin(a) * 25.5);
            px[y * n + x] = 0xFF6B3414;
        }
        var sprite = DiverArt.build(Cosmetics.DEFAULT, Weapon.WRENCH, DiverClass.MECHANIC).get(DiverArt.Anim.IDLE).getFirst();
        var glow = sprite.glow();
        for (int y = 0; y < sprite.height(); y++)
            for (int x = 0; x < sprite.width(); x++) {
                int c = sprite.get(x, y);
                if (glow != null && glow.get(x, y) >>> 24 != 0) c = glow.get(x, y);
                if (c >>> 24 == 0) continue;
                int tx = x + 8, ty = y + 12;
                if (tx >= 0 && ty >= 0 && tx < n && ty < n && Math.hypot(tx - 31.5, ty - 31.5) < 23) px[ty * n + tx] = c;
            }
        var image = scale(px, n, 16);
        ImageIO.write(image, "png", root.resolve("art-source/app-icon.png").toFile());
        for (int size : new int[] {16, 32, 128, 256, 512})
            for (int multiplier : new int[] {1, 2}) {
                int pixels = size * multiplier;
                var small = pixels >= n ? scale(px, n, pixels / n) : downscale(image, pixels);
                ImageIO.write(small, "png", out.resolve("icon_" + size + "x" + size + (multiplier == 2 ? "@2x" : "") + ".png").toFile());
            }
    }

    static BufferedImage scale(int[] px, int n, int factor) {
        var image = new BufferedImage(n * factor, n * factor, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < n * factor; y++)
            for (int x = 0; x < n * factor; x++) image.setRGB(x, y, px[(y / factor) * n + x / factor]);
        return image;
    }

    static BufferedImage downscale(BufferedImage source, int size) {
        var image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        var g = image.createGraphics();
        g.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(source, 0, 0, size, size, null);
        g.dispose();
        return image;
    }

    static int mix(int a, int b, double t) {
        t = Math.max(0, Math.min(1, t));
        int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
        return 0xFF000000 | r << 16 | g << 8 | bl;
    }
}
