package ch.zhaw.abyss.ui.art;

/**
 * Gemeinsame Tiefsee-Palette. Alle Pixelgrafiken verwenden diese Farbrampen, damit Figuren, Räume
 * und Effekte zusammenpassen.
 */
public final class Pal {
    private Pal() {}

    // Konturen und Tinte
    public static final int OUTLINE = 0xFF070A12;
    public static final int INK = 0xFF05070D;
    public static final int NIGHT = 0xFF0A1020;
    public static final int DEEP = 0xFF0F1A2E;
    public static final int ABYSS = 0xFF16263D;
    public static final int DUSK = 0xFF1F3550;
    public static final int SEA = 0xFF2B4766;
    public static final int TIDE = 0xFF3A5D80;

    // Stahl
    public static final int STEEL_0 = 0xFF121417;
    public static final int STEEL_1 = 0xFF1D2126;
    public static final int STEEL_2 = 0xFF2A3037;
    public static final int STEEL_3 = 0xFF3B434B;
    public static final int STEEL_4 = 0xFF525C66;
    public static final int STEEL_5 = 0xFF6E7A85;
    public static final int STEEL_6 = 0xFF95A1AB;
    public static final int STEEL_7 = 0xFFC4CCD2;

    // Rost und Messing
    public static final int RUST_0 = 0xFF2A1208;
    public static final int RUST_1 = 0xFF45200E;
    public static final int RUST_2 = 0xFF6B3414;
    public static final int RUST_3 = 0xFF94501C;
    public static final int RUST_4 = 0xFFC07426;
    public static final int RUST_5 = 0xFFE09B3A;
    public static final int RUST_6 = 0xFFF5C45E;
    public static final int RUST_7 = 0xFFFFE29A;

    // Cyan und Türkis
    public static final int TEAL_0 = 0xFF0B2B2E;
    public static final int TEAL_1 = 0xFF114448;
    public static final int TEAL_2 = 0xFF1A6166;
    public static final int TEAL_3 = 0xFF268A8A;
    public static final int TEAL_4 = 0xFF3FB5AD;
    public static final int TEAL_5 = 0xFF6FE0D0;
    public static final int TEAL_6 = 0xFFB4FFF0;

    // Gefahr
    public static final int RED_0 = 0xFF3A0B12;
    public static final int RED_1 = 0xFF6A1420;
    public static final int RED_2 = 0xFFA3222C;
    public static final int RED_3 = 0xFFD8403A;
    public static final int RED_4 = 0xFFFF6B4F;
    public static final int RED_5 = 0xFFFF9F80;

    // Biolumineszenz und Pflanzen
    public static final int GREEN_0 = 0xFF0E2A14;
    public static final int GREEN_1 = 0xFF1B4A22;
    public static final int GREEN_2 = 0xFF2E6E30;
    public static final int GREEN_3 = 0xFF4F9A3C;
    public static final int GREEN_4 = 0xFF86C95A;
    public static final int GREEN_5 = 0xFFC6EF8A;
    public static final int VIOLET_0 = 0xFF1E0F33;
    public static final int VIOLET_1 = 0xFF3A1A5C;
    public static final int VIOLET_2 = 0xFF5E2F8A;
    public static final int VIOLET_3 = 0xFF8F52C2;
    public static final int VIOLET_4 = 0xFFC48CF0;
    public static final int VIOLET_5 = 0xFFEBCBFF;

    // Neutrale Helltöne
    public static final int BONE = 0xFFF4F1E8;
    public static final int PAPER = 0xFFD9D4C3;
    public static final int WHITE = 0xFFFFFFFF;

    /**
     * Hellt eine Farbe auf oder dunkelt sie ab.
     *
     * @param argb Farbe
     * @param factor Faktor; über 1 heller, unter 1 dunkler
     * @return neue Farbe mit gleichem Alpha
     */
    public static int shade(int argb, double factor) {
        int a = argb & 0xFF000000;
        int r = (int) Math.min(255, ((argb >> 16) & 255) * factor);
        int g = (int) Math.min(255, ((argb >> 8) & 255) * factor);
        int b = (int) Math.min(255, (argb & 255) * factor);
        return a | r << 16 | g << 8 | b;
    }

    /**
     * Verschiebt eine Farbe zur Zielfarbe.
     *
     * @param a Ausgangsfarbe
     * @param b Zielfarbe
     * @param t Anteil 0 bis 1
     * @return Mischfarbe mit Alpha von {@code a}
     */
    public static int mix(int a, int b, double t) {
        int r = (int) Math.round(((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = (int) Math.round(((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = (int) Math.round((a & 255) * (1 - t) + (b & 255) * t);
        return (a & 0xFF000000) | r << 16 | g << 8 | bl;
    }

    /**
     * Farbrampe aus einer Grundfarbe: dunkel, mittel, hell, Glanz.
     *
     * @param base Grundfarbe
     * @return vier Stufen
     */
    public static int[] ramp(int base) {
        return new int[] {
            mix(shade(base, .55), 0xFF101428, .25),
            base,
            mix(shade(base, 1.25), 0xFFFFF0D0, .15),
            mix(shade(base, 1.5), 0xFFFFFFFF, .35)
        };
    }
}
