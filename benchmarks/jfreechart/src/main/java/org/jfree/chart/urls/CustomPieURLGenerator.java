

package org.jfree.chart.urls;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jfree.chart.plot.pie.MultiplePiePlot;
import org.jfree.chart.plot.pie.PiePlot;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.data.general.PieDataset;


public class CustomPieURLGenerator implements PieURLGenerator,
        Cloneable, PublicCloneable, Serializable {

    
    private static final long serialVersionUID = 7100607670144900503L;

    
    private final List<Map<Comparable<?>, String>> urlMaps;

    
    public CustomPieURLGenerator() {
        this.urlMaps = new ArrayList<>();
    }

    
    @Override
    public String generateURL(PieDataset dataset, Comparable<?> key,
                              int plotIndex) {
        return getURL(key, plotIndex);
    }

    
    public int getListCount() {
        return this.urlMaps.size();
    }

    
    public int getURLCount(int plotIndex) {
        int result = 0;
        Map<Comparable<?>, String> urlMap = this.urlMaps.get(plotIndex);
        if (urlMap != null) {
            result = urlMap.size();
        }
        return result;
    }

    
    public String getURL(Comparable<?> key, int plotIndex) {
        String result = null;
        if (plotIndex < getListCount()) {
            Map<Comparable<?>, String> urlMap = this.urlMaps.get(plotIndex);
            if (urlMap != null) {
                result = (String) urlMap.get(key);
            }
        }
        return result;
    }

    
    public void addURLs(Map urlMap) {
        this.urlMaps.add(urlMap);
    }

    
    @Override
    public boolean equals(Object o) {

        if (o == this) {
            return true;
        }

        if (o instanceof CustomPieURLGenerator) {
            CustomPieURLGenerator generator = (CustomPieURLGenerator) o;
            if (getListCount() != generator.getListCount()) {
                return false;
            }
            Set keySet;
            for (int pieItem = 0; pieItem < getListCount(); pieItem++) {
                if (getURLCount(pieItem) != generator.getURLCount(pieItem)) {
                    return false;
                }
                keySet = this.urlMaps.get(pieItem).keySet();
                String key;
                for (Iterator i = keySet.iterator(); i.hasNext();) {
                key = (String) i.next();
                    if (!getURL(key, pieItem).equals(
                            generator.getURL(key, pieItem))) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        CustomPieURLGenerator urlGen = new CustomPieURLGenerator();
        Map map;
        Map newMap;
        String key;

        for (Iterator i = this.urlMaps.iterator(); i.hasNext();) {
            map = (Map) i.next();

            newMap = new HashMap();
            for (Iterator j = map.keySet().iterator(); j.hasNext();) {
                key = (String) j.next();
                newMap.put(key, map.get(key));
            }

            urlGen.addURLs(newMap);
        }

        return urlGen;
    }

}
