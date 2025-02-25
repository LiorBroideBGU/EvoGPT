

package org.apache.commons.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;


public class HelpFormatter {

    
    private static final class OptionComparator implements Comparator<Option>, Serializable {
        
        private static final long serialVersionUID = 5305467873966684014L;

        
        @Override
        public int compare(final Option opt1, final Option opt2) {
            return opt1.getKey().compareToIgnoreCase(opt2.getKey());
        }
    }

    
    public static final int DEFAULT_WIDTH = 74;

    
    public static final int DEFAULT_LEFT_PAD = 1;

    
    public static final int DEFAULT_DESC_PAD = 3;

    
    public static final String DEFAULT_SYNTAX_PREFIX = "usage: ";

    
    public static final String DEFAULT_OPT_PREFIX = "-";

    
    public static final String DEFAULT_LONG_OPT_PREFIX = "--";

    
    public static final String DEFAULT_LONG_OPT_SEPARATOR = " ";

    
    public static final String DEFAULT_ARG_NAME = "arg";

    
    @Deprecated
    public int defaultWidth = DEFAULT_WIDTH;

    
    @Deprecated
    public int defaultLeftPad = DEFAULT_LEFT_PAD;

    
    @Deprecated
    public int defaultDescPad = DEFAULT_DESC_PAD;

    
    @Deprecated
    public String defaultSyntaxPrefix = DEFAULT_SYNTAX_PREFIX;

    
    @Deprecated
    public String defaultNewLine = System.lineSeparator();

    
    @Deprecated
    public String defaultOptPrefix = DEFAULT_OPT_PREFIX;

    
    @Deprecated
    public String defaultLongOptPrefix = DEFAULT_LONG_OPT_PREFIX;

    
    @Deprecated
    public String defaultArgName = DEFAULT_ARG_NAME;

    
    protected Comparator<Option> optionComparator = new OptionComparator();

    
    private String longOptSeparator = DEFAULT_LONG_OPT_SEPARATOR;

    
    private void appendOption(final StringBuffer buff, final Option option, final boolean required) {
        if (!required) {
            buff.append("[");
        }

        if (option.getOpt() != null) {
            buff.append("-").append(option.getOpt());
        } else {
            buff.append("--").append(option.getLongOpt());
        }

        // if the Option has a value and a non blank argname
        if (option.hasArg() && (option.getArgName() == null || !option.getArgName().isEmpty())) {
            buff.append(option.getOpt() == null ? longOptSeparator : " ");
            buff.append("<").append(option.getArgName() != null ? option.getArgName() : getArgName()).append(">");
        }

        // if the Option is not a required option
        if (!required) {
            buff.append("]");
        }
    }

    
    private void appendOptionGroup(final StringBuffer buff, final OptionGroup group) {
        if (!group.isRequired()) {
            buff.append("[");
        }

        final List<Option> optList = new ArrayList<>(group.getOptions());
        if (getOptionComparator() != null) {
            Collections.sort(optList, getOptionComparator());
        }
        // for each option in the OptionGroup
        for (final Iterator<Option> it = optList.iterator(); it.hasNext();) {
            // whether the option is required or not is handled at group level
            appendOption(buff, it.next(), true);

            if (it.hasNext()) {
                buff.append(" | ");
            }
        }

        if (!group.isRequired()) {
            buff.append("]");
        }
    }

    
    protected String createPadding(final int len) {
        final char[] padding = new char[len];
        Arrays.fill(padding, ' ');

        return new String(padding);
    }

    
    protected int findWrapPos(final String text, final int width, final int startPos) {
        // the line ends before the max wrap pos or a new line char found
        int pos = text.indexOf('\n', startPos);
        if (pos != -1 && pos <= width) {
            return pos + 1;
        }

        pos = text.indexOf('\t', startPos);
        if (pos != -1 && pos <= width) {
            return pos + 1;
        }

        if (startPos + width >= text.length()) {
            return -1;
        }

        // look for the last whitespace character before startPos+width
        for (pos = startPos + width; pos >= startPos; --pos) {
            final char c = text.charAt(pos);
            if (c == ' ' || c == '\n' || c == '\r') {
                break;
            }
        }

        // if we found it - just return
        if (pos > startPos) {
            return pos;
        }

        // if we didn't find one, simply chop at startPos+width
        pos = startPos + width;

        return pos == text.length() ? -1 : pos;
    }

    
    public String getArgName() {
        return defaultArgName;
    }

    
    public int getDescPadding() {
        return defaultDescPad;
    }

    
    public int getLeftPadding() {
        return defaultLeftPad;
    }

    
    public String getLongOptPrefix() {
        return defaultLongOptPrefix;
    }

    
    public String getLongOptSeparator() {
        return longOptSeparator;
    }

    
    public String getNewLine() {
        return defaultNewLine;
    }

    
    public Comparator<Option> getOptionComparator() {
        return optionComparator;
    }

    
    public String getOptPrefix() {
        return defaultOptPrefix;
    }

    
    public String getSyntaxPrefix() {
        return defaultSyntaxPrefix;
    }

    
    public int getWidth() {
        return defaultWidth;
    }

    
    public void printHelp(final int width, final String cmdLineSyntax, final String header, final Options options, final String footer) {
        printHelp(width, cmdLineSyntax, header, options, footer, false);
    }

    
    public void printHelp(final int width, final String cmdLineSyntax, final String header, final Options options, final String footer,
        final boolean autoUsage) {
        final PrintWriter pw = new PrintWriter(System.out);

        printHelp(pw, width, cmdLineSyntax, header, options, getLeftPadding(), getDescPadding(), footer, autoUsage);
        pw.flush();
    }

    
    public void printHelp(final PrintWriter pw, final int width, final String cmdLineSyntax, final String header, final Options options, final int leftPad,
        final int descPad, final String footer) {
        printHelp(pw, width, cmdLineSyntax, header, options, leftPad, descPad, footer, false);
    }

    
    public void printHelp(final PrintWriter pw, final int width, final String cmdLineSyntax, final String header, final Options options, final int leftPad,
        final int descPad, final String footer, final boolean autoUsage) {
        if (cmdLineSyntax == null || cmdLineSyntax.isEmpty()) {
            throw new IllegalArgumentException("cmdLineSyntax not provided");
        }

        if (autoUsage) {
            printUsage(pw, width, cmdLineSyntax, options);
        } else {
            printUsage(pw, width, cmdLineSyntax);
        }

        if (header != null && !header.isEmpty()) {
            printWrapped(pw, width, header);
        }

        printOptions(pw, width, options, leftPad, descPad);

        if (footer != null && !footer.isEmpty()) {
            printWrapped(pw, width, footer);
        }
    }

    
    public void printHelp(final String cmdLineSyntax, final Options options) {
        printHelp(getWidth(), cmdLineSyntax, null, options, null, false);
    }

    
    public void printHelp(final String cmdLineSyntax, final Options options, final boolean autoUsage) {
        printHelp(getWidth(), cmdLineSyntax, null, options, null, autoUsage);
    }

    
    public void printHelp(final String cmdLineSyntax, final String header, final Options options, final String footer) {
        printHelp(cmdLineSyntax, header, options, footer, false);
    }

    
    public void printHelp(final String cmdLineSyntax, final String header, final Options options, final String footer, final boolean autoUsage) {
        printHelp(getWidth(), cmdLineSyntax, header, options, footer, autoUsage);
    }

    
    public void printOptions(final PrintWriter pw, final int width, final Options options, final int leftPad, final int descPad) {
        final StringBuffer sb = new StringBuffer();

        renderOptions(sb, width, options, leftPad, descPad);
        pw.println(sb.toString());
    }

    
    public void printUsage(final PrintWriter pw, final int width, final String cmdLineSyntax) {
        final int argPos = cmdLineSyntax.indexOf(' ') + 1;

        printWrapped(pw, width, getSyntaxPrefix().length() + argPos, getSyntaxPrefix() + cmdLineSyntax);
    }

    
    public void printUsage(final PrintWriter pw, final int width, final String app, final Options options) {
        // initialize the string buffer
        final StringBuffer buff = new StringBuffer(getSyntaxPrefix()).append(app).append(" ");

        // create a list for processed option groups
        final Collection<OptionGroup> processedGroups = new ArrayList<>();

        final List<Option> optList = new ArrayList<>(options.getOptions());
        if (getOptionComparator() != null) {
            Collections.sort(optList, getOptionComparator());
        }
        // iterate over the options
        for (final Iterator<Option> it = optList.iterator(); it.hasNext();) {
            // get the next Option
            final Option option = it.next();

            // check if the option is part of an OptionGroup
            final OptionGroup group = options.getOptionGroup(option);

            // if the option is part of a group
            if (group != null) {
                // and if the group has not already been processed
                if (!processedGroups.contains(group)) {
                    // add the group to the processed list
                    processedGroups.add(group);

                    // add the usage clause
                    appendOptionGroup(buff, group);
                }

                // otherwise the option was displayed in the group
                // previously so ignore it.
            }

            // if the Option is not part of an OptionGroup
            else {
                appendOption(buff, option, option.isRequired());
            }

            if (it.hasNext()) {
                buff.append(" ");
            }
        }

        // call printWrapped
        printWrapped(pw, width, buff.toString().indexOf(' ') + 1, buff.toString());
    }

    
    public void printWrapped(final PrintWriter pw, final int width, final int nextLineTabStop, final String text) {
        final StringBuffer sb = new StringBuffer(text.length());

        renderWrappedTextBlock(sb, width, nextLineTabStop, text);
        pw.println(sb.toString());
    }

    
    public void printWrapped(final PrintWriter pw, final int width, final String text) {
        printWrapped(pw, width, 0, text);
    }

    
    protected StringBuffer renderOptions(final StringBuffer sb, final int width, final Options options, final int leftPad, final int descPad) {
        final String lpad = createPadding(leftPad);
        final String dpad = createPadding(descPad);

        // first create list containing only <lpad>-a,--aaa where
        // -a is opt and --aaa is long opt; in parallel look for
        // the longest opt string this list will be then used to
        // sort options ascending
        int max = 0;
        final List<StringBuffer> prefixList = new ArrayList<>();

        final List<Option> optList = options.helpOptions();

        if (getOptionComparator() != null) {
            Collections.sort(optList, getOptionComparator());
        }

        for (final Option option : optList) {
            final StringBuffer optBuf = new StringBuffer();

            if (option.getOpt() == null) {
                optBuf.append(lpad).append("   ").append(getLongOptPrefix()).append(option.getLongOpt());
            } else {
                optBuf.append(lpad).append(getOptPrefix()).append(option.getOpt());

                if (option.hasLongOpt()) {
                    optBuf.append(',').append(getLongOptPrefix()).append(option.getLongOpt());
                }
            }

            if (option.hasArg()) {
                final String argName = option.getArgName();
                if (argName != null && argName.isEmpty()) {
                    // if the option has a blank argname
                    optBuf.append(' ');
                } else {
                    optBuf.append(option.hasLongOpt() ? longOptSeparator : " ");
                    optBuf.append("<").append(argName != null ? option.getArgName() : getArgName()).append(">");
                }
            }

            prefixList.add(optBuf);
            max = Math.max(optBuf.length(), max);
        }

        int x = 0;

        for (final Iterator<Option> it = optList.iterator(); it.hasNext();) {
            final Option option = it.next();
            final StringBuilder optBuf = new StringBuilder(prefixList.get(x++).toString());

            if (optBuf.length() < max) {
                optBuf.append(createPadding(max - optBuf.length()));
            }

            optBuf.append(dpad);

            final int nextLineTabStop = max + descPad;

            if (option.getDescription() != null) {
                optBuf.append(option.getDescription());
            }

            renderWrappedText(sb, width, nextLineTabStop, optBuf.toString());

            if (it.hasNext()) {
                sb.append(getNewLine());
            }
        }

        return sb;
    }

    
    protected StringBuffer renderWrappedText(final StringBuffer sb, final int width, final int nextLineTabStop, final String text) {
        String render = text;
        int nextLineTabStopPos = nextLineTabStop;
        int pos = findWrapPos(render, width, 0);

        if (pos == -1) {
            sb.append(rtrim(render));

            return sb;
        }
        sb.append(rtrim(render.substring(0, pos))).append(getNewLine());

        if (nextLineTabStopPos >= width) {
            // stops infinite loop happening
            nextLineTabStopPos = 1;
        }

        // all following lines must be padded with nextLineTabStop space characters
        final String padding = createPadding(nextLineTabStopPos);

        while (true) {
            render = padding + render.substring(pos).trim();
            pos = findWrapPos(render, width, 0);

            if (pos == -1) {
                sb.append(render);

                return sb;
            }

            if (render.length() > width && pos == nextLineTabStopPos - 1) {
                pos = width;
            }

            sb.append(rtrim(render.substring(0, pos))).append(getNewLine());
        }
    }

    
    private Appendable renderWrappedTextBlock(final StringBuffer sb, final int width, final int nextLineTabStop, final String text) {
        try {
            final BufferedReader in = new BufferedReader(new StringReader(text));
            String line;
            boolean firstLine = true;
            while ((line = in.readLine()) != null) {
                if (!firstLine) {
                    sb.append(getNewLine());
                } else {
                    firstLine = false;
                }
                renderWrappedText(sb, width, nextLineTabStop, line);
            }
        } catch (final IOException e) { // NOPMD
            // cannot happen
        }

        return sb;
    }

    
    protected String rtrim(final String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }

        int pos = s.length();

        while (pos > 0 && Character.isWhitespace(s.charAt(pos - 1))) {
            --pos;
        }

        return s.substring(0, pos);
    }

    
    public void setArgName(final String name) {
        this.defaultArgName = name;
    }

    
    public void setDescPadding(final int padding) {
        this.defaultDescPad = padding;
    }

    
    public void setLeftPadding(final int padding) {
        this.defaultLeftPad = padding;
    }

    
    public void setLongOptPrefix(final String prefix) {
        this.defaultLongOptPrefix = prefix;
    }

    
    public void setLongOptSeparator(final String longOptSeparator) {
        this.longOptSeparator = longOptSeparator;
    }

    
    public void setNewLine(final String newline) {
        this.defaultNewLine = newline;
    }

    
    public void setOptionComparator(final Comparator<Option> comparator) {
        this.optionComparator = comparator;
    }

    
    public void setOptPrefix(final String prefix) {
        this.defaultOptPrefix = prefix;
    }

    
    public void setSyntaxPrefix(final String prefix) {
        this.defaultSyntaxPrefix = prefix;
    }

    
    public void setWidth(final int width) {
        this.defaultWidth = width;
    }

}
