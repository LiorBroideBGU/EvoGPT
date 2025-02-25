

package org.apache.commons.csv;

import static org.apache.commons.csv.Constants.BACKSLASH;
import static org.apache.commons.csv.Constants.COMMA;
import static org.apache.commons.csv.Constants.COMMENT;
import static org.apache.commons.csv.Constants.CR;
import static org.apache.commons.csv.Constants.CRLF;
import static org.apache.commons.csv.Constants.DOUBLE_QUOTE_CHAR;
import static org.apache.commons.csv.Constants.EMPTY;
import static org.apache.commons.csv.Constants.LF;
import static org.apache.commons.csv.Constants.PIPE;
import static org.apache.commons.csv.Constants.SP;
import static org.apache.commons.csv.Constants.TAB;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.function.Uncheck;


public final class CSVFormat implements Serializable {

    
    public static class Builder {

        
        public static Builder create() {
            return new Builder(CSVFormat.DEFAULT);
        }

        
        public static Builder create(final CSVFormat csvFormat) {
            return new Builder(csvFormat);
        }

        private boolean allowMissingColumnNames;

        private boolean autoFlush;

        private Character commentMarker;

        private String delimiter;

        private DuplicateHeaderMode duplicateHeaderMode;

        private Character escapeCharacter;

        private String[] headerComments;

        private String[] headers;

        private boolean ignoreEmptyLines;

        private boolean ignoreHeaderCase;

        private boolean ignoreSurroundingSpaces;

        private String nullString;

        private Character quoteCharacter;

        private String quotedNullString;

        private QuoteMode quoteMode;

        private String recordSeparator;

        private boolean skipHeaderRecord;

        private boolean trailingDelimiter;

        private boolean trim;

        private Builder(final CSVFormat csvFormat) {
            this.delimiter = csvFormat.delimiter;
            this.quoteCharacter = csvFormat.quoteCharacter;
            this.quoteMode = csvFormat.quoteMode;
            this.commentMarker = csvFormat.commentMarker;
            this.escapeCharacter = csvFormat.escapeCharacter;
            this.ignoreSurroundingSpaces = csvFormat.ignoreSurroundingSpaces;
            this.allowMissingColumnNames = csvFormat.allowMissingColumnNames;
            this.ignoreEmptyLines = csvFormat.ignoreEmptyLines;
            this.recordSeparator = csvFormat.recordSeparator;
            this.nullString = csvFormat.nullString;
            this.headerComments = csvFormat.headerComments;
            this.headers = csvFormat.headers;
            this.skipHeaderRecord = csvFormat.skipHeaderRecord;
            this.ignoreHeaderCase = csvFormat.ignoreHeaderCase;
            this.trailingDelimiter = csvFormat.trailingDelimiter;
            this.trim = csvFormat.trim;
            this.autoFlush = csvFormat.autoFlush;
            this.quotedNullString = csvFormat.quotedNullString;
            this.duplicateHeaderMode = csvFormat.duplicateHeaderMode;
        }

        
        public CSVFormat build() {
            return new CSVFormat(this);
        }

        
        @Deprecated
        public Builder setAllowDuplicateHeaderNames(final boolean allowDuplicateHeaderNames) {
            setDuplicateHeaderMode(allowDuplicateHeaderNames ? DuplicateHeaderMode.ALLOW_ALL : DuplicateHeaderMode.ALLOW_EMPTY);
            return this;
        }

        
        public Builder setAllowMissingColumnNames(final boolean allowMissingColumnNames) {
            this.allowMissingColumnNames = allowMissingColumnNames;
            return this;
        }

        
        public Builder setAutoFlush(final boolean autoFlush) {
            this.autoFlush = autoFlush;
            return this;
        }

        
        public Builder setCommentMarker(final char commentMarker) {
            setCommentMarker(Character.valueOf(commentMarker));
            return this;
        }

        
        public Builder setCommentMarker(final Character commentMarker) {
            if (isLineBreak(commentMarker)) {
                throw new IllegalArgumentException("The comment start marker character cannot be a line break");
            }
            this.commentMarker = commentMarker;
            return this;
        }

        
        public Builder setDelimiter(final char delimiter) {
            return setDelimiter(String.valueOf(delimiter));
        }

        
        public Builder setDelimiter(final String delimiter) {
            if (containsLineBreak(delimiter)) {
                throw new IllegalArgumentException("The delimiter cannot be a line break");
            }
            if (delimiter.isEmpty()) {
                throw new IllegalArgumentException("The delimiter cannot be empty");
            }
            this.delimiter = delimiter;
            return this;
        }

        
        public Builder setDuplicateHeaderMode(final DuplicateHeaderMode duplicateHeaderMode) {
          this.duplicateHeaderMode = Objects.requireNonNull(duplicateHeaderMode, "duplicateHeaderMode");
          return this;
        }

        
        public Builder setEscape(final char escapeCharacter) {
            setEscape(Character.valueOf(escapeCharacter));
            return this;
        }

        
        public Builder setEscape(final Character escapeCharacter) {
            if (isLineBreak(escapeCharacter)) {
                throw new IllegalArgumentException("The escape character cannot be a line break");
            }
            this.escapeCharacter = escapeCharacter;
            return this;
        }

        
        public Builder setHeader(final Class<? extends Enum<?>> headerEnum) {
            String[] header = null;
            if (headerEnum != null) {
                final Enum<?>[] enumValues = headerEnum.getEnumConstants();
                header = new String[enumValues.length];
                Arrays.setAll(header, i -> enumValues[i].name());
            }
            return setHeader(header);
        }

        
        public Builder setHeader(final ResultSet resultSet) throws SQLException {
            return setHeader(resultSet != null ? resultSet.getMetaData() : null);
        }

        
        public Builder setHeader(final ResultSetMetaData resultSetMetaData) throws SQLException {
            String[] labels = null;
            if (resultSetMetaData != null) {
                final int columnCount = resultSetMetaData.getColumnCount();
                labels = new String[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    labels[i] = resultSetMetaData.getColumnLabel(i + 1);
                }
            }
            return setHeader(labels);
        }

        
        public Builder setHeader(final String... header) {
            this.headers = CSVFormat.clone(header);
            return this;
        }

        
        public Builder setHeaderComments(final Object... headerComments) {
            this.headerComments = CSVFormat.clone(toStringArray(headerComments));
            return this;
        }

        
        public Builder setHeaderComments(final String... headerComments) {
            this.headerComments = CSVFormat.clone(headerComments);
            return this;
        }

        
        public Builder setIgnoreEmptyLines(final boolean ignoreEmptyLines) {
            this.ignoreEmptyLines = ignoreEmptyLines;
            return this;
        }

        
        public Builder setIgnoreHeaderCase(final boolean ignoreHeaderCase) {
            this.ignoreHeaderCase = ignoreHeaderCase;
            return this;
        }

        
        public Builder setIgnoreSurroundingSpaces(final boolean ignoreSurroundingSpaces) {
            this.ignoreSurroundingSpaces = ignoreSurroundingSpaces;
            return this;
        }

        
        public Builder setNullString(final String nullString) {
            this.nullString = nullString;
            this.quotedNullString = quoteCharacter + nullString + quoteCharacter;
            return this;
        }

        
        public Builder setQuote(final char quoteCharacter) {
            setQuote(Character.valueOf(quoteCharacter));
            return this;
        }

        
        public Builder setQuote(final Character quoteCharacter) {
            if (isLineBreak(quoteCharacter)) {
                throw new IllegalArgumentException("The quoteChar cannot be a line break");
            }
            this.quoteCharacter = quoteCharacter;
            return this;
        }

        
        public Builder setQuoteMode(final QuoteMode quoteMode) {
            this.quoteMode = quoteMode;
            return this;
        }

        
        public Builder setRecordSeparator(final char recordSeparator) {
            this.recordSeparator = String.valueOf(recordSeparator);
            return this;
        }

        
        public Builder setRecordSeparator(final String recordSeparator) {
            this.recordSeparator = recordSeparator;
            return this;
        }

        
        public Builder setSkipHeaderRecord(final boolean skipHeaderRecord) {
            this.skipHeaderRecord = skipHeaderRecord;
            return this;
        }

        
        public Builder setTrailingDelimiter(final boolean trailingDelimiter) {
            this.trailingDelimiter = trailingDelimiter;
            return this;
        }

        
        public Builder setTrim(final boolean trim) {
            this.trim = trim;
            return this;
        }
    }

    
    public enum Predefined {

        
        Default(CSVFormat.DEFAULT),

        
        Excel(CSVFormat.EXCEL),

        
        InformixUnload(CSVFormat.INFORMIX_UNLOAD),

        
        InformixUnloadCsv(CSVFormat.INFORMIX_UNLOAD_CSV),

        
        MongoDBCsv(CSVFormat.MONGODB_CSV),

        
        MongoDBTsv(CSVFormat.MONGODB_TSV),

        
        MySQL(CSVFormat.MYSQL),

        
        Oracle(CSVFormat.ORACLE),

        
        PostgreSQLCsv(CSVFormat.POSTGRESQL_CSV),

        
        PostgreSQLText(CSVFormat.POSTGRESQL_TEXT),

        
        RFC4180(CSVFormat.RFC4180),

        
        TDF(CSVFormat.TDF);

