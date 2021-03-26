package promstudy.dataVisualisation;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JOptionPane;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.imgscalr.Scalr;

public class LogoComponent extends JComponent {

	protected int margin = 50,  step = 50, width,
			height;
	protected ArrayList<String> f;
	protected ArrayList<Color> colors;
	protected RenderingHints renderHints;
	protected Color backgroundColor = Color.WHITE;
	private RealMatrix F;
	private RealVector R;
	public static int minSize = 4;
	private ArrayList<String> letters;
	public  int letterWidth = 32;
	private static int maxLetterHeight = 500;
	public static int gap;
	public static int  offset=1;
	private static Map<String, Color> customColors;
	double maxLetter;
	double coef;
	public static boolean showLines = false;
	public static boolean showNumbers = false;
	public double vscale = 0.2;
	double origVScale = 1;
	double origCoef = 1;
	private int quality =0;
	

	public LogoComponent(RealMatrix F, RealVector R, ArrayList<String> letters) {
		this.F = F;
		this.R = R;
		this.letters = letters;
		colors = new ArrayList<>();
		colors.add(Color.decode("#1b8a44"));
		colors.add(Color.decode("#2175c8"));
		colors.add(Color.decode("#fbd26b"));
		//colors.add(Color.BLACK);
		colors.add(Color.decode("#ed7569"));

        customColors = new HashMap<>();
        customColors.put("A", Color.decode("#228B22"));
        customColors.put("T",Color.decode("#e62200")  );
        customColors.put("G", Color.ORANGE);
        customColors.put("C",Color.decode("#00008B"));

		colors.add(Color.BLACK);


		//Collections.shuffle(colors);

		renderHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		renderHints.put(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);

		maxLetter = Double.MIN_VALUE;
		for (int i = 0; i < R.getDimension(); i++) {

			double aSize = 0;
			for (int j = 0; j < letters.size(); j++) {
				double h = F.getEntry(j, i) * R.getEntry(i);
				aSize += h;
			}

			if (aSize > maxLetter) {
				maxLetter = aSize;
			}

		}
		coef = 2.0 / maxLetter;
		origCoef = coef;
		setPreferredSize(new Dimension(R.getDimension() * letterWidth + 100,
				maxLetterHeight + 2 * step));
	}

	public static void chooseMinSize() {
		try {
			minSize = Integer.parseInt(JOptionPane
					.showInputDialog("Choose min size (default 8):"));
		} catch (Exception ex) {
		}
	}

	public static void chooseColor() {
		try {
			String letter = JOptionPane.showInputDialog("Choose letter:");
			Color color = JColorChooser.showDialog(null, "Choose a color",
					Color.blue);
			customColors.put(letter.toUpperCase(), color);
		} catch (Exception ex) {
		}
	}

	public  void chooseLetterWidth() {
		try {
			letterWidth = Integer.parseInt(JOptionPane
					.showInputDialog("Choose letter width (default 160):"));
		} catch (Exception ex) {
		}
	}

	public static void chooseGap() {
		try {
			gap = Integer.parseInt(JOptionPane
					.showInputDialog("Choose gap (default 10):"));
		} catch (Exception ex) {
		}
	}

