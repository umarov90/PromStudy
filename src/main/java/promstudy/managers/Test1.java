package promstudy.managers;


import promstudy.common.Element2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.stream.Stream;

;

/**
 * Created by ramzan on 23/01/18.
 */
public class Test1 {
    static int pos = -1;
    static int margin = 10;
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
                    }else if (option.equals("-margin")) {
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
        boolean b = true;
        Scanner scan = new Scanner(setp);
        while(scan.hasNextLine()){
            String line = scan.nextLine();
            if(line.endsWith("prediction")){
                Scanner scan2 = new Scanner(line);
                int pp = scan2.nextInt();
                if(pp>=pos-margin && pp<=pos+margin){
                    if(b) {
                        c++;
                        b = false;
                    }
                }else{
                    e++;
                }
            }else {
                b = true;
            }
        }
        System.out.println(c);
        System.out.println(e);
    }
}
