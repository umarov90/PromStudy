package promstudy.managers;

import promstudy.visualization.Trend;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * Created by ramzan on 23/01/18.
 */
public class draw2 {
    static int step = 15;
    static int count = 10;
    static int w = 1200;
    static int h = 900;
    static int maxF = 10;

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
                    } else if (option.equals("-count")) {
                        count = Integer.parseInt(parameter);
                    } else if (option.equals("-step")) {
                        step = Integer.parseInt(parameter);
                    } else if (option.equals("-maxf")) {
                        maxF = Integer.parseInt(parameter);
                    } else {
                        System.err.println("Unknown option: " + option);
                        System.err.println("Available Options: ");
                        System.err.println("-setp: predictions from PredSet.jar");
                        System.err.println("-count: number of graphs per trend ");
                        System.err.println("-step: step for sliding window (default is 1)");
                        System.err.println("-out: output directory");
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
        Trend[] trends = new Trend[count];
        int ti = 0;
        ArrayList<double[]> array1 = new ArrayList<>();
        ArrayList<String> names = new ArrayList<>();
        for (int i = 0; i < 4; i++) {

            names.add("");

            int t = (int) (arrays.get(i).length/step);
            double [] toAdd = new double[t];
            for(int j = 0; j<toAdd.length; j++){
                toAdd[j] = arrays.get(i)[j*step];
            }
            array1.add(toAdd);
            trends[0] = new Trend(array1, names, step);
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
        File out = new File(outDir.getAbsolutePath() + File.separator + "Filex.png");
        try {
            JComponent jc = trends[0];
            jc.setSize(new Dimension(w, h));
            jc.repaint();
            BufferedImage myImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = myImage.createGraphics();
            jc.paint(g);
            BufferedImage result = new BufferedImage(
                    w, h, //work these out
                    BufferedImage.TYPE_INT_RGB);
            Graphics gg = result.getGraphics();
            gg.drawImage(myImage, 0, 0, null);
            ImageIO.write(result, "png", out);
        } catch (Exception e) {
            e.printStackTrace();
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
