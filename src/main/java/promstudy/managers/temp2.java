package promstudy.managers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class temp2 {
    public static void main(String[] a) {
        int c = 0;
        int t = 0;
        int t2 = 0;
        int k = 0;
        int m = 0;
        int ti = 0;
        int ti2 = 0;
        double[][] w = new double[8][4];
        try (BufferedReader br = new BufferedReader(new FileReader("tmp8"))) {
            for (String line; (line = br.readLine()) != null; ) {
                try {
                    if (line.startsWith(">")) {

                    } else {
                        t++;
                        if (line.charAt(2) == 'C') {
                            t2++;
                            for (int i = 0; i < 8; i++) {
                                w[i][getIndex(line.charAt(i))] = w[i][getIndex(line.charAt(i))] + 1;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        DecimalFormat df = new DecimalFormat("####0.00");
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 4; j++) {
                w[i][j] = w[i][j] / t2;
                System.out.print(df.format(w[i][j]) + ",");
            }
            System.out.println();
        }

        double pvsum = 0;
        double nvsum = 0;
        t = 0;
        t2 = 0;
        c  = 0;
        int c2 = 0;
        try (BufferedReader br = new BufferedReader(new FileReader("tmp8"))) {
            for (String line; (line = br.readLine()) != null; ) {
                try {
                    if (line.startsWith(">")) {

                    } else {
                        double[][] wc = new double[8][4];
                        for (int i = 0; i < 8; i++) {
                            wc[i][getIndex(line.charAt(i))] = 1;
                        }
                        double sum = 0;
                        for (int i = 0; i < 8; i++) {
                            for (int j = 0; j < 4; j++) {
                                sum += wc[i][j] * w[i][j];
                            }
                        }
                        if (line.charAt(2) == 'C') {
                            pvsum += sum;
                            if(sum>=2.84){
                                c++;
                            }
                            t2++;
                        } else {
                            if(sum>=2.84){
                                c2++;
                            }
                            nvsum += sum;
                            t++;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(pvsum / t2);
        System.out.println(nvsum / t);
        System.out.println(c2);
        System.out.println((double) c/t2);
    }

    public static int getIndex(char a) {
        if (a == 'A') {
            return 0;
        } else if (a == 'T') {
            return 1;
        } else if (a == 'G') {
            return 2;
        } else if (a == 'C') {
            return 3;
        }
        return -1;
    }
}
