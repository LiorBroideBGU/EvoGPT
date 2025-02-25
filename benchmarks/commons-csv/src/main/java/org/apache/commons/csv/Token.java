

package org.apache.commons.csv;

import static org.apache.commons.csv.Token.Type.INVALID;


final class Token {

    enum Type {
        
        INVALID,

        
        TOKEN,

        
        EOF,

        
        EORECORD,

        
        COMMENT
    }

    
    private static final int INITIAL_TOKEN_LENGTH = 50;

    
    Type type = INVALID;

    
    final StringBuilder content = new StringBuilder(INITIAL_TOKEN_LENGTH);

    
    boolean isReady;

    boolean isQuoted;

    void reset() {
        content.setLength(0);
        type = INVALID;
        isReady = false;
        isQuoted = false;
    }

    
    @Override
    public String toString() {
        return type.name() + " [" + content.toString() + "]";
    }
}