	public void applySizePref() {
		setPreferredSize(new Dimension(R.getDimension() * letterWidth + 100,
				(int) (vscale * maxLetterHeight + 2 * step)));
		revalidate();
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

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		margin = (int) (getHeight() * 0.5);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		setPreferredSize(new Dimension(R.getDimension() * letterWidth + 100,
				(int) (vscale * maxLetterHeight + 2 * step)));
		height = getHeight() - step;
		width = getWidth();
		g2d.setRenderingHints(renderHints);
		g2d.setColor(backgroundColor);
		g2d.fillRect(0, 0, width, height + step);
		g2d.setFont(new Font("Arial", Font.PLAIN, 28));
		g2d.setStroke(new BasicStroke(3));
		g2d.setColor(Color.BLACK);
		// drawing axis
		//g2d.drawLine(step, height, step, step);
		//g2d.drawString("bits", step / 2 - 10, step / 2);
		int n = 10;
		int sm = (int) (maxLetterHeight * vscale);
		double ml = (double)(height - step)/sm;
		
		for (int i = 0; i <= n; i+=n/2) {
			int h = (int) (height - i * ((double) (height - step) / n));
			//g2d.drawLine(step - 10, h, step, h);
			double d = ml*(maxLetter * ((double) i / n));
			DecimalFormat df = new DecimalFormat("#.##");
			//g2d.drawString(df.format(d), step / 2 - 10, h + 5);
		}


		g2d.setColor(Color.BLACK);
		int[] totalHeights = new int[R.getDimension()];
		double[] totalValues = new double[R.getDimension()];
		for (int i = 0; i < R.getDimension(); i++) {
			g2d.setColor(Color.BLACK);
			g2d.setFont(new Font("Arial", Font.PLAIN, 12));
			//g2d.drawString((offset + i) + "", step + gap + i * letterWidth
			//		+ (int) (letterWidth * 0.4), height + 35);

			LetterAndValue[] lav = new LetterAndValue[letters.size()];

			for (int j = 0; j < letters.size(); j++) {
				double h = F.getEntry(j, i) * R.getEntry(i);
				int letterSize = (int) Math.round(h
						* ((maxLetterHeight) / 2));
				letterSize = (int) (vscale * letterSize);
				totalHeights[i] += letterSize;
				totalValues[i] += h;
				if (letterSize < minSize) {
					letterSize = 0;
				}
				Color col;
				if (customColors.containsKey(letters.get(j))) {
					col = customColors.get(letters.get(j));
				} else {
					col = colors.get(j % colors.size());
				}
				lav[j] = new LetterAndValue(letters.get(j).charAt(0),
						letterSize, col);
			}

			int hh = height;
			Arrays.sort(lav);
			for (int j = 0; j < letters.size(); j++) {
				try {

					Font font = new Font("Verdana", Font.BOLD, 1024);
					font = font.deriveFont(
							Collections.singletonMap(
									TextAttribute.WEIGHT, TextAttribute.WEIGHT_SEMIBOLD));
					g2d.setFont(font);
					CharacterImageGenerator characterGenerator = new CharacterImageGenerator(
							g2d.getFontMetrics(), lav[j].col);

					Image img = characterGenerator.getImage(lav[j].c);

					int x = 5 + step + gap + i * letterWidth;
					int y = (int) (hh - coef * lav[j].val);
					int w = (int) (letterWidth * 0.96);
					int h = (int) (coef * lav[j].val);
					BufferedImage bimage = toBufferedImage(img);
					//BufferedImage img2 = resizeImageWithHint(bimage, w, h);

					BufferedImage img2;
					if(quality==0){
						img2= Scalr.resize(bimage, Scalr.Method.SPEED, Scalr.Mode.FIT_EXACT,
							               w, h, Scalr.OP_ANTIALIAS);
					}else{
						img2 = Scalr.resize(bimage, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_EXACT,
					               w, h, Scalr.OP_ANTIALIAS);
					}
					
					g2d.drawImage(img2, x, y, w, h, null);
				} catch (Exception ex) {

				}
				hh = (int) (hh - coef * lav[j].val);
			}
		}
		if (showLines) {
			for (int i = 0; i < totalHeights.length; i++) {
				int xx = 5 + step + gap + i * letterWidth;
				int yy = (int) (height - coef * totalHeights[i]);
				g2d.drawLine(xx, yy, xx + letterWidth, yy);
				if (showNumbers) {
					g2d.setFont(new Font("Verdana", Font.PLAIN, 12));
					g2d.drawString(String.format("%.2f", totalValues[i]), xx,
							yy - 10);
				}
			}
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

	private static BufferedImage resizeImageWithHint(
			BufferedImage originalImage, int w, int h) {

		BufferedImage resizedImage = new BufferedImage(w, h,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = resizedImage.createGraphics();

		g.setComposite(AlphaComposite.Src);

		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.drawImage(originalImage, 0, 0, w, h, null);
		g.dispose();
		return resizedImage;
	}

	public void increaseWidth() {
		try {
			letterWidth = letterWidth + 10;
			applySizePref();
		} catch (Exception ex) {
		}

	}

	public void decreaseWidth() {
		try {
			letterWidth = letterWidth - 10;
			applySizePref();
		} catch (Exception ex) {
		}

	}

	public void increaseHeight() {
		try {
			vscale += 0.1 * origVScale;
			applySizePref();
		} catch (Exception ex) {
		}
		setPreferredSize(new Dimension(R.getDimension() * letterWidth + 100,
				(int) (vscale * maxLetterHeight + 2 * step)));

	}
	
	public void decreaseHeight() {
		try {
			if (vscale > 0.1 * origVScale) {
				vscale -= 0.1 * origVScale;
				applySizePref();
			}
		} catch (Exception ex) {
		}

	}



	public static void showLines(boolean b) {
		showLines = b;
	}

	public static void showNumbers(boolean b) {
		showNumbers = b;
	}

	public void setQuality(int i) {
		quality = i;
		
	}

	public static void chooseOffset() {
		try {
			offset = Integer.parseInt(JOptionPane
					.showInputDialog("Choose offset:"));
		} catch (Exception ex) {
		}
		
	}

}
