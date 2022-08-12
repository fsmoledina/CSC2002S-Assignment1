import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MeanFilterSerial {
    public static void main(String[] args)throws IOException{
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
    public class ForkBlur extends RecursiveAction {
        private int[] mSource;
        private int mStart;
        private int mLength;
        private int[] mDestination;

        // Processing window size; should be odd.
        private int mBlurWidth = 15;

        public ForkBlur(int[] src, int start, int length, int[] dst) {
            mSource = src;
            mStart = start;
            mLength = length;
            mDestination = dst;
        }

        protected void computeDirectly() {
            int sidePixels = (mBlurWidth - 1) / 2;
            for (int index = mStart; index < mStart + mLength; index++) {
                // Calculate average.
                float rt = 0, gt = 0, bt = 0;
                for (int mi = -sidePixels; mi <= sidePixels; mi++) {
                    int mindex = Math.min(Math.max(mi + index, 0),
                            mSource.length - 1);
                    int pixel = mSource[mindex];
                    rt += (float)((pixel & 0x00ff0000) >> 16)
                            / mBlurWidth;
                    gt += (float)((pixel & 0x0000ff00) >>  8)
                            / mBlurWidth;
                    bt += (float)((pixel & 0x000000ff) >>  0)
                            / mBlurWidth;
                }

                // Reassemble destination pixel.
                int dpixel = (0xff000000     ) |
                        (((int)rt) << 16) |
                        (((int)gt) <<  8) |
                        (((int)bt) <<  0);
                mDestination[index] = dpixel;
            }
        }
}//class ends here