        private final CSVFormat format;

        Predefined(final CSVFormat format) {
            this.format = format;
        }

        
        public CSVFormat getFormat() {
            return format;
        }
    }

    
    public static final CSVFormat DEFAULT = new CSVFormat(COMMA, DOUBLE_QUOTE_CHAR, null, null, null, false, true, CRLF, null, null, null, false, false, false,
            false, false, false, DuplicateHeaderMode.ALLOW_ALL);

    
    // @formatter:off
    public static final CSVFormat EXCEL = DEFAULT.builder()
            .setIgnoreEmptyLines(false)
            .setAllowMissingColumnNames(true)
            .build();
    // @formatter:on

    
    // @formatter:off
    public static final CSVFormat INFORMIX_UNLOAD = DEFAULT.builder()
            .setDelimiter(PIPE)
            .setEscape(BACKSLASH)
            .setQuote(DOUBLE_QUOTE_CHAR)
            .setRecordSeparator(LF)
            .build();
    // @formatter:on

    
    // @formatter:off
    public static final CSVFormat INFORMIX_UNLOAD_CSV = DEFAULT.builder()
            .setDelimiter(COMMA)
            .setQuote(DOUBLE_QUOTE_CHAR)
            .setRecordSeparator(LF)
            .build();
    // @formatter:on

    
    // @formatter:off
    public static final CSVFormat MONGODB_CSV = DEFAULT.builder()
            .setDelimiter(COMMA)
            .setEscape(DOUBLE_QUOTE_CHAR)
            .setQuote(DOUBLE_QUOTE_CHAR)
            .setQuoteMode(QuoteMode.MINIMAL)
            .setSkipHeaderRecord(false)
            .build();
    // @formatter:off

    
    // @formatter:off
    public static final CSVFormat MONGODB_TSV = DEFAULT.builder()
            .setDelimiter(TAB)
            .setEscape(DOUBLE_QUOTE_CHAR)
            .setQuote(DOUBLE_QUOTE_CHAR)
            .setQuoteMode(QuoteMode.MINIMAL)
            .setSkipHeaderRecord(false)
            .build();
    // @formatter:off

    
    // @formatter:off
    public static final CSVFormat MYSQL = DEFAULT.builder()
            .setDelimiter(TAB)
            .setEscape(BACKSLASH)
            .setIgnoreEmptyLines(false)
            .setQuote(null)
            .setRecordSeparator(LF)
            .setNullString(Constants.SQL_NULL_STRING)
            .setQuoteMode(QuoteMode.ALL_NON_NULL)
            .build();
    // @formatter:off

    
    // @formatter:off
    public static final CSVFormat ORACLE = DEFAULT.builder()
            .setDelimiter(COMMA)
            .setEscape(BACKSLASH)
            .setIgnoreEmptyLines(false)
            .setQuote(DOUBLE_QUOTE_CHAR)
            .setNullString(Constants.SQL_NULL_STRING)
            .setTrim(true)
            .setRecordSeparator(System.lineSeparator())
            .setQuoteMode(QuoteMode.MINIMAL)
            .build();
    // @formatter:off

    
    // @formatter:off
    public static final CSVFormat POSTGRESQL_CSV = DEFAULT.builder()
            .setDelimiter(COMMA)
            .setEscape(null)
            .setIgnoreEmptyLines(false)
            .setQuote(DOUBLE_QUOTE_CHAR)
            .setRecordSeparator(LF)
            .setNullString(EMPTY)
            .setQuoteMode(QuoteMode.ALL_NON_NULL)
            .build();
    // @formatter:off

    
    // @formatter:off
    public static final CSVFormat POSTGRESQL_TEXT = DEFAULT.builder()
            .setDelimiter(TAB)
            .setEscape(BACKSLASH)
            .setIgnoreEmptyLines(false)
            .setQuote(null)
            .setRecordSeparator(LF)
            .setNullString(Constants.SQL_NULL_STRING)
            .setQuoteMode(QuoteMode.ALL_NON_NULL)
            .build();
    // @formatter:off

    
    public static final CSVFormat RFC4180 = DEFAULT.builder().setIgnoreEmptyLines(false).build();

