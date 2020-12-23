package promstudy.visualization;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

public class Histogram extends DataComponent {

    private int numberOfBins, maxCount;
    private static boolean histogramShape;
    private int[][] points;
    private double maxX, minX, binSize;
    private ArrayList<double[]> vals;
    ArrayList<String> names;

    public Histogram(ArrayList<double[]> vals, ArrayList<String> names) {
        super("histogram");
        this.vals = vals;
        this.names = names;
    }

    public void setBins(int n) {
        numberOfBins = n;
        points = new int[vals.size()][numberOfBins];
        refresh();
    }

    private void refresh() {
        maxX = -Double.MAX_VALUE;
        minX = Double.MAX_VALUE;
        maxCount = 0;
        for (int i = 0; i < vals.size(); i++) {
            for (int j = 0; j < vals.get(i).length; j++) {
                double x = vals.get(i)[j];
                if (x > maxX) {
                    maxX = x;
                }
                if (x < minX) {
                    minX = x;
                }
            }
        }
        binSize = (maxX - minX) / numberOfBins;
        for (int i = 0; i < vals.size(); i++) {
            for (int j = 0; j < vals.get(i).length; j++) {
                double x = vals.get(i)[j];
                int t = (int) Math.floor((x - minX) / binSize);
                if (t == numberOfBins) {
                    t--;
                }
                points[i][t]++;
                if (points[i][t] > maxCount) {
                    maxCount = points[i][t];
                }
            }
        }
        applySizePref();
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        height = getHeight() - margin - step;
        width = getWidth();
        g2d.setRenderingHints(renderHints);
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, width, height + step);
        g2d.setColor(Color.BLACK);
        xPoint = (maxX - minX) / ((width - step) / step);
        xStep = step / xPoint;

        int n = scale / 2 + 1;
        if (maxX - minX < 1) {
            n = 2 + scale / 2;
        }

        //x-axis
        for (int i = step + step; i < width; i += step) {
            g2d.setColor(gridColor);
            g2d.drawLine(i, height, i, 0);
            g2d.setColor(textColor);
            double val = Round(minX + xPoint * (i - step) / step, n);
            String value = String.valueOf(val);
            if (value.endsWith(".0")) {
                value = value.substring(0, value.length() - 2);
            }
            g2d.drawString(value, i - 5 * n, height + 15);
        }

        yPoint = (double) maxCount / ((height) / step);
        if (yPoint != Math.floor(yPoint)) {
            yPoint = Math.floor(yPoint) + 1;
        }
        yStep = step / yPoint;

        //y-axis
        for (int i = height - step; i > -3; i -= step) {
            g2d.setColor(gridColor);
            g2d.drawLine(step, i, width, i);
            g2d.setColor(textColor);
            double val = (-yPoint * (i - height) / step);
            String value = String.valueOf(val);
            if (value.endsWith(".0")) {
                value = value.substring(0, value.length() - 2);
            }
            g2d.drawString(value, 5, i + 5);

        }
        g2d.setColor(gridColor);
        g2d.drawLine(0, height, width, height);
        g2d.drawLine(step, height + step, step, 0);
        g2d.drawLine(0, height+step-1, width, height+step-1);
        //drawing margin
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, height + step, width, margin);
        //drawing labels
        g2d.setColor(textColor);
        if (!histogramShape) {
            for (int i = 0; i < vals.size(); i++) {
                g2d.setColor(colors[i % colors.length]);
                g2d.drawString(names.get(i), 5 + (i / 2) * 60, height - 30 + margin + 20 * (i % 2) + step);
                for (int j = 0; j < numberOfBins; j++) {
                    if (points[i][j] > 0) {
                        g2d.fillRect(Round(j * binSize * xStep + step) - dotSize / 2,
                                height - Round(points[i][j] * yStep) - dotSize / 2, dotSize, dotSize);
                        if (j < numberOfBins - 1) {
                            g2d.drawLine(Round(j * binSize * xStep + step), height - Round(points[i][j] * yStep),
                                    Round((j + 1) * binSize * xStep + step), height - Round(points[i][j + 1] * yStep));
                        }
                    }
                }
            }
        } else {
            for (int j = 0; j < numberOfBins; j++) {
                ArrayList<Integer> index = new ArrayList<Integer>();
                for (int i = 0; i < vals.size(); i++) {
                    int max = -Integer.MAX_VALUE, it = 0;
                    for (int c = 0; c < vals.size(); c++) {
                        if (points[c][j] > max && !index.contains(c)) {
                            max = points[c][j];
                            it= c;
                        }
                    }
                    index.add(it);
                    int ci = index.get(index.size() - 1);
                    g2d.setColor(colors[ci % colors.length]);
                    g2d.drawString(names.get(ci), 5 + (ci / 3) * 60, height - 30 + margin + 20 * (ci % 2) + step);
                    if (points[ci][j] > 0) {
                        int y = Round(points[ci][j] * yStep);
                        g2d.fillRect(Round(j * binSize * xStep + step), height - y, Round(binSize * xStep), y);
                    }
                }
            }
        }
    }

    @Override
    public void scaleIncrease() {
        dw = (width / scale) * (scale + 1);
        scale++;
        this.setSize(dw, height);
        applySizePref();
        repaint();
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

    public static void setShape(boolean s) {
        histogramShape = s;
    }
}
