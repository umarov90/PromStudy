package promstudy.visualization;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

public class Trend3 extends DataComponent {

    private int maxCount;
    private double max, min;
    private ArrayList<double[]> arrays;
    private ArrayList<String> names;
    private double wStep;
    private int offset;
    public static boolean thick = false;

    public Trend3(ArrayList<double[]> arrays,
                  ArrayList<String> names, int wStep) {
        super("trend");
        this.arrays = arrays;
        this.names = names;
        this.wStep = wStep;
        refresh();
    }

    public Trend3(ArrayList<double[]> arrays,
                  ArrayList<String> names, int wStep, int offset) {
        super("trend");
        this.arrays = arrays;
        this.names = names;
        this.wStep = wStep;
        this.offset = offset;
        refresh();
    }

    private void refresh() {
        maxCount = -Integer.MAX_VALUE;
        max = -Double.MAX_VALUE;
        min = Double.MAX_VALUE;
        for (int i = 0; i < arrays.size(); i++) {
            if (arrays.get(i).length > maxCount) {
                maxCount = arrays.get(i).length;
            }
            for (int j = 0; j < arrays.get(i).length; j++) {
                double n = arrays.get(i)[j];
                if (n > max) {
                    max = n;
                }
                if (n < min) {
                    min = n;
                }
            }
        }
        if (!thick) {
            max = 1.0;
            min = 0.0;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        margin = 240;
        Graphics2D g2d = (Graphics2D) g;
        step = (getHeight() - margin) / (5);
        height = getHeight() - margin;
        width = getWidth();
        g2d.setRenderingHints(renderHints);
        Font origFont = new Font("Arial", Font.PLAIN, 45);
        g2d.setFont(origFont);

        AffineTransform affineTransform = new AffineTransform();
        affineTransform.rotate(Math.toRadians(45), 0, 0);
        Font rotatedFont = origFont.deriveFont(affineTransform);


        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, width, height + margin);
        int leftMargin = 130;
        int n = 2;
        offset = -1100;

        // x-axis
        xStep = ((double) width - leftMargin) / (arrays.get(0).length + 100);
        wStep = 100 * xStep;
        int cc = 1;
        int ii = 0;
        for (double i = leftMargin; i < width + wStep; i += wStep) {
            int ri =(int) Math.round(i);
            g2d.setColor(gridColor);
            g2d.drawLine(ri, height, ri, 0);
            g2d.setColor(textColor);
            int val = Math.round(offset + (100 * ii));
            int val2 = Math.round(offset + (100 * (ii+1)));
            String value = String.valueOf(val);
            String value2 = String.valueOf(val2);
            if (val > 0) {
                value = "+" + value;
            }
            if (val2 > 0) {
                value2 = "+" + value2;
            }
            String fVal = "(" + value + " : " + value2 + ")";
            int l = fVal.length();
            if(cc%2==0 && 100*ii < arrays.get(0).length) {
                g2d.setFont(rotatedFont);
                g2d.drawString(fVal, ri - 30, height + 35);
                g2d.setFont(origFont);
            }
            cc++;
            ii++;
        }

        // y-axis
        yPoint = 1 / ((double) (height / step));
        yStep = step / yPoint;
        for (int i = height - step; i > -3; i -= step) {
            g2d.setColor(gridColor);
            g2d.drawLine(leftMargin, i, width, i);
            g2d.setColor(textColor);
            double val;
            if (thick) {
                val = Round(min + yPoint * (height - i) / step, 0);
            } else {
                val = Round(min + yPoint * (height - i) / step, 2);
            }
            String value = String.valueOf(val);
            if (value.endsWith(".0")) {
                value = value.substring(0, value.length() - 2);
            }
            g2d.drawString(value, leftMargin - (n - 1) * 90, i + 30);

        }
        g2d.setColor(gridColor);
        g2d.drawLine(0, height, width, height);
        g2d.drawLine(leftMargin, height, leftMargin, 0);
        // drawing margin
        //g2d.setColor(backgroundColor);
        //g2d.fillRect(0, height + step, width, margin);
        // drawing labels
        g2d.setColor(textColor);
        for (int i = 0; i < arrays.size(); i++) {
            g2d.setColor(colors[i % colors.length]);
            g2d.setColor(new Color(g2d.getColor().getRed(), g2d.getColor().getGreen(), g2d.getColor().getBlue(), 120));
            for (int j = 0; j < arrays.get(i).length; j++) {
                double v = arrays.get(i)[j];
                if(v>0.02) {
                    g2d.setStroke(new BasicStroke(1));
                    g2d.fillRect((int) (j * xStep + leftMargin + wStep) - 10, height - (int) Math.round(yStep * v), 21, (int) Math.round(yStep * v));
                }
            }
        }
    }

    @Override
    public void scaleIncrease() {
        if (xPoint > 1) {
            dw = (width / scale) * (scale + 1);
            scale++;
            this.setSize(dw, height);
            applySizePref();
            repaint();
        }
    }

    @Override
    public void scaleDecrease() {
        if (scale > 1) {
            dw = (width / scale) * (scale - 1);
            this.setSize(dw, height);
            applySizePref();
            scale--;
            repaint();
        }

    }

}
