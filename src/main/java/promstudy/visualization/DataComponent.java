package promstudy.visualization;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.RenderingHints;
import javax.swing.JComponent;

public abstract class DataComponent extends JComponent {

    protected int scale = 1, margin = 0, dotSize = 3, step = 50, width, height;
    protected Color[] colors;
    protected RenderingHints renderHints;
    protected static Color backgroundColor = Color.WHITE;
    protected static Color gridColor = Color.LIGHT_GRAY;
    protected static Color textColor = Color.BLACK;
    protected Color decThresholdColor = new Color(131, 204, 107);
    protected int dw, dh;
    private String type;
    protected double xPoint;
    protected double xStep;
    protected double yPoint;
    protected double yStep;

    public static void setBlackTheme() {
        backgroundColor = new Color(23, 25, 29);
        gridColor = new Color(32, 33, 36);
        textColor = new Color(204, 204, 204);
    }

    public static void setWhiteTheme() {
        backgroundColor = Color.WHITE;
        gridColor = Color.LIGHT_GRAY;
        textColor = Color.BLACK;
    }

    public String getType() {
        return type;
    }

    public DataComponent(String t) {
        initColors();
        renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        renderHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        type = t;
    }

    public void applySizePref() {
        this.setSize(dw, dh);
        setPreferredSize(new Dimension(dw, dh + margin));
    }

    public void dotSizeInc() {
        dotSize++;
    }

    public void dotSizeDec() {
        if (dotSize > 1) {
            dotSize--;
        }
    }

    public void scaleIncrease() {
        scale++;
        applySizePref();
        repaint();
    }

    public void scaleDecrease() {
        if (scale > 1) {
            scale--;
            applySizePref();
            repaint();
        }
    }

    public int getScale() {
        return scale;
    }

    public int getMargin() {
        return margin;
    }

    public double Round(double value, int dp) {
        double p = Math.pow(10, dp);
        value = value * p;
        double tmp = Math.round(value);
        return tmp / p;
    }

    public int Round(double val) {
        return (int) Math.round(val);
    }

    public void setType(String t) {
        type = t;
    }

    private void initColors() {
        colors = new Color[28];
        colors[0] = new Color(255, 62, 62);
        colors[1] = new Color(0, 154, 255);
        colors[2] = Color.CYAN;
        colors[3] = Color.GREEN;
        colors[4] = Color.MAGENTA;
        colors[5] = Color.ORANGE;
        colors[6] = Color.PINK;
        colors[7] = Color.decode("#8B2252");
        colors[8] = Color.decode("#DA70D6");
        colors[9] = Color.decode("#483D8B");
        colors[10] = Color.decode("#000080");
        colors[11] = Color.decode("#708090");
        colors[12] = Color.decode("#CDC1C5");
        colors[13] = Color.decode("#FF82AB");
        colors[14] = Color.decode("#00FA9A");
        colors[15] = Color.decode("#2E8B57");
        colors[16] = Color.decode("#DC143C");
        colors[17] = Color.decode("#6B8E23");
        colors[18] = Color.decode("#EEEE00");
        colors[19] = Color.decode("#FFD700");
        colors[20] = Color.decode("#FFA500");
        colors[21] = Color.decode("#8B7355");
        colors[22] = Color.decode("#EE4000");
        colors[23] = Color.decode("#CD4F39");
        colors[24] = Color.decode("#F5DEB3");
        colors[25] = Color.decode("#8B864E");
        colors[26] = Color.decode("#CD919E");
        colors[27] = Color.decode("#551A8B");
        //Collections.shuffle(Arrays.asList(colors));
    }
}
