package promstudy.visualization;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class ProfileComponent extends DataComponent {

    private int maxCount;
    private double max, min;
    private ArrayList<double[]> arrays;
    private ArrayList<String> names;
    private int wStep;
    private int offset = -5000;
    public static boolean thick = false;

    public ProfileComponent(ArrayList<double[]> arrays,
                            ArrayList<String> names) {
        super("trend");
        this.arrays = arrays;
        this.names = names;
        dotSize = 14;
        margin = 40;
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
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        step = (getHeight() - margin) / (names.size() + 1);
        height = getHeight() - margin;
        width = getWidth();
        int leftMargin = 160;
        g2d.setRenderingHints(renderHints);
        g2d.setFont(new Font("Arial", Font.PLAIN, 26));
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, width, height + step);

        int n = 2;

        // x-axis
        xStep = ((double) width - leftMargin) / 10001;
        xPoint = step / xStep;
        wStep = (int) (2500 * xStep);
        for (int i = leftMargin; i < width; i += wStep) {
            g2d.setColor(gridColor);
            g2d.drawLine(i, height, i, 0);
            g2d.setColor(textColor);
            double val = Math.round(offset + (2500 * (i - leftMargin) / wStep));
            String value = String.valueOf(val);
            if (value.endsWith(".0")) {
                value = value.substring(0, value.length() - 2);
            }


            if(val == 0){
                value = "+1";
            }else if(val>0){
                value="+"+value;
            }
            int l = value.length();
            if(val<0){
                l=l-1;
            }
            g2d.drawString(value, i - 20 * (l - 1), height + 25);

        }

        // y-axis
        yPoint = (max - min) / ((double) (height / step));
        yStep = step / yPoint;
        int ii = 0;
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
            if (names.size() > ii) {
                g2d.drawString(names.get(ii++), 10, i + 5);
            }
        }
        g2d.setColor(gridColor);
        g2d.drawLine(0, height, width, height);
        g2d.drawLine(leftMargin, height, leftMargin, 0);
        //g2d.drawLine(0, height + step - 1, width, height + step - 1);
        // drawing margin
        //g2d.setColor(backgroundColor);
        //g2d.fillRect(0, height + step, width, margin);
        // drawing labels
        g2d.setColor(textColor);
        for (int i = 0; i < arrays.size(); i++) {
            g2d.setColor(colors[i % colors.length]);
            //g2d.drawString(names.get(i), 5 + (i / 2) * 60, height - 30 + margin
            //        + 20 * (i % 2) + step);
            g2d.setStroke(new BasicStroke(2));
            for (int j = 0; j < arrays.get(i).length; j++) {
                double v = arrays.get(i)[j];
                g2d.drawRect((int) (v * xStep + leftMargin) - dotSize / 4, (int) Math.round(height - step * i - step) - 1 * dotSize,
                        dotSize/2, 2 * dotSize);
                g2d.setColor(new Color(g2d.getColor().getRed(), g2d.getColor().getGreen(), g2d.getColor().getBlue(), 55));
                g2d.fillRect((int) (v * xStep + leftMargin) - dotSize / 4, (int) Math.round(height - step * i - step) - 1 * dotSize,
                        dotSize/2, 2 * dotSize);
                g2d.setColor(colors[i % colors.length]);
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

    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(new File("FP004854.txt"));
            ArrayList<String> names = new ArrayList<>();
            ArrayList<double[]> values = new ArrayList<>();
            ArrayList<Double> temp = new ArrayList<>();
            String name = "";
            while (scanner.hasNext()) {
                if (scanner.hasNextDouble()) {
                    temp.add(scanner.nextDouble());
                } else {
                    if (name.length() != 0) {
                        values.add(temp.stream().mapToDouble(d -> d).toArray());
                        names.add(name);
                        temp.clear();
                    }
                    name = scanner.next();
                }
            }
            if (name.length() != 0) {
                values.add(temp.stream().mapToDouble(d -> d).toArray());
                names.add(name);
                temp.clear();
            }
            ProfileComponent pc = new ProfileComponent(values, names);
            saveComponent(pc, new File("test_profile.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void saveComponent(JComponent jc, File outputfile) throws IOException {
        int width = 850;
        int height = 400;
        BufferedImage result = new BufferedImage(
                width, height, //work these out
                BufferedImage.TYPE_INT_RGB);
        Graphics gg = result.getGraphics();
        jc.setSize(new Dimension(width, height));
        jc.repaint();
        jc.paint(gg);
        ImageIO.write(result, "png", outputfile);

    }

}
