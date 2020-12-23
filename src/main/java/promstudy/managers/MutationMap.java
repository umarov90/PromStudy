package promstudy.managers;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import promstudy.clustering.KMeansMatrices;
import promstudy.common.FastaParser;
import promstudy.visualization.SalMapComp;
import promstudy.visualization.Trend;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by umarovr on 3/22/18.
 */
public class MutationMap {
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
    private static int mutVizSize = 81;
    private static int midpoint = sLen / 2 - mutVizSize/2;
    static int w = 40 * 81;
    static int h = 6 * 40 + 100;
    private static boolean ignoreCore = false;
    private static int numSeq = 100;
    public static ArrayList<Integer> goodSNP;
    public static HashMap<Integer, Integer> refcount;
    public static HashMap<Integer, Integer> altcount;

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

                goodSNP = new ArrayList<>();
                refcount = new HashMap<>();
                altcount = new HashMap<>();
                try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Jumee\\Desktop\\trash\\ttt.tsv"))) {
                    br.readLine();
                    for (String line; (line = br.readLine()) != null; ) {
                        String[] info = line.trim().split("\t");
                        int pos = Integer.parseInt(info[2]);
                        double r1 = Double.parseDouble(info[5]);
                        double a1 = Double.parseDouble(info[6]);
                        double r2 = Double.parseDouble(info[9]);
                        double a2 = Double.parseDouble(info[10]);
                        double c1 = a1 - r1;
                        double c2 = a2 - r2;

                        double refm1 = Double.parseDouble(info[5]);
                        double altm1 = Double.parseDouble(info[6]);
                        double refm2 = Double.parseDouble(info[9]);
                        double altm2 = Double.parseDouble(info[10]);

                        double altm = (altm1 + altm2) / 2;
                        double refm = (refm1 + refm2) / 2;

                        double effect1 = Math.abs(Math.max(altm1, refm1) / Math.min(altm1, refm1));
                        double effect2 = Math.abs(Math.max(altm2, refm2) / Math.min(altm2, refm2));
                        //if(sim(refm1, refm2) && sim(altm1, altm2)){
                        //if (Math.abs(effect1) > 2.0 && Math.abs(effect2) > 2.0) {
                        //if ((effect1 > 2 && effect2 > 2)) {
                            if (Math.abs(altm1 - refm1) > 1 && Math.abs(altm2 - refm2) > 1) { //&& Math.abs(altm2 - refm2) > 5
                                goodSNP.add(pos);
                                refcount.put(pos, Integer.parseInt(info[3]));
                                altcount.put(pos, Integer.parseInt(info[4]));
                            }
                       // }
                        // }
                        //}
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("ttt parsed");
                Object[] o = FastaParser.parse(toPred, 100);
                sequences = (float[][][]) o[0];
                names = (ArrayList<String>) o[1];
                //analyseSal();
                analyseMut();
                //analyseSNP();
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static boolean sim(double r, double a) {
        double d = r / a;
        if (d > 0.5 && d < 2.0) {
            return true;
        }
        return false;
    }

    public static void analyseSNP() {


       /* HashMap<String, Integer> cage = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Jumee\\Desktop\\hg19.cage_peak_phase1and2combined_coord.bed.tsv"))) {
            for (String line; (line = br.readLine()) != null; ) {
                try {
                    String[] info = line.trim().split("\t");
                    cage.put(info[0] + ":" + info[7], Integer.parseInt(info[4]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        ArrayList<float[]> arrays1 = new ArrayList<>();
        ArrayList<Boolean> effect = new ArrayList<>();
        ArrayList<Double> effectv = new ArrayList<>();
        ArrayList<String> nuc = new ArrayList<>();
        nuc.add("A");
        nuc.add("C");
        nuc.add("G");
        nuc.add("T");
        int miss = 0;
        ArrayList<Integer> finalSNPs = new ArrayList<>();
        System.out.println("total sequences: " + sequences.length);
        for (int i = 0; i < sequences.length; i++) {
            float[][] seq = sequences[i];
            //FastaParser.reverse(seq);
            if(!names.get(i).contains("chr1,")){
                //continue;
            }
            String[] info = names.get(i).trim().split("\\s*,\\s*");
            int tss = Integer.parseInt(info[6]) + 1;
            //if (cage.get(info[0].substring(1) + ":" + tss) > 300) {
            //continue;
            //}
            String ref = info[2];
            String alt = info[3];
            String strand = info[7];
            double val = Double.parseDouble(info[5]);
            if (val > -5) {
                //continue;
            }

            if (!goodSNP.contains(Integer.parseInt(info[1]))) {
                continue;
            }
            int pos = Integer.parseInt(info[1]) - 1 - (Integer.parseInt(info[6]) - 500);
            if (pos < 450 || pos > 550) {
                //continue;
            }
            if (strand.equals("-")) {
                //continue;
                pos = Math.abs(pos - 1000);
                ref = comp(ref);
                alt = comp(alt);
            }
            if (seq[pos][0] == 0 && seq[pos][1] == 0
                    && seq[pos][2] == 0 && seq[pos][3] == 0) {
                continue;
            }
            float[][] seq2 = cloneArray(seq);
            if (seq2[pos][nuc.indexOf(ref)] != 1) {
                miss++;
                //continue;
            }
            if (Math.abs(Double.parseDouble(info[5])) > 0.0) {
                //continue;
            }
            effect.add(info[4].equals("true"));
            effectv.add(Double.parseDouble(info[5]));
            float[][][] toPredict = new float[2][sLen][4];

            pos = pos + 5;
            seq2[pos] = new float[4];
            seq2[pos][nuc.indexOf(ref)] = 1;
            toPredict[0] = seq2;

            seq2 = cloneArray(seq);
            seq2[pos] = new float[4];
            seq2[pos][nuc.indexOf(alt)] = 1;
            toPredict[1] = seq2;
            arrays1.add(p.predict(toPredict));
            if (arrays1.get(arrays1.size() - 1)[0] > 0.5) {
                // System.out.println();
            }
            finalSNPs.add(Integer.parseInt(info[1]));
            if (i % 100 == 0) {
                System.out.println(i);
            }
        }
        System.out.println("Missed: " + miss);
        System.out.println("selected total: " + arrays1.size());
        double c = 0;
        double e = 0;
        double sc = 0;
        double se = 0;
        double refcountC = 0;
        double altcountC = 0;
        double refcountE = 0;
        double altcountE = 0;
        double x[] = new double[arrays1.size()];
        double y[] = new double[arrays1.size()];
        for (int ari = 0; ari < arrays1.size(); ari++) {
            float[] ar1 = arrays1.get(ari);
            if (ar1[0] + ar1[1] < 1.0) {
                //continue;
            }

            if(Math.abs(ar1[0] - ar1[1]) < 0.05){
                //continue;
            }
            //System.out.println( (ar1[1] - ar1[0]) + ", " + effectv.get(ari));
            if (ar1[0] < ar1[1] == effect.get(ari)) {
                //refcountC += refcount.get(finalSNPs.get(ari));
                //altcountC += altcount.get(finalSNPs.get(ari));
                sc += effectv.get(ari);
                c++;
            } else {
                //refcountE += refcount.get(finalSNPs.get(ari));
                //altcountE += altcount.get(finalSNPs.get(ari));
                se += effectv.get(ari);
                e++;
            }
            x[ari] = effectv.get(ari);
            y[ari] = ar1[1] - ar1[0];
        }
        System.out.println(c);
        System.out.println(e);
        System.out.println(c / (c + e));
        sc /= c;
        se /= e;
        refcountC /= c;
        altcountC /= c;
        refcountE /= e;
        altcountE /= e;
        System.out.println(sc);
        System.out.println(se);
        System.out.println(refcountC);
        System.out.println(altcountC);
        System.out.println(refcountE);
        System.out.println(altcountE);
        //System.out.println(Correlation(x, y));
    }

    public static double Correlation(double[] xs, double[] ys) {
        //TODO: check here that arrays are not null, of the same length etc

        double sx = 0.0;
        double sy = 0.0;
        double sxx = 0.0;
        double syy = 0.0;
        double sxy = 0.0;

        int n = xs.length;

        for (int i = 0; i < n; ++i) {
            double x = xs[i];
            double y = ys[i];

            sx += x;
            sy += y;
            sxx += x * x;
            syy += y * y;
            sxy += x * y;
        }

        // covariation
        double cov = sxy / n - sx * sy / n / n;
        // standard error of x
        double sigmax = Math.sqrt(sxx / n - sx * sx / n / n);
        // standard error of y
        double sigmay = Math.sqrt(syy / n - sy * sy / n / n);

        // correlation is just a normalized covariation
        return cov / sigmax / sigmay;
    }

    private static String comp(String ref) {
        if (ref.equals("A")) {
            return "T";
        } else if (ref.equals("T")) {
            return "A";
        } else if (ref.equals("G")) {
            return "C";
        } else if (ref.equals("C")) {
            return "G";
        } else {
            return null;
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
            for (int j = midpoint; j < midpoint + 81; j++) {
                float[][] seq2 = cloneArray(seq);
                float[] tmp = seq2[j].clone();
                for (int d = 0; d < tmp.length; d++) {
                    if (tmp[d] == 1) {
                        tmp[d] += 0.01;
                    }
                }
                seq2[j] = tmp;
                toPredict[j - midpoint] = seq2;
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

        ArrayList<RealMatrix> clusters = KMeansMatrices.freqMatrix(resultsCore);

        for (int i = 0; i < clusters.size(); i++) {
            try {
                saveComponents(new SalMapComp[]{new SalMapComp(clusters.get(i).getColumn(0))}, "png", new File(output + "_sal_core_" + i + ".png"));
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

        try {
            saveComponents(new SalMapComp[]{new SalMapComp(trackCoreTotal)}, "png", new File(output + "_sal_core.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void analyseMut() {
        ArrayList<float[]> arrays1 = new ArrayList<>();
        System.out.println("Progress: ");
        for (int i = 0; i < numSeq; i++) {
            float[][] seq = sequences[i];
            int total = 4 * mutVizSize + 1;
            float[][][] toPredict = new float[total][sLen][4];
            for (int j = midpoint * 4; j < midpoint * 4 + mutVizSize * 4; j++) {
                float[][] seq2 = cloneArray(seq);
                float[] tmp = new float[]{0, 0, 0, 0};
                tmp[j % 4] = 1;
                seq2[j / 4] = tmp;
                toPredict[j - midpoint * 4] = seq2;
            }
            toPredict[toPredict.length - 1] = seq;
            arrays1.add(p.predict(toPredict));
            if ((i + 1) % 10 == 0) {
                System.out.print((i + 1) + " ");
            }
        }


        ArrayList<RealMatrix> resultsCore = new ArrayList<>();
        //ArrayList<RealMatrix> resultsNoCore = new ArrayList<>();
        double[] trackNoCoreTotal = new double[4 * (arrays1.get(0).length - 1) * step];
        double[] trackCoreTotal = new double[4 * (arrays1.get(0).length - 1) * step];
        for (int ari = 0; ari < arrays1.size(); ari++) {
            float[] ar1 = arrays1.get(ari);
            double maxScore = ar1[ar1.length - 1];
            //double[] trackNoCore = new double[(arrays1.get(0).length - 1) * step];
            double[] trackCore = new double[(arrays1.get(0).length - 1) * step];
            for (int c = 0; c < ar1.length - 1; c++) {
                //if (c > 4 * 950 && c < 4 * 1051) {
                trackCore[c] = (trackCore[c] + (ar1[c] - maxScore));
                trackCoreTotal[c] = (trackCoreTotal[c] + (ar1[c] - maxScore));
                //} else {
                //trackNoCore[c] = (trackNoCore[c] + (ar1[c] - maxScore));
                //trackNoCoreTotal[c] = (trackNoCoreTotal[c] + (ar1[c] - maxScore));
                //}
            }
            resultsCore.add(new Array2DRowRealMatrix(trackCore));
            //resultsNoCore.add(new Array2DRowRealMatrix(trackNoCore));
        }

        ArrayList<RealMatrix> clusters = KMeansMatrices.freqMatrix(resultsCore);

        Trend.thick = true;
        for (int i = 0; i < trackCoreTotal.length; i++) {
            if (trackCoreTotal[i] > 0) {
                //trackCoreTotal[i] = Math.log10(trackCoreTotal[i]);
            }
        }
        try {
            saveComponents(new SalMapComp[]{new SalMapComp(trackCoreTotal)}, "png", new File(output + "_mut_core_total.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < clusters.size(); i++) {
            try {
                saveComponents(new SalMapComp[]{new SalMapComp(clusters.get(i).getColumn(0))}, "png", new File(output + "_mut_core_" + i + ".png"));
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
