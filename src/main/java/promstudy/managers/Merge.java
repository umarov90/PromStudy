package promstudy.managers;

import promstudy.common.FastaParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by ramzan on 14/02/18.
 */
public class Merge {

    static String f1;
    static String f2;
    static String output;
    static int max;
    static int check = 0;
    static int check2 = 1;

    public static void main(String[] args) {
        if (args.length > 0) {
            try {
                for (int i = 0; i < args.length / 2; i++) {
                    String option = args[2 * i];
                    String parameter = args[2 * i + 1];
                    if (option.equals("-out")) {
                        output = parameter;
                    } else if (option.equals("-f1")) {
                        f1 = parameter;
                    } else if (option.equals("-f2")) {
                        f2 = parameter;
                    } else if (option.equals("-max")) {
                        max = Integer.parseInt(parameter);
                    }else if (option.equals("-check")) {
                        check = Integer.parseInt(parameter);
                    } else if (option.equals("-check2")) {
                        check2 = Integer.parseInt(parameter);
                    }else {
                        System.err.println("Unknown option: " + option);
                        System.err.println("Available Options: ");
                        System.err.println("-setp: predictions from PredSet.jar");
                        System.err.println("-params: file with HMM parameters ");
                        System.err.println("-step: step for sliding window (default is 1)");
                        System.err.println("-out: output file with predicted TSS");
                        return;
                    }
                }
                merge();
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private static void merge() throws FileNotFoundException {
        float[][][] seqs1 = FastaParser.parse(new File(f1));
        float[][][] seqs2 = FastaParser.parse(new File(f2));
        ArrayList<float[][]> s1 = new ArrayList<>();
        ArrayList<float[][]> s2 = new ArrayList<>();
        int d = 0;
        for(float[][] s:seqs1){
            boolean a = true;
            if(check==1) {
                for (float[][] rs : s1) {
                    if (Arrays.deepEquals(s, rs)) {
                        d++;
                        a = false;
                        break;
                    }
                }
            }
            if(a) {
                s1.add(s);
            }
        }

        for(float[][] s:seqs2){
            boolean a = true;
            if(check==1) {
                for (float[][] rs : s2) {
                    if (Arrays.deepEquals(s, rs)) {
                        d++;
                        a = false;
                        break;
                    }
                }
            }
            if(a){
                if(check2==1) {
                    for (float[][] rs : s1) {
                        if (Arrays.deepEquals(s, rs)) {
                            d++;
                            a = false;
                            break;
                        }
                    }
                }
            }
            if(a) {
                s2.add(s);
            }
        }
        Collections.shuffle(s1);
        Collections.shuffle(s2);

        if(s1.size()+s2.size()>max && s1.size()> 0.5*max) {
            s1.subList((int)Math.floor(0.5*max), s1.size()).clear();
        }

        s1.addAll(s2);
        if(s1.size()>max) {
            s1.subList(max, s1.size()).clear();
        }
        System.out.println("Number of duplicates: " + d);
        try (PrintWriter out = new PrintWriter(output)) {
            out.println(FastaParser.toString(s1));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
