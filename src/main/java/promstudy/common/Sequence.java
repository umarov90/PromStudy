package promstudy.common;

public class Sequence {
    public String name;
    public float[][] sequence;

    public Sequence(String name, float[][] sequence){
        this.name = name;
        this.sequence = sequence;
    }

    public String toString(){
        return ">" + name + "\n" + FastaParser.reverse(sequence);
    }
}
