

package org.jfree.chart.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.Serializable;
import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.HashUtils;


public class DefaultShadowGenerator implements ShadowGenerator, Serializable {

    private static final long serialVersionUID = 2732993885591386064L;

    
    private int shadowSize;

    
    private Color shadowColor;

    
    private float shadowOpacity;

    
    private double angle;

    
    private int distance;

    
    public DefaultShadowGenerator() {
        this(5, Color.BLACK, 0.5f, 5, -Math.PI / 4);
    }

    
    public DefaultShadowGenerator(int size, Color color, float opacity,
            int distance, double angle) {
        Args.nullNotPermitted(color, "color");
        this.shadowSize = size;
        this.shadowColor = color;
        this.shadowOpacity = opacity;
        this.distance = distance;
        this.angle = angle;
    }

    
    public int getShadowSize() {
        return this.shadowSize;
    }

    
    public Color getShadowColor() {
        return this.shadowColor;
    }

    
    public float getShadowOpacity() {
        return this.shadowOpacity;
    }

    
    public int getDistance() {
        return this.distance;
    }

    
    public double getAngle() {
        return this.angle;
    }

    
    @Override
    public int calculateOffsetX() {
        return (int) (Math.cos(this.angle) * this.distance) - this.shadowSize;
    }

    
    @Override
    public int calculateOffsetY() {
        return -(int) (Math.sin(this.angle) * this.distance) - this.shadowSize;
    }

    
    @Override
    public BufferedImage createDropShadow(BufferedImage source) {
        BufferedImage subject = new BufferedImage(
                source.getWidth() + this.shadowSize * 2,
                source.getHeight() + this.shadowSize * 2,
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = subject.createGraphics();
        g2.drawImage(source, null, this.shadowSize, this.shadowSize);
        g2.dispose();
        applyShadow(subject);
        return subject;
    }

    
    protected void applyShadow(BufferedImage image) {
        int dstWidth = image.getWidth();
        int dstHeight = image.getHeight();

        int left = (this.shadowSize - 1) >> 1;
        int right = this.shadowSize - left;
        int xStart = left;
        int xStop = dstWidth - right;
        int yStart = left;
        int yStop = dstHeight - right;

        int shadowRgb = this.shadowColor.getRGB() & 0x00FFFFFF;

        int[] aHistory = new int[this.shadowSize];
        int historyIdx;

        int aSum;

        int[] dataBuffer = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
        int lastPixelOffset = right * dstWidth;
        float sumDivider = this.shadowOpacity / this.shadowSize;

        // horizontal pass

        for (int y = 0, bufferOffset = 0; y < dstHeight; y++, bufferOffset = y * dstWidth) {
            aSum = 0;
            historyIdx = 0;
            for (int x = 0; x < this.shadowSize; x++, bufferOffset++) {
                int a = dataBuffer[bufferOffset] >>> 24;
                aHistory[x] = a;
                aSum += a;
            }

            bufferOffset -= right;

            for (int x = xStart; x < xStop; x++, bufferOffset++) {
                int a = (int) (aSum * sumDivider);
                dataBuffer[bufferOffset] = a << 24 | shadowRgb;

                // substract the oldest pixel from the sum
                aSum -= aHistory[historyIdx];

                // get the lastest pixel
                a = dataBuffer[bufferOffset + right] >>> 24;
                aHistory[historyIdx] = a;
                aSum += a;

                if (++historyIdx >= this.shadowSize) {
                    historyIdx -= this.shadowSize;
                }
            }
        }

        // vertical pass
        for (int x = 0, bufferOffset = 0; x < dstWidth; x++, bufferOffset = x) {
            aSum = 0;
            historyIdx = 0;
            for (int y = 0; y < this.shadowSize; y++,
                    bufferOffset += dstWidth) {
                int a = dataBuffer[bufferOffset] >>> 24;
                aHistory[y] = a;
                aSum += a;
            }

            bufferOffset -= lastPixelOffset;

            for (int y = yStart; y < yStop; y++, bufferOffset += dstWidth) {
                int a = (int) (aSum * sumDivider);
                dataBuffer[bufferOffset] = a << 24 | shadowRgb;

                // substract the oldest pixel from the sum
                aSum -= aHistory[historyIdx];

                // get the lastest pixel
                a = dataBuffer[bufferOffset + lastPixelOffset] >>> 24;
                aHistory[historyIdx] = a;
                aSum += a;

                if (++historyIdx >= this.shadowSize) {
                    historyIdx -= this.shadowSize;
                }
            }
        }
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof DefaultShadowGenerator)) {
            return false;
        }
        DefaultShadowGenerator that = (DefaultShadowGenerator) obj;
        if (this.shadowSize != that.shadowSize) {
            return false;
        }
        if (!this.shadowColor.equals(that.shadowColor)) {
            return false;
        }
        if (this.shadowOpacity != that.shadowOpacity) {
            return false;
        }
        if (this.distance != that.distance) {
            return false;
        }
        if (this.angle != that.angle) {
            return false;
        }
        return true;
    }

    
    @Override
    public int hashCode() {
        int hash = HashUtils.hashCode(17, this.shadowSize);
        hash = HashUtils.hashCode(hash, this.shadowColor);
        hash = HashUtils.hashCode(hash, this.shadowOpacity);
        hash = HashUtils.hashCode(hash, this.distance);
        hash = HashUtils.hashCode(hash, this.angle);
        return hash;
    }

}
