package promstudy.managers;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Scanner;

;

/**
 * Created by ramzan on 23/01/18.
 */
public class TestPromID {
    static int pos = 5001;
    static int margin = 500;

    public static void main(String[] args) {
        try {
            String[] sets = new String[]{"TATA+.respi", "TATA-.respi", "TATA-+.respi"};
            //String[] sets = new String[]{"TATA-.respi"};
            double[][] res = new double[3][];
            for (int s = 0; s < sets.length; s++) {
                String set = sets[s];
                res[s] = evaluate(new File(set));
            }
           /* System.out.print("PromID");
            System.out.print(", ");
            DecimalFormat df = new DecimalFormat("###.###");
            for (int k = 0; k < res[0].length; k++) {
                System.out.print(df.format(res[0][k]) + ", " + df.format(res[1][k]) + ", " + df.format(res[2][k]));
                if (k != res[0].length - 1) {
                    System.out.print(", ");
                }
            }
            System.out.println();
            System.exit(0);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static double[] evaluate(File setp) throws FileNotFoundException {
        double c = 0;
        double e = 0;
        //double t = 0;
        double s = 0;
        boolean cb = true;
        Scanner scan = new Scanner(setp);
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            if (line.startsWith("<")) {
                cb = true;
                s++;
            }
            if (line.startsWith("Position")) {
                line = line.replaceAll(",", "");
                Scanner scan2 = new Scanner(line);
                scan2.next();
                int pp = scan2.nextInt();
                scan2.next();
                double score = scan2.nextDouble();
                if (score >= 0.4) {
                    if (pp >= pos - margin && pp <= pos + margin) {
                        if (cb) {
                            c++;
                            cb = false;
                            //System.out.println(pp);
                        }
                    } else {
                        e++;
                    }
                }
            }
        }
        //System.out.println(s);
        //System.out.println(c);
        //System.out.println(e);
        //System.out.println();
        //System.out.println("Sensitivity: " + c * 100.0 / s);

        double fp = e;
        double tn = s * (10000 - 500 * 2);
        double tp = c;
        double fn = s - c;

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
        res[4] = 2 * ((res[3] * res[2]) / (res[3] + res[2]));
        res[5] = e / c;
        res[6] = e / (10 * s);
        String cc = res[2] + "  " + res[3] + "  " + res[4];
        System.out.println(cc);
        return res;
    }
}
