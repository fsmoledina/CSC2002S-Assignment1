
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class MeanFilterSerial {
    static String inputImageName, outputImageName;

    static BufferedImage bufferedImage = null;
    static BufferedImage bufferedMeanImage = null;
    static File file = null;

    static int windowWidth;
    static int width, height;

    static int [][] pixelValues, meanPixelValues;

    static int [] alphaRGB, sumAlphaRgb, alphaRgb;

    static void calculateMeans() {
        meanPixelValues = new int [height][width];

        for (int i=0; i<height; i++) {
            for (int j=0; j<width; j++) {
                meanPixelValues[i][j] = meanFunction(i, j);
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

    static int meanFunction(int i, int j) {
        int meanRadius = (windowWidth-1)/2;
        sumAlphaRgb = new int [4];

        int count=0;
        for (int k = i-meanRadius; k <= i+meanRadius; k++) {
            for (int m=j-meanRadius; m<=j-meanRadius; m++) {
                if ((m>=0 && m<width) && (k>=0 && k<height)) {
                    alphaRgb = getRGB(m, k);
                    sumAlphaRgb[0] += alphaRgb[0];
                    sumAlphaRgb[1] += alphaRgb[1];
                    sumAlphaRgb[2] += alphaRgb[2];
                    sumAlphaRgb[3] += alphaRgb[3];
                    count++;
                }
            }
        }

        // average
        sumAlphaRgb[0] = (int)Math.round((sumAlphaRgb[0]*1.0)/count);
        sumAlphaRgb[1] = (int)Math.round((sumAlphaRgb[1]*1.0)/count);
        sumAlphaRgb[2] = (int)Math.round((sumAlphaRgb[2]*1.0)/count);
        sumAlphaRgb[3] = (int)Math.round((sumAlphaRgb[3]*1.0)/count);

        int p = (sumAlphaRgb[0]<<24) | (sumAlphaRgb[1]<<16) | (sumAlphaRgb[2]<<8) | sumAlphaRgb[3];

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
        calculateMeans();
        long difference = System.currentTimeMillis() - initial;

        System.out.println("Time lapsed is "+difference+" milliseconds");

        outputImage();

        // open image, save pix as int [][]
    }
}