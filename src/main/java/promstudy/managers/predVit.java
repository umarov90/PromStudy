package promstudy.managers;

import promstudy.common.posAndVal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Created by ramzan on 23/01/18.
 */
public class predVit {
    static int step = 1;
    static int patience = -1;

    public static void main(String[] args) {
        File setp = null;
        String params = null;
        String output = null;
        int s = 0;
        if (args.length > 0) {
            try {
                for (int i = 0; i < args.length / 2; i++) {
                    String option = args[2 * i];
                    String parameter = args[2 * i + 1];
                    if (option.equals("-setp")) {
                        setp = new File(parameter);
                    } else if (option.equals("-out")) {
                        output = parameter;
                    } else if (option.equals("-params")) {
                        params = parameter;
                    } else if (option.equals("-seq")) {
                        s = Integer.parseInt(parameter);
                    } else if (option.equals("-step")) {
                        step = Integer.parseInt(parameter);
                    } else if (option.equals("-patience")) {
                        patience = Integer.parseInt(parameter);
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
                if (setp != null) {
                    predVit2(setp, params, output);
                }
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private static void predVit(File setp, String params, String output) {
        int K = 2;
        int N = 2;
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
        double[][] A = new double[K][K];
        double[][] B = new double[K][N];
        double[] p = new double[2];
        try {
            FileReader fileReader = new FileReader(new File(params));
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            bufferedReader.readLine();
            line = bufferedReader.readLine();
            p[0] = Double.parseDouble(line.split(",")[0]);
            p[1] = Double.parseDouble(line.split(",")[1]);
            bufferedReader.readLine();
            line = bufferedReader.readLine();
            A[0][0] = Double.parseDouble(line.split(",")[0]);
            A[0][1] = Double.parseDouble(line.split(",")[1]);
            line = bufferedReader.readLine();
            A[1][0] = Double.parseDouble(line.split(",")[0]);
            A[1][1] = Double.parseDouble(line.split(",")[1]);
            bufferedReader.readLine();
            line = bufferedReader.readLine();
            B[0][0] = Double.parseDouble(line.split(",")[0]);
            B[0][1] = Double.parseDouble(line.split(",")[1]);
            line = bufferedReader.readLine();
            B[1][0] = Double.parseDouble(line.split(",")[0]);
            B[1][1] = Double.parseDouble(line.split(",")[1]);
            fileReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        //Viterbi
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
            double[][] T1 = new double[K][T];
            int[][] T2 = new int[K][T];

            for (int i = 0; i < K; i++) {
                T1[i][0] = p[i] * B[i][observations[0]];
            }
            for (int i = 1; i < observations.length; i++) {
                for (int j = 0; j < K; j++) {
                    double k1 = T1[0][i - 1] * A[0][j] * B[j][observations[i]];
                    double k2 = T1[1][i - 1] * A[1][j] * B[j][observations[i]];
                    T1[j][i] = Math.max(k1, k2);
                    if (k2 > k1) {
                        T2[j][i] = 1;
                    }
                }
            }
            int[] z = new int[T];
            if (T1[1][T - 1] > T1[0][T - 1]) {
                z[T - 1] = 1;
            }
            int[] x = new int[T];
            x[T - 1] = z[T - 1];
            for (int i = T - 1; i > 0; i--) {
                z[i - 1] = T2[z[i]][i];
                x[i - 1] = z[i - 1];
            }
            String st = "";
            for (int i = 0; i < x.length; i++) {
                if (x[i] == 1) {
                    st += i * step + ", ";
                }
            }
            if (st.endsWith(", ")) {
                st = st.substring(0, st.length() - 2);
            }
            sb.append(st);
            sb.append("\n");
        }
        try (PrintWriter out = new PrintWriter(output)) {
            out.println(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void predVit2(File setp, String params, String output) throws Exception {
        int K = 2;
        int N = 2;
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
        double[][] A = new double[K][K];
        double[][] B = new double[K][N];
        double[] p = new double[2];
        try {
            FileReader fileReader = new FileReader(new File(params));
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            bufferedReader.readLine();
            line = bufferedReader.readLine();
            p[0] = Double.parseDouble(line.split(",")[0]);
            p[1] = Double.parseDouble(line.split(",")[1]);
            bufferedReader.readLine();
            line = bufferedReader.readLine();
            A[0][0] = Double.parseDouble(line.split(",")[0]);
            A[0][1] = Double.parseDouble(line.split(",")[1]);
            line = bufferedReader.readLine();
            A[1][0] = Double.parseDouble(line.split(",")[0]);
            A[1][1] = Double.parseDouble(line.split(",")[1]);
            bufferedReader.readLine();
            line = bufferedReader.readLine();
            B[0][0] = Double.parseDouble(line.split(",")[0]);
            B[0][1] = Double.parseDouble(line.split(",")[1]);
            line = bufferedReader.readLine();
            B[1][0] = Double.parseDouble(line.split(",")[0]);
            B[1][1] = Double.parseDouble(line.split(",")[1]);
            fileReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();

        //Viterbi
        for (int ari = 0; ari < arrays.size(); ari++) {
            ArrayList<posAndVal> list = new ArrayList<>();
            double[] ar = arrays.get(ari);

            int T = ar.length;
            int[] observations = new int[ar.length];
            for (int i = 0; i < ar.length; i++) {
                if (ar[i] > 0.6) {
                    observations[i] = 1;
                } else {
                    observations[i] = 0;
                }
            }
            double[][] T1 = new double[K][T];
            int[][] T2 = new int[K][T];

            for (int i = 0; i < K; i++) {
                T1[i][0] = elnproduct(eln(p[i]), eln(B[i][observations[1]]));
            }
            for (int i = 1; i < observations.length; i++) {
                for (int j = 0; j < K; j++) {
                    double k1 = elnproduct(T1[0][i - 1], elnproduct(eln(A[0][j]), eln(B[j][observations[i]])));
                    double k2 = elnproduct(T1[1][i - 1], elnproduct(eln(A[1][j]), eln(B[j][observations[i]])));
                    T1[j][i] = maxe(k1, k2);
                    if (k2 > k1 || (Double.isNaN(k1) && !Double.isNaN(k2))) {
                        T2[j][i] = 1;
                    }
                    if(!Double.isNaN(k1) && !Double.isNaN(k2)) {
                        list.add(new posAndVal(i, k2 - k1));
                    }
                }
            }
            int[] z = new int[T];
            if (T1[1][T - 1] > T1[0][T - 1]) {
                z[T - 1] = 1;
            }
            int[] x = new int[T];
            x[T - 1] = z[T - 1];
            for (int i = T - 1; i > 0; i--) {
                z[i - 1] = T2[z[i]][i];
                x[i - 1] = z[i - 1];
            }
            String st = "";
            double max = -1;
            int maxI=-1;
            int c = 0;
            if(patience == -1){
                for (int i = 0; i < x.length; i++) {
                    if (x[i] == 1) {
                        st += i * step + "(" + ar[i]+"), ";
                    }
                }
            }else {
                for (int i = 0; i < x.length; i++) {
                    if (x[i] == 0) {
                        if (maxI != -1) {
                            if (c++ >= patience) {
                                st += maxI * step + "(" + max + "), ";
                                maxI = -1;
                                max = -1;
                                c = 0;
                            }
                        }
                    }else if (x[i] == 1) {
                        if(max<ar[i]){
                            max = ar[i];
                            maxI = i;
                            c = 0;
                        }
                    }
                }
                if(maxI!=-1){
                    st += maxI * step + "(" + max + "), ";
                }
            }
            if (st.endsWith(", ")) {
                st = st.substring(0, st.length() - 2);
            }
            sb.append(st);
            sb.append("\n");

            list.sort(new Comparator<posAndVal>() {
                @Override
                public int compare(posAndVal o1, posAndVal o2) {
                    return Double.compare(o2.value, o1.value);
                }
            });
            sb.append("Positions sorted by the probability that they are promoter region start:");
            sb.append("\n");
            String prefix = "";
            for(int i = 0;i<20; i++){
                sb.append(prefix);
                prefix = ", ";
                sb.append(list.get(i).position * step + 1);
            }

            sb.append("\n");
            sb.append("\n");
        }

        try (PrintWriter out = new PrintWriter(output)) {
            out.println(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static double maxe(double k1, double k2) {
        if (Double.isNaN(k1) && Double.isNaN(k2)) {
            return Double.NaN;
        } else {
            if (Double.isNaN(k1)) {
                return k2;
            } else if (Double.isNaN(k2)) {
                return k1;
            } else {
                return Math.max(k1, k2);
            }
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
