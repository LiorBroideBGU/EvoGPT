

package org.jfree.chart.urls;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Objects;

import org.jfree.chart.internal.Args;
import org.jfree.data.general.PieDataset;


public class StandardPieURLGenerator implements PieURLGenerator, Serializable {

    
    private static final long serialVersionUID = 1626966402065883419L;

    
    private String prefix = "index.html";

    
    private String categoryParamName = "category";

    
    private String indexParamName = "pieIndex";

    
    public StandardPieURLGenerator() {
        this("index.html");
    }

    
    public StandardPieURLGenerator(String prefix) {
        this(prefix, "category");
    }

    
    public StandardPieURLGenerator(String prefix, String categoryParamName) {
        this(prefix, categoryParamName, "pieIndex");
    }

    
    public StandardPieURLGenerator(String prefix, String categoryParamName,
            String indexParamName) {
        Args.nullNotPermitted(prefix, "prefix");
        Args.nullNotPermitted(categoryParamName, "categoryParamName");
        this.prefix = prefix;
        this.categoryParamName = categoryParamName;
        this.indexParamName = indexParamName;
    }

    
    @Override
    public String generateURL(PieDataset dataset, Comparable key,
            int pieIndex) {
        String url = this.prefix;
        try {
            if (url.contains("?")) {
                url += "&amp;" + this.categoryParamName + "="
                        + URLEncoder.encode(key.toString(), "UTF-8");
            } else {
                url += "?" + this.categoryParamName + "="
                        + URLEncoder.encode(key.toString(), "UTF-8");
            }
            if (this.indexParamName != null) {
                url += "&amp;" + this.indexParamName + "=" + pieIndex;
            }
        } catch (UnsupportedEncodingException e) {  // this won't happen :)
            throw new RuntimeException(e);
        }
        return url;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof StandardPieURLGenerator)) {
            return false;
        }
        StandardPieURLGenerator that = (StandardPieURLGenerator) obj;
        if (!this.prefix.equals(that.prefix)) {
            return false;
        }
        if (!this.categoryParamName.equals(that.categoryParamName)) {
            return false;
        }
        if (!Objects.equals(this.indexParamName, that.indexParamName)) {
            return false;
        }
        return true;
    }
}
