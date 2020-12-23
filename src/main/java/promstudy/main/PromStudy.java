package promstudy.main;

import promstudy.common.FastaParser;
import promstudy.managers.Predictor;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class PromStudy {
    private static Predictor p;
    private static float[][][] sequences;
    private static int step = 1;
    private static int sLen = 1500;

    public static void main(String[] args) {
        File toPred = null;
        String range=null;
        String output=null;
        if (args.length > 0) {
            try {
                for (int i = 0; i < args.length / 2; i++) {
                    String option = args[2 * i];
                    String parameter = args[2 * i + 1];
                    if (option.equals("-set")) {
                        toPred = new File(parameter);
                    } else if (option.equals("-mod")) {
                        p = new Predictor(parameter);
                    } else if (option.equals("-out")) {
                       output = parameter;
                    } else if (option.equals("-range")) {
                        range = parameter;
                    }else if (option.equals("-step")) {
                        step = Integer.parseInt(parameter);
                    }else if (option.equals("-sl")) {
                        sLen = Integer.parseInt(parameter);
                    } else {
                        System.err.println("Unknown option: " + option);
                        System.err.println("Available Options: ");
                        System.err.println("-set: file with long sequences");
                        System.err.println("-range: range of sequences to analyze");
                        System.err.println("-mod: location of trained model file");
                        System.err.println("-step: step for sliding window (default is 1)");
                        System.err.println("-out: output file with scores");
                        return;
                    }
                }
                if (toPred != null) {
                    sequences = FastaParser.parse(toPred);
                    analyse(range, output);
                }
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        try {
            //s.positive = FastaParser.parse(new File("data/small_pos.seq"));
            //s.negative = FastaParser.parse(new File("data/small_neg.seq"));
            //s.sequences = FastaParser.parse(new File("data/ara2K_200.fa_2"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void analyse(String range, String output) {
        StringBuilder sb = new StringBuilder();
        int c = 0;
        for (int i = 0; i < sequences.length; i++) {
            float[][] seq = sequences[i];
            int total = (int) Math.ceil((seq.length - sLen) / step) + 1;
            float[][][] toPredict = new float[total][sLen][4];
            for (int j = 0; j < toPredict.length; j++) {
                toPredict[j] = Arrays.copyOfRange(seq, j * step, j * step + sLen);
            }
            float[] r = p.predict(toPredict);
            sb.append("Sequence " + (i + 1)+"\n");
            for (int j = 0; j<r.length; j++) {
                float f = r[j];
                sb.append(f);
                sb.append(", ");
            }
            sb.delete(sb.length()-2, sb.length());
            sb.append("\n");
            if(i%10==0) {
                System.out.print(i + " - ");
            }
        }
        try(  PrintWriter out = new PrintWriter( output )  ){
            out.println( sb.toString() );
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
