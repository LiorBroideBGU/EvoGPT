

package org.apache.commons.csv;

import static org.apache.commons.csv.Token.Type.TOKEN;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.io.function.Uncheck;


public final class CSVParser implements Iterable<CSVRecord>, Closeable {

    final class CSVRecordIterator implements Iterator<CSVRecord> {
        private CSVRecord current;

        private CSVRecord getNextRecord() {
            return Uncheck.get(CSVParser.this::nextRecord);
        }

        @Override
        public boolean hasNext() {
            if (CSVParser.this.isClosed()) {
                return false;
            }
            if (this.current == null) {
                this.current = this.getNextRecord();
            }

            return this.current != null;
        }

        @Override
        public CSVRecord next() {
            if (CSVParser.this.isClosed()) {
                throw new NoSuchElementException("CSVParser has been closed");
            }
            CSVRecord next = this.current;
            this.current = null;

            if (next == null) {
                // hasNext() wasn't called before
                next = this.getNextRecord();
                if (next == null) {
                    throw new NoSuchElementException("No more CSV records available");
                }
            }

            return next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    
    private static final class Headers {

        
        final Map<String, Integer> headerMap;

        
        final List<String> headerNames;

        Headers(final Map<String, Integer> headerMap, final List<String> headerNames) {
            this.headerMap = headerMap;
            this.headerNames = headerNames;
        }
    }

    
    public static CSVParser parse(final File file, final Charset charset, final CSVFormat format) throws IOException {
        Objects.requireNonNull(file, "file");
        return parse(file.toPath(), charset, format);
    }

    
    @SuppressWarnings("resource")
    public static CSVParser parse(final InputStream inputStream, final Charset charset, final CSVFormat format)
            throws IOException {
        Objects.requireNonNull(inputStream, "inputStream");
        Objects.requireNonNull(format, "format");
        return parse(new InputStreamReader(inputStream, charset), format);
    }

    
    @SuppressWarnings("resource")
    public static CSVParser parse(final Path path, final Charset charset, final CSVFormat format) throws IOException {
        Objects.requireNonNull(path, "path");
        Objects.requireNonNull(format, "format");
        return parse(Files.newInputStream(path), charset, format);
    }

    
    public static CSVParser parse(final Reader reader, final CSVFormat format) throws IOException {
        return new CSVParser(reader, format);
    }

    
    public static CSVParser parse(final String string, final CSVFormat format) throws IOException {
        Objects.requireNonNull(string, "string");
        Objects.requireNonNull(format, "format");

        return new CSVParser(new StringReader(string), format);
    }

    
    @SuppressWarnings("resource")
    public static CSVParser parse(final URL url, final Charset charset, final CSVFormat format) throws IOException {
        Objects.requireNonNull(url, "url");
        Objects.requireNonNull(charset, "charset");
        Objects.requireNonNull(format, "format");

        return new CSVParser(new InputStreamReader(url.openStream(), charset), format);
    }

    private String headerComment;

    private String trailerComment;

    private final CSVFormat format;

    private final Headers headers;

    private final Lexer lexer;

    private final CSVRecordIterator csvRecordIterator;

    
    private final List<String> recordList = new ArrayList<>();

    
    private long recordNumber;

    
    private final long characterOffset;

    private final Token reusableToken = new Token();

    
    public CSVParser(final Reader reader, final CSVFormat format) throws IOException {
        this(reader, format, 0, 1);
    }

    
    @SuppressWarnings("resource")
    public CSVParser(final Reader reader, final CSVFormat format, final long characterOffset, final long recordNumber)
        throws IOException {
        Objects.requireNonNull(reader, "reader");
        Objects.requireNonNull(format, "format");

        this.format = format.copy();
        this.lexer = new Lexer(format, new ExtendedBufferedReader(reader));
        this.csvRecordIterator = new CSVRecordIterator();
        this.headers = createHeaders();
        this.characterOffset = characterOffset;
        this.recordNumber = recordNumber - 1;
    }

    private void addRecordValue(final boolean lastRecord) {
        final String input = this.format.trim(this.reusableToken.content.toString());
        if (lastRecord && input.isEmpty() && this.format.getTrailingDelimiter()) {
            return;
        }
        this.recordList.add(handleNull(input));
    }

    
    @Override
    public void close() throws IOException {
        if (this.lexer != null) {
            this.lexer.close();
        }
    }

    private Map<String, Integer> createEmptyHeaderMap() {
        return this.format.getIgnoreHeaderCase() ?
                new TreeMap<>(String.CASE_INSENSITIVE_ORDER) :
                new LinkedHashMap<>();
    }

    
    private Headers createHeaders() throws IOException {
        Map<String, Integer> hdrMap = null;
        List<String> headerNames = null;
        final String[] formatHeader = this.format.getHeader();
        if (formatHeader != null) {
            hdrMap = createEmptyHeaderMap();
            String[] headerRecord = null;
            if (formatHeader.length == 0) {
                // read the header from the first line of the file
                final CSVRecord nextRecord = this.nextRecord();
                if (nextRecord != null) {
                    headerRecord = nextRecord.values();
                    headerComment = nextRecord.getComment();
                }
            } else {
                if (this.format.getSkipHeaderRecord()) {
                    final CSVRecord nextRecord = this.nextRecord();
                    if (nextRecord != null) {
                        headerComment = nextRecord.getComment();
                    }
                }
                headerRecord = formatHeader;
            }

            // build the name to index mappings
            if (headerRecord != null) {
                // Track an occurrence of a null, empty or blank header.
                boolean observedMissing = false;
                for (int i = 0; i < headerRecord.length; i++) {
                    final String header = headerRecord[i];
                    final boolean blankHeader = CSVFormat.isBlank(header);
                    if (blankHeader && !this.format.getAllowMissingColumnNames()) {
                        throw new IllegalArgumentException(
                            "A header name is missing in " + Arrays.toString(headerRecord));
                    }

                    final boolean containsHeader = blankHeader ? observedMissing : hdrMap.containsKey(header);
                    final DuplicateHeaderMode headerMode = this.format.getDuplicateHeaderMode();
                    final boolean duplicatesAllowed = headerMode == DuplicateHeaderMode.ALLOW_ALL;
                    final boolean emptyDuplicatesAllowed = headerMode == DuplicateHeaderMode.ALLOW_EMPTY;

                    if (containsHeader && !duplicatesAllowed && !(blankHeader && emptyDuplicatesAllowed)) {
                        throw new IllegalArgumentException(
                            String.format(
                                "The header contains a duplicate name: \"%s\" in %s. If this is valid then use CSVFormat.Builder.setDuplicateHeaderMode().",
                                header, Arrays.toString(headerRecord)));
                    }
                    observedMissing |= blankHeader;
                    if (header != null) {
                        hdrMap.put(header, Integer.valueOf(i));
                        if (headerNames == null) {
                            headerNames = new ArrayList<>(headerRecord.length);
                        }
                        headerNames.add(header);
                    }
                }
            }
        }
        if (headerNames == null) {
            headerNames = Collections.emptyList(); // immutable
        } else {
            headerNames = Collections.unmodifiableList(headerNames);
        }
        return new Headers(hdrMap, headerNames);
    }

    
    public long getCurrentLineNumber() {
        return this.lexer.getCurrentLineNumber();
    }

    
    public String getFirstEndOfLine() {
        return lexer.getFirstEol();
    }

    
    public String getHeaderComment() {
        return headerComment;
    }

    
    public Map<String, Integer> getHeaderMap() {
        if (this.headers.headerMap == null) {
            return null;
        }
        final Map<String, Integer> map = createEmptyHeaderMap();
        map.putAll(this.headers.headerMap);
        return map;
    }

    
    Map<String, Integer> getHeaderMapRaw() {
        return this.headers.headerMap;
    }

    
    public List<String> getHeaderNames() {
        return Collections.unmodifiableList(headers.headerNames);
    }

    
    public long getRecordNumber() {
        return this.recordNumber;
    }

    
    public List<CSVRecord> getRecords() {
        return stream().collect(Collectors.toList());
    }

    
    public String getTrailerComment() {
        return trailerComment;
    }

    
    private String handleNull(final String input) {
        final boolean isQuoted = this.reusableToken.isQuoted;
        final String nullString = format.getNullString();
        final boolean strictQuoteMode = isStrictQuoteMode();
        if (input.equals(nullString)) {
            // nullString = NULL(String), distinguish between "NULL" and NULL in ALL_NON_NULL or NON_NUMERIC quote mode
            return strictQuoteMode && isQuoted ? input : null;
        }
        // don't set nullString, distinguish between "" and ,, (absent values) in All_NON_NULL or NON_NUMERIC quote mode
        return strictQuoteMode && nullString == null && input.isEmpty() && !isQuoted ? null : input;
    }

    
    public boolean hasHeaderComment() {
        return headerComment != null;
    }

    
    public boolean hasTrailerComment() {
        return trailerComment != null;
    }

    
    public boolean isClosed() {
        return this.lexer.isClosed();
    }

    
    private boolean isStrictQuoteMode() {
        return this.format.getQuoteMode() == QuoteMode.ALL_NON_NULL ||
               this.format.getQuoteMode() == QuoteMode.NON_NUMERIC;
    }

    
    @Override
    public Iterator<CSVRecord> iterator() {
        return csvRecordIterator;
    }

    
    CSVRecord nextRecord() throws IOException {
        CSVRecord result = null;
        this.recordList.clear();
        StringBuilder sb = null;
        final long startCharPosition = lexer.getCharacterPosition() + this.characterOffset;
        do {
            this.reusableToken.reset();
            this.lexer.nextToken(this.reusableToken);
            switch (this.reusableToken.type) {
            case TOKEN:
                this.addRecordValue(false);
                break;
            case EORECORD:
                this.addRecordValue(true);
                break;
            case EOF:
                if (this.reusableToken.isReady) {
                    this.addRecordValue(true);
                } else if (sb != null) {
                    trailerComment = sb.toString();
                }
                break;
            case INVALID:
                throw new IOException("(line " + this.getCurrentLineNumber() + ") invalid parse sequence");
            case COMMENT: // Ignored currently
                if (sb == null) { // first comment for this record
                    sb = new StringBuilder();
                } else {
                    sb.append(Constants.LF);
                }
                sb.append(this.reusableToken.content);
                this.reusableToken.type = TOKEN; // Read another token
                break;
            default:
                throw new IllegalStateException("Unexpected Token type: " + this.reusableToken.type);
            }
        } while (this.reusableToken.type == TOKEN);

        if (!this.recordList.isEmpty()) {
            this.recordNumber++;
            final String comment = sb == null ? null : sb.toString();
            result = new CSVRecord(this, this.recordList.toArray(Constants.EMPTY_STRING_ARRAY), comment,
                this.recordNumber, startCharPosition);
        }
        return result;
    }

    
    public Stream<CSVRecord> stream() {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator(), Spliterator.ORDERED), false);
    }

}
