

package org.apache.commons.csv;

import static org.apache.commons.csv.Constants.BACKSPACE;
import static org.apache.commons.csv.Constants.CR;
import static org.apache.commons.csv.Constants.END_OF_STREAM;
import static org.apache.commons.csv.Constants.FF;
import static org.apache.commons.csv.Constants.LF;
import static org.apache.commons.csv.Constants.TAB;
import static org.apache.commons.csv.Constants.UNDEFINED;
import static org.apache.commons.csv.Token.Type.COMMENT;
import static org.apache.commons.csv.Token.Type.EOF;
import static org.apache.commons.csv.Token.Type.EORECORD;
import static org.apache.commons.csv.Token.Type.INVALID;
import static org.apache.commons.csv.Token.Type.TOKEN;

import java.io.Closeable;
import java.io.IOException;


final class Lexer implements Closeable {

    private static final String CR_STRING = Character.toString(CR);
    private static final String LF_STRING = Character.toString(LF);

    
    private static final char DISABLED = '\ufffe';

    private final char[] delimiter;
    private final char[] delimiterBuf;
    private final char[] escapeDelimiterBuf;
    private final char escape;
    private final char quoteChar;
    private final char commentStart;

    private final boolean ignoreSurroundingSpaces;
    private final boolean ignoreEmptyLines;

    
    private final ExtendedBufferedReader reader;
    private String firstEol;

    private boolean isLastTokenDelimiter;

    Lexer(final CSVFormat format, final ExtendedBufferedReader reader) {
        this.reader = reader;
        this.delimiter = format.getDelimiterString().toCharArray();
        this.escape = mapNullToDisabled(format.getEscapeCharacter());
        this.quoteChar = mapNullToDisabled(format.getQuoteCharacter());
        this.commentStart = mapNullToDisabled(format.getCommentMarker());
        this.ignoreSurroundingSpaces = format.getIgnoreSurroundingSpaces();
        this.ignoreEmptyLines = format.getIgnoreEmptyLines();
        this.delimiterBuf = new char[delimiter.length - 1];
        this.escapeDelimiterBuf = new char[2 * delimiter.length - 1];
    }

    
    @Override
    public void close() throws IOException {
        reader.close();
    }

    
    long getCharacterPosition() {
        return reader.getPosition();
    }

    
    long getCurrentLineNumber() {
        return reader.getCurrentLineNumber();
    }

    String getFirstEol(){
        return firstEol;
    }

    boolean isClosed() {
        return reader.isClosed();
    }

    boolean isCommentStart(final int ch) {
        return ch == commentStart;
    }

    
    boolean isDelimiter(final int ch) throws IOException {
        isLastTokenDelimiter = false;
        if (ch != delimiter[0]) {
            return false;
        }
        if (delimiter.length == 1) {
            isLastTokenDelimiter = true;
            return true;
        }
        reader.lookAhead(delimiterBuf);
        for (int i = 0; i < delimiterBuf.length; i++) {
            if (delimiterBuf[i] != delimiter[i+1]) {
                return false;
            }
        }
        final int count = reader.read(delimiterBuf, 0, delimiterBuf.length);
        isLastTokenDelimiter = count != END_OF_STREAM;
        return isLastTokenDelimiter;
    }

    
    boolean isEndOfFile(final int ch) {
        return ch == END_OF_STREAM;
    }

    
    boolean isEscape(final int ch) {
        return ch == escape;
    }

    
    boolean isEscapeDelimiter() throws IOException {
        reader.lookAhead(escapeDelimiterBuf);
        if (escapeDelimiterBuf[0] != delimiter[0]) {
            return false;
        }
        for (int i = 1; i < delimiter.length; i++) {
            if (escapeDelimiterBuf[2 * i] != delimiter[i] || escapeDelimiterBuf[2 * i - 1] != escape) {
                return false;
            }
        }
        final int count = reader.read(escapeDelimiterBuf, 0, escapeDelimiterBuf.length);
        return count != END_OF_STREAM;
    }

    private boolean isMetaChar(final int ch) {
        return ch == escape || ch == quoteChar || ch == commentStart;
    }

    boolean isQuoteChar(final int ch) {
        return ch == quoteChar;
    }

    
    boolean isStartOfLine(final int ch) {
        return ch == LF || ch == CR || ch == UNDEFINED;
    }

