package promstudy.managers;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import promstudy.clustering.KMeans;
import promstudy.clustering.KMeansMatrices;
import promstudy.clustering.Sequence;
import promstudy.common.FastaParser;
import promstudy.dataVisualisation.LogoComponent;
import promstudy.visualization.Trend;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Created by umarovr on 3/22/18.
 */
public class MotifMap {
    private static Predictor p;
    private static float[][][] sequences;
    private static int step = 1;
    private static int sLen = 1001;
    private static int sd = 10;
    private static int minDist = 1000;
    private static String output = "mot_out_";
    private static ArrayList<String> names;
    private static double dt = 0.5;
    private static int count = 1;
    static int w = 500;
    static int h = 100;
    private static boolean ignoreCore = false;
    private static int numSeq = 18000; //7970
    private static int motifLen = 7;

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
                    } else if (option.equals("-ml")) {
                        motifLen = Integer.parseInt(parameter);
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
                analyseMotifs();
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static void detectMotifs() {

    }
    public static void analyseMotifs() {
        int tss = 500;
        ArrayList<Motif> motifsKnown = new ArrayList<>();
        File dir = new File("PWM");
        File[] directoryListing = dir.listFiles();
        for (File child : directoryListing) {
            String name = child.getName().replaceFirst("[.][^.]+$", "");
            motifsKnown.add(new Motif(child, name, tss));
        }

        Random rand = new Random();
        ArrayList<float[]> arrays1 = new ArrayList<>();
        ArrayList<float[]> arrays2 = new ArrayList<>();

        if (new File("arrays1.bin").exists()) {
            try {
                FileInputStream fileIn = new FileInputStream("arrays1.bin");
                ObjectInputStream in = new ObjectInputStream(fileIn);
                arrays1 = (ArrayList<float[]>) in.readObject();
                in.close();
                fileIn.close();
                fileIn = new FileInputStream("arrays2.bin");
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
                int total = seq.length - motifLen + 1;
                float[][][] toPredict = new float[total][sLen][4];
                for (int j = 0; j < toPredict.length - 1; j++) {
                    float[][] seq2 = cloneArray(seq);
                    for (int n = 0; n < motifLen; n++) {
                        if (j + n == tss || j + n == tss - 1) {
                            continue;
                        }
                        int nd = rand.nextInt(4);
                        for (int d = 0; d < seq2[n].length; d++) {
                            seq2[j + n][d] = 0;
                        }
                        seq2[j + n][nd] = 1;
                    }
                    toPredict[j] = seq2;
                }
                toPredict[toPredict.length - 1] = seq;
                arrays1.add(p.predict(toPredict));
                if ((i + 1) % 10 == 0) {
                    System.out.print((i + 1) + " ");
                }
                toPredict = new float[motifsKnown.size()][sLen][4];
                for (int j = 0; j < motifsKnown.size(); j++) {
                    float[][] seq2 = cloneArray(seq);
                    for (int n = 0; n < motifsKnown.get(j).fm.getColumnDimension(); n++) {
                        int pos = motifsKnown.get(j).start + n;
                        if (pos == tss || pos == tss - 1) {
                            continue;
                        }
                        int nd = rand.nextInt(4);
                        for (int d = 0; d < 4; d++) {
                            if (d == nd) {
                                seq2[pos][d] = 0;
                            } else {
                                seq2[pos][d] = 0;
                            }
                        }
                    }
                    toPredict[j] = seq2;
                }
                arrays2.add(p.predict(toPredict));
            }
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream("arrays1.bin");
                ObjectOutputStream out = new ObjectOutputStream(fos);
                out.writeObject(arrays1);
                fos = new FileOutputStream("arrays2.bin");
                out = new ObjectOutputStream(fos);
                out.writeObject(arrays2);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ArrayList<Sequence> trackNoCoreTotal = new ArrayList<>();
        ArrayList<Sequence> trackCore = new ArrayList<>();
        OldMotif tata = new OldMotif(15);
        OldMotif ccat = new OldMotif(12);
        OldMotif inr = new OldMotif(8);
        OldMotif tct = new OldMotif(8);

        ArrayList<String> cons = new ArrayList<>();
        ArrayList<String> urls = new ArrayList<>();
        boolean start = false;
        String con = "";
        double conLen = 0;
        int cc = 0;
        int ee = 0;
        HashMap<String, SimpleMotif> workingcons = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader("JASPAR2020.txt"))) {
            for (String line; (line = br.readLine()) != null; ) {
                try {
                    if (line.startsWith("URL")) {
                        urls.add(line);
                        cons.add(con);
                        conLen += con.length();
                        con = "";
                        start = false;
                    } else if (start) {
                        Scanner scanner = new Scanner(line);
                        double a, t, g, c;
                        a = scanner.nextDouble();
                        c = scanner.nextDouble();
                        g = scanner.nextDouble();
                        t = scanner.nextDouble();
                        if (a >= c && a >= g && a >= t) {
                            con += "A";
                        } else if (c >= a && c >= g && c >= t) {
                            con += "C";
                        } else if (g >= c && g >= a && g >= t) {
                            con += "G";
                        } else {
                            con += "T";
                        }
                    } else if (line.startsWith("letter-probability")) {
                        start = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Average jaspar len: " + conLen / cons.size());
        double total[] = new double[arrays1.get(0).length];
        int selected = 0;
        ArrayList<RealMatrix> coreM = new ArrayList<>();
        ArrayList<RealMatrix> downM = new ArrayList<>();
        ArrayList<RealMatrix> upM = new ArrayList<>();
        ArrayList<RealMatrix> allM = new ArrayList<>();
        for (int ari = 0; ari < numSeq; ari++) {
            float[][] seq = sequences[ari];
            float[] ar1 = arrays1.get(ari);
            float[] ar2 = arrays2.get(ari);
            double maxScore = ar1[ar1.length - 1];
            ArrayList<Sequence> trackNoCore = new ArrayList<>();
            if (maxScore < 0.5) {
                continue;
            }
            selected++;
            RealMatrix m = new Array2DRowRealMatrix(101, 1);
            RealMatrix ma = new Array2DRowRealMatrix(1001, 1);
            RealMatrix mu = new Array2DRowRealMatrix(450, 1);
            RealMatrix md = new Array2DRowRealMatrix(450, 1);
            for (int c = 0; c < ar1.length - 1; c++) {
                if (maxScore - ar1[c] <= 0) {
                    //continue;
                }
                double v = ar1[c] / maxScore;
                if (c > 450 && c < 551) {
                    trackCore.add(new Sequence(flatten(seq, c, motifLen), v, c));
                } else {
                    trackNoCore.add(new Sequence(flatten(seq, c, motifLen), v, c));
                }
                total[c] += (maxScore - ar1[c]);
                if (c < 450) {
                    mu.setEntry(c, 0, maxScore - ar1[c]);
                } else if (c < 551) {
                    m.setEntry(c - 450, 0, maxScore - ar1[c]);
                } else {
                    md.setEntry(c - 551, 0, maxScore - ar1[c]);
                }
                ma.setEntry(c, 0, maxScore - ar1[c]);
            }
            allM.add(ma);
            coreM.add(m);
            upM.add(mu);
            downM.add(md);
            double[] temp = tatascore(seq, tss);
            if (temp[0] >= -8.16) {
                int p = (int) (tss - temp[1]);
                tata.add(flatten(seq, p, 15));
                tata.count++;
                tata.pos.add(p);
                tata.effect += ar1[p] / maxScore;
            }

            temp = ccaatscore(seq, tss);
            if (temp[0] >= -4.54) {
                int p = (int) (tss - temp[1]);
                ccat.add(flatten(seq, p, 12));
                ccat.count++;
                ccat.pos.add(p);
                ccat.effect += ar1[p] / maxScore;
            }

            double score = inrscore(seq, tss);
            if (score >= -3.75) {
                inr.add(flatten(seq, 498, 8));
                inr.count++;
                inr.effect += ar2[0] / maxScore;
            }
            score = tctscore(seq, tss);
            if (score >= 12.84) {
                tct.add(flatten(seq, 498, 8));
                tct.count++;
                tct.effect += ar2[0] / maxScore;
            }
            ArrayList<Sequence> chosen = new ArrayList<>();
            Collections.sort(trackNoCore, (o1, o2) -> Double.compare(o1.v, o2.v));
            //Collections.reverse(trackNoCore);
            String fa = FastaParser.reverse(seq);
            for (int i = 0; i < cons.size(); i++) {
                if (fa.contains(cons.get(i))) {
                    ArrayList<Integer> inds = findWord(fa, cons.get(i));
                    for (int j : inds) {
                        if (j < ar1.length) {
                            double change = ar1[j] / maxScore;
                            if (change < 0.9 && (j < 400 || j > 600)) {
                                //System.out.println(urls.get(i) + " " + j);
                                cc++;
                                if (workingcons.containsKey(cons.get(i))) {
                                    workingcons.get(cons.get(i)).count++;
                                    workingcons.get(cons.get(i)).pos.add(j);
                                    workingcons.get(cons.get(i)).position += j;
                                    workingcons.get(cons.get(i)).effect += change;
                                } else {
                                    workingcons.put(cons.get(i), new SimpleMotif(1, j, change));
                                }
                            } else {
                                ee++;
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < trackNoCore.size(); i++) {
                boolean add = true;
                if (cons.contains(trackNoCore.get(i).toString())) {
                    //System.out.println();
                }
                for (Sequence s : chosen) {
                    if (Math.abs(trackNoCore.get(i).p - s.p) < 100) {
                        add = false;
                        break;
                    }
                }
                if (add) {
                    chosen.add(trackNoCore.get(i));
                }
            }
            trackNoCoreTotal.addAll(chosen);
        }
        Iterator it = workingcons.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            SimpleMotif sm = (SimpleMotif) pair.getValue();
            int tm = sm.count;
            if (tm > 50) {
                System.out.println(pair.getKey() + " = " + sm.count + "  " + sm.getPosition(500) + " "
                        + "  " + sm.position / sm.count + "   " + (sm.effect / sm.count) + "   " + urls.get(cons.indexOf(pair.getKey())));
            }
        }
        System.out.println();
        try {
            ArrayList<RealMatrix> ms = KMeansMatrices.freqMatrix(downM);
            BufferedWriter br = new BufferedWriter(new FileWriter("md.csv"));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 450; i++) {
                for (int j = 0; j < ms.size(); j++) {
                    sb.append(ms.get(j).getEntry(i, 0));
                    sb.append(",");
                }
                sb.append("\n");
            }
            br.write(sb.toString());
            br.close();
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        try {
            ArrayList<RealMatrix> ms = KMeansMatrices.freqMatrix(coreM);
            BufferedWriter br = new BufferedWriter(new FileWriter("mc.csv"));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 101; i++) {
                for (int j = 0; j < ms.size(); j++) {
                    sb.append(ms.get(j).getEntry(i, 0));
                    sb.append(",");
                }
                sb.append("\n");
            }
            br.write(sb.toString());
            br.close();
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        try {
            ArrayList<RealMatrix> ms = KMeansMatrices.freqMatrix(upM);
            BufferedWriter br = new BufferedWriter(new FileWriter("mu.csv"));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 450; i++) {
                for (int j = 0; j < ms.size(); j++) {
                    sb.append(ms.get(j).getEntry(i, 0));
                    sb.append(",");
                }
                sb.append("\n");
            }
            br.write(sb.toString());
            br.close();
        } catch (
                IOException e) {
            e.printStackTrace();
        }

        try {
            ArrayList<RealMatrix> ms = KMeansMatrices.freqMatrix(allM);
            BufferedWriter br = new BufferedWriter(new FileWriter("ma.csv"));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 1001; i++) {
                for (int j = 0; j < ms.size(); j++) {
                    sb.append(ms.get(j).getEntry(i, 0));
                    sb.append(",");
                }
                sb.append("\n");
            }
            br.write(sb.toString());
            br.close();
        } catch (
                IOException e) {
            e.printStackTrace();
        }
        System.out.println("Total len is " + trackNoCoreTotal.size());

        int itNum = 1;
        int imp = 0;
        for (
                int k = 0;
                k < itNum; k++) {
            for (int ari = 0; ari < numSeq; ari++) {
                float[][] seq = sequences[ari];
                float[] ar1 = arrays1.get(ari);
                float[] ar2 = arrays2.get(ari);
                double maxScore = ar1[ar1.length - 1];
                if (maxScore > 3.8) {
                    imp++;
                    //continue;
                }
                for (int i = 0; i < motifsKnown.size(); i++) {
                    int p = motifsKnown.get(i).detect(seq);
                    if (p != -1) {
                        //motifsKnown.get(i).add(flatten(seq, p, 12));
                        motifsKnown.get(i).count++;
                        motifsKnown.get(i).pos.add(p);
                        Double d = ar1[p] / maxScore;
                        if (!d.isNaN() && maxScore > 0.5) {
                            motifsKnown.get(i).effect += d;
                            motifsKnown.get(i).ec++;
                        }
                    }
                }
            }
            if (k < itNum - 1) {
                for (int i = 0; i < motifsKnown.size(); i++) {
                    //motifsKnown.get(i).update();
                }
            }
        }

        System.out.println(imp);
        System.out.println("Name, Frequency, Position, Effect");
        for (
                Motif m : motifsKnown) {
            System.out.println(m.name + ", " + (double) m.count / numSeq + ", " + m.getPosition(tss) + ", " + m.effect / m.ec);
            saveLogo(m.fm, m.name + "_new");
        }

        System.out.println("Name, Frequency, Position, Effect");
        System.out.println("TATA, " + (double) tata.count / numSeq + ", " + tata.getPosition(tss) + ", " + tata.effect / tata.count);
        System.out.println("CCAT, " + (double) ccat.count / numSeq + ", " + ccat.getPosition(tss) + ", " + ccat.effect / ccat.count);
        System.out.println("Inr, " + (double) inr.count / numSeq + ", " + "-2" + ", " + inr.effect / inr.count);
        System.out.println("TCT, " + (double) tct.count / numSeq + ", " + "-2" + ", " + tct.effect / tct.count);

        saveLogo(tata.fm, "tata");

        saveLogo(ccat.fm, "ccat");

        saveLogo(inr.fm, "inr");

        saveLogo(tct.fm, "tct");

        ArrayList<RealMatrix> motifs = KMeans.freqMatrix(trackCore, tss);
        for (
                int i = 0; i < motifs.size(); i++) {
            saveLogo(motifs.get(i), "core" + i);
        }

        motifs = KMeans.freqMatrix(trackNoCoreTotal, tss);
        double bdt = 0;
        for (
                int i = 0; i < motifs.size(); i++) {
            saveLogo(motifs.get(i), "no_core" + i);
            String conNew = getCon(motifs.get(i));
            int index = 0;
            int bd = 100;
            for (int j = 0; j < cons.size(); j++) {
                int dif = distance(conNew, cons.get(j)); //- Math.abs(motifLen - cons.get(j).length())
                if (dif <= bd) {
                    bd = dif;
                    index = j;
                }
            }
            bdt += bd;
            System.out.println("Found!" + urls.get(index) + " Cluster " + i + " " + conNew + "         " + cons.get(index) + "        " + bd);
        }
        System.out.println("Avg dif " + bdt / motifs.size());
        Trend.thick = true;

    }

    public static ArrayList<Integer> findWord(String textString, String word) {
        ArrayList<Integer> indexes = new ArrayList<Integer>();
        String lowerCaseTextString = textString.toLowerCase();
        String lowerCaseWord = word.toLowerCase();

        int index = 0;
        while (index != -1) {
            index = lowerCaseTextString.indexOf(lowerCaseWord, index);
            if (index != -1) {
                indexes.add(index);
                index++;
            }
        }
        return indexes;
    }

    private static String getCon(RealMatrix m) {
        String con = "";
        for (int i = 0; i < m.getColumnDimension(); i++) {
            double a, t, g, c;
            a = m.getEntry(0, i);
            t = m.getEntry(1, i);
            g = m.getEntry(2, i);
            c = m.getEntry(3, i);
            if (a >= c && a >= g && a >= t) {
                con += "A";
            } else if (c >= a && c >= g && c >= t) {
                con += "C";
            } else if (g >= c && g >= a && g >= t) {
                con += "G";
            } else {
                con += "T";
            }
        }
        return con;
    }

    public static int distance(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        // i == 0
        int[] costs = new int[b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }

    private static int[] flatten(float[][] seq, int c, int motifLen) {
        int[] fs = new int[motifLen];
        for (int i = 0; i < motifLen; i++) {
            if (seq[c + i][0] == 1) {
                fs[i] = 1;
            } else if (seq[c + i][1] == 1) {
                fs[i] = 2;
            } else if (seq[c + i][2] == 1) {
                fs[i] = 3;
            } else if (seq[c + i][3] == 1) {
                fs[i] = 4;
            }
        }
        return fs;
    }

    private static void saveLogo(RealMatrix freqMatrix, String name) {
        ArrayList<String> letters = new ArrayList<>();
        letters.add("A");
        letters.add("C");
        letters.add("G");
        letters.add("T");
        //letters.add("N");
        for (int i = 0; i < freqMatrix.getColumnDimension(); i++) {
            double csum = 0;
            for (int j = 0; j < freqMatrix.getRowDimension(); j++) {
                csum += freqMatrix.getEntry(j, i);
            }
            for (int j = 0; j < freqMatrix.getRowDimension(); j++) {
                freqMatrix.setEntry(j, i, (double) freqMatrix.getEntry(j, i) / csum);
            }
        }
        RealVector H = new ArrayRealVector(freqMatrix.getColumnDimension());
        //double e = (1 / Math.log(2)) * ((letters.size() - 1) / (2 * line));
        for (int i = 0; i < freqMatrix.getColumnDimension(); i++) {
            double sum = 0;
            for (int j = 0; j < letters.size(); j++) {
                if (freqMatrix.getEntry(j, i) != 0) {
                    sum += freqMatrix.getEntry(j, i) * (Math.log(freqMatrix.getEntry(j, i)) / Math.log(2));
                }
            }
            H.setEntry(i, -sum);
        }
        RealVector Rseq = new ArrayRealVector(freqMatrix.getColumnDimension(), 2).subtract(H);
        LogoComponent logo = new LogoComponent(freqMatrix, Rseq, letters);
        try {
            saveComponent(logo, "png", new File(output + name + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void saveComponent(Object c, String format, File outputfile) throws IOException {
        BufferedImage myImage;
        JComponent jc = (JComponent) c;
        if (jc instanceof LogoComponent) {
            ((LogoComponent) jc).setQuality(1);
        }
        jc.setSize(jc.getPreferredSize().width, jc.getPreferredSize().height);
        jc.repaint();

        myImage = new BufferedImage(jc.getWidth(), jc.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = myImage.createGraphics();
        jc.paint(g);
        try {
            ImageIO.write(myImage, format, outputfile);

        } catch (Exception e) {
            e.printStackTrace();
        }
        jc.setSize(jc.getWidth(), jc.getHeight());

        if (jc instanceof LogoComponent) {
            ((LogoComponent) jc).setQuality(0);
        }
    }


    public static float[][] cloneArray(float[][] src) {
        int length = src.length;
        float[][] target = new float[length][src[0].length];
        for (int i = 0; i < length; i++) {
            System.arraycopy(src[i], 0, target[i], 0, src[i].length);
        }
        return target;
    }

    public static double[] tatascore(float[][] a, int tss) {
        double[][] tata = new double[][]{{-1.02, -1.68, 0, -0.28}, {-3.05, 0, -2.74, -2.06}, {0, -2.28, -4.28, -5.22}, {-4.61, 0, -4.61, -3.49}, {
                0, -2.34, -3.77, -5.17}, {0, -0.52, -4.73, -4.63}, {0, -3.65, -2.65, -4.12}, {0, -0.37, -1.5, -3.74}, {
                -0.01, -1.4, 0, -1.13}, {-0.94, -0.97, 0, -0.05}, {-0.54, -1.4, -0.09, 0}, {-0.48, -0.82, 0, -0.05}, {
                -0.48, -0.66, 0, -0.11}, {-0.74, -0.54, 0, -0.28}, {-0.62, -0.61, 0, -0.4}};
        double maxScore = -1000;
        int maxI = -1000;
        for (int p = 0; p < 14; p++) {
            double[][] seq = new double[15][4];
            for (int i = 0; i < tata.length; i++) {
                for (int j = 0; j < 4; j++) {
                    seq[i][j] = a[tss - 39 + p + i][j];
                }
            }
            double score = 0;
            for (int i = 0; i < tata.length; i++) {
                for (int j = 0; j < 4; j++) {
                    score = score + tata[i][j] * seq[i][j];
                }
            }
            if (score > maxScore) {
                maxScore = score;
                maxI = 39 - p;
            }
        }
        return new double[]{maxScore, maxI};
    }

    public static double[] ccaatscore(float[][] a, int tss) {
        double[][] ccat = {{-0.02, 0, -1.46, -0.01}, {-0.49, -0.01, -0.24, 0}, {-1.19, 0, -1.26, -0.57}, {0, -3.16, -0.4, -3.46}, {
                -0.61, -1.44, 0, -2.45}, {-4.39, -3.99, -4.03, 0}, {-4.4, -4, -4.4, 0}, {0, -4.37, -4.37, -4.37}, {
                0, -1.33, -1.69, -2.45}, {-2.12, 0, -2.26, -4.27}, {-1.32, -2.84, -0.47, 0}, {0, -3.57, -0.81, -2.64}};
        double maxScore = -1000;
        int maxI = -1000;

        for (int p = 0; p < 142; p++) {
            double[][] seq = new double[12][4];
            for (int i = 0; i < ccat.length; i++) {
                for (int j = 0; j < 4; j++) {
                    seq[i][j] = a[tss - 200 + p + i][j];
                }
            }

            double score = 0;
            for (int i = 0; i < ccat.length; i++) {
                for (int j = 0; j < 4; j++) {
                    score = score + ccat[i][j] * seq[i][j];
                }
            }
            if (score > maxScore) {
                maxScore = score;
                maxI = 200 - p;
            }
        }
        return new double[]{maxScore, maxI};
    }

    public static double inrscore(float[][] b, int tss) {
        double[][] inr = new double[][]{{-1.14, 0, -0.75, -1.16}, {-5.26, -5.26, -5.26, 0}, {0, -2.74, -5.21, -5.21}, {-1.51, -0.29, 0, -0.41}, {-0.65, 0, -4.56, -0.45}, {-0.55, -0.36, -0.86, 0}, {-0.91, 0, -0.38, -0.29}, {-0.82, 0, -0.65, -0.18}};
        double[][] a = new double[8][4];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < 4; j++) {
                a[i][j] = b[tss - 2 + i][j];
            }
        }
        double score = 0;
        for (int i = 0; i < inr.length; i++) {
            for (int j = 0; j < 4; j++) {
                score = score + inr[i][j] * a[i][j];
            }
        }
        return score;
    }

    public static double tctscore(float[][] b, int tss) {
        double[][] tct = new double[][]{{0.08, 0.35, 0.30, 0.27}, {0.08, 0.32, 0.17, 0.43}, {0.00, 0.00, 0.00, 11.00}, {0.07, 0.62, 0.08, 0.24}, {0.09, 0.32, 0.16, 0.43}, {0.11, 0.43, 0.15, 0.30}, {0.09, 0.33, 0.22, 0.36}, {0.10, 0.28, 0.24, 0.38}};
        double[][] a = new double[8][4];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < 4; j++) {
                a[i][j] = b[tss - 2 + i][j];
            }
        }
        double score = 0;
        for (int i = 0; i < tct.length; i++) {
            for (int j = 0; j < 4; j++) {
                score = score + tct[i][j] * a[i][j];
            }
        }
        return score;
    }


}

class Motif {
    public ArrayList<Integer> pos;
    public double effect = 0;
    public int count = 0;
    public RealMatrix fm;
    public RealMatrix fmNew;
    public String name;
    public int start;
    public double cutoff;
    public int tss;
    public String lastMot;
    public int ec;

    public Motif(File file, String name, int tss) {
        this.name = name;
        this.tss = tss;
        double[][] pwm = new double[4][];

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine();
            cutoff = Double.parseDouble(line.split(",")[0]);
            line = br.readLine();
            start = Integer.parseInt(line.split(",")[0]);
            if (start < 0) {
                start = tss + start;
            } else {
                start = tss + (start - 1);
            }

            String a = br.readLine();
            String c = br.readLine();
            String g = br.readLine();
            String t = br.readLine();

            pwm[0] = toArray(a, ",");
            pwm[1] = toArray(t, ",");
            pwm[2] = toArray(g, ",");
            pwm[3] = toArray(c, ",");
        } catch (IOException e) {
            e.printStackTrace();
        }


        this.fm = new Array2DRowRealMatrix(pwm);
        norm();
        this.pos = new ArrayList<>();
    }

    private void norm() {
        for (int i = 0; i < 4; i++) {
            double max = -1;
            for (int j = 0; j < fm.getColumnDimension(); j++) {
                if (fm.getEntry(i, j) > max) {
                    max = fm.getEntry(i, j);
                }
            }
            double scale = 1.0 / max;
            for (int j = 0; j < fm.getColumnDimension(); j++) {
                fm.setEntry(i, j, fm.getEntry(i, j) * scale);
            }
        }
    }

    public void add(int[] sequence) {
        for (int j = 0; j < fm.getColumnDimension(); j++) {
            if (sequence[j] > 0) {
                fm.addToEntry(sequence[j] - 1, j, 1);
            }
        }
    }

    public String getPosition(int tss) {
        int min = Integer.MAX_VALUE;
        int max = -Integer.MAX_VALUE;
        for (int p : pos) {
            if (p > max) {
                max = p;
            }
            if (p < min) {
                min = p;
            }
        }
        String l = "";
        if (min < tss) {
            l = "[" + (min - tss);
        } else {
            l = "[+" + (min - tss + 1);
        }

        String r = "";
        if (max < tss) {
            r = (max - tss) + "]";
        } else {
            r = "+" + (max - tss + 1) + "]";
        }
        return l + " : " + r;
    }

    public int detect(float[][] seq) {
        int p = -1;
        int r = 10;
        double bestScore = -Double.MAX_VALUE;
        for (int n = 0; n < r; n++) {
            double score = 1;
            for (int i = 0; i < fm.getColumnDimension(); i++) {
                double val = 0;
                for (int d = 0; d < 4; d++) {
                    val += seq[start + i - r / 2 + n][d] * fm.getEntry(d, i);
                }
                score *= val;
            }
            if (score > bestScore) {
                p = start - r / 2 + n;
                bestScore = score;
            }
        }
        lastMot = FastaParser.reverse(Arrays.copyOfRange(seq, p, p + fm.getColumnDimension()));
        if (bestScore >= cutoff) {
            return p;
        } else {
            return -1;
        }
    }

    public static double[] toArray(String input, String delimiter) {

        return Arrays.stream(input.split(delimiter))
                .mapToDouble(Double::parseDouble)
                .toArray();
    }

    public void update() {
        fm = fmNew.scalarMultiply(1.0 / count);
        norm();
        fmNew = new Array2DRowRealMatrix(fm.getRowDimension(), fm.getColumnDimension());
    }
}

class OldMotif {
    public ArrayList<Integer> pos;
    public double effect = 0;
    public int count = 0;
    public RealMatrix fm;

    public OldMotif(int seqLen) {
        this.fm = new Array2DRowRealMatrix(4, seqLen);
        this.pos = new ArrayList<>();
    }

    public void add(int[] sequence) {
        for (int j = 0; j < fm.getColumnDimension(); j++) {
            if (sequence[j] > 0) {
                fm.addToEntry(sequence[j] - 1, j, 1);
            }
        }
    }

    public String getPosition(int tss) {
        int min = Integer.MAX_VALUE;
        int max = -Integer.MAX_VALUE;
        for (int p : pos) {
            if (p > max) {
                max = p;
            }
            if (p < min) {
                min = p;
            }
        }
        String l = "";
        if (min < tss) {
            l = "[" + (min - tss);
        } else {
            l = "[+" + (min - tss + 1);
        }

        String r = "";
        if (max < tss) {
            r = (max - tss) + "]";
        } else {
            r = "+" + (max - tss + 1) + "]";
        }
        return l + " : " + r;
    }
}

class SimpleMotif {
    public int position;
    public double effect;
    public int count = 1;
    public ArrayList<Integer> pos = new ArrayList<>();

    public SimpleMotif(int i, int j, double change) {
        count = i;
        pos.add(j);
        position = j;
        effect = change;
    }

    public String getPosition(int tss) {
        int min = Integer.MAX_VALUE;
        int max = -Integer.MAX_VALUE;
        for (int p : pos) {
            if (p > max) {
                max = p;
            }
            if (p < min) {
                min = p;
            }
        }
        String l = "";
        if (min < tss) {
            l = "[" + (min - tss);
        } else {
            l = "[+" + (min - tss + 1);
        }

        String r = "";
        if (max < tss) {
            r = (max - tss) + "]";
        } else {
            r = "+" + (max - tss + 1) + "]";
        }
        return l + " : " + r;
    }
}