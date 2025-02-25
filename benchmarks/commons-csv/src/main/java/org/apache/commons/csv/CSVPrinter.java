

package org.apache.commons.csv;

import static org.apache.commons.csv.Constants.CR;
import static org.apache.commons.csv.Constants.LF;
import static org.apache.commons.csv.Constants.SP;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.io.function.IOStream;


public final class CSVPrinter implements Flushable, Closeable {

    
    private final Appendable appendable;

    private final CSVFormat format;

    
    private boolean newRecord = true;

    
    public CSVPrinter(final Appendable appendable, final CSVFormat format) throws IOException {
        Objects.requireNonNull(appendable, "appendable");
        Objects.requireNonNull(format, "format");

        this.appendable = appendable;
        this.format = format.copy();
        // TODO: Is it a good idea to do this here instead of on the first call to a print method?
        // It seems a pain to have to track whether the header has already been printed or not.
        final String[] headerComments = format.getHeaderComments();
        if (headerComments != null) {
            for (final String line : headerComments) {
                this.printComment(line);
            }
        }
        if (format.getHeader() != null && !format.getSkipHeaderRecord()) {
            this.printRecord((Object[]) format.getHeader());
        }
    }

    @Override
    public void close() throws IOException {
        close(false);
    }

    
    public void close(final boolean flush) throws IOException {
        if (flush || format.getAutoFlush()) {
            flush();
        }
        if (appendable instanceof Closeable) {
            ((Closeable) appendable).close();
        }
    }

    
    @Override
    public void flush() throws IOException {
        if (appendable instanceof Flushable) {
            ((Flushable) appendable).flush();
        }
    }

    
    public Appendable getOut() {
        return this.appendable;
    }

    
    public synchronized void print(final Object value) throws IOException {
        format.print(value, appendable, newRecord);
        newRecord = false;
    }

    
    public synchronized void printComment(final String comment) throws IOException {
        if (comment == null || !format.isCommentMarkerSet()) {
            return;
        }
        if (!newRecord) {
            println();
        }
        appendable.append(format.getCommentMarker().charValue());
        appendable.append(SP);
        for (int i = 0; i < comment.length(); i++) {
            final char c = comment.charAt(i);
            switch (c) {
            case CR:
                if (i + 1 < comment.length() && comment.charAt(i + 1) == LF) {
                    i++;
                }
                //$FALL-THROUGH$ break intentionally excluded.
            case LF:
                println();
                appendable.append(format.getCommentMarker().charValue());
                appendable.append(SP);
                break;
            default:
                appendable.append(c);
                break;
            }
        }
        println();
    }

    
    public synchronized void printHeaders(final ResultSet resultSet) throws IOException, SQLException {
        printRecord((Object[]) format.builder().setHeader(resultSet).build().getHeader());
    }

    
    public synchronized void println() throws IOException {
        format.println(appendable);
        newRecord = true;
    }

    
    @SuppressWarnings("resource")
    public synchronized void printRecord(final Iterable<?> values) throws IOException {
        IOStream.of(values).forEachOrdered(this::print);
        println();
    }

    
    public void printRecord(final Object... values) throws IOException {
        printRecord(Arrays.asList(values));
    }

    
    @SuppressWarnings("resource") // caller closes.
    public synchronized void printRecord(final Stream<?> values) throws IOException {
        IOStream.adapt(values).forEachOrdered(this::print);
        println();
    }

    private void printRecordObject(final Object value) throws IOException {
        if (value instanceof Object[]) {
            this.printRecord((Object[]) value);
        } else if (value instanceof Iterable) {
            this.printRecord((Iterable<?>) value);
        } else {
            this.printRecord(value);
        }
    }

    
    @SuppressWarnings("resource")
    public void printRecords(final Iterable<?> values) throws IOException {
        IOStream.of(values).forEachOrdered(this::printRecordObject);
    }

    
    public void printRecords(final Object... values) throws IOException {
        printRecords(Arrays.asList(values));
    }

    
    public void printRecords(final ResultSet resultSet) throws SQLException, IOException {
        final int columnCount = resultSet.getMetaData().getColumnCount();
        while (resultSet.next()) {
            for (int i = 1; i <= columnCount; i++) {
                final Object object = resultSet.getObject(i);
                // TODO Who manages the Clob? The JDBC driver or must we close it? Is it driver-dependent?
                print(object instanceof Clob ? ((Clob) object).getCharacterStream() : object);
            }
            println();
        }
    }

    
    public void printRecords(final ResultSet resultSet, final boolean printHeader) throws SQLException, IOException {
        if (printHeader) {
            printHeaders(resultSet);
        }
        printRecords(resultSet);
    }

    
    @SuppressWarnings({ "resource" }) // Caller closes.
    public void printRecords(final Stream<?> values) throws IOException {
        IOStream.adapt(values).forEachOrdered(this::printRecordObject);
    }
}
