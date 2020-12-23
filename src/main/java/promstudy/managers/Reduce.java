package promstudy.managers;

import promstudy.common.FastaParser;
import promstudy.common.posAndVal;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Created by ramzan on 23/01/18.
 */
public class Reduce {
    static String inN;
    static String inP;
    static String outN;
    static String outP;
    static String posDir;
    static String negDir;


    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                for (int i = 0; i < args.length / 2; i++) {
                    String option = args[2 * i];
                    String parameter = args[2 * i + 1];
                    if (option.equals("-inn")) {
                        inN = parameter;
                    } else if (option.equals("-inp")) {
                        inP = parameter;
                    } else if (option.equals("-outn")) {
                        outN = parameter;
                    } else if (option.equals("-outp")) {
                        outP = parameter;
                    } else if (option.equals("-posd")) {
                        posDir = parameter;
                    } else if (option.equals("-negd")) {
                        negDir = parameter;
                    } else {
                        System.err.println("Unknown option: " + option);
                        System.err.println("Available Options: ");
                        System.err.println("-setp: predictions from PredSet.jar");
                        System.err.println("-params: file with HMM parameters ");
                        System.err.println("-step: step for sliding window (default is 1)");
                        System.err.println("-out: output file with predicted TSS");
                        return;
                    }
                }
                reduce();
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private static void reduce() throws FileNotFoundException {
        ArrayList<float[][]> posToRemove = findCommon(posDir);
        ArrayList<float[][]> negToRemove = findCommon(negDir);
        float[][][] posOld = FastaParser.parse(new File(inP));
        float[][][] negOld = FastaParser.parse(new File(inN));

        ArrayList<float[][]> posNew = reduce(posOld, posToRemove);
        ArrayList<float[][]> negNew = reduce(negOld, negToRemove);

        try (PrintWriter out = new PrintWriter(outN)) {
            out.println(FastaParser.toString(negNew));
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (PrintWriter out = new PrintWriter(outP)) {
            out.println(FastaParser.toString(posNew));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private static ArrayList<float[][]> reduce(float[][][] oldSet, ArrayList<float[][]> toRemove) {
        ArrayList<float[][]> newSet = new ArrayList<>();
        for (int i = 0; i < oldSet.length; i++) {
            float[][] seq = oldSet[i];
            boolean r = true;
            for (float[][] s : toRemove) {
                if (Arrays.deepEquals(s, seq)) {
                    r = false;
                    break;
                }
            }
            if (r) {
                newSet.add(seq);
            }
        }
        return newSet;
    }

    private static ArrayList<float[][]> findCommon(String dir) throws FileNotFoundException {
        File[] files = new File(dir).listFiles();
        ArrayList<float[][]> toRemove = new ArrayList<>();
        for (File f : files) {
            float[][][] seqs = FastaParser.parse(f);
            if (toRemove.isEmpty()) {
                toRemove.addAll(Arrays.asList(seqs));
            } else {
                for (int j = toRemove.size() - 1; j >= 0; j--) {
                    float[][] s = toRemove.get(j);
                    boolean r = true;
                    for (int i = 0; i < seqs.length; i++) {
                        if (Arrays.deepEquals(s, seqs[i])) {
                            r = false;
                            break;
                        }
                    }
                    if (r) {
                        toRemove.remove(j);
                    }
                }
            }
        }
        return toRemove;
    }


}
