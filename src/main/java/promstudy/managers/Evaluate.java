package promstudy.managers;


import promstudy.common.Element2;
import promstudy.common.FastaParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;;
import java.util.Collections;
import java.util.stream.Stream;

/**
 * Created by ramzan on 23/01/18.
 */
public class Evaluate {
    static int pos = -1;
    private static double dt = 0.5;
    private static int minDist = 1000;
    private static int mr = 500;
    private static int step = 1;

    public static void main(String[] args) {
        File setp = null;
        if (args.length > 0) {
            try {
                for (int i = 0; i < args.length / 2; i++) {
                    String option = args[2 * i];
                    String parameter = args[2 * i + 1];
                    if (option.equals("-setp")) {
                        setp = new File(parameter);
                    } else if (option.equals("-pos")) {
                        pos = Integer.parseInt(parameter);
                    } else if (option.equals("-md")) {
                        minDist = Integer.parseInt(parameter);
                    } else if (option.equals("-mr")) {
                        mr = Integer.parseInt(parameter);
                    } else if (option.equals("-step")) {
                        step = Integer.parseInt(parameter);
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
                if (setp != null) {
                    evaluate(setp);
                }
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private static void evaluate(File setp) {
        ArrayList<double[]> arrays = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(setp);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(",")) {
                    double[] arr = Stream.of(line.split(","))
                            .mapToDouble(Double::parseDouble)
                            .toArray();
                    arrays.add(arr);
                }
            }
            fileReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        int c = 0;
        int t = 0;
        int a = 0;
        for (int ari = 0; ari < arrays.size(); ari++) {
            boolean nc = true;
            double[] ar = arrays.get(ari);
            ArrayList<Element2> elements = new ArrayList<>();

            for (int i = 0; i < ar.length; i++) {
                elements.add(new Element2(i * step, (float) ar[i], ar[i] > dt));
            }
            Collections.sort(elements, Collections.reverseOrder());
            ArrayList<Integer> inds = new ArrayList<>();
            for (int k = 0; k < elements.size(); k++) {
                boolean kek = false;
                if (k == 0 && elements.get(k).index >= (pos - mr) && elements.get(k).index <= (pos + mr)) {
                    a++;
                    kek = true;
                }
                if (!elements.get(k).p) {
                    break;
                }
                if (notClose(elements.get(k).index, inds)) {
                    inds.add(elements.get(k).index);
                    if (elements.get(k).index >= (pos - mr) && elements.get(k).index <= (pos + mr)) {
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
        double fp = t;
        double tn = arrays.size() * (10000 - 600 - mr * 2);
        double tp = c;
        double fn = arrays.size() - c;
        double mcc = (tp * tn - fp * fn) / Math.sqrt((tp + fp) * (tp + fn) * (tn + fp) * (tn + fn));
        double recall = tp / (tp + fn);
        double precision = tp / (tp + fp);
        double f1 = 2*((precision*recall)/(precision+recall));
        System.out.println("F1: " + f1);
        System.out.println("Total: " + arrays.size());
        System.out.println("Correct: " + c);
        System.out.println("Recall: " + recall);
        System.out.println("Precision: " + precision);
        System.out.println("False positives: " + t);
        System.out.println("Error rate: " + (t * 100.0 / c) + "%");
        System.out.println("Error per 1000BP: " + (double) t / (10 * arrays.size()));
        System.out.println("True promoter is biggest: " + (a * 100.0 / arrays.size()) + "%");

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
}
