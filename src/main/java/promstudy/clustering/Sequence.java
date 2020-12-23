package promstudy.clustering;

import java.util.ArrayList;

public class Sequence implements Comparable {
    public Double sim;
    public int[] seq;
    public double v;
    public int p;

    public Sequence(int[] seq, double v, int p) {
        this.seq = seq;
        this.v = v;
        this.p = p;
    }

    public String toString() {
        String s = "";
        ArrayList<String> letters = new ArrayList<>();
        letters.add("N");
        letters.add("A");
        letters.add("T");
        letters.add("G");
        letters.add("C");
        for(int i =0; i< seq.length; i++){
            s+=letters.get(seq[i]);
        }
        return s;
    }

    @Override
    public int compareTo(Object o) {
        return Double.compare(this.v, ((Sequence) o).v);
    }
}
