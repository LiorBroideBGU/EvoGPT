

package org.apache.commons.cli;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Path;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class TypeHandler {

    
    private static final int HEX_RADIX = 16;

    
    private static Map<Class<?>, Converter<?, ?>> converterMap = new HashMap<>();

    static {
        resetConverters();
    }

    
    @Deprecated // since 1.7.0
    public static Class<?> createClass(final String className) throws ParseException {
        return createValue(className, Class.class);
    }

    
    @Deprecated // since 1.7.0
    public static Date createDate(final String str) {
        try {
            return createValue(str, Date.class);
        } catch (final ParseException e) {
            throw new RuntimeException(e);
        }
    }

    
    @Deprecated // since 1.7.0
    public static File createFile(final String str) {
        try {
            return createValue(str, File.class);
        } catch (final ParseException e) {
            throw new RuntimeException(e);
        }
    }

    
    @Deprecated // since 1.7.0
    public static File[] createFiles(final String str) {
        // to implement/port:
        // return FileW.findFiles(str);
        throw new UnsupportedOperationException("Not yet implemented");
    }

    
    @Deprecated // since 1.7.0
    public static Number createNumber(final String str) throws ParseException {
        return createValue(str, Number.class);
    }

    
    @Deprecated // since 1.7.0
    public static Object createObject(final String className) throws ParseException {
        return createValue(className, Object.class);
    }

    
    @Deprecated // since 1.7.0
    public static URL createURL(final String str) throws ParseException {
        return createValue(str, URL.class);
    }

    
    @SuppressWarnings("unchecked") // returned value will have type T because it is fixed by clazz
    public static <T> T createValue(final String str, final Class<T> clazz) throws ParseException {
        try {
            return (T) getConverter(clazz).apply(str);
        } catch (final Throwable e) {
            throw ParseException.wrap(e);
        }
    }

    
    @Deprecated // since 1.7.0
    public static Object createValue(final String str, final Object obj) throws ParseException {
        return createValue(str, (Class<?>) obj);
    }

    
    public static Converter<?, ?> getConverter(final Class<?> clazz) {
        final Converter<?, ?> converter = converterMap.get(clazz);
        return converter == null ? Converter.DEFAULT : converter;
    }

    
    public static void noConverters() {
        converterMap.clear();
    }

    
    @Deprecated // since 1.7.0
    public static FileInputStream openFile(final String str) throws ParseException {
        return createValue(str, FileInputStream.class);
    }

    
    public static void register(final Class<?> clazz, final Converter<?, ?> converter) {
        if (converter == null) {
            converterMap.remove(clazz);
        } else {
            converterMap.put(clazz, converter);
        }
    }

    
    public static void resetConverters() {
        converterMap.clear();
        converterMap.put(Object.class, Converter.OBJECT);
        converterMap.put(Class.class, Converter.CLASS);
        converterMap.put(Date.class, Converter.DATE);
        converterMap.put(File.class, Converter.FILE);
        converterMap.put(Path.class, Converter.PATH);
        converterMap.put(Number.class, Converter.NUMBER);
        converterMap.put(URL.class, Converter.URL);
        converterMap.put(FileInputStream.class, FileInputStream::new);
        converterMap.put(Long.class, Long::parseLong);
        converterMap.put(Integer.class, Integer::parseInt);
        converterMap.put(Short.class, Short::parseShort);
        converterMap.put(Byte.class, Byte::parseByte);
        converterMap.put(Character.class, s -> s.startsWith("\\u") ? Character.toChars(Integer.parseInt(s.substring(2), HEX_RADIX))[0] : s.charAt(0));
        converterMap.put(Double.class, Double::parseDouble);
        converterMap.put(Float.class, Float::parseFloat);
        converterMap.put(BigInteger.class, BigInteger::new);
        converterMap.put(BigDecimal.class, BigDecimal::new);
    }
}
