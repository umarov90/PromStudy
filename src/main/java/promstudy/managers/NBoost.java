package promstudy.managers;

import promstudy.common.Element;
import promstudy.common.FastaParser;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

public class NBoost {
    private static Predictor p;
    private static float[][][] sequences;
    private static int step;
    private static int sLen = 251;
    private static String output = null;
    private static int pos;
    private static int d = -1;
    private static ArrayList<String> negatives = new ArrayList<>();
    private static ArrayList<String> positives = new ArrayList<>();
    //private static List<String> safeNegatives;
    //private static List<String> safePositives;
    private static int maxSize = 2500;
    private static int mps =  4;


    public static void main(String[] args) {
        //safeNegatives = Collections.synchronizedList(negatives);
        //safePositives = Collections.synchronizedList(positives);
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
                    } else if (option.equals("-step")) {
                        step = Integer.parseInt(parameter);
                    } else if (option.equals("-pos")) {
                        pos = Integer.parseInt(parameter);
                    } else if (option.equals("-d")) {
                        d = Integer.parseInt(parameter);
                    }else if (option.equals("-sl")) {
                        sLen = Integer.parseInt(parameter);
                    } else if (option.equals("-mps")) {
                        mps = Integer.parseInt(parameter);
                    } else {
                        System.err.println("Unknown option: " + option);
                        System.err.println("Available Options: ");
                        System.err.println("-set: file with long sequences");
                        System.err.println("-mod: location of trained model file");
                        System.err.println("-step: step for sliding window (default is 1)");
                        System.err.println("-pos: promoter region start");
                        System.err.println("-out: output file with scores");
                        return;
                    }
                }
                if (toPred != null) {
                    sequences = FastaParser.parse(toPred);
                    analyse(output);
                }
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        try {
            //s.positive = FastaParser.parse(new File("data/small_pos.seq"));
            //s.negative = FastaParser.parse(new File("data/small_neg.seq"));
            //s.sequences = FastaParser.parse(new File("data/ara2K_200.fa_2"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void analyse(String output) {
        ArrayList<float[][]> seqs = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < sequences.length; i++) {
            float[][] seq = sequences[i];
            seqs.add(seq);
        }
        //seqs.parallelStream().forEach(o ->
        //        findNegatives(o)
        //);
        for(int i = 0; i<seqs.size(); i++){
            findNegatives(seqs.get(i));
            if(i%100==0) {
                System.out.print(i+"("+negatives.size()+") - ");
            }
        }
        Collections.shuffle(negatives);
        System.out.println("New negatives count: " + negatives.size());
        //negatives.subList(maxSize, negatives.size()).clear();
        //System.out.println(negatives.size());
        for (int i = 0; i < negatives.size(); i++) {
            sb.append(">\n");
            sb.append(negatives.get(i));
            sb.append("\n");
        }
        try (PrintWriter out = new PrintWriter(output + "_n")) {
            out.println(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        sb = new StringBuilder();
        for (int i = 0; i < positives.size(); i++) {
            sb.append(">\n");
            sb.append(positives.get(i));
            sb.append("\n");
        }
        try (PrintWriter out = new PrintWriter(output + "_p")) {
            out.println(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void findNegatives(float[][] seq) {
        int total = (int) Math.ceil((seq.length - sLen) / step) + 1;
        float[][][] toPredict = new float[total][sLen][4];
        for (int j = 0; j < toPredict.length; j++) {
            toPredict[j] = Arrays.copyOfRange(seq, (j* step) , (j* step)  + sLen);
        }
        float[] r = p.predict(toPredict);
        int maxI = -1;
        double max = -1;
        if (d == -1) {
            d = sLen;
        }
        Element[] e = new Element[r.length];
        for (int j = 0; j < r.length; j++) {
            e[j] = new Element(j, r[j]);
        }
        Arrays.sort(e, Collections.reverseOrder());
        ArrayList<Integer> inds = new ArrayList<>();
        for (int j = 0; j < e.length; j++) {
            if (e[j].value > 0.5) {
                if(e[j].index < (pos - d) / step || e[j].index  > (pos + d) / step){
                    if(notClose(e[j].index, inds)){
                        inds.add(e[j].index);
                        negatives.add(FastaParser.reverse(toPredict[e[j].index]));
                    }
                }
            }else{
                break;
            }
            if(inds.size() > mps){
                break;
            }
        }
        if (r[pos / step] < 0.5) {
            positives.add(FastaParser.reverse(toPredict[pos / step]));
        }
    }

    private static boolean notClose(int index, ArrayList<Integer> inds) {
        boolean nc = true;
        for(Integer i:inds){
            if(Math.abs(i-index) < 40){
                nc = false;
                break;
            }
        }
        return nc;
    }
}