    private static final long serialVersionUID = 2L;

    
    // @formatter:off
    public static final CSVFormat TDF = DEFAULT.builder()
            .setDelimiter(TAB)
            .setIgnoreSurroundingSpaces(true)
            .build();
    // @formatter:on

    
    @SafeVarargs
    static <T> T[] clone(final T... values) {
        return values == null ? null : values.clone();
    }

    
    private static boolean contains(final String source, final char searchCh) {
        return Objects.requireNonNull(source, "source").indexOf(searchCh) >= 0;
    }

    
    private static boolean containsLineBreak(final String source) {
        return contains(source, CR) || contains(source, LF);
    }

    static boolean isBlank(final String value) {
        return value == null || value.trim().isEmpty();
    }

    
    private static boolean isLineBreak(final char c) {
        return c == LF || c == CR;
    }

    
    private static boolean isLineBreak(final Character c) {
        return c != null && isLineBreak(c.charValue());
    }

    
    private static boolean isTrimChar(final char ch) {
        return ch <= SP;
    }

    
    private static boolean isTrimChar(final CharSequence charSequence, final int pos) {
        return isTrimChar(charSequence.charAt(pos));
    }

    
    public static CSVFormat newFormat(final char delimiter) {
        return new CSVFormat(String.valueOf(delimiter), null, null, null, null, false, false, null, null, null, null, false, false, false, false, false, false,
                DuplicateHeaderMode.ALLOW_ALL);
    }

    static String[] toStringArray(final Object[] values) {
        if (values == null) {
            return null;
        }
        final String[] strings = new String[values.length];
        Arrays.setAll(strings, i -> Objects.toString(values[i], null));
        return strings;
    }

    static CharSequence trim(final CharSequence charSequence) {
        if (charSequence instanceof String) {
            return ((String) charSequence).trim();
        }
        final int count = charSequence.length();
        int len = count;
        int pos = 0;

        while (pos < len && isTrimChar(charSequence, pos)) {
            pos++;
        }
        while (pos < len && isTrimChar(charSequence, len - 1)) {
            len--;
        }
        return pos > 0 || len < count ? charSequence.subSequence(pos, len) : charSequence;
    }

    
    public static CSVFormat valueOf(final String format) {
        return Predefined.valueOf(format).getFormat();
    }

    private final DuplicateHeaderMode duplicateHeaderMode;

    private final boolean allowMissingColumnNames;

    private final boolean autoFlush;

    
    private final Character commentMarker;

    private final String delimiter;

    
    private final Character escapeCharacter;

    
    private final String[] headers;

    
    private final String[] headerComments;

    private final boolean ignoreEmptyLines;

    
    private final boolean ignoreHeaderCase;

    
    private final boolean ignoreSurroundingSpaces;

    
    private final String nullString;

    
    private final Character quoteCharacter;

    private final String quotedNullString;

    private final QuoteMode quoteMode;

    
    private final String recordSeparator;

    private final boolean skipHeaderRecord;

    private final boolean trailingDelimiter;

    private final boolean trim;

