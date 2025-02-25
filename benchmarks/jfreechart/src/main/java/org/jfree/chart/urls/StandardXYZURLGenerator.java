

package org.jfree.chart.urls;

import org.jfree.data.xy.XYZDataset;


public class StandardXYZURLGenerator extends StandardXYURLGenerator
        implements XYZURLGenerator {

    
    @Override
    public String generateURL(XYZDataset dataset, int series, int item) {
        return super.generateURL(dataset, series, item);
    }

}
