package promstudy.managers;

import promstudy.common.Element;
import promstudy.common.Element2;
import promstudy.common.FastaParser;
import promstudy.common.SubSeq;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

/**
 * Created by umarovr on 3/22/18.
 */
public class PromFindM {
    private static Predictor p1, p2;
    private static float[][][] sequences;
    private static int step = 1;
    private static int sLen = 81;
    private static int sd1 = 10;
    private static int sd2 = 100;
    private static int minDist = 10;
    private static String output = "out.txt";
    private static ArrayList<String> names;
    private static double dt1 = 0.5;
    private static double dt2 = 0.5;
    private static boolean m = false;

    private static double c = 0;
    private static double t = 0;
    private static boolean nc = true;

    static int ws = 0;
    static int wl = 251;
    static int sp = 4800;
    static String set = "TATA+";
    static int rm;

    static int ggg = 0;
    static StringBuilder tpvsb = new StringBuilder();
    static StringBuilder tmvsb = new StringBuilder();

    public static void main(String[] args) {
        File toPred = null;
        if (args.length > 0) {
            try {
                for (int i = 0; i < args.length / 2; i++) {
                    String option = args[2 * i];
                    String parameter = args[2 * i + 1];
                    if (option.equals("-set")) {
                        toPred = new File(parameter);
                    } else if (option.equals("-mod1")) {
                        //p1 = new Predictor(parameter);
                    } else if (option.equals("-mod2")) {
                        //p2 = new Predictor(parameter);
                    } else if (option.equals("-out")) {
                        output = parameter;
                    } else if (option.equals("-sl")) {
                        sLen = Integer.parseInt(parameter);
                    } else if (option.equals("-sd1")) {
                        sd1 = Integer.parseInt(parameter);
                    } else if (option.equals("-sd2")) {
                        sd2 = Integer.parseInt(parameter);
                    } else if (option.equals("-dt1")) {
                        dt1 = Double.parseDouble(parameter);
                    } else if (option.equals("-dt2")) {
                        dt2 = Double.parseDouble(parameter);
                    } else if (option.equals("-md")) {
                        minDist = Integer.parseInt(parameter);
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


                String[] sets = new String[]{"tata+", "tata-", "both"};
                int[] mds = new int[]{1000};
                String[] types = new String[]{"1 model", "2 model", "2 model matrix"};
                DecimalFormat df = new DecimalFormat("###.###");
                System.out.println();
                for (int d = 0; d < 2; d++) {
                    if (d == 0) {
                        dt1 = 0.5;
                        dt2 = 0.5;
                    } else {
                        dt2 = 0.3;
                        dt2 = 0.3;
                    }
                    for (int m = 0; m < mds.length; m++) {
                        System.out.println("MinDist = " + mds[m] + "DT = " + dt2);
                        for (int j = 0; j < 1; j++) {
                            double[][] res = new double[3][];
                            for (int s = 0; s < 3; s++) {
                                sLen = 600;
                                ws = 0;
                                wl = sLen;
                                sp = 4800;
                                minDist = mds[m];
                                set = sets[s];
                                toPred = new File("final_sets" + File.separator + set + ".fa");
                                Object[] o = FastaParser.parse(toPred, sLen);
                                sequences = (float[][][]) o[0];
                                names = (ArrayList<String>) o[1];
                                res[s] = analyse(1);
                            }
                            System.out.print(types[j]);
                            System.out.print(" ");
                            System.out.print("[-200 +400]");
                            System.out.print(", ");
                            for (int k = 0; k < res[0].length; k++) {
                                System.out.print(df.format(res[0][k]) + "& " + df.format(res[1][k]) + "& " + df.format(res[2][k]));
                                if (k != res[0].length - 1) {
                                    System.out.print("& ");
                                }
                            }
                            System.out.println();
                        }

                        System.out.println();
                    }
                }
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try (PrintWriter out = new PrintWriter(new BufferedWriter
                    (new FileWriter("TATA+_Predictions", false)))) {
                out.print(tpvsb.toString().trim());
            } catch (Exception e) {
                e.printStackTrace();
            }
            try (PrintWriter out = new PrintWriter(new BufferedWriter
                    (new FileWriter("TATA-_Predictions", false)))) {
                out.print(tmvsb.toString().trim());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static double[] analyse(int m) {

        ArrayList<double[]> arrays1 = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader("final_sets" + File.separator + "tata+_on_" + set + ".res");
            //FileReader fileReader = new FileReader("C:\\Users\\Jumee\\Dropbox\\PromStudy\\PromID\\res_model_PromCNN_on_BOTH.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(",")) {
                    double[] arr = Stream.of(line.split(","))
                            .mapToDouble(Double::parseDouble)
                            .toArray();
                    arrays1.add(arr);
                }
            }
            fileReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<double[]> arrays2 = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader("final_sets" + File.separator + "tata-_on_" + set + ".res");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(",")) {
                    double[] arr = Stream.of(line.split(","))
                            .mapToDouble(Double::parseDouble)
                            .toArray();
                    arrays2.add(arr);
                }
            }
            fileReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        c = 0;
        t = 0;
        nc = true;
        for (int ari = 0; ari < arrays1.size(); ari++) {
            nc = true;
            float[] ar1 = toFloatArray(arrays1.get(ari));
            float[] ar2 = toFloatArray(arrays2.get(ari));
            float[][] seq = sequences[ari];
            sb.append(names.get(ari) + "\n");
            sb.append("Length: " + seq.length + "\n");
            ArrayList<Integer> inds = new ArrayList<>();

            if (m == 0) {
                pick(sd2, sd2, ar2, dt2, sb, seq, "", inds, false);
            } else if (m == 1) {
                pick(sd2, sd2, ar2, dt2, sb, seq, "", inds, false);
                pick(sd1, sd1, ar1, dt1, sb, seq, "", inds, false);
            } else {
                pick(sd2, sd2, ar2, dt2, sb, seq, "", inds, false);
                pick(sd1, sd1, ar1, dt1, sb, seq, "", inds, true);
            }


            if (ari != arrays1.size() - 1) {
                sb.append("\n");
            }
        }

        double fp = t;
        double tn = arrays1.size() * (10000 - wl - 500 * 2);
        double tp = c;
        double fn = arrays1.size() - c;

        DecimalFormat df = new DecimalFormat("###.###");

        //System.out.println("Total: " + arrays1.size());
        //System.out.println("Correct: " + c);
        //System.out.println("Sensitivity: " + df.format(tp / (tp + fn)));
        //System.out.println("False positives: " + t);
        //System.out.println("MCC: " + df.format(mcc));
        //System.out.println("Specificity: " +  df.format(tn / (tn + fp)));
        //System.out.println("Error rate: " + df.format(t / c));
        //System.out.println("Error per 1000BP: " + df.format(t / (10 * arrays1.size())));

        try (PrintWriter out = new PrintWriter(output)) {
            //out.println(sb.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //System.out.println(ggg);
        double[] res = new double[7];
        res[0] = c;
        res[1] = fp;
        res[2] = tp / (tp + fn);
        res[3] = tp / (tp + fp);
        res[4] = 2 * (( res[3] * res[2]) / (res[3] + res[2]));
        res[5] = t / c;
        res[6] = t / (10 * arrays1.size());
        return res;
    }

    private static double score(float[][] floats) {
        double max = -Double.MAX_VALUE;
        double[][] w = new double[][]{{-1.02, -1.68, 0, -0.28},
                {-3.05, 0, -2.74, -2.06},
                {0, -2.28, -4.28, -5.22},
                {-4.61, 0, -4.61, -3.49},
                {0, -2.34, -3.77, -5.17},
                {0, -0.52, -4.73, -4.63},
                {0, -3.65, -2.65, -4.12},
                {0, -0.37, -1.5, -3.74},
                {-0.01, -1.4, 0, -1.13},
                {-0.94, -0.97, 0, -0.05},
                {-0.54, -1.4, -0.09, 0},
                {-0.48, -0.82, 0, -0.05},
                {-0.48, -0.66, 0, -0.11},
                {-0.74, -0.54, 0, -0.28},
                {-0.62, -0.61, 0, -0.4}};
        for (int p = 0; p < 10; p++) {
            double sum = 0;
            float[][] tata;
            if (ws + 168 - 5 + p >= 0 && ws + 183 - 5 + p < floats.length) {
                tata = Arrays.copyOfRange(floats, ws + 168 - 5 + p, ws + 183 - 5 + p);
            } else {
                tata = Arrays.copyOfRange(floats, ws + 168, ws + 183);
            }
            for (int i = 0; i < tata.length; i++) {
                for (int j = 0; j < tata[i].length; j++) {
                    sum += tata[i][j] * w[i][j];
                }
            }
            if (sum > max) {
                max = sum;
            }
        }
        return max;
    }

    private static void pick(int s, int e, float[] ar, double dt, StringBuilder sb, float[][] seq, String s1, ArrayList<Integer> inds, boolean m) {
        ArrayList<Element2> element = new ArrayList<>();
        boolean ttt = true;
        for (int i = s; i < ar.length - e; i++) {
            boolean p = false;
            float sum = 0;
            for (int j = i - s; j <= i + e; j++) {
                sum += ar[j];
                if (ar[j] > dt) {
                    p = true;
                }
            }
            Element2 el = new Element2(i, sum, p);
            double v = -8.16;
            //v = -6;
/*           if (el.index >= (sp - 5) && el.index <= (sp + 5)) {
                if(score(Arrays.copyOfRange(seq, i, i + sLen)) > v) {
                  if(ttt) {
                     ggg++;
                       ttt = false;
                  }
                }
            }*/
            if (!m) {
                element.add(el);
            } else if (m && score(Arrays.copyOfRange(seq, i, i + sLen)) > v) {
                element.add(el);
            }
        }
        Collections.sort(element, Collections.reverseOrder());
        for (int k = 0; k < element.size(); k++) {
            if (!element.get(k).p) {
                break;
            }
            if (notClose(element.get(k).index, inds)) {
                //float[][] toRev = Arrays.copyOfRange(seq, element.get(k).index * step, element.get(k).index * step + sLen);
                if (inds.size() > 0) {
                    sb.append("\n");
                }
                //sb.append("Position " + (element.get(k).index + 1) + " Score " + element.get(k).value + s1 + "\n" + FastaParser.reverse(toRev));
                inds.add(element.get(k).index);
                if (element.get(k).index >= (sp - 500) && element.get(k).index <= (sp + 500)) {
                    if(set.equals("tata+")){
                        tpvsb.append(element.get(k).index+201);
                        tpvsb.append("\n");
                    }
                    if(set.equals("tata-")){
                        tmvsb.append(element.get(k).index+201);
                        tmvsb.append("\n");
                    }
                    if (nc) {
                        c++;
                        nc = false;
                    }
                } else {
                    t++;
                }
            }
        }
    }

    private static boolean notClose(int index, ArrayList<Integer> inds) {
        boolean nc = true;
        for (Integer i : inds) {
            if (Math.abs(i - index) < minDist) {
                nc = false;
                break;
            }
        }
        return nc;
    }

    public static float[] toFloatArray(double[] arr) {
        if (arr == null) return null;
        int n = arr.length;
        float[] ret = new float[n];
        for (int i = 0; i < n; i++) {
            ret[i] = (float) arr[i];
        }
        return ret;
    }
}