    private CSVFormat(final Builder builder) {
        this.delimiter = builder.delimiter;
        this.quoteCharacter = builder.quoteCharacter;
        this.quoteMode = builder.quoteMode;
        this.commentMarker = builder.commentMarker;
        this.escapeCharacter = builder.escapeCharacter;
        this.ignoreSurroundingSpaces = builder.ignoreSurroundingSpaces;
        this.allowMissingColumnNames = builder.allowMissingColumnNames;
        this.ignoreEmptyLines = builder.ignoreEmptyLines;
        this.recordSeparator = builder.recordSeparator;
        this.nullString = builder.nullString;
        this.headerComments = builder.headerComments;
        this.headers = builder.headers;
        this.skipHeaderRecord = builder.skipHeaderRecord;
        this.ignoreHeaderCase = builder.ignoreHeaderCase;
        this.trailingDelimiter = builder.trailingDelimiter;
        this.trim = builder.trim;
        this.autoFlush = builder.autoFlush;
        this.quotedNullString = builder.quotedNullString;
        this.duplicateHeaderMode = builder.duplicateHeaderMode;
        validate();
    }

    
    private CSVFormat(final String delimiter, final Character quoteChar, final QuoteMode quoteMode, final Character commentStart, final Character escape,
            final boolean ignoreSurroundingSpaces, final boolean ignoreEmptyLines, final String recordSeparator, final String nullString,
            final Object[] headerComments, final String[] header, final boolean skipHeaderRecord, final boolean allowMissingColumnNames,
            final boolean ignoreHeaderCase, final boolean trim, final boolean trailingDelimiter, final boolean autoFlush,
            final DuplicateHeaderMode duplicateHeaderMode) {
        this.delimiter = delimiter;
        this.quoteCharacter = quoteChar;
        this.quoteMode = quoteMode;
        this.commentMarker = commentStart;
        this.escapeCharacter = escape;
        this.ignoreSurroundingSpaces = ignoreSurroundingSpaces;
        this.allowMissingColumnNames = allowMissingColumnNames;
        this.ignoreEmptyLines = ignoreEmptyLines;
        this.recordSeparator = recordSeparator;
        this.nullString = nullString;
        this.headerComments = toStringArray(headerComments);
        this.headers = clone(header);
        this.skipHeaderRecord = skipHeaderRecord;
        this.ignoreHeaderCase = ignoreHeaderCase;
        this.trailingDelimiter = trailingDelimiter;
        this.trim = trim;
        this.autoFlush = autoFlush;
        this.quotedNullString = quoteCharacter + nullString + quoteCharacter;
        this.duplicateHeaderMode = duplicateHeaderMode;
        validate();
    }

    private void append(final char c, final Appendable appendable) throws IOException {
        //try {
            appendable.append(c);
        //} catch (final IOException e) {
        //    throw new UncheckedIOException(e);
        //}
    }

    private void append(final CharSequence csq, final Appendable appendable) throws IOException {
        //try {
            appendable.append(csq);
        //} catch (final IOException e) {
        //    throw new UncheckedIOException(e);
        //}
    }

    
    public Builder builder() {
        return Builder.create(this);
    }

    
    CSVFormat copy() {
        return builder().build();
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        final CSVFormat other = (CSVFormat) obj;
        return duplicateHeaderMode == other.duplicateHeaderMode && allowMissingColumnNames == other.allowMissingColumnNames &&
                autoFlush == other.autoFlush && Objects.equals(commentMarker, other.commentMarker) && Objects.equals(delimiter, other.delimiter) &&
                Objects.equals(escapeCharacter, other.escapeCharacter) && Arrays.equals(headers, other.headers) &&
                Arrays.equals(headerComments, other.headerComments) && ignoreEmptyLines == other.ignoreEmptyLines &&
                ignoreHeaderCase == other.ignoreHeaderCase && ignoreSurroundingSpaces == other.ignoreSurroundingSpaces &&
                Objects.equals(nullString, other.nullString) && Objects.equals(quoteCharacter, other.quoteCharacter) && quoteMode == other.quoteMode &&
                Objects.equals(quotedNullString, other.quotedNullString) && Objects.equals(recordSeparator, other.recordSeparator) &&
                skipHeaderRecord == other.skipHeaderRecord && trailingDelimiter == other.trailingDelimiter && trim == other.trim;
    }

    
    public String format(final Object... values) {
        return Uncheck.get(() -> format_(values));
    }

