package promstudy.common;

public class Element implements Comparable<Element> {

    public int index;
    public float value;

    public Element(int index, float value){
        this.index = index;
        this.value = value;
    }

    public int compareTo(Element e) {
        return Float.compare(this.value, e.value);
    }
}
