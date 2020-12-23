package promstudy.common;

public class Element3 implements Comparable<Element3> {

    public String seq;
    public double value;

    public Element3(String seq, double value){
        this.seq = seq;
        this.value = value;
    }

    public int compareTo(Element3 e) {
        return Double.compare(this.value, e.value);
    }
}
