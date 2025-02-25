

package org.jfree.chart.encoders;

import java.util.HashMap;
import java.util.Map;


public class ImageEncoderFactory {

    
    private static Map<String, String> encoders = null;

    static {
        init();
    }

    
    private static void init() {
        encoders = new HashMap<>();
        encoders.put("jpeg", "org.jfree.chart.encoders.SunJPEGEncoderAdapter");
        encoders.put("png", "org.jfree.chart.encoders.SunPNGEncoderAdapter");
    }

    
    public static void setImageEncoder(String format,
                                       String imageEncoderClassName) {
        if (format == null)
            throw new IllegalArgumentException("Image format must not be null");
        if (imageEncoderClassName == null)
            throw new IllegalArgumentException("Image encoder class name must not be null");
        encoders.put(format, imageEncoderClassName);
    }

    
    public static ImageEncoder newInstance(String format) {
        ImageEncoder imageEncoder;
        String className = encoders.get(format);
        try {
            Class<?> imageEncoderClass = Class.forName(className);
            imageEncoder = (ImageEncoder) imageEncoderClass.getDeclaredConstructor().newInstance();
        }
        catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage(), e);
        }
        return imageEncoder;
    }

    
    public static ImageEncoder newInstance(String format, float quality) {
        ImageEncoder imageEncoder = newInstance(format);
        imageEncoder.setQuality(quality);
        return imageEncoder;
    }

    
    public static ImageEncoder newInstance(String format,
                                           boolean encodingAlpha) {
        ImageEncoder imageEncoder = newInstance(format);
        imageEncoder.setEncodingAlpha(encodingAlpha);
        return imageEncoder;
    }

    
    public static ImageEncoder newInstance(String format, float quality,
                                           boolean encodingAlpha) {
        ImageEncoder imageEncoder = newInstance(format);
        imageEncoder.setQuality(quality);
        imageEncoder.setEncodingAlpha(encodingAlpha);
        return imageEncoder;
    }

}
