package promstudy.common;

public class ClassAndValue implements Comparable{
    public double value;
    public int classIndex;
    public ClassAndValue(int classIndex, double value){
        this.classIndex=classIndex;
        this.value=value;
    }
    public int compareTo(Object o) {
        if (o instanceof ClassAndValue) {
                ClassAndValue t = (ClassAndValue) o;
                if (this.value >t.value)
                    return 1;
                else if (this.value <t.value)
                    return -1;
            }
            return 0;
    }

}

