package promstudy.managers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class HG {
    public static void main(String[] a) {
        checkNegs();
    }

    private static void checkHG() {
        long n = 0;
        boolean rec = false;
        StringBuilder sb = new StringBuilder();
        ArrayList<String> chrs = new ArrayList<>();
        //"C:\\Users\\ramzan\\Downloads\\hg38.fa"
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\ramzan\\Downloads\\hg38.fa"))) {
            for (String line; (line = br.readLine()) != null; ) {
                if (line.startsWith(">c") && rec) {
                    break;
                }
                n++;
                if(rec){
                    sb.append(line);
                }
                if (line.startsWith(">chr5")) {
                    rec = true;
                }
            }
        } catch (Exception e) {

        }
        String chr5 = sb.toString().replaceAll("\\s+","");
        System.out.println(chr5.length());
        System.out.println(chr5.substring(218313, 218333));
        /*try (PrintWriter out = new PrintWriter("chr5.fa")) {
            out.println(sb.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }*/
    }
    private static void checkNegs() {
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\ramzan\\Desktop\\negs_new.txt"))) {
            for (String line; (line = br.readLine()) != null; ) {
               System.out.println(line.length());
            }
        } catch (Exception e) {

        }

    }
    private static void checkCAGE() {
        int proms = 0;
        int prev = -1;
        String chr = null;
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\ramzan\\Downloads\\F5_CAGE_anno.GENCODEv27_hg38.cage_cluster.coord.bed"))) {
            for (String line; (line = br.readLine()) != null; ) {
                String[] v = line.split("\t");
                if (v[5].equals("+")) {
                    if (chr == null || !chr.equals(v[0])) {
                        chr = v[0];
                        proms++;
                        prev = Integer.parseInt(v[6]);
                    } else if (Math.abs(prev - Integer.parseInt(v[6])) > 15000) {
                        proms++;
                        prev = Integer.parseInt(v[6]);
                    }
                }
            }
        } catch (Exception e) {

        }
        System.out.println(proms);
    }

    private static void checkCHR() {
        ArrayList<Integer> chr1 = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader("chr1_gt+"))) {
            for (String line; (line = br.readLine()) != null; ) {
                chr1.add(Integer.parseInt(line));
            }
        } catch (Exception e) {

        }
        double tp = 0;
        double e = 0;
        double n = 0;
        ArrayList<Integer> preds = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("chr1.out"))) {
            for (String line; (line = br.readLine()) != null; ) {
                if (line.startsWith("Position")) {
                    int p = Integer.parseInt(line.split("\\s+")[1]);
                    preds.add(p);
                    boolean c = false;
                    for(Integer i : chr1){
                        if(Math.abs(p - i) < 600){
                            c = true;
                            break;
                        }
                    }
                    if(c){
                        tp++;
                    }else{
                        e++;
                    }
                    n++;
                }
            }
        } catch (Exception ex) {

        }
        System.out.println(tp);
        System.out.println(n);
        System.out.println(tp/n);
        int m = 0;
        for(Integer pr: preds){
            if(pr < 629208){
                m++;
            }
        }
        for(Integer d: chr1){
            boolean notFound = true;
            for(Integer pr: preds){
                if(Math.abs(pr - d) < 600){
                    notFound = false;
                    break;
                }
            }
            if(notFound){
                e++;
            }
        }
        System.out.println(e + " - " + chr1.size() + " - " + e/chr1.size());
    }
}
