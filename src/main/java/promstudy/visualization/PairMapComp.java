package promstudy.visualization;

import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PairMapComp extends DataComponent {

    private int maxCount;
    private double max, min, mid, norm;
    private double[][] array;
    private double wStep;
    private int offset;
    public static boolean thick = false;
    private static Map<String, Color> customColors;
    private int tss;

    public PairMapComp(double[][] array, int tss) {
        super("trend");
        this.tss = tss;
        this.array = array;

        max = -Double.MAX_VALUE;
        min = Double.MAX_VALUE;
        ArrayList<Double> vals = new ArrayList<>();

        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                double d = Math.log(100 * Math.abs(array[i][j]) + 1);
                //double d = array[i][j];
                if (array[i][j] < 0) {
                    d = -1 * d;
                }
                if (d == 0 || Double.isNaN(d)) {
                    continue;
                }
                array[i][j] = d;

                if (d > max) {
                    max = d;
                }
                if (d < min) {
                    min = d;
                }
                vals.add(d);
            }
        }
        Collections.sort(vals);
        norm = vals.get(vals.size() / 2);
        mid = 0;
        //System.out.println(max + " " + mid + " " + min);
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        margin = 20;
        Graphics2D g2d = (Graphics2D) g;
        height = getHeight();
        width = getWidth();
        g2d.setRenderingHints(renderHints);
        int fs = 20;
        Font origFont = new Font("Arial", Font.PLAIN, fs);
        g2d.setFont(origFont);

        AffineTransform affineTransform = new AffineTransform();
        affineTransform.rotate(Math.toRadians(45), 0, 0);
        Font rotatedFont = origFont.deriveFont(affineTransform);


        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(textColor);
        step = 30;

        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                if (array[i][j] == 0) {
                    continue;
                }
                if (array[i][j] > mid) {
                    g2d.setColor(colors[0]);
                    g2d.setColor(new Color(g2d.getColor().getRed(), g2d.getColor().getGreen(), g2d.getColor().getBlue(),
                            (int) Math.floor(255 * (Math.abs(array[i][j] - mid) / (max - mid)))));
                } else if (array[i][j] < mid) {
                    g2d.setColor(colors[1]);
                    g2d.setColor(new Color(g2d.getColor().getRed(), g2d.getColor().getGreen(), g2d.getColor().getBlue(),
                            (int) Math.floor(255 * (Math.abs(array[i][j] - mid) / Math.abs(min - mid)))));
                } else {
                    g2d.setColor(Color.WHITE);
                }

                g2d.fillRect(step * j, step * i, step, step);
                g2d.setFont(new Font("Arial", Font.PLAIN, 9));
                g2d.setColor(Color.BLACK);
                g2d.drawString(getPosition(i) + "", step * j + 10, step * i + 10);
                g2d.drawString(getPosition(j) + "", step * j + 10, step * i + 20);
            }

            //String result = String.format("%.4f", array[i]);
            //g2d.drawString(result, step + (i/4) * step, height - (5*step - step*(i%4)));
            //g2d.setFont(origFont);
        }
    }

    public String getPosition(int p) {
        if (p < tss) {
            return "" + (p - tss);
        } else if (p > tss) {
            return "+" + (p - tss + 1);
        } else {
            return "+1";
        }
    }

    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        BufferedImage bimage = new BufferedImage(img.getWidth(null),
                img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        return bimage;
    }
}
