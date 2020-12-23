package promstudy.managers;

import promstudy.common.Element2;
import promstudy.common.FastaParser;
import promstudy.visualization.Trend;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

/**
 * Created by umarovr on 3/22/18.
 */
public class PromType {
    private static Predictor p;
    private static float[][][] sequences;
    private static int step = 100;
    private static int sLen = 1500;
    private static int sd = 10;
    private static int minDist = 1000;
    private static String output = "out.txt";
    private static ArrayList<String> names;
    private static double dt = 0.5;
    private static int count = 1;
    static int w = 1550;
    static int h = 900;

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
                        System.err.println("-out: output");
                        return;
                    }
                }
                Object[] o = FastaParser.parse(toPred, sLen);
                sequences = (float[][][]) o[0];
                names = (ArrayList<String>) o[1];
                Trend.thick = true;
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
            float[][] seq = Arrays.copyOfRange(sequences[i], 4000, 5500);
            int total = (int) Math.ceil((seq.length - step) / step) + 1 + 1;
            float[][][] toPredict = new float[total][sLen][4];
            for (int j = 0; j < toPredict.length - 1; j++) {
                float[][] seq2 = cloneArray(seq);
                for (int k = 0; k < step; k++) {
                    float[] tmp = new float[]{0, 0, 0, 0};
                    Random r = new Random();
                    tmp[r.nextInt(4)] = 1;
                    seq2[j * step + k] = tmp;
                }
                toPredict[j] = seq2;
            }
            toPredict[toPredict.length - 1] = seq;
            arrays1.add(p.predict(toPredict));
            if ((i + 1) % 10 == 0) {
                System.out.print((i + 1) + " ");
            }
            if ((i + 1) > 150) {
                break;
            }
        }
        System.out.println();

        File outDir = new File(output);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }

        Trend[] trends = new Trend[count];
        int ti = 0;
        StringBuilder[] sbMin = new StringBuilder[(arrays1.get(0).length - 1)];
        StringBuilder[] sbMax = new StringBuilder[(arrays1.get(0).length - 1)];
        for(int l = 0; l<sbMax.length; l++){
            sbMin[l] = new StringBuilder();
            sbMax[l] = new StringBuilder();
        }
        double[] stMin = new double[(arrays1.get(0).length - 1) * step];
        double[] stMax = new double[(arrays1.get(0).length - 1) * step];
        for (int ari = 0; ari < arrays1.size(); ari++) {
            double max = -Double.MAX_VALUE;
            double min = Double.MAX_VALUE;
            double sMin = 0;
            double sMax = 0;
            float[] ar1 = arrays1.get(ari);
            double maxScore = ar1[ar1.length - 1];
            double t[] = new double[ar1.length - 1];
            double t2[] = new double[ar1.length - 1];

            for (int c = 0; c < t.length; c++) {
                t[c] = ar1[c];
                t2[c] = maxScore;
                if (ar1[c] > max) {
                    max = ar1[c];
                    sMax = c * step;
                }
                if (ar1[c] < min) {
                    min = ar1[c];
                    stMin[c * step] =  (stMin[c * step] + (maxScore - ar1[c]));
                }
            }
            stMax[(int) sMax]++;
            stMin[(int) sMin]++;
        }
        ArrayList<String> n = new ArrayList<>();
        n.add(" ");
        n.add(" ");
        ArrayList<double[]> array1 = new ArrayList<>();
        array1.add(stMax);
        array1.add(stMin);
        Trend trend = new Trend(array1, n, 1);
        File out1 = new File(output);
        try {
            saveComponents(new Trend[]{trend}, "png", out1);
        } catch (Exception e) {

        }
    }

    private static void saveComponents(JComponent c[], String format, File outputfile) throws IOException {

        int cols = (int) Math.round(Math.sqrt((4.0 / 3.0) * count));
        int rows = (int) Math.ceil((double) count / cols);
        int width = cols * w;
        int height = rows * h;
        BufferedImage myImage = null;
        BufferedImage result = new BufferedImage(
                width, height, //work these out
                BufferedImage.TYPE_INT_RGB);
        Graphics gg = result.getGraphics();

        ArrayList<BufferedImage> bis = new ArrayList<>();
        for (JComponent jc : c) {
            jc.setSize(new Dimension(w, h));
            jc.repaint();
            myImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = myImage.createGraphics();
            jc.paint(g);
            bis.add(myImage);
        }

        int x = 0;
        int y = 0;
        for (BufferedImage bi : bis) {
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