    private char mapNullToDisabled(final Character c) {
        return c == null ? DISABLED : c.charValue();
    }

    
    Token nextToken(final Token token) throws IOException {

        // Get the last read char (required for empty line detection)
        int lastChar = reader.getLastChar();

        // read the next char and set eol
        int c = reader.read();
        
        boolean eol = readEndOfLine(c);

        // empty line detection: eol AND (last char was EOL or beginning)
        if (ignoreEmptyLines) {
            while (eol && isStartOfLine(lastChar)) {
                // Go on char ahead ...
                lastChar = c;
                c = reader.read();
                eol = readEndOfLine(c);
                // reached the end of the file without any content (empty line at the end)
                if (isEndOfFile(c)) {
                    token.type = EOF;
                    // don't set token.isReady here because no content
                    return token;
                }
            }
        }

        // Did we reach EOF during the last iteration already? EOF
        if (isEndOfFile(lastChar) || !isLastTokenDelimiter && isEndOfFile(c)) {
            token.type = EOF;
            // don't set token.isReady here because no content
            return token;
        }

        if (isStartOfLine(lastChar) && isCommentStart(c)) {
            final String line = reader.readLine();
            if (line == null) {
                token.type = EOF;
                // don't set token.isReady here because no content
                return token;
            }
            final String comment = line.trim();
            token.content.append(comment);
            token.type = COMMENT;
            return token;
        }

        // Important: make sure a new char gets consumed in each iteration
        while (token.type == INVALID) {
            // ignore whitespaces at beginning of a token
            if (ignoreSurroundingSpaces) {
                while (Character.isWhitespace((char)c) && !isDelimiter(c) && !eol) {
                    c = reader.read();
                    eol = readEndOfLine(c);
                }
            }

            // ok, start of token reached: encapsulated, or token
            if (isDelimiter(c)) {
                // empty token return TOKEN("")
                token.type = TOKEN;
            } else if (eol) {
                // empty token return EORECORD("")
                // noop: token.content.append("");
                token.type = EORECORD;
            } else if (isQuoteChar(c)) {
                // consume encapsulated token
                parseEncapsulatedToken(token);
            } else if (isEndOfFile(c)) {
                // end of file return EOF()
                // noop: token.content.append("");
                token.type = EOF;
                token.isReady = true; // there is data at EOF
            } else {
                // next token must be a simple token
                // add removed blanks when not ignoring whitespace chars...
                parseSimpleToken(token, c);
            }
        }
        return token;
    }

    
    private Token parseEncapsulatedToken(final Token token) throws IOException {
        token.isQuoted = true;
        // Save current line number in case needed for IOE
        final long startLineNumber = getCurrentLineNumber();
        int c;
        while (true) {
            c = reader.read();

            if (isEscape(c)) {
                if (isEscapeDelimiter()) {
                    token.content.append(delimiter);
                } else {
                    final int unescaped = readEscape();
                    if (unescaped == END_OF_STREAM) { // unexpected char after escape
                        token.content.append((char) c).append((char) reader.getLastChar());
                    } else {
                        token.content.append((char) unescaped);
                    }
                }
            } else if (isQuoteChar(c)) {
                if (isQuoteChar(reader.lookAhead())) {
                    // double or escaped encapsulator -> add single encapsulator to token
                    c = reader.read();
                    token.content.append((char) c);
                } else {
                    // token finish mark (encapsulator) reached: ignore whitespace till delimiter
                    while (true) {
                        c = reader.read();
                        if (isDelimiter(c)) {
                            token.type = TOKEN;
                            return token;
                        }
                        if (isEndOfFile(c)) {
                            token.type = EOF;
                            token.isReady = true; // There is data at EOF
                            return token;
                        }
                        if (readEndOfLine(c)) {
                            token.type = EORECORD;
                            return token;
                        }
                        if (!Character.isWhitespace((char)c)) {
                            // error invalid char between token and next delimiter
                            throw new IOException("Invalid char between encapsulated token and delimiter at line: " +
                                    getCurrentLineNumber() + ", position: " + getCharacterPosition());
                        }
                    }
                }
            } else if (isEndOfFile(c)) {
                // error condition (end of file before end of token)
                throw new IOException("(startline " + startLineNumber +
                        ") EOF reached before encapsulated token finished");
            } else {
                // consume character
                token.content.append((char) c);
            }
        }
    }

    
    private Token parseSimpleToken(final Token token, int ch) throws IOException {
        // Faster to use while(true)+break than while(token.type == INVALID)
        while (true) {
            if (readEndOfLine(ch)) {
                token.type = EORECORD;
                break;
            }
            if (isEndOfFile(ch)) {
                token.type = EOF;
                token.isReady = true; // There is data at EOF
                break;
            }
            if (isDelimiter(ch)) {
                token.type = TOKEN;
                break;
            }
            // continue
            if (isEscape(ch)) {
                if (isEscapeDelimiter()) {
                    token.content.append(delimiter);
                } else {
                    final int unescaped = readEscape();
                    if (unescaped == END_OF_STREAM) { // unexpected char after escape
                        token.content.append((char) ch).append((char) reader.getLastChar());
                    } else {
                        token.content.append((char) unescaped);
                    }
                }
            } else {
                token.content.append((char) ch);
            }
            ch = reader.read(); // continue
        }

        if (ignoreSurroundingSpaces) {
            trimTrailingSpaces(token.content);
        }

        return token;
    }

    
    boolean readEndOfLine(int ch) throws IOException {
        // check if we have \r\n...
        if (ch == CR && reader.lookAhead() == LF) {
            // note: does not change ch outside of this method!
            ch = reader.read();
            // Save the EOL state
            if (firstEol == null) {
                this.firstEol = Constants.CRLF;
            }
        }
        // save EOL state here.
        if (firstEol == null) {
            if (ch == LF) {
                this.firstEol = LF_STRING;
            } else if (ch == CR) {
                this.firstEol = CR_STRING;
            }
        }

        return ch == LF || ch == CR;
    }

    // TODO escape handling needs more work
    
    int readEscape() throws IOException {
        // the escape char has just been read (normally a backslash)
        final int ch = reader.read();
        switch (ch) {
        case 'r':
            return CR;
        case 'n':
            return LF;
        case 't':
            return TAB;
        case 'b':
            return BACKSPACE;
        case 'f':
            return FF;
        case CR:
        case LF:
        case FF: // TODO is this correct?
        case TAB: // TODO is this correct? Do tabs need to be escaped?
        case BACKSPACE: // TODO is this correct?
            return ch;
        case END_OF_STREAM:
            throw new IOException("EOF whilst processing escape sequence");
        default:
            // Now check for meta-characters
            if (isMetaChar(ch)) {
                return ch;
            }
            // indicate unexpected char - available from in.getLastChar()
            return END_OF_STREAM;
        }
    }

    void trimTrailingSpaces(final StringBuilder buffer) {
        int length = buffer.length();
        while (length > 0 && Character.isWhitespace(buffer.charAt(length - 1))) {
            length--;
        }
        if (length != buffer.length()) {
            buffer.setLength(length);
        }
    }
}
