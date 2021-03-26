package promstudy.clustering;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class KMeansMatrices {

    public static int k = 20;
    public static int itNum = 10;
    public static int scale = 1;
    public static int maxSize = 1000;
    private static int seqLen = 15;

    public static ArrayList<RealMatrix> freqMatrix(ArrayList<RealMatrix> input) {
        RealMatrix s[] = new RealMatrix[input.size()];
        for (int i = 0; i < s.length; i++) {
            s[i] = input.get(i);
        }

        ArrayList<Integer> ki = new ArrayList<>();

        Random r = new Random(9025);
        while (ki.size() != k) {
            int rn = r.nextInt(s.length);
            if (!ki.contains(rn)) {
                ki.add(rn);
            }
        }

        ArrayList<Cluster> clusters = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            clusters.add(new Cluster(s[ki.get(i)]));
        }

        System.out.println();
        for (int i = 0; i < itNum; i++) {
            for (int j = 0; j < k; j++) {
                clusters.get(j).matrices.clear();
                clusters.get(j).dist = -1;
            }
            for (int j = 0; j < s.length; j++) {
                int c = closest(s[j], clusters);
                clusters.get(c).matrices.add(s[j]);
            }

            for (int j = k - 1; j >= 0; j--) {
                if (clusters.get(j).matrices.size() <= 1) {
                    clusters.remove(j);
                }
            }

            for (int j = 0; j < clusters.size(); j++) {
                clusters.get(j).mean = mean(clusters.get(j).matrices);
            }

            for (int c = 0; c < clusters.size(); c++) {
                System.out.print(clusters.get(c).matrices.size() + "  ");
            }
            System.out.println();
            int cs = clusters.size();
            for (int j = cs - 1; j >= 0; j--) {
                if (clusters.get(j).matrices.size() <= 1) {
                    clusters.remove(j);
                }
            }
            Collections.sort(clusters);
            int maxInd = clusters.size();
            int ind = 0;
            if(i < itNum - 1) {
                while (clusters.size() < k) {
                    if (clusters.get(ind % maxInd).matrices.size() > 2) {
                        clusters.add(new Cluster(clusters.get(ind % maxInd).getDistant()));
                    }
                    ind++;
                }
            }
        }


        for (int i = 0; i < clusters.size(); i++) {
            System.out.println("Cluster (" + i + ") size " + clusters.get(i).matrices.size());
        }
        ArrayList<RealMatrix> fms = new ArrayList<>();
        for (int c = 0; c < clusters.size(); c++) {
            RealMatrix fm = new Array2DRowRealMatrix(input.get(0).getRowDimension(), input.get(0).getColumnDimension());
            for (int i = 0; i < clusters.get(c).matrices.size() && i < maxSize; i++) {
                RealMatrix rm = clusters.get(c).matrices.get(i);
                fm = fm.add(rm);
            }
            fm = fm.scalarMultiply(1.0 / clusters.get(c).matrices.size());
            fms.add(fm);
        }
        return fms;
    }

    private static RealMatrix mean(ArrayList<RealMatrix> cluster) {
        RealMatrix mean = cluster.get(0);
        for (int i = 1; i < cluster.size(); i++) {
            mean.add(cluster.get(i));
        }
        mean.scalarMultiply(1.0 / cluster.size());

        double bestScore = Double.MAX_VALUE;
        int index = -1;
        for (int i = 0; i < cluster.size(); i++) {
            double score = score(mean, cluster.get(i));
            if (score < bestScore) {
                bestScore = score;
                index = i;
            }
        }
        mean = cluster.get(index);
        return mean;
    }

    private static int closest(RealMatrix m, ArrayList<Cluster> clusters) {
        double bestScore = Double.MAX_VALUE;
        int index = -1;
        for (int i = 0; i < clusters.size(); i++) {
            double score = score(clusters.get(i).mean, m);
            if (score < bestScore && clusters.get(i).matrices.size() < maxSize ) { //&& clusters.get(i).matrices.size() < 600
                bestScore = score;
                index = i;
            }
        }
        return index;
    }

    private static double score(RealMatrix mean, RealMatrix m) {
        double norm = m.subtract(mean).getFrobeniusNorm();

        return norm;
    }

    public static int findPopular(int[] a) {

        if (a == null || a.length == 0)
            return 0;

        Arrays.sort(a);

        int previous = a[0];
        int popular = a[0];
        int count = 1;
        int maxCount = 1;

        for (int i = 1; i < a.length; i++) {
            if (a[i] == previous)
                count++;
            else {
                if (count > maxCount) {
                    popular = a[i - 1];
                    maxCount = count;
                }
                previous = a[i];
                count = 1;
            }
        }

        return count > maxCount ? a[a.length - 1] : popular;

    }

}

class Cluster implements Comparable {
    public ArrayList<RealMatrix> matrices;
    public RealMatrix mean;
    public double dist = -1;

    public Cluster(RealMatrix mean) {
        this.mean = mean;
        matrices = new ArrayList<>();
        matrices.add(mean);
    }

    public double dist() {
        if (dist != -1) {
            return dist;
        }
        double sum = 0;
        for (RealMatrix m : matrices) {
            sum += m.subtract(mean).getFrobeniusNorm();
        }
        sum /= matrices.size();
        dist = sum;
        return sum;
    }

    @Override
    public int compareTo(Object o) {
        return Double.compare(((Cluster) o).matrices.size(), this.matrices.size());
    }

    public RealMatrix getDistant() {
        RealMatrix distant = null;
        double max = -Double.MAX_VALUE;
        for (RealMatrix m : matrices) {
            double n = m.subtract(mean).getFrobeniusNorm();
            if (n > max) {
                max = n;
                distant = m;
            }
        }
        matrices.remove(distant);
        return distant;
    }
}