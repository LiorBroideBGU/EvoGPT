

package org.apache.commons.cli;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


public class OptionGroup implements Serializable {
    
    private static final long serialVersionUID = 1L;

    
    private final Map<String, Option> optionMap = new LinkedHashMap<>();

    
    private String selected;

    
    private boolean required;

    
    public OptionGroup addOption(final Option option) {
        // key - option name
        // value - the option
        optionMap.put(option.getKey(), option);

        return this;
    }

    
    public Collection<String> getNames() {
        // the key set is the collection of names
        return optionMap.keySet();
    }

    
    public Collection<Option> getOptions() {
        // the values are the collection of options
        return optionMap.values();
    }

    
    public String getSelected() {
        return selected;
    }

    
    public boolean isRequired() {
        return required;
    }

    
    public void setRequired(final boolean required) {
        this.required = required;
    }

    
    public void setSelected(final Option option) throws AlreadySelectedException {
        if (option == null) {
            // reset the option previously selected
            selected = null;
            return;
        }

        // if no option has already been selected or the
        // same option is being reselected then set the
        // selected member variable
        if (selected != null && !selected.equals(option.getKey())) {
            throw new AlreadySelectedException(this, option);
        }
        selected = option.getKey();
    }

    
    @Override
    public String toString() {
        final StringBuilder buff = new StringBuilder();

        final Iterator<Option> iter = getOptions().iterator();

        buff.append("[");

        while (iter.hasNext()) {
            final Option option = iter.next();

            if (option.getOpt() != null) {
                buff.append("-");
                buff.append(option.getOpt());
            } else {
                buff.append("--");
                buff.append(option.getLongOpt());
            }

            if (option.getDescription() != null) {
                buff.append(" ");
                buff.append(option.getDescription());
            }

            if (iter.hasNext()) {
                buff.append(", ");
            }
        }

        buff.append("]");

        return buff.toString();
    }
}
