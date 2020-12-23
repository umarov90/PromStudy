package promstudy.managers;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

;

/**
 * Created by ramzan on 23/01/18.
 */
public class Test2 {
    static int pos = -1;
    static int margin = 50;
    static int sd = 10;
    private static double dt = 0.5;
    private static int minDist = 200;

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
                    } else if (option.equals("-sd")) {
                        sd = Integer.parseInt(parameter);
                    } else if (option.equals("-dt")) {
                        dt = Double.parseDouble(parameter);
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
                if (setp != null) {
                    evaluate(setp);
                }
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private static void evaluate(File setp) throws FileNotFoundException {
        int c = 0;
        int e = 0;
        boolean cb = true;
        Scanner scan = new Scanner(setp);
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            if(line.startsWith(">")) {
                cb = true;
            }
            if (line.startsWith("Position ")) {
                Scanner scan2 = new Scanner(line);
                scan2.next();
                int pp = scan2.nextInt();
                if (pp >= pos - margin && pp <= pos + margin) {
                    if(cb) {
                        c++;
                        cb = false;
                    }
                } else {
                    e++;
                }
            }
        }
        System.out.println(c);
        System.out.println(e);
    }
}
