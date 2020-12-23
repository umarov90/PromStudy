package promstudy.visualization;

import java.awt.*;
import java.util.ArrayList;

public class Trend extends DataComponent {

    private int maxCount;
    private double max, min;
    private ArrayList<double[]> arrays;
    private ArrayList<String> names;
    private int wStep;
    private int offset;
    public static boolean thick = false;

    public Trend(ArrayList<double[]> arrays,
                 ArrayList<String> names, int wStep) {
        super("trend");
        this.arrays = arrays;
        this.names = names;
        this.wStep = wStep;
        refresh();
    }

    public Trend(ArrayList<double[]> arrays,
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
        if(!thick){
            max = 1.0;
            min = 0.0;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        height = getHeight() - margin - step;
        width = getWidth();
        g2d.setRenderingHints(renderHints);
        g2d.setFont(new Font("Arial", Font.PLAIN, 26));
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, width, height + step);

        int n = 2;
        int leftMargin = 80;
        // x-axis
        xPoint = (double) maxCount / (((double) width - step) / step);
        if (xPoint != Math.floor(xPoint)) {
            if (thick) {
                xPoint = Math.floor(xPoint) + 1;
            } else {
                xPoint = Math.floor(xPoint);//+ 1;
            }
        }
        xStep = step / xPoint;
        for (int i = leftMargin + step; i < width; i += step) {
            g2d.setColor(gridColor);
            g2d.drawLine(i, height, i, 0);
            g2d.setColor(textColor);
            double val = offset + (xPoint * (i - leftMargin) / step) * wStep;
            String value = String.valueOf(val);
            if (value.endsWith(".0")) {
                value = value.substring(0, value.length() - 2);
            }
            if(i%(2*step)!=0){
                g2d.drawString(value, i - 6 * (value.length() - 1), height + 15);
            }
        }

        // y-axis
        yPoint = (max - min) / ((double) (height / step));
        yStep = step / yPoint;
        for (int i = height - step; i > -3; i -= step) {
            g2d.setColor(gridColor);
            g2d.drawLine(step, i, width, i);
            g2d.setColor(textColor);
            double val;
            if(thick) {
                val = Round(min + yPoint * (height - i) / step, 0);
            }else{
                val = Round(min + yPoint * (height - i) / step, 2);
            }
            String value = String.valueOf(val);
            if (value.endsWith(".0")) {
                value = value.substring(0, value.length() - 2);
            }
            g2d.drawString(value, step - 25 - n * 5, i + 15);

        }
        g2d.setColor(gridColor);
        g2d.drawLine(0, height, width, height);
        g2d.drawLine(step, height + step, step, 0);
        g2d.drawLine(0, height + step - 1, width, height + step - 1);
        // drawing margin
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, height + step, width, margin);
        // drawing labels
        g2d.setColor(textColor);
        for (int i = 0; i < arrays.size(); i++) {
            g2d.setColor(colors[i % colors.length]);
            //g2d.drawString(names.get(i), 5 + (i / 2) * 60, height - 30 + margin
             //       + 20 * (i % 2) + step);

            for (int j = 0; j < arrays.get(i).length; j++) {
                double v = arrays.get(i)[j];
                g2d.setColor(new Color(g2d.getColor().getRed(), g2d.getColor().getGreen(), g2d.getColor().getBlue(), 5));
                g2d.fillRect((int) (j * xStep + leftMargin) - dotSize / 2, height
                                - (int) Math.round(yStep * (v - min)) - dotSize / 2,
                        dotSize, dotSize);

                Color old = colors[i % colors.length];
                if (thick) {
                    Color newColor = new Color(old.getRed(), old.getGreen(), old.getBlue(), 90);
                    g2d.setColor(newColor);
                } else {
                    g2d.setColor(old);
                }
                g2d.setStroke(new BasicStroke(2));
                if (j < arrays.get(i).length - 1) {
                    double v2 = arrays.get(i)[j + 1];
                    if (thick) {
                        g2d.setStroke(new BasicStroke(5));
                    }
                    g2d.drawLine((int) (j * xStep + leftMargin),
                            height - (int) Math.round(yStep * (v - min)),
                            (int) ((j + 1) * xStep + leftMargin),
                            height - (int) Math.round(yStep * (v2 - min)));
                    g2d.setStroke(new BasicStroke(1));
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