    private String format_(final Object... values) throws IOException {
        final StringWriter out = new StringWriter();
        try (CSVPrinter csvPrinter = new CSVPrinter(out, this)) {
            csvPrinter.printRecord(values);
            final String res = out.toString();
            final int len = recordSeparator != null ? res.length() - recordSeparator.length() : res.length();
            return res.substring(0, len);
        }
    }

    
    @Deprecated
    public boolean getAllowDuplicateHeaderNames() {
        return duplicateHeaderMode == DuplicateHeaderMode.ALLOW_ALL;
    }

    
    public boolean getAllowMissingColumnNames() {
        return allowMissingColumnNames;
    }

    
    public boolean getAutoFlush() {
        return autoFlush;
    }

    
    public Character getCommentMarker() {
        return commentMarker;
    }

    
    @Deprecated
    public char getDelimiter() {
        return delimiter.charAt(0);
    }

    
    public String getDelimiterString() {
        return delimiter;
    }

    
    public DuplicateHeaderMode getDuplicateHeaderMode() {
        return duplicateHeaderMode;
    }

    
    public Character getEscapeCharacter() {
        return escapeCharacter;
    }

    
    public String[] getHeader() {
        return headers != null ? headers.clone() : null;
    }

    
    public String[] getHeaderComments() {
        return headerComments != null ? headerComments.clone() : null;
    }

    
    public boolean getIgnoreEmptyLines() {
        return ignoreEmptyLines;
    }

    
    public boolean getIgnoreHeaderCase() {
        return ignoreHeaderCase;
    }

    
    public boolean getIgnoreSurroundingSpaces() {
        return ignoreSurroundingSpaces;
    }

    
    public String getNullString() {
        return nullString;
    }

    
    public Character getQuoteCharacter() {
        return quoteCharacter;
    }

    
    public QuoteMode getQuoteMode() {
        return quoteMode;
    }

    
    public String getRecordSeparator() {
        return recordSeparator;
    }

    
    public boolean getSkipHeaderRecord() {
        return skipHeaderRecord;
    }

    
    public boolean getTrailingDelimiter() {
        return trailingDelimiter;
    }

    
    public boolean getTrim() {
        return trim;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(headers);
        result = prime * result + Arrays.hashCode(headerComments);
        return prime * result + Objects.hash(duplicateHeaderMode, allowMissingColumnNames, autoFlush, commentMarker, delimiter, escapeCharacter,
                ignoreEmptyLines, ignoreHeaderCase, ignoreSurroundingSpaces, nullString, quoteCharacter, quoteMode, quotedNullString, recordSeparator,
                skipHeaderRecord, trailingDelimiter, trim);
    }

    
    public boolean isCommentMarkerSet() {
        return commentMarker != null;
    }

    
    private boolean isDelimiter(final char ch, final CharSequence charSeq, final int startIndex, final char[] delimiter, final int delimiterLength) {
        if (ch != delimiter[0]) {
            return false;
        }
        final int len = charSeq.length();
        if (startIndex + delimiterLength > len) {
            return false;
        }
        for (int i = 1; i < delimiterLength; i++) {
            if (charSeq.charAt(startIndex + i) != delimiter[i]) {
                return false;
            }
        }
        return true;
    }

    
    public boolean isEscapeCharacterSet() {
        return escapeCharacter != null;
    }

    
    public boolean isNullStringSet() {
        return nullString != null;
    }

    
    public boolean isQuoteCharacterSet() {
        return quoteCharacter != null;
    }

    
    public CSVParser parse(final Reader reader) throws IOException {
        return new CSVParser(reader, this);
    }

    
    public CSVPrinter print(final Appendable out) throws IOException {
        return new CSVPrinter(out, this);
    }

    
    @SuppressWarnings("resource")
    public CSVPrinter print(final File out, final Charset charset) throws IOException {
        // The writer will be closed when close() is called.
        return new CSVPrinter(new OutputStreamWriter(new FileOutputStream(out), charset), this);
    }

    
    public synchronized void print(final Object value, final Appendable out, final boolean newRecord) throws IOException {
        // null values are considered empty
        // Only call CharSequence.toString() if you have to, helps GC-free use cases.
        CharSequence charSequence;
        if (value == null) {
            // https://issues.apache.org/jira/browse/CSV-203
            if (null == nullString) {
                charSequence = EMPTY;
            } else if (QuoteMode.ALL == quoteMode) {
                charSequence = quotedNullString;
            } else {
                charSequence = nullString;
            }
        } else if (value instanceof CharSequence) {
            charSequence = (CharSequence) value;
        } else if (value instanceof Reader) {
            print((Reader) value, out, newRecord);
            return;
        } else {
            charSequence = value.toString();
        }
        charSequence = getTrim() ? trim(charSequence) : charSequence;
        print(value, charSequence, out, newRecord);
    }

    private synchronized void print(final Object object, final CharSequence value, final Appendable out, final boolean newRecord) throws IOException {
        final int offset = 0;
        final int len = value.length();
        if (!newRecord) {
            out.append(getDelimiterString());
        }
        if (object == null) {
            out.append(value);
        } else if (isQuoteCharacterSet()) {
            // The original object is needed so can check for Number
            printWithQuotes(object, value, out, newRecord);
        } else if (isEscapeCharacterSet()) {
            printWithEscapes(value, out);
        } else {
            out.append(value, offset, len);
        }
    }

    
    @SuppressWarnings("resource")
    public CSVPrinter print(final Path out, final Charset charset) throws IOException {
        return print(Files.newBufferedWriter(out, charset));
    }

    private void print(final Reader reader, final Appendable out, final boolean newRecord) throws IOException {
        // Reader is never null
        if (!newRecord) {
            append(getDelimiterString(), out);
        }
        if (isQuoteCharacterSet()) {
            printWithQuotes(reader, out);
        } else if (isEscapeCharacterSet()) {
            printWithEscapes(reader, out);
        } else if (out instanceof Writer) {
            IOUtils.copyLarge(reader, (Writer) out);
        } else {
            IOUtils.copy(reader, out);
        }

    }

    
    public CSVPrinter printer() throws IOException {
        return new CSVPrinter(System.out, this);
    }

    
    public synchronized void println(final Appendable appendable) throws IOException {
        if (getTrailingDelimiter()) {
            append(getDelimiterString(), appendable);
        }
        if (recordSeparator != null) {
            append(recordSeparator, appendable);
        }
    }

    
    public synchronized void printRecord(final Appendable appendable, final Object... values) throws IOException {
        for (int i = 0; i < values.length; i++) {
            print(values[i], appendable, i == 0);
        }
        println(appendable);
    }

    
    private void printWithEscapes(final CharSequence charSeq, final Appendable appendable) throws IOException {
        int start = 0;
        int pos = 0;
        final int end = charSeq.length();

        final char[] delim = getDelimiterString().toCharArray();
        final int delimLength = delim.length;
        final char escape = getEscapeCharacter().charValue();

        while (pos < end) {
            char c = charSeq.charAt(pos);
            final boolean isDelimiterStart = isDelimiter(c, charSeq, pos, delim, delimLength);
            if (c == CR || c == LF || c == escape || isDelimiterStart) {
                // write out segment up until this char
                if (pos > start) {
                    appendable.append(charSeq, start, pos);
                }
                if (c == LF) {
                    c = 'n';
                } else if (c == CR) {
                    c = 'r';
                }

                appendable.append(escape);
                appendable.append(c);

                if (isDelimiterStart) {
                    for (int i = 1; i < delimLength; i++) {
                        pos++;
                        c = charSeq.charAt(pos);
                        appendable.append(escape);
                        appendable.append(c);
                    }
                }

                start = pos + 1; // start on the current char after this one
            }
            pos++;
        }

        // write last segment
        if (pos > start) {
            appendable.append(charSeq, start, pos);
        }
    }

