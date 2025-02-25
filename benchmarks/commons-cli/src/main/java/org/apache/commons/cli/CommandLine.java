

package org.apache.commons.cli;

import static org.apache.commons.cli.Util.EMPTY_STRING_ARRAY;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;


public class CommandLine implements Serializable {

    
    public static final class Builder {

        
        private final CommandLine commandLine = new CommandLine();

        
        public Builder addArg(final String arg) {
            commandLine.addArg(arg);
            return this;
        }

        
        public Builder addOption(final Option opt) {
            commandLine.addOption(opt);
            return this;
        }

        
        public CommandLine build() {
            return commandLine;
        }
    }

    
    private static final long serialVersionUID = 1L;

    
    private final List<String> args = new LinkedList<>();

    
    private final List<Option> options = new ArrayList<>();

    
    protected CommandLine() {
        // nothing to do
    }

    
    protected void addArg(final String arg) {
        if (arg != null) {
            args.add(arg);
        }
    }

    
    protected void addOption(final Option opt) {
        if (opt != null) {
            options.add(opt);
        }
    }

    
    public List<String> getArgList() {
        return args;
    }

    
    public String[] getArgs() {
        return args.toArray(Util.EMPTY_STRING_ARRAY);
    }

    
    @Deprecated
    public Object getOptionObject(final char opt) {
        return getOptionObject(String.valueOf(opt));
    }

    
    @Deprecated
    public Object getOptionObject(final String opt) {
        try {
            return getParsedOptionValue(opt);
        } catch (final ParseException pe) {
            System.err.println("Exception found converting " + opt + " to desired type: " + pe.getMessage());
            return null;
        }
    }

    
    public Properties getOptionProperties(final Option option) {
        final Properties props = new Properties();

        for (final Option processedOption : options) {
            if (processedOption.equals(option)) {
                final List<String> values = processedOption.getValuesList();
                if (values.size() >= 2) {
                    // use the first 2 arguments as the key/value pair
                    props.put(values.get(0), values.get(1));
                } else if (values.size() == 1) {
                    // no explicit value, handle it as a boolean
                    props.put(values.get(0), "true");
                }
            }
        }

        return props;
    }

    
    public Properties getOptionProperties(final String opt) {
        final Properties props = new Properties();

        for (final Option option : options) {
            if (opt.equals(option.getOpt()) || opt.equals(option.getLongOpt())) {
                final List<String> values = option.getValuesList();
                if (values.size() >= 2) {
                    // use the first 2 arguments as the key/value pair
                    props.put(values.get(0), values.get(1));
                } else if (values.size() == 1) {
                    // no explicit value, handle it as a boolean
                    props.put(values.get(0), "true");
                }
            }
        }

        return props;
    }

    
    public Option[] getOptions() {
        return options.toArray(Option.EMPTY_ARRAY);
    }

    
    public String getOptionValue(final char opt) {
        return getOptionValue(String.valueOf(opt));
    }

    
    public String getOptionValue(final char opt, final String defaultValue) {
        return getOptionValue(String.valueOf(opt), () -> defaultValue);
    }

    
    public String getOptionValue(final char opt, final Supplier<String> defaultValue) {
        return getOptionValue(String.valueOf(opt), defaultValue);
    }

    
    public String getOptionValue(final Option option) {
        if (option == null) {
            return null;
        }
        final String[] values = getOptionValues(option);
        return values == null ? null : values[0];
    }

    
    public String getOptionValue(final Option option, final String defaultValue) {
        return getOptionValue(option, () -> defaultValue);
    }

    
    public String getOptionValue(final Option option, final Supplier<String> defaultValue) {
        final String answer = getOptionValue(option);
        return answer != null ? answer : defaultValue.get();
    }

    
    public String getOptionValue(final String opt) {
        return getOptionValue(resolveOption(opt));
    }

    
    public String getOptionValue(final String opt, final String defaultValue) {
        return getOptionValue(resolveOption(opt), () -> defaultValue);
    }

    
    public String getOptionValue(final String opt, final Supplier<String> defaultValue) {
        return getOptionValue(resolveOption(opt), defaultValue);
    }


    
    public String[] getOptionValues(final char opt) {
        return getOptionValues(String.valueOf(opt));
    }

    
    public String[] getOptionValues(final Option option) {
        final List<String> values = new ArrayList<>();

        for (final Option processedOption : options) {
            if (processedOption.equals(option)) {
                values.addAll(processedOption.getValuesList());
            }
        }

        return values.isEmpty() ? null : values.toArray(EMPTY_STRING_ARRAY);
    }

    
    public String[] getOptionValues(final String opt) {
        return getOptionValues(resolveOption(opt));
    }

    
    public <T> T getParsedOptionValue(final char opt) throws ParseException {
        return getParsedOptionValue(String.valueOf(opt));
    }

    
    public <T> T getParsedOptionValue(final char opt, final T defaultValue) throws ParseException {
        return getParsedOptionValue(String.valueOf(opt), defaultValue);
    }

    
    public <T> T getParsedOptionValue(final char opt, final Supplier<T> defaultValue) throws ParseException {
        return getParsedOptionValue(String.valueOf(opt), defaultValue);
    }

    
    public <T> T getParsedOptionValue(final Option option) throws ParseException {
        return  getParsedOptionValue(option, () -> null);
    }

    
    public <T> T getParsedOptionValue(final Option option, final T defaultValue) throws ParseException {
        return getParsedOptionValue(option, () -> defaultValue);
    }

    
    @SuppressWarnings("unchecked")
    public <T> T getParsedOptionValue(final Option option, final Supplier<T> defaultValue) throws ParseException {
        final String res = option == null ? null : getOptionValue(option);

        try {
            if (res == null) {
                return defaultValue == null ? null : defaultValue.get();
            }
            return (T) option.getConverter().apply(res);
        } catch (final Throwable e) {
            throw ParseException.wrap(e);
        }
    }

    
    public <T> T getParsedOptionValue(final String opt) throws ParseException {
        return getParsedOptionValue(resolveOption(opt));
    }

    
    public <T> T getParsedOptionValue(final String opt, final T defaultValue) throws ParseException {
        return getParsedOptionValue(resolveOption(opt), defaultValue);
    }

    
    public <T> T getParsedOptionValue(final String opt, final Supplier<T> defaultValue) throws ParseException {
        return getParsedOptionValue(resolveOption(opt), defaultValue);
    }

    

    

    
    public boolean hasOption(final char opt) {
        return hasOption(String.valueOf(opt));
    }

    
    public boolean hasOption(final Option opt) {
        return options.contains(opt);
    }

    
    public boolean hasOption(final String opt) {
        return hasOption(resolveOption(opt));
    }

    
    public Iterator<Option> iterator() {
        return options.iterator();
    }

    
    private Option resolveOption(final String opt) {
        final String actual = Util.stripLeadingHyphens(opt);
        if (actual != null) {
            for (final Option option : options) {
                if (actual.equals(option.getOpt()) || actual.equals(option.getLongOpt())) {
                    return option;
                }
            }
        }
        return null;
    }
}
