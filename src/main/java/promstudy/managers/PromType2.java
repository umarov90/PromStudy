package promstudy.managers;

import promstudy.common.FastaParser;
import promstudy.visualization.Trend;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by umarovr on 3/22/18.
 */
public class PromType2 {
    private static Predictor p;
    private static float[][][] sequences;
    private static int step = 1;
    private static int sLen = 1500;
    private static int sd = 10;
    private static int minDist = 1000;
    private static String output = "out.txt";
    private static ArrayList<String> names;
    private static double dt = 0.5;
    private static int count = 1;
    static int w = 1600;
    static int h = 1200;

    public static void main(String[] args) {
        File toPred = null;
        if (args.length > 0) {
            try {
                for (int i = 0; i < args.length / 2; i++) {
                    String option = args[2 * i];
                    String parameter = args[2 * i + 1];
                    if (option.equals("-set")) {
                        toPred = new File(parameter);
                    } else if (option.equals("-mod")) {
                        p = new Predictor(parameter);
                    } else if (option.equals("-out")) {
                        output = parameter;
                    } else {
                        System.err.println("Unknown option: " + option);
                        System.err.println("Available Options: ");
                        System.err.println("-set: file with long sequences");
                        System.err.println("-mod: location of trained model");
                        System.err.println("-out: output file with scores");
                        System.err.println("-sl: sliding window size");
                        System.err.println("-sd: parameter for promoter search");
                        System.err.println("-mp: maximum promoters to output");
                        System.err.println("-md: minimum distance allowed between promoters");
                        return;
                    }
                }
                Object[] o = FastaParser.parse(toPred, sLen);
                sequences = (float[][][]) o[0];
                names = (ArrayList<String>) o[1];
                analyse();
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static void analyse() {
        ArrayList<float[]> arrays1 = new ArrayList<>();
        System.out.println("Progress: ");
        for (int i = 0; i < sequences.length; i++) {
            float[][] seq = Arrays.copyOfRange(sequences[i], 3900, 5600);
            int total = (int) Math.ceil((seq.length - sLen) / step) + 1;
            float[][][] toPredict = new float[total][sLen][4];
            for (int j = 0; j < toPredict.length; j++) {
                toPredict[j] = Arrays.copyOfRange(seq, j * step, j * step + sLen);
            }
            arrays1.add(p.predict(toPredict));
            if ((i + 1) % 10 == 0) {
                System.out.print((i + 1) + " ");
                if ((i + 1) >= 100){
                    break;
                }
            }
        }
        System.out.println();

        File outDir = new File(output);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        Trend[] trends = new Trend[count];
        int ti = 0;
        for (int ari = 0; ari < arrays1.size(); ari++) {
            float[] ar1 = arrays1.get(ari);
            ArrayList<String> names = new ArrayList<>();
            names.add("");
            ArrayList<double[]> array1 = new ArrayList<>();
            double t[] = new double[ar1.length];
            for(int c = 0; c<t.length; c++){
                t[c] = ar1[c];
            }
            array1.add(t);
            trends[ti++] = new Trend(array1, names, step, -100);
            if (ti >= count || ari==arrays1.size()-1) {
                ti = 0;
                try {
                    File out = new File(outDir.getAbsolutePath() + File.separator + "File " + (ari / count + 1) + ".png");
                    saveComponents(trends, "png", out);
                } catch (Exception e) {

                }
            }

        }
    }

    private static void saveComponents(JComponent c[], String format, File outputfile) throws IOException {

        int cols = (int) Math.round(Math.sqrt((4.0 / 3.0) * count));
        int rows = (int)Math.ceil( (double)count / cols);
        int width =cols * w;
        int height = rows * h;
        BufferedImage myImage = null;
        BufferedImage result = new BufferedImage(
                width, height, //work these out
                BufferedImage.TYPE_INT_RGB);
        Graphics gg = result.getGraphics();

        ArrayList<BufferedImage> bis = new ArrayList<>();
        for(JComponent jc : c) {
            jc.setSize(new Dimension(w, h));
            jc.repaint();
            myImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = myImage.createGraphics();
            jc.paint(g);
            bis.add(myImage);
        }

        int x = 0;
        int y = 0;
        for (BufferedImage bi:bis) {
            gg.drawImage(bi, x, y, null);
            x += w;
            if (x >= result.getWidth()) {
                x = 0;
                y += h;
            }
        }


        ImageIO.write(result, format, outputfile);

    }
    public static float[][] cloneArray(float[][] src) {
        int length = src.length;
        float[][] target = new float[length][src[0].length];
        for (int i = 0; i < length; i++) {
            System.arraycopy(src[i], 0, target[i], 0, src[i].length);
        }
        return target;
    }
}
