

package org.jfree.data.time;

import org.jfree.chart.internal.Args;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class MovingAverage {

    
    public static TimeSeriesCollection createMovingAverage(
            TimeSeriesCollection source, String suffix, int periodCount,
            int skip) {

        Args.nullNotPermitted(source, "source");
        if (periodCount < 1) {
            throw new IllegalArgumentException("periodCount must be greater "
                    + "than or equal to 1.");
        }

        TimeSeriesCollection result = new TimeSeriesCollection();
        for (int i = 0; i < source.getSeriesCount(); i++) {
            TimeSeries sourceSeries = source.getSeries(i);
            TimeSeries maSeries = createMovingAverage(sourceSeries,
                    sourceSeries.getKey() + suffix, periodCount, skip);
            result.addSeries(maSeries);
        }
        return result;

    }

    
    public static <S extends Comparable<S>> TimeSeries<S> createMovingAverage(
            TimeSeries<S> source, S name, int periodCount, int skip) {

        Args.nullNotPermitted(source, "source");
        if (periodCount < 1) {
            throw new IllegalArgumentException("periodCount must be greater " 
                    + "than or equal to 1.");
        }

        TimeSeries<S> result = new TimeSeries<>(name);
        if (source.getItemCount() > 0) {

            // if the initial averaging period is to be excluded, then
            // calculate the index of the
            // first data item to have an average calculated...
            long firstSerial = source.getTimePeriod(0).getSerialIndex() + skip;

            for (int i = source.getItemCount() - 1; i >= 0; i--) {

                // get the current data item...
                RegularTimePeriod period = source.getTimePeriod(i);
                long serial = period.getSerialIndex();

                if (serial >= firstSerial) {
                    // work out the average for the earlier values...
                    int n = 0;
                    double sum = 0.0;
                    long serialLimit = period.getSerialIndex() - periodCount;
                    int offset = 0;
                    boolean finished = false;

                    while ((offset < periodCount) && (!finished)) {
                        if ((i - offset) >= 0) {
                            TimeSeriesDataItem item = source.getRawDataItem(
                                    i - offset);
                            RegularTimePeriod p = item.getPeriod();
                            Number v = item.getValue();
                            long currentIndex = p.getSerialIndex();
                            if (currentIndex > serialLimit) {
                                if (v != null) {
                                    sum = sum + v.doubleValue();
                                    n = n + 1;
                                }
                            }
                            else {
                                finished = true;
                            }
                        }
                        offset = offset + 1;
                    }
                    if (n > 0) {
                        result.add(period, sum / n);
                    }
                    else {
                        result.add(period, null);
                    }
                }

            }
        }
        return result;
    }

    
    public static <S extends Comparable<S>> TimeSeries<S> createPointMovingAverage(
            TimeSeries<S> source, S name, int pointCount) {

        Args.nullNotPermitted(source, "source");
        if (pointCount < 2) {
            throw new IllegalArgumentException("periodCount must be greater " 
                    + "than or equal to 2.");
        }

        TimeSeries<S> result = new TimeSeries<>(name);
        double rollingSumForPeriod = 0.0;
        for (int i = 0; i < source.getItemCount(); i++) {
            // get the current data item...
            TimeSeriesDataItem current = source.getRawDataItem(i);
            RegularTimePeriod period = current.getPeriod();
            // FIXME: what if value is null on next line?
            rollingSumForPeriod += current.getValue().doubleValue();

            if (i > pointCount - 1) {
                // remove the point i-periodCount out of the rolling sum.
                TimeSeriesDataItem startOfMovingAvg = source.getRawDataItem(
                        i - pointCount);
                rollingSumForPeriod -= startOfMovingAvg.getValue()
                        .doubleValue();
                result.add(period, rollingSumForPeriod / pointCount);
            }
            else if (i == pointCount - 1) {
                result.add(period, rollingSumForPeriod / pointCount);
            }
        }
        return result;
    }

    
    public static XYDataset createMovingAverage(XYDataset source, String suffix,
            long period, long skip) {

        return createMovingAverage(source, suffix, (double) period,
                (double) skip);

    }


    
    public static XYDataset createMovingAverage(XYDataset source,
            String suffix, double period, double skip) {

        Args.nullNotPermitted(source, "source");
        XYSeriesCollection result = new XYSeriesCollection();
        for (int i = 0; i < source.getSeriesCount(); i++) {
            XYSeries s = createMovingAverage(source, i, source.getSeriesKey(i)
                    + suffix, period, skip);
            result.addSeries(s);
        }
        return result;
    }

    
    public static XYSeries createMovingAverage(XYDataset source,
            int series, String name, double period, double skip) {

        Args.nullNotPermitted(source, "source");
        if (period < Double.MIN_VALUE) {
            throw new IllegalArgumentException("period must be positive.");
        }
        if (skip < 0.0) {
            throw new IllegalArgumentException("skip must be >= 0.0.");
        }

        XYSeries result = new XYSeries(name);

        if (source.getItemCount(series) > 0) {

            // if the initial averaging period is to be excluded, then
            // calculate the lowest x-value to have an average calculated...
            double first = source.getXValue(series, 0) + skip;

            for (int i = source.getItemCount(series) - 1; i >= 0; i--) {

                // get the current data item...
                double x = source.getXValue(series, i);

                if (x >= first) {
                    // work out the average for the earlier values...
                    int n = 0;
                    double sum = 0.0;
                    double limit = x - period;
                    int offset = 0;
                    boolean finished = false;

                    while (!finished) {
                        if ((i - offset) >= 0) {
                            double xx = source.getXValue(series, i - offset);
                            Number yy = source.getY(series, i - offset);
                            if (xx > limit) {
                                if (yy != null) {
                                    sum = sum + yy.doubleValue();
                                    n = n + 1;
                                }
                            }
                            else {
                                finished = true;
                            }
                        }
                        else {
                            finished = true;
                        }
                        offset = offset + 1;
                    }
                    if (n > 0) {
                        result.add(x, sum / n);
                    }
                    else {
                        result.add(x, null);
                    }
                }

            }
        }

        return result;

    }

}
