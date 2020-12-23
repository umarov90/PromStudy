package promstudy.managers;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SNPs {
    public static void main(String[] a) {
        HashMap<String, SNP> snps = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Jumee\\Desktop\\ttt.tsv"))) {
            br.readLine();
            for (String line; (line = br.readLine()) != null; ) {
                try {
                    SNP snp = new SNP(line, true);
                    if(snp.use) {
                        snps.put(snp.id, snp);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
  /*      try (BufferedReader br = new BufferedReader(new FileReader("k562.matched.control.LP190708.txt.tsv"))) {
            br.readLine();
            for (String line; (line = br.readLine()) != null; ) {
                try {
                    SNP snp = new SNP(line);
                    snp.use = true;
                    snps.put(snp.id, snp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (BufferedReader br = new BufferedReader(new FileReader("hepg2.matched.control.LP190708.txt.tsv"))) {
            br.readLine();
            for (String line; (line = br.readLine()) != null; ) {
                try {
                    SNP snp = new SNP(line);
                    if (snps.containsKey(snp.id)) {
                        if (SNP.correlate(snps.get(snp.id), snp)) {
                            snps.get(snp.id).use = true;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        StringBuilder sb = new StringBuilder();
        Iterator it = snps.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            SNP snp = (SNP) pair.getValue();
            if (!snp.use) {
                it.remove();
            } else {
                sb.append(snp.chr + ", " + snp.pos + ", " + snp.ref + ", " + snp.alt + ", " + snp.inc  + ", " + (snp.altm - snp.refm) + "\n");
            }
        }
        try (PrintWriter out = new PrintWriter("SNP_all")) {
            out.print(sb.toString().trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println(snps.size());
    }
}
