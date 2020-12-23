package promstudy.managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;

public class temp {
    public static void main(String[] a) {
        ArrayList<Integer> cage = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Jumee\\Desktop\\cage.tsv"))) {
            for (String line; (line = br.readLine()) != null; ) {
                cage.add(Integer.parseInt(line));
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        double c = 0;
        double e = 0;
        ArrayList<Integer> cageu = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("C:\\Users\\Jumee\\Desktop\\chr1_stb.txt"))) {
            for (String line; (line = br.readLine()) != null; ) {
                Scanner scan = new Scanner(line);
                if(scan.hasNextInt()){
                    int p = scan.nextInt();
                    boolean found = false;
                    for(Integer m: cage){
                        if(Math.abs(m-p)<500){
                            found = true;
                            break;
                        }
                    }
                    if(found && !cageu.contains(p)){
                        c++;
                        cageu.add(p);
                    }
                    if(!found){
                        e++;
                    }
                }
            }
        }catch(Exception ex){
            ex.printStackTrace();
        }
        System.out.println(c);
        System.out.println(e);
        System.out.println(c/cage.size());
    }
}
