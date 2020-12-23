package promstudy.clustering;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

public class KMeans {

    public static int k = 3;
    public static int itNum = 100;
    public static int scale = 1;
    public static int maxSize = 5000000;
    private static int seqLen = 10;

    public static ArrayList<RealMatrix> freqMatrix(ArrayList<Sequence> input, int tss) {
        seqLen = input.get(0).seq.length;
        Collections.sort(input);
        //Collections.reverse(input);
        System.out.println(input.size());
        input.subList((int) (0.05 * input.size()), input.size()).clear();
        System.out.println(input.size());
        Sequence s[] = new Sequence[input.size()];
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

        ArrayList<SequenceCluster> clusters = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            clusters.add(new SequenceCluster(s[ki.get(i)]));
        }

        System.out.println();
        for (int i = 0; i < itNum; i++) {
            for (int j = 0; j < k; j++) {
                clusters.get(j).sequences.clear();
                clusters.get(j).dist = -1;
            }
            for (int j = 0; j < s.length; j++) {
                int c = closest(s[j], clusters);
                if(c!=-1) {
                    clusters.get(c).sequences.add(s[j]);
                }else{
                    clusters.get(0).sequences.add(s[j]);
                }
            }

            for (int j = k - 1; j >= 0; j--) {
                if (clusters.get(j).sequences.size() <= 1) {
                    clusters.remove(j);
                }
            }

            for (int j = 0; j < clusters.size(); j++) {
                clusters.get(j).mean = mean(clusters.get(j).sequences);
            }

            for (int c = 0; c < clusters.size(); c++) {
                System.out.print(clusters.get(c).sequences.size() + "  ");
            }
            System.out.println();
            int cs = clusters.size();
            for (int j = cs - 1; j >= 0; j--) {
                if (clusters.get(j).sequences.size() <= 1) {
                    clusters.remove(j);
                }
            }
            Collections.sort(clusters);
            int maxInd = clusters.size();
            int ind = 0;
            if (i < itNum - 1) {
                while (clusters.size() < k) {
                    if (clusters.get(ind % maxInd).sequences.size() > 2) {
                        clusters.add(new SequenceCluster(clusters.get(ind % maxInd).getDistant()));
                    }
                    ind++;
                }
            }
        }
        // Reduce the cluster 2 times by removing the distant
        for (int i = 0; i < clusters.size(); i++) {
            int n = (int)(0.5 * clusters.get(i).sequences.size());
            /*if(n < 50000) {
                for (int j = 0; j < n; j++) {
                    clusters.get(i).getDistant();
                    if (j % 50 == 0) {
                        clusters.get(i).mean = mean(clusters.get(i).sequences);
                    }
                }
            }*/
            double score = 0;
            for (int j = 0; j < clusters.get(i).sequences.size(); j++) {
                score += clusters.get(i).sequences.get(j).v;
            }
            score /= clusters.get(i).sequences.size();
            System.out.println("Cluster (" + (i) + ") size " + clusters.get(i).sequences.size() + " effect " + score
                    + " location " + getPosition(tss, clusters.get(i).sequences));
        }
        // bestCluster = Integer.parseInt(JOptionPane.showInputDialog("123"));
        ArrayList<RealMatrix> fms = new ArrayList<>();
        for (int c = 0; c < clusters.size(); c++) {
            RealMatrix fm = new Array2DRowRealMatrix(4, seqLen);
            for (int i = 0; i < clusters.get(c).sequences.size() && i < maxSize; i++) {
                Sequence sequence = clusters.get(c).sequences.get(i);
                for (int j = 0; j < seqLen; j++) {
                    if (sequence.seq[j] > 0) {
                        fm.addToEntry(sequence.seq[j] - 1, j, 1); //Math.pow(sequence.v, scale)
                    }
                }
            }
            fms.add(fm);
        }
        return fms;
    }

    private static Sequence mean(ArrayList<Sequence> cluster) {
        int count[][] = new int[seqLen][cluster.size()];
        double p = 0;
        for (int i = 0; i < cluster.size(); i++) {
            p += cluster.get(i).p;
            for (int j = 0; j < seqLen; j++) {
                count[j][i] = cluster.get(i).seq[j];
            }
        }
        int mean[] = new int[seqLen];
        for (int i = 0; i < seqLen; i++) {
            mean[i] = findPopular(count[i]);
        }
        return new Sequence(mean, 0, (int) (p / cluster.size()));
    }

    private static int closest(Sequence is, ArrayList<SequenceCluster> clusters) {
        double bestScore = -1;
        int index = -1;
        for (int i = 0; i < clusters.size(); i++) {
            double score = 0;
            if (Math.abs(clusters.get(i).mean.p - is.p) < 50) {
                score = score(clusters.get(i).mean, is.seq);
            }
            if (score > bestScore && score >= 3) {
                bestScore = score;
                index = i;
            }
        }
        return index;
    }

    private static int score(Sequence mean, int[] is) {
        int count = 0;
        for (int i = 0; i < mean.seq.length; i++) {
            if (mean.seq[i] == is[i] && mean.seq[i] != 0) {
                count++;
            }
        }
        return count;
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

    public static String getPosition(int tss, ArrayList<Sequence> cluster) {
        int min = Integer.MAX_VALUE;
        int max = -Integer.MAX_VALUE;
        for (Sequence p : cluster) {
            if (p.p > max) {
                max = p.p;
            }
            if (p.p < min) {
                min = p.p;
            }
        }
        String l = "";
        if (min < tss) {
            l = "[" + (min - tss);
        } else {
            l = "[+" + (min - tss + 1);
        }

        String r = "";
        if (max < tss) {
            r = (max - tss) + "]";
        } else {
            r = "+" + (max - tss + 1) + "]";
        }
        return l + " : " + r;
    }
}

class SequenceCluster implements Comparable {
    public ArrayList<Sequence> sequences;
    public Sequence mean;
    public double dist = -1;

    public SequenceCluster(Sequence mean) {
        this.mean = mean;
        sequences = new ArrayList<>();
        sequences.add(mean);
    }

    public double dist() {
        if (dist != -1) {
            return dist;
        }
        double sum = 0;
        for (Sequence m : sequences) {
            int e = 0;
            for (int i = 0; i < m.seq.length; i++) {
                if (m.seq[i] != mean.seq[i]) {
                    e++;
                }
            }
            sum += e;
        }
        sum /= sequences.size();
        dist = sum;
        return sum;
    }

    @Override
    public int compareTo(Object o) {
        return Double.compare(((SequenceCluster) o).dist(), this.dist());
    }

    public Sequence getDistant() {
        Sequence distant = null;
        double max = -Double.MAX_VALUE;
        for (Sequence m : sequences) {
            int e = 0;
            for (int i = 0; i < m.seq.length; i++) {
                if (m.seq[i] != mean.seq[i]) {
                    e++;
                }
            }
            if (e > max) {
                max = e;
                distant = m;
            }
        }
        sequences.remove(distant);
        return distant;
    }
}