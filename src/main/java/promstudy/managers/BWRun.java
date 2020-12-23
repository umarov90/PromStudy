package promstudy.managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.stream.Stream;

public class BWRun {
    public static void main(String[] args) {
        File setp = null;
        int iter = 5;
        double lr = 1;
        String output = null;
        if (args.length > 0) {
            try {
                for (int i = 0; i < args.length / 2; i++) {
                    String option = args[2 * i];
                    String parameter = args[2 * i + 1];
                    if (option.equals("-setp")) {
                        setp = new File(parameter);
                    } else if (option.equals("-out")) {
                        output = parameter;
                    } else if (option.equals("-it")) {
                        iter = Integer.parseInt(parameter);
                    } else if (option.equals("-lr")) {
                        lr = Double.parseDouble(parameter);
                    } else {
                        System.err.println("Unknown option: " + option);
                        System.err.println("Available Options: ");
                        System.err.println("-setp: predictions from PredSet.jar");
                        System.err.println("-it: number of iterations for Baum-Welch");
                        System.err.println("-out: output file with new HMM parameters");
                        return;
                    }
                }
                if (setp != null) {
                    bwrun2(setp, output, iter, lr);
                }
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private static void bwrun(File setp, String output, int iterations, double lr) {
        int K = 2;
        int N = 2;
        double[][] A = new double[K][K];
        A[0][0] = 0.99;
        A[0][1] = 0.01;
        A[1][1] = 0.20;
        A[1][0] = 0.80;
        double[][] B = new double[K][N];
        B[0][0] = 0.99;
        B[0][1] = 0.01;
        B[1][0] = 0.01;
        B[1][1] = 0.99;
        double[] p = new double[2];
        p[0] = 0.99;
        p[1] = 0.01;

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
        double dd = 0.01;
        for (int it = 0; it < iterations; it++) {
            System.out.println(A[0][1] + ",  " + B[0][2]);
            for (int ari = 0; ari < arrays.size(); ari++) {
                double[] ar = arrays.get(ari);

                int T = ar.length;
                int[] observations = new int[ar.length];
                for (int i = 0; i < ar.length; i++) {
                    if (ar[i] > 0.7) {
                        observations[i] = 1;
                    }  else {
                        observations[i] = 0;
                    }
                }


                //Baum-Welch
                double[][] a = new double[K][T];
                double[][] b = new double[K][T];
                //forward
                for (int i = 0; i < K; i++) {
                    a[i][0] = p[i] * B[i][observations[0]];
                }
                for (int t = 1; t < observations.length; t++) {
                    for (int i = 0; i < K; i++) {
                        double sum = 0;
                        for (int j = 0; j < K; j++) {
                            sum += a[j][t - 1] * A[j][i];
                        }
                        a[i][t] = B[i][observations[t]] * sum;
                    }
                }
                //backward
                for (int i = 0; i < K; i++) {
                    b[i][T - 1] = 1;
                }
                for (int t = T - 2; t >= 0; t--) {
                    for (int i = 0; i < K; i++) {
                        double sum = 0;
                        for (int j = 0; j < K; j++) {
                            sum += b[j][t + 1] * A[i][j] * B[j][observations[t + 1]];
                        }
                        b[i][t] = sum;
                    }
                }
                //update
                double[][] y = new double[K][T];
                double[][][] e = new double[K][K][T - 1];
                for (int t = 0; t < observations.length; t++) {
                    for (int i = 0; i < K; i++) {
                        double sum = 0;
                        for (int j = 0; j < K; j++) {
                            sum += a[j][t] * b[j][t];
                        }
                        y[i][t] = a[i][t] * b[i][t] / sum;
                    }
                }

                for (int t = 0; t < observations.length - 1; t++) {
                    for (int i = 0; i < K; i++) {
                        for (int j = 0; j < K; j++) {
                            double sum = 0;
                            for (int ii = 0; ii < K; ii++) {
                                for (int jj = 0; jj < K; jj++) {
                                    sum += a[ii][t] * A[ii][jj] * b[jj][t + 1] * B[jj][observations[t + 1]];
                                }
                            }
                            e[i][j][t] = a[i][t] * A[i][j] * b[j][t + 1] * B[j][observations[t + 1]] / sum;
                        }
                    }
                }

                for (int i = 0; i < K; i++) {
                    p[i] = (1 - dd) * p[i] + dd * (y[i][1]);
                    ;
                }
                for (int i = 0; i < K; i++) {
                    for (int j = 0; j < K; j++) {
                        double sum1 = 0, sum2 = 0;
                        for (int t = 0; t < observations.length - 1; t++) {
                            sum1 += e[i][j][t];
                            sum2 += y[i][t];
                        }
                        A[i][j] = (1 - dd) * A[i][j] + dd * (sum1 / sum2);
                    }
                }
                for (int i = 0; i < K; i++) {
                    for (int j = 0; j < N; j++) {
                        double sum1 = 0, sum2 = 0;
                        for (int t = 0; t < observations.length; t++) {
                            if (j == observations[t]) {
                                sum1 += y[i][t];
                            }
                            sum2 += y[i][t];
                        }
                        B[i][j] = (1 - dd) * B[i][j] + dd * (sum1 / sum2);
                    }
                }
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Initial probabilities\n");
        sb.append(p[0]);
        sb.append(",");
        sb.append(p[1]);
        sb.append("\n");
        sb.append("Transition probabilities\n");
        sb.append(A[0][0]);
        sb.append(",");
        sb.append(A[0][1]);
        sb.append("\n");
        sb.append(A[1][0]);
        sb.append(",");
        sb.append(A[1][1]);
        sb.append("\n");
        sb.append("Emission probabilities\n");
        sb.append(B[0][0]);
        sb.append(",");
        sb.append(B[0][1]);
        sb.append(",");
        sb.append(B[0][2]);
        sb.append("\n");
        sb.append(B[1][0]);
        sb.append(",");
        sb.append(B[1][1]);
        sb.append(",");
        sb.append(B[1][2]);

        try (PrintWriter out = new PrintWriter(output)) {
            out.println(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void bwrun2(File setp, String output, int iterations, double lr) throws Exception {
        int K = 2;
        int N = 3;
        double[][] A = new double[K][K];
        A[0][0] = 0.95;
        A[0][1] = 0.05;
        A[1][1] = 0.01;
        A[1][0] = 0.99;
        double[][] B = new double[K][N];
        B[0][0] = 0.70;
        B[0][1] = 0.19;
        B[0][2] = 0.01;
        B[1][0] = 0.01;
        B[1][1] = 0.09;
        B[1][2] = 0.90;
        double[] p = new double[2];
        p[0] = 0.99;
        p[1] = 0.01;

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
        for (int it = 0; it < iterations; it++) {
            System.out.println(A[0][1] + ",  " + B[0][2]+ ",  " + B[1][2]);
            for (int ari = 0; ari < arrays.size(); ari++) {
                double[] ar = arrays.get(ari);

                int T = ar.length;
                int[] observations = new int[ar.length];
                for (int i = 0; i < ar.length; i++) {
                    if (ar[i] > 0.7) {
                        observations[i] = 2;
                    } else if (ar[i] > 0.3) {
                        observations[i] = 1;
                    } else {
                        observations[i] = 0;
                    }
                }
                //Baum-Welch
                double[][] a = new double[K][T];
                double[][] b = new double[K][T];
                //forward
                for (int i = 0; i < K; i++) {
                    a[i][0] = elnproduct(eln(p[i]), eln(B[i][observations[0]]));
                }
                for (int t = 1; t < observations.length; t++) {
                    for (int i = 0; i < K; i++) {
                        double logalpha = Double.NaN;
                        for (int j = 0; j < K; j++) {
                            logalpha = elnsum(logalpha, elnproduct(a[j][t - 1], eln(A[j][i])));
                        }
                        a[i][t] = elnproduct(logalpha, eln(B[i][observations[t]]));
                    }
                }
                //backward
                for (int i = 0; i < K; i++) {
                    b[i][T - 1] = 0;
                }
                for (int t = T - 2; t >= 0; t--) {
                    for (int i = 0; i < K; i++) {
                        double logbeta = Double.NaN;
                        for (int j = 0; j < K; j++) {
                            logbeta = elnsum(logbeta, elnproduct(eln(A[i][j]), elnproduct(eln(B[j][observations[t + 1]]), b[j][t + 1])));
                        }
                        b[i][t] = logbeta;
                    }
                }
                //update
                double[][] y = new double[K][T];
                double[][][] e = new double[K][K][T - 1];
                for (int t = 0; t < observations.length; t++) {
                    double normalizer = Double.NaN;
                    for (int i = 0; i < K; i++) {
                        y[i][t] = elnproduct(a[i][t], b[i][t]);
                        normalizer = elnsum(normalizer, y[i][t]);
                    }
                    for (int i = 0; i < K; i++) {
                        y[i][t] = elnproduct(y[i][t], -normalizer);
                    }
                }

                for (int t = 0; t < observations.length - 1; t++) {
                    double normalizer = Double.NaN;
                    for (int i = 0; i < K; i++) {
                        for (int j = 0; j < K; j++) {
                            e[i][j][t] = elnproduct(a[i][t], elnproduct(eln(A[i][j]), elnproduct(eln(B[j][observations[t + 1]]), b[j][t + 1])));
                            normalizer = elnsum(normalizer, e[i][j][t]);
                        }
                    }
                    for (int i = 0; i < K; i++) {
                        for (int j = 0; j < K; j++) {
                            e[i][j][t] = elnproduct(e[i][j][t], -normalizer);
                        }
                    }
                }

                for (int i = 0; i < K; i++) {
                    p[i] = (1 - lr) * p[i] + lr * eexp(y[i][1]);
                }

                for (int i = 0; i < K; i++) {
                    for (int j = 0; j < K; j++) {
                        double numerator = Double.NaN;
                        double denominator = Double.NaN;
                        for (int t = 0; t < observations.length - 1; t++) {
                            numerator = elnsum(numerator, e[i][j][t]);
                            denominator = elnsum(denominator, y[i][t]);
                        }
                        A[i][j] = (1 - lr) * A[i][j] + lr * eexp(elnproduct(numerator, -denominator));
                    }
                }
                for (int i = 0; i < K; i++) {
                    for (int j = 0; j < N; j++) {
                        double numerator = Double.NaN;
                        double denominator = Double.NaN;
                        for (int t = 0; t < observations.length; t++) {
                            if (j == observations[t]) {
                                numerator = elnsum(numerator, y[i][t]);
                            }
                            denominator = elnsum(denominator, y[i][t]);
                        }
                        B[i][j] = (1 - lr) * B[i][j] + lr * eexp(elnproduct(numerator, -denominator));
                    }
                }
                //System.out.println(A[0][1] + ",  " + B[0][2]+ ",  " + B[1][2]);
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Initial probabilities\n");
        sb.append(p[0]);
        sb.append(",");
        sb.append(p[1]);
        sb.append("\n");
        sb.append("Transition probabilities\n");
        sb.append(A[0][0]);
        sb.append(",");
        sb.append(A[0][1]);
        sb.append("\n");
        sb.append(A[1][0]);
        sb.append(",");
        sb.append(A[1][1]);
        sb.append("\n");
        sb.append("Emission probabilities\n");
        sb.append(B[0][0]);
        sb.append(",");
        sb.append(B[0][1]);
        sb.append(",");
        sb.append(B[0][2]);
        sb.append("\n");
        sb.append(B[1][0]);
        sb.append(",");
        sb.append(B[1][1]);
        sb.append(",");
        sb.append(B[1][2]);

        try (PrintWriter out = new PrintWriter(output)) {
            out.println(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static double eexp(double x) {
        if (Double.isNaN(x)) {
            return 0;
        } else {
            return Math.exp(x);
        }
    }

    public static double eln(double x) throws Exception {
        if (x == 0) {
            return Double.NaN;
        } else if (x > 0) {
            return Math.log(x);
        } else {
            throw new Exception();
        }
    }

    public static double elnsum(double x, double y) throws Exception {
        if (Double.isNaN(x) || Double.isNaN(y)) {
            if (Double.isNaN(x)) {
                return y;
            } else {
                return x;
            }
        } else {
            if (x > y) {
                return x + eln(1 + Math.exp(y - x));
            } else {
                return y + eln(1 + Math.exp(x - y));
            }
        }
    }

    public static double elnproduct(double x, double y) throws Exception {
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return Double.NaN;
        } else {
            return x + y;
        }
    }


}
