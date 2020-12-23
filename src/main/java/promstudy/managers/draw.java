package promstudy.managers;

import promstudy.visualization.DataComponent;
import promstudy.visualization.Trend;
import promstudy.visualization.Trend2;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * Created by ramzan on 23/01/18.
 */
public class draw {
    static int step = 1;
    static int count = 1;
    static int w = 1050;
    static int h = 300;
    static int maxF = 20;

    public static void main(String[] args) {
        File setp = null;
        String output = null;
        int s = 0;
        if (args.length > 0) {
            try {
                for (int i = 0; i < args.length / 2; i++) {
                    String option = args[2 * i];
                    String parameter = args[2 * i + 1];
                    if (option.equals("-setp")) {
                        setp = new File(parameter);
                    } else if (option.equals("-out")) {
                        output = parameter;
                    } else if (option.equals("-w")) {
                        w = Integer.parseInt(parameter);
                    }else if (option.equals("-h")) {
                        h = Integer.parseInt(parameter);
                    } else if (option.equals("-count")) {
                        count = Integer.parseInt(parameter);
                    }  else if (option.equals("-maxf")) {
                        maxF = Integer.parseInt(parameter);
                    } else {
                        System.err.println("Unknown option: " + option);
                        System.err.println("Available Options: ");
                        System.err.println("-setp: predictions from PredSet.jar");
                        System.err.println("-out: output directory");
                        System.err.println("-w: width of one landscape");
                        System.err.println("-h: height of one landscape");
                        System.err.println("-count: number of graphs per picture ");
                        System.err.println("-maxf: maximum number of generated files (default 5000) ");
                        return;
                    }
                }
                if (setp != null) {
                    draw(setp, count, output);
                }
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    private static void draw(File setp, int params, String output) {
        ArrayList<double[]> arrays = new ArrayList<>();
        try {
            FileReader fileReader = new FileReader(setp);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains(",")) {
                    double[] arr = Stream.of(line.split(","))
                            .mapToDouble(Double::parseDouble)
                            .toArray();
                    arrays.add(arr);
                }
            }
            fileReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        File outDir = new File(output);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        Trend2[] trends = new Trend2[count];
        int ti = 0;
        for (int i = 0; i < arrays.size(); i++) {
            ArrayList<String> names = new ArrayList<>();
            names.add("");
            ArrayList<double[]> array1 = new ArrayList<>();
            array1.add(arrays.get(i));
            trends[ti++] = new Trend2(array1, names, step);
            if (ti >= count || i==arrays.size()-1) {
                ti = 0;
                try {
                    if((i / count + 1) > maxF){
                        return;
                    }
                    File out = new File(outDir.getAbsolutePath() + File.separator + "File " + (i / count + 1) + ".png");
                    saveComponents(trends, "png", out);
                } catch (Exception e) {

                }
            }
        }
    }

    private static void saveComponents(JComponent c[], String format, File outputfile) throws IOException {

        int cols = (int) Math.round(Math.sqrt((4.0 / 3.0) * count));
        int rows = (int)Math.ceil( (double)count / cols);
        int width =cols * w;
        int height = rows * h;
        BufferedImage myImage = null;
        BufferedImage result = new BufferedImage(
                width, height, //work these out
                BufferedImage.TYPE_INT_RGB);
        Graphics gg = result.getGraphics();

        ArrayList<BufferedImage> bis = new ArrayList<>();
        for(JComponent jc : c) {
            jc.setSize(new Dimension(w, h));
            jc.repaint();
            myImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = myImage.createGraphics();
            jc.paint(g);
            bis.add(myImage);
        }

        int x = 0;
        int y = 0;
        for (BufferedImage bi:bis) {
            gg.drawImage(bi, x, y, null);
            x += w;
            if (x >= result.getWidth()) {
                x = 0;
                y += h;
            }
        }


        ImageIO.write(result, format, outputfile);

    }

}
