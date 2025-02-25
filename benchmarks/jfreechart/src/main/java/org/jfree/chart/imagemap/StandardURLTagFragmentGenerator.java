

package org.jfree.chart.imagemap;


public class StandardURLTagFragmentGenerator
        implements URLTagFragmentGenerator {

    
    public StandardURLTagFragmentGenerator() {
        super();
    }

    
    @Override
    public String generateURLFragment(String urlText) {
        // the URL text should already have been escaped by the URL generator
        return " href=\"" + urlText + "\"";
    }

}
