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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by umarovr on 3/22/18.
 */
public class FPStats {
    private static float[][][] sequences;
    private static int step = 1;
    private static int sLen = 2001;
    private static int sd = 10;
    private static int minDist = 1000;
    private static String output = "output";
    private static ArrayList<String> names;
    private static double dt = 0.5;
    private static int count = 1;
    static int w = 40 * 81;
    static int h = 6 * 40 + 100;
    private static boolean ignoreCore = false;
    private static int numSeq = 20000;
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
                analyseFP();
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static void analyseFP() {
        int tss = 1000;
        ArrayList<Motif> motifsKnown = new ArrayList<>();
        File dir = new File("PWM");
        File[] directoryListing = dir.listFiles();
        for (File child : directoryListing) {
            String name = child.getName().replaceFirst("[.][^.]+$", "");
            motifsKnown.add(new Motif(child, name, tss));
        }

        for (int s = 0; s < sequences.length; s++) {
            float[][] seq = sequences[s];
            if (FastaParser.reverse(Arrays.copyOfRange(seq, 960, 1041)).equals("CACGCCATTATAATGCTACTCCGCCCAGGGGCCCTGAGTTGGTCCCCACTGCCCTTGTACCCCCCTCCCGCCGCGCCAGGC")) {
                System.out.println();
            }
            int c = 0;
            boolean tata = false;
            boolean inr = false;
            for (int i = 0; i < motifsKnown.size(); i++) {
                int p = motifsKnown.get(i).detect(seq);
                String name = motifsKnown.get(i).name;
                if (p != -1) {
                    FastaParser.reverse(seq).substring(p, p+7);
                    if (motifsKnown.get(i).name.toLowerCase().trim().equals("tata")) {
                        tata = true;
                    }
                    if (motifsKnown.get(i).name.toLowerCase().trim().equals("inr") &&p==998) {
                        inr = true;
                    }
                    c++;
                }
            }
            int gc = 0;
            for (int i = 960; i < 1041; i++) {
                if (seq[i][2] == 1 || seq[i][3] == 1) {
                    gc++;
                }
            }
            if (c >= 2 && gc > 50 && tata) {
                System.out.println(FastaParser.reverse(Arrays.copyOfRange(seq, 960, 1041)) + " " + names.get(s) + "         " + gc + "      " + c);
            }
        }


    }
}
