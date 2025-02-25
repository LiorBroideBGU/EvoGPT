

package org.jfree.chart.renderer;

import org.jfree.chart.internal.Args;
import org.jfree.data.DomainOrder;
import org.jfree.data.xy.XYDataset;


public class RendererUtils {

    
    public static int findLiveItemsLowerBound(XYDataset dataset, int series,
            double xLow, double xHigh) {
        Args.nullNotPermitted(dataset, "dataset");
        if (xLow >= xHigh) {
            throw new IllegalArgumentException("Requires xLow < xHigh.");
        }
        int itemCount = dataset.getItemCount(series);
        if (itemCount <= 1) {
            return 0;
        }
        if (dataset.getDomainOrder() == DomainOrder.ASCENDING) {
            // for data in ascending order by x-value, we are (broadly) looking
            // for the index of the highest x-value that is less than xLow
            int low = 0;
            int high = itemCount - 1;
            double lowValue = dataset.getXValue(series, low);
            if (lowValue >= xLow) {
                // special case where the lowest x-value is >= xLow
                return low;
            }
            double highValue = dataset.getXValue(series, high);
            if (highValue < xLow) {
                // special case where the highest x-value is < xLow
                return high;
            }
            while (high - low > 1) {
                int mid = (low + high) / 2;
                double midV = dataset.getXValue(series, mid);
                if (midV >= xLow) {
                    high = mid;
                }
                else {
                    low = mid;
                }
            }
            return high;
        }
        else if (dataset.getDomainOrder() == DomainOrder.DESCENDING) {
            // when the x-values are sorted in descending order, the lower
            // bound is found by calculating relative to the xHigh value
            int low = 0;
            int high = itemCount - 1;
            double lowValue = dataset.getXValue(series, low);
            if (lowValue <= xHigh) {
                return low;
            }
            double highValue = dataset.getXValue(series, high);
            if (highValue > xHigh) {
                return high;
            }
            while (high - low > 1) {
                int mid = (low + high) / 2;
                double midV = dataset.getXValue(series, mid);
                if (midV > xHigh) {
                    low = mid;
                }
                else {
                    high = mid;
                }
            }
            return high;
        }
        else {
            // we don't know anything about the ordering of the x-values,
            // but we can still skip any initial values that fall outside the
            // range...
            int index = 0;
            // skip any items that don't need including...
            double x = dataset.getXValue(series, index);
            while (index < itemCount && x < xLow) {
                index++;
                if (index < itemCount) {
                    x = dataset.getXValue(series, index);
                }
            }
            return Math.min(Math.max(0, index), itemCount - 1);
        }
    }

    
    public static int findLiveItemsUpperBound(XYDataset dataset, int series,
            double xLow, double xHigh) {
        Args.nullNotPermitted(dataset, "dataset");
        if (xLow >= xHigh) {
            throw new IllegalArgumentException("Requires xLow < xHigh.");
        }
        int itemCount = dataset.getItemCount(series);
        if (itemCount <= 1) {
            return 0;
        }
        if (dataset.getDomainOrder() == DomainOrder.ASCENDING) {
            int low = 0;
            int high = itemCount - 1;
            double lowValue = dataset.getXValue(series, low);
            if (lowValue > xHigh) {
                return low;
            }
            double highValue = dataset.getXValue(series, high);
            if (highValue <= xHigh) {
                return high;
            }
            int mid = (low + high) / 2;
            while (high - low > 1) {
                double midV = dataset.getXValue(series, mid);
                if (midV <= xHigh) {
                    low = mid;
                }
                else {
                    high = mid;
                }
                mid = (low + high) / 2;
            }
            return mid;
        }
        else if (dataset.getDomainOrder() == DomainOrder.DESCENDING) {
            // when the x-values are descending, the upper bound is found by
            // comparing against xLow
            int low = 0;
            int high = itemCount - 1;
            int mid = (low + high) / 2;
            double lowValue = dataset.getXValue(series, low);
            if (lowValue < xLow) {
                return low;
            }
            double highValue = dataset.getXValue(series, high);
            if (highValue >= xLow) {
                return high;
            }
            while (high - low > 1) {
                double midV = dataset.getXValue(series, mid);
                if (midV >= xLow) {
                    low = mid;
                }
                else {
                    high = mid;
                }
                mid = (low + high) / 2;
            }
            return mid;
        }
        else {
            // we don't know anything about the ordering of the x-values,
            // but we can still skip any trailing values that fall outside the
            // range...
            int index = itemCount - 1;
            // skip any items that don't need including...
            double x = dataset.getXValue(series, index);
            while (index >= 0 && x > xHigh) {
                index--;
                if (index >= 0) {
                    x = dataset.getXValue(series, index);
                }
            }
            return Math.max(index, 0);
        }
    }

    
    public static int[] findLiveItems(XYDataset dataset, int series,
            double xLow, double xHigh) {
        // here we could probably be a little faster by searching for both
        // indices simultaneously, but I'll look at that later if it seems
        // like it matters...
        int i0 = findLiveItemsLowerBound(dataset, series, xLow, xHigh);
        int i1 = findLiveItemsUpperBound(dataset, series, xLow, xHigh);
        if (i0 > i1) {
            i0 = i1;
        }
        return new int[] {i0, i1};
    }

}
