

package org.jfree.chart.encoders;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.jfree.chart.internal.Args;


public class SunJPEGEncoderAdapter implements ImageEncoder {

    
    private float quality = 0.95f;

    
    public SunJPEGEncoderAdapter() {
    }

    
    @Override
    public float getQuality() {
        return this.quality;
    }

    
    @Override
    public void setQuality(float quality) {
        if (quality < 0.0f || quality > 1.0f) {
            throw new IllegalArgumentException(
                    "The 'quality' must be in the range 0.0f to 1.0f");
        }
        this.quality = quality;
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
        Iterator iterator = ImageIO.getImageWritersByFormatName("jpeg");
        ImageWriter writer = (ImageWriter) iterator.next();
        ImageWriteParam p = writer.getDefaultWriteParam();
        p.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        p.setCompressionQuality(this.quality);
        ImageOutputStream ios = ImageIO.createImageOutputStream(outputStream);
        writer.setOutput(ios);
        writer.write(null, new IIOImage(bufferedImage, null, null), p);
        ios.flush();
        writer.dispose();
        ios.close();
    }

}
