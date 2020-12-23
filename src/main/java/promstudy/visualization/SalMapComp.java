package promstudy.visualization;

import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SalMapComp extends DataComponent {

    private int maxCount;
    private double max, min;
    private double[] array;
    private double wStep;
    private int offset;
    public static boolean thick = false;
    private static Map<String, Color> customColors;

    public SalMapComp(double[] array) {
        super("trend");
        this.array = array;

        max = -Double.MAX_VALUE;
        min = Double.MAX_VALUE;
        for (double d : array) {
            if (d > max) {
                max = d;
            }
            if (d < min) {
                min = d;
            }
        }

        customColors = new HashMap<>();
        customColors.put("A", Color.decode("#228B22"));
        customColors.put("T",Color.decode("#e62200")  );
        customColors.put("G", Color.ORANGE);
        customColors.put("C",Color.decode("#00008B"));
    }


    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        margin = 240;
        Graphics2D g2d = (Graphics2D) g;
        height = getHeight();
        width = getWidth();
        g2d.setRenderingHints(renderHints);
        int fs = 20;
        Font origFont = new Font("Arial", Font.PLAIN, fs);
        g2d.setFont(origFont);

        AffineTransform affineTransform = new AffineTransform();
        affineTransform.rotate(Math.toRadians(45), 0, 0);
        Font rotatedFont = origFont.deriveFont(affineTransform);


        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, width, height);
        g2d.setColor(textColor);
        step = 40;
        double maxL = -Double.MAX_VALUE;
        int maxI = 0;
        height = 0;

        height+=6*step+100;
        for (int i = 0; i < array.length; i++) {

/*            if((i/4)%50==0 && i%4==0){
                height+=6*step;
                String[] ls = new String[]{"A", "T", "G", "C"};
                for (int j = 0; j < 4; j++) {
                    g2d.setColor(Color.BLACK);
                    //g2d.drawString(ls[j], + fs/2, height - (4*step - step*(j%4)) -fs/2 );
                }
                ii=0;
            }*/
            if(array[i]>maxL){
                maxL =array[i];
                maxI = i;
            }
            if((((i+1) / 4)) == 149){
                int ffff = 2+2;
            }
            if(i%4==3){
                String l = "A";
                if(maxI%4==1){
                    l = "T";
                }else  if(maxI%4==2){
                    l = "G";
                }else  if(maxI%4==3){
                    l = "C";
                }
                g2d.setColor(Color.BLACK);





                //g2d.drawString(l, step + (ii/4) * step+ fs/2 + 3, height - 5*step -fs/2);

                //if((((i+1) / 4)) >= 150 && (((i+1) / 4)) <= 251) {
                    g2d.drawString("" + ((i / 4) + 1), step + (i / 4) * step + 19 - 5 * ("" + ((i / 4) + 1)).length(), height - fs / 2);
                    g2d.setFont(new Font("Verdana", Font.PLAIN, 1024));
                    CharacterImageGenerator characterGenerator = new CharacterImageGenerator(g2d.getFontMetrics(), customColors.get(l));

                    Image img = characterGenerator.getImage(l.charAt(0));
                    //new CharacterImageGenerator(g2d.getFontMetrics(), customColors.get(l)).getImage(l.charAt(0))
                    int w = 40;
                    int h = 1 + (int) (120*array[maxI]/max);
                    if(array[maxI] >0 && h>6){
                        BufferedImage bimage = toBufferedImage(img);
                        //BufferedImage img2 = resizeImageWithHint(bimage, w, h);

                        BufferedImage img2;

                        img2 = Scalr.resize(bimage, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_EXACT,
                                w, h, Scalr.OP_ANTIALIAS);


                        g2d.drawImage(img2, step + (i/4) * step, height - 5*step -2 - h, w, h, null);
                    }

                    g2d.setFont(origFont);
                //}
                maxL = -Double.MAX_VALUE;
                maxI = 0;
            }
            if(array[i]>0){
                g2d.setColor(colors[0]);
                g2d.setColor(new Color(g2d.getColor().getRed(), g2d.getColor().getGreen(), g2d.getColor().getBlue(), (int)Math.floor(255*(array[i]/max))));
            }else{
                g2d.setColor(colors[1]);
                g2d.setColor(new Color(g2d.getColor().getRed(), g2d.getColor().getGreen(), g2d.getColor().getBlue(), (int)Math.floor(255*(Math.abs(array[i]/min)))));
            }

            g2d.fillRect(step + (i/4) * step, height - (5*step - step*(i%4)), step, step);

            //g2d.setFont(new Font("Arial", Font.PLAIN, 9));
            //g2d.setColor(Color.BLACK);
            //String result = String.format("%.4f", array[i]);
            //g2d.drawString(result, step + (i/4) * step, height - (5*step - step*(i%4)));
            //g2d.setFont(origFont);
        }
    }

    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        BufferedImage bimage = new BufferedImage(img.getWidth(null),
                img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();
        return bimage;
    }
}
