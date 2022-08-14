
import java.io.File;
import java.io.IOException;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.util.concurrent.RecursiveAction;

public class MeanFilterParallel extends RecursiveAction {
    public String inputImageName, outputImageName;

    protected BufferedImage bufferedImage = null;
    protected BufferedImage bufferedMeanImage = null;
    protected File file = null;

    protected int windowWidth;
    protected int width, height;

    protected int [][] pixelValues, meanPixelValues;

    protected int [] alphaRGB, sumAlphaRgb, alphaRgb;

    protected int maxWorkload = 10000;

    protected int x0, y0, x1, y1;

    public MeanFilterParallel(int [][] pixelValues, int x0, int y0, int x1, int y1, int [][] meanPixelValues, int windowWidth) {
        this.pixelValues = pixelValues;
        this.meanPixelValues = meanPixelValues;
        this.x0=x0; this.y0=y0;
        this.x1=x1; this.y1=y1;
        this.windowWidth=windowWidth;
    }

    protected void calculateMeans() {

        for (int i=y0; i<y1; i++) {
            for (int j=x0; j<x1; j++) {
                meanPixelValues[i][j] = meanFunction(i, j);
            }
        }
    }

    protected void compute() {
        if ( (x1-x0)*(y1-y0) > maxWorkload) {
            //split workload
            int halfX = (x1-x0)/2, halfY = (y1-y0)/2;
            invokeAll(new MeanFilterParallel(pixelValues, x0,y0 halfX,halfY, meanPixelValues, windowWidth),
                    new MeanFilterParallel(pixelValues, halfX,halfY, x1,y1, meanPixelValues, windowWidth));
        }
        else {
            calculateMeans();
        }
    }

    protected int [][] getPixels() {

        //read image
        try{
            file = new File(inputImageName);
            bufferedImage = ImageIO.read(file);
        }catch(IOException e){
            //System.out.println(e);
            System.out.println("File no found");
            return null;
        }
        width = bufferedImage.getWidth();
        height = bufferedImage.getHeight();
        meanPixelValues = new int [height][width];

        int [][] pixels = new int [height][width];
        for (int i=0; i<height; i++) {
            for (int j=0; j<width; j++) {
                pixels[i][j] = bufferedImage.getRGB(j, i);
            }
        }
        return pixels;
    }

    protected int [] getRGB(int j, int i) {
        alphaRGB = new int [4];
        int p = pixelValues[i][j];
        alphaRGB[0] = (p>>24) & 0xff;
        alphaRGB[1] = (p>>16) & 0xff;
        alphaRGB[2] = (p>>8) & 0xff;
        alphaRGB[3] = p & 0xff;
        return alphaRGB;
    }

    protected int meanFunction(int i, int j) {
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

    protected static boolean outputImage() {
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

        // MeanFilterParallel forFilesIO = new MeanFilterParallel(inputImageName, outputImageName, windowWidth);

        int [][] pixelValues = forFilesIO.getPixels();
        int [][] meanPixelValues = forFilesIO.meanPixelValues;
        int width=forFilesIO.width, height=forFilesIO.height;

        MeanFilterParallel filter = new MeanFilterParallel(pixelValues, 0,0, width,height, meanPixelValues, windowWidth);

        ForkJoinPool pool = new ForkJoinPool();


        long initial = System.currentTimeMillis();
        pool.invoke(filter);
        long difference = System.currentTimeMillis() - initial;

        System.out.println("Time lapsed is "+difference+" milliseconds");

        // outputImage(meanPixelValues, );

        // open image, save pix as int [][]
    }
}