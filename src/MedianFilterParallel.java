import java.util.Arrays;
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;


public class MedianFilterParallel {
    static String inputImageName, outputImageName;

    static BufferedImage bufferedImage = null;
    static BufferedImage bufferedMeanImage = null;
    static File file = null;

    static int windowWidth;
    static int width, height;

    static int [][] pixelValues, meanPixelValues;

    static int [] alphaRGB, medianAlphaRgb, alphaRgb;

    static void findMedian() {
        meanPixelValues = new int [height][width];

        for (int i=0; i<height; i++) {
            for (int j=0; j<width; j++) {
                meanPixelValues[i][j] = medianFunction(i, j);
            }
        }
    }

    static int [][] getPixels() {

        //read image
        try{
            file = new File(inputImageName);
            bufferedImage = ImageIO.read(file);
        }catch(IOException e){
            //System.out.println(e);
            System.out.println("File not found");
            return null;
        }
        width = bufferedImage.getWidth();
        height = bufferedImage.getHeight();

        int [][] pixels = new int [height][width];
        for (int i=0; i<height; i++) {
            for (int j=0; j<width; j++) {
                pixels[i][j] = bufferedImage.getRGB(j, i);
            }
        }
        return pixels;
    }

    static int [] getRGB(int j, int i) {
        alphaRGB = new int [4];
        int p = pixelValues[i][j];
        alphaRGB[0] = (p>>24) & 0xff;
        alphaRGB[1] = (p>>16) & 0xff;
        alphaRGB[2] = (p>>8) & 0xff;
        alphaRGB[3] = p & 0xff;
        return alphaRGB;
    }

    static int medianFunction(int i, int j) {
        int meanRadius = (windowWidth-1)/2;
        medianAlphaRgb = new int [4];


        for (int k = i-meanRadius; k <= i+meanRadius; k++) {
            for (int m=j-meanRadius; m<=j-meanRadius; m++) {
                if ((m>=0 && m<width) && (k>=0 && k<height)) {
                    alphaRgb = getRGB(m, k);
                    Arrays.sort(alphaRgb);
                    medianAlphaRgb[0] = alphaRgb[0];
                    medianAlphaRgb[1] = alphaRgb[1];
                    medianAlphaRgb[2] = alphaRgb[2];
                    medianAlphaRgb[3] = alphaRgb[3];

                }
            }
        }



        int p = (medianAlphaRgb[0]<<24) | (medianAlphaRgb[1]<<16) | (medianAlphaRgb[2]<<8) | medianAlphaRgb[3];

        return p;
    }

    static boolean outputImage() {
        bufferedMeanImage = new BufferedImage(width, height, bufferedImage.getType());
        for (int i=0; i<height; i++) {
            for (int j=0; j<width; j++) {
                bufferedMeanImage.setRGB(j, i, meanPixelValues[i][j]);
            }
        }

        try{
            file = new File(outputImageName);
            return ImageIO.write(bufferedMeanImage, "jpeg", file);
        }catch(IOException e){
            // System.out.println(e);
            System.out.println("Unable to write to file");
            return false;
        }
    }

    public static void main(String[] args) {
        inputImageName  = args[0];
        outputImageName = args[1];
        windowWidth     = Integer.parseInt(args[2]);

        pixelValues = getPixels();

        long initial = System.currentTimeMillis();
        findMedian();
        long difference = System.currentTimeMillis() - initial;

        System.out.println("Time lapsed is "+difference+" milliseconds");

        outputImage();

        // open image, save pix as int [][]
    }
}