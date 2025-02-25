

package org.apache.commons.cli;


final class OptionValidator {
    
    
    static final char[] ADDITIONAL_OPTION_CHARS = {'?', '@'};
    
    static final char[] ADDITIONAL_LONG_CHARS = {'-'};

    
    private static boolean search(final char[] chars, final char c) {
        for (char a : chars) {
            if (a == c) {
                return true;
            }
        }
        return false;
    }

    
    private static boolean isValidChar(final char c) {
        return Character.isJavaIdentifierPart(c) || search(ADDITIONAL_LONG_CHARS, c);
    }

    
    private static boolean isValidOpt(final char c) {
        return Character.isJavaIdentifierPart(c) || search(ADDITIONAL_OPTION_CHARS, c);
    }

    
    static String validate(final String option) throws IllegalArgumentException {
        // if opt is NULL do not check further
        if (option == null) {
            return null;
        }

        char[] chars = option.toCharArray();

        if (!isValidOpt(chars[0])) {
            throw new IllegalArgumentException("Illegal option name '" + chars[0] + "'");
        }
        // handle the multi-character opt
        if (option.length() > 1) {
            for (int i = 1; i < chars.length; i++) {
                if (!isValidChar(chars[i])) {
                    throw new IllegalArgumentException("The option '" + option + "' contains an illegal " + "character : '" + chars[i] + "'");
                }
            }
        }
        return option;
    }
}
