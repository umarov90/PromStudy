package promstudy.managers;

public class SNP {
    public boolean use;
    public String id;
    public String chr;
    public int pos;
    public String ref;
    public String alt;
    public double refm;
    public double altm;
    public boolean inc;

    public SNP(String line) {
        String[] values = line.split("\t");
        chr = values[0];
        id = values[1];
        pos = Integer.parseInt(values[2]);
        refm = Double.parseDouble(values[5]);
        altm = Double.parseDouble(values[6]);
        ref = values[9];
        alt = values[10];
        if(altm > refm){
            inc = true;
        }
    }

    public SNP(String line, boolean b) {
        String[] values = line.split("\t");
        chr = values[0];
        id = values[1];
        pos = Integer.parseInt(values[2]);
        double refm1 = Double.parseDouble(values[5]);
        double altm1 = Double.parseDouble(values[6]);
        double refm2 = Double.parseDouble(values[9]);
        double altm2 = Double.parseDouble(values[10]);
        if( (altm1 > refm1 && altm2 > refm2) || (altm1 < refm1 && altm2 < refm2)){
            use = true;
        }
        ref = values[13];
        alt = values[14];
        altm = (altm1 + altm2)/2;
        refm = (refm1 + refm2)/2;
        if(altm > refm){
            inc = true;
        }
    }

    public static boolean correlate(SNP snp, SNP snp1) {
        if(snp.inc == snp1.inc){
            if(snp.alt.equals(snp1.alt) && snp.ref.equals(snp1.ref)){
                return  true;
            }
        }
        return false;
    }
}
