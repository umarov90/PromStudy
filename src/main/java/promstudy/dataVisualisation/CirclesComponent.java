package promstudy.dataVisualisation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class CirclesComponent extends JComponent {

	protected int step = 50, width, height;
	protected RenderingHints renderHints;
	protected Color backgroundColor = Color.WHITE;
	RealMatrix forColor;
	private RealMatrix NMI;
	private double max, min;
	public static int circleSize;

	public CirclesComponent(RealMatrix NMI, RealMatrix forColor, double max,
			double min) {
		this.NMI = NMI;
		this.forColor = forColor;
		this.max = max;
		this.min = min;

		renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		renderHints.put(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);

		circleSize = 30;

		int pw = (43 + NMI.getColumnDimension() * (circleSize + 10)) + step;
		int ph = (25 + NMI.getColumnDimension() * (circleSize + 10) + step);
		setPreferredSize(new Dimension(pw + 20, ph + 20));
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		int pw = (43 + NMI.getColumnDimension() * (circleSize + 10)) + step;
		int ph = (25 + NMI.getColumnDimension() * (circleSize + 10) + step);
		setPreferredSize(new Dimension(pw + 20, ph + 20));
		height = getHeight() - step;
		width = getWidth();
		g2d.setRenderingHints(renderHints);
		g2d.setColor(backgroundColor);
		g2d.fillRect(0, 0, width, height + step);
		g2d.setFont(new Font("Arial", Font.PLAIN, 14));

		g2d.setColor(Color.BLACK);

		int start = step;
		int startH = step;
		g2d.setFont(new Font("Arial", Font.PLAIN, 14));
		for (int j = 0; j < NMI.getColumnDimension(); j++) {
			g2d.drawString((j + 1) + "", start, startH
					+ (25 + j * (circleSize + 10)));

			g2d.drawString((j + 1) + "", start + (43 + j * (circleSize + 10)),
					start);

			g2d.drawString((j + 1) + "", pw, startH
					+ (25 + j * (circleSize + 10)));

			g2d.drawString((j + 1) + "", start + (43 + j * (circleSize + 10)),
					ph);
		}

		for (int i = 0; i < NMI.getRowDimension(); i++) {
			for (int j = 0; j < NMI.getColumnDimension(); j++) {
				double val = NMI.getEntry(i, j);
				if (i == j) {
					val = 1;
				}
				if (j >= i) {
					break;
				}
				if (forColor.getEntry(i, j) == 1) {
					g2d.setColor(Color.RED);
				} else {
					g2d.setColor(Color.BLUE);
				}
				val = (val - min) / (max - min);
				int intVal = (int) Math.round(circleSize * val);
				int halfVal = (int) Math.round(((double) intVal) / 2.0);
				int x = 2 * step + j * (circleSize + 10) - halfVal;
				int y = 10 + startH + (10 + i * (circleSize + 10)) - halfVal;
				g2d.fillOval(x + 1, y + 1, intVal, intVal);
				val = 1;
				intVal = (int) Math.round(circleSize * val);
				halfVal = (int) Math.round(((double) intVal) / 2.0);
				x = 2 * step + j * (circleSize + 10) - halfVal;
				y = 10 + startH + (10 + i * (circleSize + 10)) - halfVal;
				g2d.drawOval(x, y, intVal, intVal);
			}
		}

	}

	public static void chooseCircleSize() {
		try {
			circleSize = Integer.parseInt(JOptionPane
					.showInputDialog("Choose circle size (default 30):"));
		} catch (Exception ex) {
		}

	}

}
