package promstudy.common;

public class Element2 implements Comparable<Element2> {

    public int index;
    public float value;
    public boolean p;

    public Element2(int index, float value, boolean p){
        this.index = index;
        this.value = value;
        this.p = p;
    }

    public int compareTo(Element2 e) {
        return Float.compare(this.value, e.value);
    }
}
