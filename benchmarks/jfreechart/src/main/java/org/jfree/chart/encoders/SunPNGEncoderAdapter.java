

package org.jfree.chart.encoders;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import org.jfree.chart.internal.Args;


public class SunPNGEncoderAdapter implements ImageEncoder {

    
    @Override
    public float getQuality() {
        return 0.0f;
    }

    
    @Override
    public void setQuality(float quality) {
        //  No op
    }

    
    @Override
    public boolean isEncodingAlpha() {
        return false;
    }

    
    @Override
    public void setEncodingAlpha(boolean encodingAlpha) {
        //  No op
    }

    
    @Override
    public byte[] encode(BufferedImage bufferedImage) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        encode(bufferedImage, outputStream);
        return outputStream.toByteArray();
    }

    
    @Override
    public void encode(BufferedImage bufferedImage, OutputStream outputStream)
            throws IOException {
        Args.nullNotPermitted(bufferedImage, "bufferedImage");
        Args.nullNotPermitted(outputStream, "outputStream");
        ImageIO.write(bufferedImage, ImageFormat.PNG, outputStream);
    }

}
