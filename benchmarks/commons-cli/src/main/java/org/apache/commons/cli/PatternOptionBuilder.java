

package org.apache.commons.cli;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Date;


public class PatternOptionBuilder {
    
    public static final Class<String> STRING_VALUE = String.class;

    
    public static final Class<Object> OBJECT_VALUE = Object.class;

    
    public static final Class<Number> NUMBER_VALUE = Number.class;

    
    public static final Class<Date> DATE_VALUE = Date.class;

    
    public static final Class<?> CLASS_VALUE = Class.class;

    /// can we do this one??
    // is meant to check that the file exists, else it errors.
    // ie) it's for reading not writing.

    
    public static final Class<FileInputStream> EXISTING_FILE_VALUE = FileInputStream.class;

    
    public static final Class<File> FILE_VALUE = File.class;

    
    public static final Class<File[]> FILES_VALUE = File[].class;

    
    public static final Class<URL> URL_VALUE = URL.class;

    
    static final Converter<?, UnsupportedOperationException> NOT_IMPLEMENTED = s -> {
        throw new UnsupportedOperationException("Not yet implemented");
    };

    static {
        registerTypes();
    }

    
    @Deprecated // since="1.7.0"
    public static Object getValueClass(final char ch) {
        return getValueType(ch);
    }

    
    public static Class<?> getValueType(final char ch) {
        switch (ch) {
        case '@':
            return PatternOptionBuilder.OBJECT_VALUE;
        case ':':
            return PatternOptionBuilder.STRING_VALUE;
        case '%':
            return PatternOptionBuilder.NUMBER_VALUE;
        case '+':
            return PatternOptionBuilder.CLASS_VALUE;
        case '#':
            return PatternOptionBuilder.DATE_VALUE;
        case '<':
            return PatternOptionBuilder.EXISTING_FILE_VALUE;
        case '>':
            return PatternOptionBuilder.FILE_VALUE;
        case '*':
            return PatternOptionBuilder.FILES_VALUE;
        case '/':
            return PatternOptionBuilder.URL_VALUE;
        }

        return null;
    }

    
    public static boolean isValueCode(final char ch) {
        return ch == '@' || ch == ':' || ch == '%' || ch == '+' || ch == '#' || ch == '<' || ch == '>' || ch == '*' || ch == '/' || ch == '!';
    }

    
    public static Options parsePattern(final String pattern) {
        char opt = ' ';
        boolean required = false;
        Class<?> type = null;
        Converter<?, ?> converter = Converter.DEFAULT;

        final Options options = new Options();

        for (int i = 0; i < pattern.length(); i++) {
            final char ch = pattern.charAt(i);

            // a value code comes after an option and specifies
            // details about it
            if (!isValueCode(ch)) {
                if (opt != ' ') {
                    final Option option = Option.builder(String.valueOf(opt)).hasArg(type != null).required(required).type(type)
                            .converter(converter).build();

                    // we have a previous one to deal with
                    options.addOption(option);
                    required = false;
                    type = null;
                    converter = Converter.DEFAULT;
                }

                opt = ch;
            } else if (ch == '!') {
                required = true;
            } else {
                type = getValueType(ch);
                converter = TypeHandler.getConverter(getValueType(ch));
            }
        }

        if (opt != ' ') {
            final Option option = Option.builder(String.valueOf(opt)).hasArg(type != null).required(required).type(type).build();

            // we have a final one to deal with
            options.addOption(option);
        }

        return options;
    }

    
    public static void registerTypes() {
        TypeHandler.register(PatternOptionBuilder.FILES_VALUE, NOT_IMPLEMENTED);
    }
}
