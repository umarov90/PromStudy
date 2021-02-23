package promstudy.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

public class FastaParser {

    public static float[][][] parse(File f) throws FileNotFoundException {
        String s = new Scanner(f).useDelimiter("\\Z").next();
        String[] sp = s.split(">.*(\r?\n|\r)");
        int sLen = sp[1].replaceAll("\\s+", "").length();
        float[][][] result = new float[sp.length - 1][sLen][4];
        //skip first element (empty)
        for (int i = 0; i < result.length; i++) {
            result[i] = encode(sp[i + 1]);
        }
        return result;
    }

    public static Object[] parse(File f, int minSize) throws FileNotFoundException {
        ArrayList<float[][]> fa = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        Scanner s = new Scanner(f);
        StringBuilder seq = new StringBuilder();
        String name = "";
        while(s.hasNextLine()){
            String line = s.nextLine();
            if(line.startsWith(">")){
                if(seq.length()>=minSize){
                    boolean add = true;
//                    String[] info = name.trim().split("\\s*,\\s*");
//
//                    int pos = Integer.parseInt(info[1]) - 1 - (Integer.parseInt(info[6]) - 500);
//                    if(!MutationMap.goodSNP.contains(Integer.parseInt(info[1]))){
//                        //add = false;
//                    }
//                    if(!name.contains("chr1,")){
//                        add = false;
//                    }
//                    if (pos < 450 || pos > 551) {
//                        add = false;
//                    }
//                    if (Math.abs(Double.parseDouble(info[5])) < 10.0) {
//                        //add = false;
//                    }
                    if(add) {
                        fa.add(encode(seq.toString()));
                        names.add(name);
                    }
                }
                name = line;
                seq = new StringBuilder();
                if(fa.size() > 30000){
                    break;
                }
            }else {
                seq.append(line);
            }
        }
        if(seq.length()>=minSize){
            fa.add(encode(seq.toString()));
            names.add(name);
        }
        long seed = System.nanoTime();
        Collections.shuffle(fa, new Random(seed));
        Collections.shuffle(names, new Random(seed));
        float[][][] result = new float[fa.size()][][];
        for(int i =0; i<fa.size(); i++){
            result[i] = fa.get(i);
        }
        return new Object[]{result, names};
    }


    private static float[][] encode(String s) {
        s = s.replaceAll("\\s+", "").toUpperCase();
        float[][] result = new float[s.length()][4];
        for (int i = 0; i < result.length; i++) {
            if (s.charAt(i) == 'A') {
                result[i][0] = 1;
            } else if (s.charAt(i) == 'C') {
                result[i][1] = 1;
            } else if (s.charAt(i) == 'G') {
                result[i][2] = 1;
            } else if (s.charAt(i) == 'T') {
                result[i][3] = 1;
            } else {
                result[i][0] = 0;
            }
        }
        return result;
    }

    public static String reverse(float[][] floats) {
        StringBuilder sb = new StringBuilder();
        for(int i =0; i<floats.length; i++){
            if (floats[i][0] == 1) {
                sb.append("A");
            } else if (floats[i][1] == 1) {
                sb.append("C");
            } else if (floats[i][2] == 1) {
                sb.append("G");
            } else if (floats[i][3] == 1) {
                sb.append("T");
            }else {
                sb.append("N");
            }
        }
        return sb.toString();
    }

    public static String toString(ArrayList<float[][]> seqs) {
        StringBuilder sb = new StringBuilder();
        for (float[][] s : seqs) {
            sb.append(">\n");
            sb.append(FastaParser.reverse(s));
            sb.append("\n");
        }
        return sb.toString();
    }
}
