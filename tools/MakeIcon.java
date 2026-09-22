import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.nio.file.*;
import javax.imageio.ImageIO;

/** Vektorbasiertes App-Signet; deterministischer Export ohne Fremdgrafik. */
class MakeIcon {
    public static void main(String[] args) throws Exception {
        Path root=Path.of(args[0]);
        Path out=root.resolve("build/Abyss.iconset");Files.createDirectories(out);
        BufferedImage image=new BufferedImage(1024,1024,BufferedImage.TYPE_INT_ARGB);
        Graphics2D g=image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        g.setPaint(new GradientPaint(0,0,new Color(30,65,79),850,1024,new Color(3,16,25)));
        g.fill(new RoundRectangle2D.Double(12,12,1000,1000,218,218));
        g.setStroke(new BasicStroke(4));g.setColor(new Color(92,143,151));
        g.draw(new RoundRectangle2D.Double(37,37,950,950,180,180));
        g.setPaint(new GradientPaint(100,0,new Color(244,204,133),700,1024,new Color(112,72,40)));
        g.setStroke(new BasicStroke(27));g.drawOval(140,130,744,744);
        g.setStroke(new BasicStroke(4));g.setColor(new Color(107,187,192));g.drawOval(176,166,672,672);
        for(int i=0;i<12;i++) {
            double angle=Math.PI*2*i/12;
            double x=512+373*Math.cos(angle),y=502+373*Math.sin(angle);
            g.setColor(new Color(16,30,37));g.fill(new Ellipse2D.Double(x-14,y-14,28,28));
            g.setColor(new Color(216,178,111));g.draw(new Ellipse2D.Double(x-13,y-13,26,26));
        }
        Font font=Font.createFont(Font.TRUETYPE_FONT,root.resolve("src/main/resources/fonts/BarlowCondensed-SemiBold.ttf").toFile()).deriveFont(650f);
        Shape letter=font.createGlyphVector(g.getFontRenderContext(),"A").getOutline();
        var bounds=letter.getBounds2D();
        var transform=AffineTransform.getTranslateInstance(512-bounds.getCenterX(),495-bounds.getCenterY());
        g.setColor(new Color(242,206,147));g.fill(transform.createTransformedShape(letter));
        g.setStroke(new BasicStroke(11,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
        g.setColor(new Color(115,225,219));
        Path2D wave=new Path2D.Double();wave.moveTo(277,722);wave.curveTo(365,690,433,761,519,728);wave.curveTo(605,694,670,756,747,722);g.draw(wave);
        g.dispose();
        ImageIO.write(image,"png",root.resolve("art-source/app-icon.png").toFile());
        for(int size:new int[]{16,32,128,256,512}) for(int multiplier:new int[]{1,2}) {
            int pixels=size*multiplier;
            BufferedImage small=new BufferedImage(pixels,pixels,BufferedImage.TYPE_INT_ARGB);
            Graphics2D sg=small.createGraphics();sg.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            sg.drawImage(image,0,0,pixels,pixels,null);sg.dispose();
            ImageIO.write(small,"png",out.resolve("icon_"+size+"x"+size+(multiplier==2?"@2x":"")+".png").toFile());
        }
    }
}
