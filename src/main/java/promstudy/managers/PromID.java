package promstudy.managers;

import promstudy.common.Element2;
import promstudy.common.FastaParser;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Stream;

/**
 * Created by umarovr on 3/22/18.
 */
public class PromID {
    private static Predictor p1, p2;
    private static float[][][] sequences;
    private static int step = 1;
    private static int sLen = 600;
    private static int sd = 10;
    private static int minDist = 1000;
    private static String output = "out.txt";
    private static String matFile;
    private static ArrayList<String> names;
    private static double dt1 = 0.5;
    private static double dt2 = 0.5;
    private static int mode = 0;
    private static int ws = 0;
    private static int head = 200;
    private static int tail = 399;
    private static int matStart = 0;
    private static int matEnd = 0;
    private static double matD = -8.16;
    private static double[][] mat;

    public static void main(String[] args) {
        File toPred = null;
        if (args.length > 0) {
            try {
                for (int i = 0; i < args.length / 2; i++) {
                    String option = args[2 * i];
                    String parameter = args[2 * i + 1];
                    if (option.equals("-set")) {
                        toPred = new File(parameter);
                    } else if (option.equals("-out")) {
                        output = parameter;
                    } else if (option.equals("-tss")) {
                        output = parameter;
                    } else if (option.equals("-mode")) {
                        mode = Integer.parseInt(parameter);
                    } else if (option.equals("-up")) {
                        head = Integer.parseInt(parameter);
                    }else if (option.equals("-down")) {
                        tail = Integer.parseInt(parameter);
                    } else if (option.equals("-dt1")) {
                        dt1 = Double.parseDouble(parameter);
                    } else if (option.equals("-dt2")) {
                        dt2 = Double.parseDouble(parameter);
                    } else if (option.equals("-md")) {
                        minDist = Integer.parseInt(parameter);
                    } else if (option.equals("-step")) {
                        step = Integer.parseInt(parameter);
                    } else if (option.equals("-mat")) {
                        matFile = parameter;
                    } else {
                        System.err.println("Unknown option: " + option);
                        System.err.println("Available Options: ");
                        System.err.println("-set: file with long sequences");
                        System.err.println("-out: output file with scores");
                        System.err.println("-sl: sliding window size");
                        System.err.println("-md: minimum distance allowed between promoters");
                        return;
                    }
                }
                Object[] o = FastaParser.parse(toPred, sLen);
                sequences = (float[][][]) o[0];
                names = (ArrayList<String>) o[1];
                if(matFile!=null) {
                    Scanner scan = new Scanner(new File(matFile));
                    matEnd = scan.nextInt() + head;
                    matStart = scan.nextInt() + head;
                    matD = scan.nextDouble();
                    scan.nextLine();
                    for (int i = 0; i < 4; i++) {
                        String l = scan.nextLine();
                        ArrayList<Double> r = new ArrayList<>();
                        Scanner scanner = new Scanner(l);
                        while (scanner.hasNext()) {
                            if (scanner.hasNextDouble()) {
                                r.add(scanner.nextDouble());
                            } else {
                                scanner.next();
                            }
                        }
                        if(mat==null){
                            mat = new double[r.size()][4];
                        }
                        for(int j = 0; j<r.size(); j++){
                            mat[j][i] = r.get(j);
                        }
                    }
                }
                analyse();
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static void analyse() {
        sLen = head + tail + 1;
        p1 = new Predictor("models" + File.separator + "model_1");
        if (mode > 0) {
            p2 = new Predictor("models" + File.separator + "model_2");
        }
        ArrayList<float[]> arrays1 = new ArrayList<>();
        ArrayList<float[]> arrays2 = new ArrayList<>();
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        int[] offset = new int[sequences.length];
        System.out.println("Progress: ");
        for (int i = 0; i < sequences.length; i++) {
            float[][] seq = sequences[i];
            seq = concat(randomSeq(head), seq);
            seq = concat(seq, randomSeq(tail));
            sequences[i] = seq;
            int total = (int) Math.ceil((seq.length - sLen) / step) + 1;
            float[][][] toPredict = new float[total][sLen][4];
            for (int j = 0; j < toPredict.length; j++) {
                toPredict[j] = Arrays.copyOfRange(seq, j * step, j * step + sLen);
            }
            float[] r1 = p1.predict(toPredict);
            sb1.append("Sequence " + (i + 1) + "\n");
            for (int j = 0; j < r1.length; j++) {
                sb1.append(r1[j]);
                if (j != r1.length - 1) {
                    sb1.append(", ");
                }
            }
            sb1.append("\n");
            arrays1.add(r1);
            if (mode > 0) {
                float[] r2 = p2.predict(toPredict);
                sb2.append("Sequence " + (i + 1) + "\n");
                for (int j = 0; j < r2.length; j++) {
                    sb2.append(r2[j]);
                    if (j != r2.length - 1) {
                        sb2.append(", ");
                    }
                }
                sb2.append("\n");
                arrays2.add(r2);
            }
            if ((i + 1) % 10 == 0) {
                System.out.print((i + 1) + " ");
            }
        }

        try {
            File forDraw = new File("forDraw");
            if (!forDraw.exists() || !forDraw.isDirectory()) {
                forDraw.mkdir();
            }
            File f1 = new File("forDraw" + File.separator + "model_1.res");
            File f2 = new File("forDraw" + File.separator + "model_2.res");
            if (f1.exists()) {
                f1.delete();
            }
            if (f2.exists()) {
                f2.delete();
            }
        } catch (Exception e) {

        }
        try (PrintWriter out = new PrintWriter("forDraw" + File.separator + "model_1.res")) {
            out.print(sb1.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mode > 0) {
            try (PrintWriter out = new PrintWriter("forDraw" + File.separator + "model_2.res")) {
                out.print(sb2.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println();
        StringBuilder sb = new StringBuilder();
        for (int ari = 0; ari < arrays1.size(); ari++) {
            float[] ar1 = arrays1.get(ari);
            float[] ar2 = null;
            if (mode > 0) {
                ar2 = arrays2.get(ari);
            }
            float[][] seq = sequences[ari];
            sb.append(names.get(ari) + "\n");
            sb.append("Length: " + (seq.length-head - tail) + "\n");
            ArrayList<Integer> inds = new ArrayList<>();
            pick(sd, sd, ar1, dt1, sb, seq, "", inds, false);
            if (mode > 0) {
                pick(sd, sd, ar2, dt2, sb, seq, "", inds, mode == 2);
            }
            if (ari != arrays1.size() - 1) {
                sb.append("\n");
            }
        }
        try (PrintWriter out = new PrintWriter(output)) {
            out.println(sb.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println();
    }

    private static float[][] randomSeq(int s) {
        float[][] r = new float[s][4];
        Random rand = new Random();
        for (int i = 0; i < s; i++) {
            r[i][rand.nextInt(4)] = 1;
        }
        return r;
    }

    private static void pick(int s, int e, float[] ar, double dt, StringBuilder sb, float[][] seq, String s1, ArrayList<Integer> inds, boolean m) {
        ArrayList<Element2> element = new ArrayList<>();
        for (int i = 1; i < ar.length - 1; i++) {
            Element2 el = new Element2(i * step, ar[i], ar[i] > dt);
            if (!m) {
                element.add(el);
            } else if (m && score(Arrays.copyOfRange(seq, i, i + sLen)) > matD) {
                element.add(el);
            }
        }
        Collections.sort(element, Collections.reverseOrder());
        for (int k = 0; k < element.size(); k++) {
            if (!element.get(k).p) {
                break;
            }
            if (notClose(element.get(k).index, inds)) {
                //float[][] toRev = Arrays.copyOfRange(seq, element.get(k).index, element.get(k).index + sLen);
                if (inds.size() > 0) {
                    sb.append("\n");
                }
                sb.append("Position " + (element.get(k).index + 1) + " Score " + element.get(k).value + s1); //+ "\n" + FastaParser.reverse(toRev));
                inds.add(element.get(k).index);
            }
        }
    }

    private static double score(float[][] floats) {
        double max = -Double.MAX_VALUE;
        for (int p = matStart; p < matEnd - mat.length; p++) {
            double sum = 0;
            float[][] tata;
            //ws + 168 - 5 + p >= 0 && ws + 183 - 5 + p < floats.length

            tata = Arrays.copyOfRange(floats, p, p + mat.length);
            if(tata==null){
                int g = 2+ 2;
            }
            for (int i = 0; i < tata.length; i++) {
                if(tata[i]==null){
                    int g = 2+ 2;
                }
                for (int j = 0; j < tata[i].length; j++) {
                    sum += tata[i][j] * mat[i][j];
                }
            }
            if (sum > max) {
                max = sum;
            }
        }
        return max;
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

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