    private void printWithEscapes(final Reader reader, final Appendable appendable) throws IOException {
        int start = 0;
        int pos = 0;

        @SuppressWarnings("resource") // Temp reader on input reader.
        final ExtendedBufferedReader bufferedReader = new ExtendedBufferedReader(reader);
        final char[] delim = getDelimiterString().toCharArray();
        final int delimLength = delim.length;
        final char escape = getEscapeCharacter().charValue();
        final StringBuilder builder = new StringBuilder(IOUtils.DEFAULT_BUFFER_SIZE);

        int c;
        while (-1 != (c = bufferedReader.read())) {
            builder.append((char) c);
            final boolean isDelimiterStart = isDelimiter((char) c, builder.toString() + new String(bufferedReader.lookAhead(delimLength - 1)), pos, delim,
                    delimLength);
            if (c == CR || c == LF || c == escape || isDelimiterStart) {
                // write out segment up until this char
                if (pos > start) {
                    append(builder.substring(start, pos), appendable);
                    builder.setLength(0);
                    pos = -1;
                }
                if (c == LF) {
                    c = 'n';
                } else if (c == CR) {
                    c = 'r';
                }

                append(escape, appendable);
                append((char) c, appendable);

                if (isDelimiterStart) {
                    for (int i = 1; i < delimLength; i++) {
                        c = bufferedReader.read();
                        append(escape, appendable);
                        append((char) c, appendable);
                    }
                }

                start = pos + 1; // start on the current char after this one
            }
            pos++;
        }

        // write last segment
        if (pos > start) {
            append(builder.substring(start, pos), appendable);
        }
    }

    
    // the original object is needed so can check for Number
    private void printWithQuotes(final Object object, final CharSequence charSeq, final Appendable out, final boolean newRecord) throws IOException {
        boolean quote = false;
        int start = 0;
        int pos = 0;
        final int len = charSeq.length();

        final char[] delim = getDelimiterString().toCharArray();
        final int delimLength = delim.length;
        final char quoteChar = getQuoteCharacter().charValue();
        // If escape char not specified, default to the quote char
        // This avoids having to keep checking whether there is an escape character
        // at the cost of checking against quote twice
        final char escapeChar = isEscapeCharacterSet() ? getEscapeCharacter().charValue() : quoteChar;

        QuoteMode quoteModePolicy = getQuoteMode();
        if (quoteModePolicy == null) {
            quoteModePolicy = QuoteMode.MINIMAL;
        }
        switch (quoteModePolicy) {
        case ALL:
        case ALL_NON_NULL:
            quote = true;
            break;
        case NON_NUMERIC:
            quote = !(object instanceof Number);
            break;
        case NONE:
            // Use the existing escaping code
            printWithEscapes(charSeq, out);
            return;
        case MINIMAL:
            if (len <= 0) {
                // Always quote an empty token that is the first
                // on the line, as it may be the only thing on the
                // line. If it were not quoted in that case,
                // an empty line has no tokens.
                if (newRecord) {
                    quote = true;
                }
            } else {
                char c = charSeq.charAt(pos);

                if (c <= COMMENT) {
                    // Some other chars at the start of a value caused the parser to fail, so for now
                    // encapsulate if we start in anything less than '#'. We are being conservative
                    // by including the default comment char too.
                    quote = true;
                } else {
                    while (pos < len) {
                        c = charSeq.charAt(pos);
                        if (c == LF || c == CR || c == quoteChar || c == escapeChar || isDelimiter(c, charSeq, pos, delim, delimLength)) {
                            quote = true;
                            break;
                        }
                        pos++;
                    }

                    if (!quote) {
                        pos = len - 1;
                        c = charSeq.charAt(pos);
                        // Some other chars at the end caused the parser to fail, so for now
                        // encapsulate if we end in anything less than ' '
                        if (isTrimChar(c)) {
                            quote = true;
                        }
                    }
                }
            }

            if (!quote) {
                // No encapsulation needed - write out the original value
                out.append(charSeq, start, len);
                return;
            }
            break;
        default:
            throw new IllegalStateException("Unexpected Quote value: " + quoteModePolicy);
        }

        if (!quote) {
            // No encapsulation needed - write out the original value
            out.append(charSeq, start, len);
            return;
        }

        // We hit something that needed encapsulation
        out.append(quoteChar);

        // Pick up where we left off: pos should be positioned on the first character that caused
        // the need for encapsulation.
        while (pos < len) {
            final char c = charSeq.charAt(pos);
            if (c == quoteChar || c == escapeChar) {
                // write out the chunk up until this point
                out.append(charSeq, start, pos);
                out.append(escapeChar); // now output the escape
                start = pos; // and restart with the matched char
            }
            pos++;
        }

        // Write the last segment
        out.append(charSeq, start, pos);
        out.append(quoteChar);
    }

    
    private void printWithQuotes(final Reader reader, final Appendable appendable) throws IOException {

        if (getQuoteMode() == QuoteMode.NONE) {
            printWithEscapes(reader, appendable);
            return;
        }

        int pos = 0;

        final char quote = getQuoteCharacter().charValue();
        final StringBuilder builder = new StringBuilder(IOUtils.DEFAULT_BUFFER_SIZE);

        append(quote, appendable);

        int c;
        while (-1 != (c = reader.read())) {
            builder.append((char) c);
            if (c == quote) {
                // write out segment up until this char
                if (pos > 0) {
                    append(builder.substring(0, pos), appendable);
                    append(quote, appendable);
                    builder.setLength(0);
                    pos = -1;
                }

                append((char) c, appendable);
            }
            pos++;
        }

        // write last segment
        if (pos > 0) {
            append(builder.substring(0, pos), appendable);
        }

        append(quote, appendable);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Delimiter=<").append(delimiter).append('>');
        if (isEscapeCharacterSet()) {
            sb.append(' ');
            sb.append("Escape=<").append(escapeCharacter).append('>');
        }
        if (isQuoteCharacterSet()) {
            sb.append(' ');
            sb.append("QuoteChar=<").append(quoteCharacter).append('>');
        }
        if (quoteMode != null) {
            sb.append(' ');
            sb.append("QuoteMode=<").append(quoteMode).append('>');
        }
        if (isCommentMarkerSet()) {
            sb.append(' ');
            sb.append("CommentStart=<").append(commentMarker).append('>');
        }
        if (isNullStringSet()) {
            sb.append(' ');
            sb.append("NullString=<").append(nullString).append('>');
        }
        if (recordSeparator != null) {
            sb.append(' ');
            sb.append("RecordSeparator=<").append(recordSeparator).append('>');
        }
        if (getIgnoreEmptyLines()) {
            sb.append(" EmptyLines:ignored");
        }
        if (getIgnoreSurroundingSpaces()) {
            sb.append(" SurroundingSpaces:ignored");
        }
        if (getIgnoreHeaderCase()) {
            sb.append(" IgnoreHeaderCase:ignored");
        }
        sb.append(" SkipHeaderRecord:").append(skipHeaderRecord);
        if (headerComments != null) {
            sb.append(' ');
            sb.append("HeaderComments:").append(Arrays.toString(headerComments));
        }
        if (headers != null) {
            sb.append(' ');
            sb.append("Header:").append(Arrays.toString(headers));
        }
        return sb.toString();
    }

