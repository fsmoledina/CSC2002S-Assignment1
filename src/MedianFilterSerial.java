import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.*;
import java.util.*;

public class MedianFilterSerial {
    public static void main(String[] args)throws IOException {
        BufferedImage img = null;
        File f = null;

        //get image width and height
        int width = img.getWidth();
        int height = img.getHeight();

        //get pixel value
        int p = img.getRGB(0,0);

        //get alpha
        int a = (p>>24) & 0xff;

        //get red
        int r = (p>>16) & 0xff;

        //get green
        int g = (p>>8) & 0xff;

        //get blue
        int b = p & 0xff;

        a = 255;
        r = 100;
        g = 150;
        b = 200;

        //set the pixel value
        p = (a<<24) | (r<<16) | (g<<8) | b;
        img.setRGB(0, 0, p);


    }//main() ends here
}//class ends here