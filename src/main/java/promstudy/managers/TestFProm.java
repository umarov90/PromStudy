package promstudy.managers;


import promstudy.common.FastaParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;

;

/**
 * Created by ramzan on 23/01/18.
 */
public class TestFProm {
    static int pos = -1;
    static int margin = 300;

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
                    } else if (option.equals("-margin")) {
                        margin = Integer.parseInt(parameter);
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
                String[] sets = new String[]{"TATA+.res", "TATA-.res", "TATA-+.res"};
                double[][] res = new double[3][];
                for (int s = 0; s < 3; s++) {
                    String set = sets[s];
                    res[s] = evaluate(new File(set));
                }
                System.out.print("FProm");
                System.out.print(", ");
                DecimalFormat df = new DecimalFormat("###.###");
                for (int k = 0; k < res[0].length; k++) {
                    System.out.print(df.format(res[0][k]) + ", " + df.format(res[1][k]) + ", " + df.format(res[2][k]));
                    if (k != res[0].length - 1) {
                        System.out.print(", ");
                    }
                }
                System.out.println();
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private static double[] evaluate(File setp) throws FileNotFoundException {
        double c = 0;
        double e = 0;
        //double t = 0;
        double s = 0;
        boolean cb = true;
        Scanner scan = new Scanner(setp);
        StringBuilder sb = new StringBuilder();
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            if (line.startsWith("Sequence")) {
                cb = true;
                s++;
            }
            if (line.startsWith("TSS:")) {
                line = line.replaceAll(",", "");
                Scanner scan2 = new Scanner(line);
                scan2.next();
                int pp = scan2.nextInt();
                if(sb.length()!=0){
                    sb.append(", ");
                }
                sb.append(pp);
                scan2.next();
                double score = scan2.nextDouble();
                //if (score > 4.26) {
                    if (pp >= pos - margin && pp <= pos + margin) {
                        if (cb) {
                            c++;
                            cb = false;
                        }
                    } else {
                        e++;
                    }
               // }
            }
        }
        //System.out.println(s);
        //System.out.println(c);
        //System.out.println(e);
        //System.out.println();
        //System.out.println("Sensitivity: " + c * 100.0 / s);
        try (PrintWriter out = new PrintWriter("FPROM.val")) {
            out.println(sb.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        double fp = e;
        double tn = s * (10000 - 300 - 500 * 2);
        double tp = c;
        double fn = s - c;
        double mcc = (tp * tn - fp * fn) / Math.sqrt((tp + fp) * (tp + fn) * (tn + fp) * (tn + fn));

        DecimalFormat df = new DecimalFormat("###.###");

        //System.out.println("Total: " + s);
        //System.out.println("Correct: " + c);
        //System.out.println("Sensitivity: " +  df.format(tp / (tp + fn)));
        //System.out.println("False positives: " + fp);
        //System.out.println("MCC: " +  df.format(mcc));
        //System.out.println("Specificity: " +  df.format(tn / (tn + fp)));
        //System.out.println("Error rate: " +  df.format(e / c));
        //System.out.println("Error per 1000BP: " +  df.format(e / (10 * s)));

        double[] res = new double[7];
        res[0] = c;
        res[1] = fp;
        res[2] = tp / (tp + fn);
        res[3] = tp / (tp + fp);
        res[4] = mcc;
        res[5] = e / c;
        res[6] = e / (10 * s);
        return res;
    }
}
