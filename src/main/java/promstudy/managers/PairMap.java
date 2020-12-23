package promstudy.managers;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import promstudy.clustering.KMeansMatrices;
import promstudy.common.FastaParser;
import promstudy.visualization.PairMapComp;
import promstudy.visualization.SalMapComp;
import promstudy.visualization.Trend;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by umarovr on 3/22/18.
 */
public class PairMap {
    private static Predictor p;
    private static float[][][] sequences;
    private static int step = 1;
    private static int sLen = 1001;
    private static int sd = 10;
    private static int minDist = 1000;
    private static String output = "output";
    private static ArrayList<String> names;
    private static double dt = 0.5;
    private static int count = 1;
    private static int anLen = 81;
    private static int start = 460;
    static int w = 30 * anLen;
    static int h = 30 * anLen;
    private static boolean ignoreCore = false;
    private static int numSeq = 18000; //7900

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
                    } else if (option.equals("-core")) {
                        ignoreCore = Integer.parseInt(parameter) == 0;
                    } else if (option.equals("-ns")) {
                        numSeq = Integer.parseInt(parameter);
                    } else {
                        System.err.println("Unknown option: " + option);
                        System.err.println("Available Options: ");
                        System.err.println("-set: file with long sequences");
                        System.err.println("-mod: location of trained model");
                        System.err.println("-out: output");
                        return;
                    }
                }
                Object[] o = FastaParser.parse(toPred, 100);
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
        ArrayList<float[]> arrays2 = new ArrayList<>();
        if (new File("arrays1_pm.bin").exists()) {
            try {
                FileInputStream fileIn = new FileInputStream("arrays1_pm.bin");
                ObjectInputStream in = new ObjectInputStream(fileIn);
                arrays1 = (ArrayList<float[]>) in.readObject();
                in.close();
                fileIn.close();
                fileIn = new FileInputStream("arrays2_pm.bin");
                in = new ObjectInputStream(fileIn);
                arrays2 = (ArrayList<float[]>) in.readObject();
                in.close();
                fileIn.close();
            } catch (Exception e) {

            }
        } else {
            System.out.println("Progress: ");
            for (int i = 0; i < numSeq; i++) {
                //float[][] seq = Arrays.copyOfRange(sequences[i], 4800, 5400);
                float[][] seq = sequences[i];
                int total = anLen + 1;
                float[][][] toPredict = new float[total][sLen][4];
                for (int j = 0; j < toPredict.length - 1; j++) {
                    float[][] seq2 = cloneArray(seq);
                    float[] tmp = seq2[start + j].clone();
                    for (int d = 0; d < tmp.length; d++) {
                        if (tmp[d] == 1) {
                            tmp[d] = 0;
                        }
                    }
                    seq2[start + j] = tmp;
                    toPredict[j] = seq2;
                }
                toPredict[toPredict.length - 1] = seq;
                arrays1.add(p.predict(toPredict));

                total = (anLen * anLen - anLen) / 2;
                toPredict = new float[total][sLen][4];
                int ind = 0;
                for (int r = 0; r < anLen; r++) {
                    for (int j = 0; j < r; j++) {
                        float[][] seq2 = cloneArray(seq);

                        float[] tmp = seq2[start + j].clone();
                        for (int d = 0; d < tmp.length; d++) {
                            if (tmp[d] == 1) {
                                tmp[d] = 0;
                            }
                        }
                        seq2[start + j] = tmp;

                        tmp = seq2[start + r].clone();
                        for (int d = 0; d < tmp.length; d++) {
                            if (tmp[d] == 1) {
                                tmp[d] = 0;
                            }
                        }
                        seq2[start + r] = tmp;

                        toPredict[ind++] = seq2;
                    }
                }
                arrays2.add(p.predict(toPredict));

                if ((i + 1) % 10 == 0) {
                    System.out.print((i + 1) + " ");
                }
            }
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream("arrays1_pm.bin");
                ObjectOutputStream out = new ObjectOutputStream(fos);
                out.writeObject(arrays1);
                fos = new FileOutputStream("arrays2_pm.bin");
                out = new ObjectOutputStream(fos);
                out.writeObject(arrays2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        double max = -Double.MAX_VALUE;
        double min = Double.MAX_VALUE;
        double[][] total = new double[anLen][anLen];
        ArrayList<RealMatrix> results = new ArrayList<>();
        for (int ari = 0; ari < arrays1.size(); ari++) {
            float[][] seq = sequences[ari];
            float[] ar1 = arrays1.get(ari);
            float[] ar2 = arrays2.get(ari);
            double maxScore = ar1[ar1.length - 1];
            if (maxScore < 0.5) {
                continue;
            }

            int ind = 0;
            double[][] data = new double[anLen][anLen];
            for (int r = 0; r < anLen; r++) {
                for (int j = 0; j < r; j++) {
                    Double v = 0.0;
                    //if (Math.abs(maxScore - ar1[r]) + Math.abs(maxScore - ar1[j]) > 0.000001) {
                    //if(r>35 && r < 45 && j > 35 && j < 45){
                    //    v = 0.0;
                    //}
                    //else {
                    v = Math.abs(maxScore - ar2[ind]) - Math.abs((maxScore - ar1[r]) + (maxScore - ar1[j]));
                    //}
                    if (v > 0) {
                        v = 1 / (Math.exp(-1000 * Math.abs(v)) + 1)  - 0.5;
                    } else if (v < 0) {
                        v = -1 * (1 / (Math.exp(-1000 * Math.abs(v)) + 1) - 0.5);
                    } else {
                        v = 0.0;
                    }
                    //}
                    //Double v = Math.abs(maxScore - ar2[ind]) - (Math.abs(maxScore - ar1[r]) + Math.abs(maxScore - ar1[j]));
                    ind++;
                    if (!v.isNaN()) {
                        if (v > max) {
                            max = v;
                        }
                        if (v < min) {
                            min = v;
                        }
                        data[r][j] += v;
                        total[r][j] += v;
                    }

                    //data[r][j] = Math.abs(maxScore - ar2[ind++]);
                    //kmeans cluster
                }
            }
            results.add(new Array2DRowRealMatrix(data));
        }
        System.out.println(max + "  " + min);

        RealMatrix tm = new Array2DRowRealMatrix(total);
        tm = tm.scalarMultiply(1.0 / arrays1.size());
        try {
            saveComponents(new PairMapComp[]{new PairMapComp(tm.getData(), 40)}, "png", new File(output + "_pair_map_total.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }


        ArrayList<RealMatrix> clusters = KMeansMatrices.freqMatrix(results);


        Trend.thick = true;
        for (int i = 0; i < clusters.size(); i++) {
            try {
                saveComponents(new PairMapComp[]{new PairMapComp(clusters.get(i).getData(), 40)}, "png", new File(output + "_pair_map_" + i + ".png"));
            } catch (Exception e) {
                e.printStackTrace();
            }
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
