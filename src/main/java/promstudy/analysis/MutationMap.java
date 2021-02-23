package promstudy.analysis;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import promstudy.clustering.KMeansMatrices;
import promstudy.common.FastaParser;
import promstudy.common.Utils;
import promstudy.common.Predictor;
import promstudy.visualization.SalMapComp;
import promstudy.visualization.Trend;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by umarovr on 3/22/18.
 */
public class MutationMap {
    private static Predictor p;
    private static float[][][] sequences;
    private static int step = 1;
    private static int sLen = 1001;
    private static int tss = 41;
    private static int sd = 10;
    private static int minDist = 1000;
    private static String output = "output";
    private static double dt = 0.5;
    private static int count = 1;
    private static int mutVizSize = 81;
    private static int start = sLen / 2 - mutVizSize/2;
    static int w = 40 * 83;
    static int h = 6 * 40 + 100;
    private static int numSeq = 1000;

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
                    }  else if (option.equals("-ns")) {
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
                //analyseSal();
                analyseMut();
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static void analyseSal() {
        ArrayList<float[]> arrays1 = new ArrayList<>();
        System.out.println("Progress: ");
        for (int i = 0; i < numSeq; i++) {
            //float[][] seq = Arrays.copyOfRange(sequences[i], 4800, 5400);
            float[][] seq = sequences[i];
            int total = 81 + 1;
            float[][][] toPredict = new float[total][sLen][4];
            for (int j = start; j < start + 81; j++) {
                float[][] seq2 = Utils.cloneArray(seq);
                float[] tmp = seq2[j].clone();
                for (int d = 0; d < tmp.length; d++) {
                    if (tmp[d] == 1) {
                        tmp[d] += 0.000005;
                    }
                }
                seq2[j] = tmp;
                toPredict[j - start] = seq2;
            }
            toPredict[toPredict.length - 1] = seq;
            arrays1.add(p.predict(toPredict));
            if ((i + 1) % 10 == 0) {
                System.out.print((i + 1) + " ");
            }
        }
        ArrayList<RealMatrix> resultsCore = new ArrayList<>();
        //double[] trackNoCore = new double[4 * (arrays1.get(0).length - 1) * step];
        double[] trackCoreTotal = new double[4 * (arrays1.get(0).length - 1) * step];
        for (int ari = 0; ari < arrays1.size(); ari++) {
            float[][] seq = sequences[ari];
            float[] ar1 = arrays1.get(ari);
            double maxScore = ar1[ar1.length - 1];
            double[] trackCore = new double[4 * (arrays1.get(0).length - 1) * step];
            for (int c = 0; c < ar1.length - 1; c++) {
                int n = 0;
                if (seq[c][0] == 1) {
                    n = 0;
                } else if (seq[c][1] == 1) {
                    n = 1;
                } else if (seq[c][2] == 1) {
                    n = 2;
                } else if (seq[c][3] == 1) {
                    n = 3;
                }
                int p = c * 4 + n;
                trackCore[p] = (trackCore[p] + (ar1[c] - maxScore));
                trackCoreTotal[p] = (trackCoreTotal[p] + (ar1[c] - maxScore));
                if (c > 800 && c < 1200) {

                } else {
                    //trackNoCore[p] = (trackNoCore[p] + (ar1[c] - maxScore));
                }
            }
            resultsCore.add(new Array2DRowRealMatrix(trackCore));
        }

        try {
            Utils.saveComponents(new SalMapComp[]{new SalMapComp(trackCoreTotal, tss)},
                    "png", new File(output + "_sal_core.png"), count, w, h);
        } catch (Exception e) {
            e.printStackTrace();
        }


        ArrayList<RealMatrix> clusters = KMeansMatrices.freqMatrix(resultsCore);

        for (int i = 0; i < clusters.size(); i++) {
            try {
                Utils.saveComponents(new SalMapComp[]{new SalMapComp(clusters.get(i).getColumn(0), tss)}, "png",
                        new File(output + "_sal_core_" + i + ".png"), count, w, h);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Trend.thick = true;
        try {
            //saveComponents(new SalMapComp[]{new SalMapComp(trackNoCore)}, "png", new File(output + "_sal_no_core.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void analyseMut() {
        ArrayList<float[]> arrays1 = new ArrayList<>();
        // Make all the required predictions or load them if they are already done
        String input_file = "prom1_mm.bin"; //"enhancer_mm.bin"
        if (new File(input_file).exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(input_file);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                arrays1 = (ArrayList<float[]>) in.readObject();
                in.close();
                fileIn.close();
            } catch (Exception e) {

            }
        } else {
            System.out.println("Progress: ");
            for (int i = 0; i < numSeq; i++) {
                float[][] seq = sequences[i];
                int total = 4 * mutVizSize + 1;
                float[][][] toPredict = new float[total][sLen][4];
                for (int j = start * 4; j < start * 4 + mutVizSize * 4; j++) {
                    float[][] seq2 = Utils.cloneArray(seq);
                    float[] tmp = new float[]{0, 0, 0, 0};
                    tmp[j % 4] = 1;
                    seq2[j / 4] = tmp;
                    toPredict[j - start * 4] = seq2;
                }
                toPredict[toPredict.length - 1] = seq;
                arrays1.add(p.predict(toPredict));
                if ((i + 1) % 10 == 0) {
                    System.out.print((i + 1) + " ");
                }
            }
            // Save the predictions to disk
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(input_file);
                ObjectOutputStream out = new ObjectOutputStream(fos);
                out.writeObject(arrays1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ArrayList<RealMatrix> resultsCore = new ArrayList<>();
        double[] trackCoreTotal = new double[4 * (arrays1.get(0).length - 1) * step];
        for (int ari = 0; ari < arrays1.size(); ari++) {
            float[] ar1 = arrays1.get(ari);
            double maxScore = ar1[ar1.length - 1];
            double[] trackCore = new double[(arrays1.get(0).length - 1) * step];
            for (int c = 0; c < ar1.length - 1; c++) {
                trackCore[c] = (trackCore[c] + (ar1[c] - maxScore));
                trackCoreTotal[c] = (trackCoreTotal[c] + (ar1[c] - maxScore));
            }
            resultsCore.add(new Array2DRowRealMatrix(trackCore));
        }

        ArrayList<RealMatrix> clusters = KMeansMatrices.freqMatrix(resultsCore);

        Trend.thick = true;
        for (int i = 0; i < trackCoreTotal.length; i++) {
            if (trackCoreTotal[i] > 0) {
                //trackCoreTotal[i] = Math.log10(trackCoreTotal[i]);
            }
        }
        try {
            Utils.saveComponents(new SalMapComp[]{new SalMapComp(trackCoreTotal, tss)},
                    "png", new File(output + "_mut_core_total.png"), count, w, h);
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < clusters.size(); i++) {
            try {
                Utils.saveComponents(new SalMapComp[]{new SalMapComp(clusters.get(i).getColumn(0), tss)},
                        "png", new File(output + "_mut_core_" + i + ".png"), count, w, h);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