    String trim(final String value) {
        return getTrim() ? value.trim() : value;
    }

    
    private void validate() throws IllegalArgumentException {
        if (containsLineBreak(delimiter)) {
            throw new IllegalArgumentException("The delimiter cannot be a line break");
        }

        if (quoteCharacter != null && contains(delimiter, quoteCharacter.charValue())) {
            throw new IllegalArgumentException("The quoteChar character and the delimiter cannot be the same ('" + quoteCharacter + "')");
        }

        if (escapeCharacter != null && contains(delimiter, escapeCharacter.charValue())) {
            throw new IllegalArgumentException("The escape character and the delimiter cannot be the same ('" + escapeCharacter + "')");
        }

        if (commentMarker != null && contains(delimiter, commentMarker.charValue())) {
            throw new IllegalArgumentException("The comment start character and the delimiter cannot be the same ('" + commentMarker + "')");
        }

        if (quoteCharacter != null && quoteCharacter.equals(commentMarker)) {
            throw new IllegalArgumentException("The comment start character and the quoteChar cannot be the same ('" + commentMarker + "')");
        }

        if (escapeCharacter != null && escapeCharacter.equals(commentMarker)) {
            throw new IllegalArgumentException("The comment start and the escape character cannot be the same ('" + commentMarker + "')");
        }

        if (escapeCharacter == null && quoteMode == QuoteMode.NONE) {
            throw new IllegalArgumentException("Quote mode set to NONE but no escape character is set");
        }

        // Validate headers
        if (headers != null && duplicateHeaderMode != DuplicateHeaderMode.ALLOW_ALL) {
            final Set<String> dupCheckSet = new HashSet<>(headers.length);
            final boolean emptyDuplicatesAllowed = duplicateHeaderMode == DuplicateHeaderMode.ALLOW_EMPTY;
            for (final String header : headers) {
                final boolean blank = isBlank(header);
                // Sanitise all empty headers to the empty string "" when checking duplicates
                final boolean containsHeader = !dupCheckSet.add(blank ? "" : header);
                if (containsHeader && !(blank && emptyDuplicatesAllowed)) {
                    throw new IllegalArgumentException(
                        String.format(
                            "The header contains a duplicate name: \"%s\" in %s. If this is valid then use CSVFormat.Builder.setDuplicateHeaderMode().",
                            header, Arrays.toString(headers)));
                }
            }
        }
    }

    
    @Deprecated
    public CSVFormat withAllowDuplicateHeaderNames() {
        return builder().setDuplicateHeaderMode(DuplicateHeaderMode.ALLOW_ALL).build();
    }

    
    @Deprecated
    public CSVFormat withAllowDuplicateHeaderNames(final boolean allowDuplicateHeaderNames) {
        final DuplicateHeaderMode mode = allowDuplicateHeaderNames ? DuplicateHeaderMode.ALLOW_ALL : DuplicateHeaderMode.ALLOW_EMPTY;
        return builder().setDuplicateHeaderMode(mode).build();
    }

    
    @Deprecated
    public CSVFormat withAllowMissingColumnNames() {
        return builder().setAllowMissingColumnNames(true).build();
    }

    
    @Deprecated
    public CSVFormat withAllowMissingColumnNames(final boolean allowMissingColumnNames) {
        return builder().setAllowMissingColumnNames(allowMissingColumnNames).build();
    }

    
    @Deprecated
    public CSVFormat withAutoFlush(final boolean autoFlush) {
        return builder().setAutoFlush(autoFlush).build();
    }

    
    @Deprecated
    public CSVFormat withCommentMarker(final char commentMarker) {
        return builder().setCommentMarker(commentMarker).build();
    }

    
    @Deprecated
    public CSVFormat withCommentMarker(final Character commentMarker) {
        return builder().setCommentMarker(commentMarker).build();
    }

    
    @Deprecated
    public CSVFormat withDelimiter(final char delimiter) {
        return builder().setDelimiter(delimiter).build();
    }

    
    @Deprecated
    public CSVFormat withEscape(final char escape) {
        return builder().setEscape(escape).build();
    }

    
    @Deprecated
    public CSVFormat withEscape(final Character escape) {
        return builder().setEscape(escape).build();
    }

    
    @Deprecated
    public CSVFormat withFirstRecordAsHeader() {
        // @formatter:off
        return builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .build();
        // @formatter:on
    }

    
    @Deprecated
    public CSVFormat withHeader(final Class<? extends Enum<?>> headerEnum) {
        return builder().setHeader(headerEnum).build();
    }

    
    @Deprecated
    public CSVFormat withHeader(final ResultSet resultSet) throws SQLException {
        return builder().setHeader(resultSet).build();
    }

    
    @Deprecated
    public CSVFormat withHeader(final ResultSetMetaData resultSetMetaData) throws SQLException {
        return builder().setHeader(resultSetMetaData).build();
    }

    
    @Deprecated
    public CSVFormat withHeader(final String... header) {
        return builder().setHeader(header).build();
    }

    
    @Deprecated
    public CSVFormat withHeaderComments(final Object... headerComments) {
        return builder().setHeaderComments(headerComments).build();
    }

    
    @Deprecated
    public CSVFormat withIgnoreEmptyLines() {
        return builder().setIgnoreEmptyLines(true).build();
    }

    
    @Deprecated
    public CSVFormat withIgnoreEmptyLines(final boolean ignoreEmptyLines) {
        return builder().setIgnoreEmptyLines(ignoreEmptyLines).build();
    }

    
    @Deprecated
    public CSVFormat withIgnoreHeaderCase() {
        return builder().setIgnoreHeaderCase(true).build();
    }

    
    @Deprecated
    public CSVFormat withIgnoreHeaderCase(final boolean ignoreHeaderCase) {
        return builder().setIgnoreHeaderCase(ignoreHeaderCase).build();
    }

    
    @Deprecated
    public CSVFormat withIgnoreSurroundingSpaces() {
        return builder().setIgnoreSurroundingSpaces(true).build();
    }

    
    @Deprecated
    public CSVFormat withIgnoreSurroundingSpaces(final boolean ignoreSurroundingSpaces) {
        return builder().setIgnoreSurroundingSpaces(ignoreSurroundingSpaces).build();
    }

    
    @Deprecated
    public CSVFormat withNullString(final String nullString) {
        return builder().setNullString(nullString).build();
    }

    
    @Deprecated
    public CSVFormat withQuote(final char quoteChar) {
        return builder().setQuote(quoteChar).build();
    }

    
    @Deprecated
    public CSVFormat withQuote(final Character quoteChar) {
        return builder().setQuote(quoteChar).build();
    }

    
    @Deprecated
    public CSVFormat withQuoteMode(final QuoteMode quoteMode) {
        return builder().setQuoteMode(quoteMode).build();
    }

    
    @Deprecated
    public CSVFormat withRecordSeparator(final char recordSeparator) {
        return builder().setRecordSeparator(recordSeparator).build();
    }

    
    @Deprecated
    public CSVFormat withRecordSeparator(final String recordSeparator) {
        return builder().setRecordSeparator(recordSeparator).build();
    }

    
    @Deprecated
    public CSVFormat withSkipHeaderRecord() {
        return builder().setSkipHeaderRecord(true).build();
    }

    
    @Deprecated
    public CSVFormat withSkipHeaderRecord(final boolean skipHeaderRecord) {
        return builder().setSkipHeaderRecord(skipHeaderRecord).build();
    }

    
    @Deprecated
    public CSVFormat withSystemRecordSeparator() {
        return builder().setRecordSeparator(System.lineSeparator()).build();
    }

    
    @Deprecated
    public CSVFormat withTrailingDelimiter() {
        return builder().setTrailingDelimiter(true).build();
    }

    
    @Deprecated
    public CSVFormat withTrailingDelimiter(final boolean trailingDelimiter) {
        return builder().setTrailingDelimiter(trailingDelimiter).build();
    }

    
    @Deprecated
    public CSVFormat withTrim() {
        return builder().setTrim(true).build();
    }

    
    @Deprecated
    public CSVFormat withTrim(final boolean trim) {
        return builder().setTrim(trim).build();
    }
}
