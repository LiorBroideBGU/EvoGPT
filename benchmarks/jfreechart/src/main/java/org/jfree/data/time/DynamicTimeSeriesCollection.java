

package org.jfree.data.time;

import java.util.Calendar;
import java.util.TimeZone;

import org.jfree.data.DomainInfo;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.jfree.data.xy.IntervalXYDataset;


public class DynamicTimeSeriesCollection extends AbstractIntervalXYDataset
        implements IntervalXYDataset, DomainInfo, RangeInfo {

    
    public static final int START = 0;

    
    public static final int MIDDLE = 1;

    
    public static final int END = 2;

    
    private int maximumItemCount = 2000;  // an arbitrary safe default value

    
    protected int historyCount;

    
    private Comparable[] seriesKeys;

    
    private Class timePeriodClass = Minute.class;   // default value;

    
    protected RegularTimePeriod[] pointsInTime;

    
    private int seriesCount;

    
    protected class ValueSequence {

        
        float[] dataPoints;

        
        public ValueSequence() {
            this(DynamicTimeSeriesCollection.this.maximumItemCount);
        }

        
        public ValueSequence(int length) {
            this.dataPoints = new float[length];
            for (int i = 0; i < length; i++) {
                this.dataPoints[i] = 0.0f;
            }
        }

        
        public void enterData(int index, float value) {
            this.dataPoints[index] = value;
        }

        
        public float getData(int index) {
            return this.dataPoints[index];
        }
    }

    
    protected ValueSequence[] valueHistory;

    
    protected Calendar workingCalendar;

    
    private int position;

    
    private boolean domainIsPointsInTime;

    
    private int oldestAt;  // as a class variable, initializes == 0

    
    private int newestAt;

    // cached values used for interface DomainInfo:

    
    private long deltaTime;

    
    private Long domainStart;

    
    private Long domainEnd;

    
    private Range domainRange;

    // Cached values used for interface RangeInfo: (note minValue pinned at 0)
    //   A single set of extrema covers the entire SeriesCollection

    
    private Float minValue = 0.0f;

    
    private Float maxValue = null;

    
    private Range valueRange;  // autoinit's to null.

    
    public DynamicTimeSeriesCollection(int nSeries, int nMoments) {
        this(nSeries, nMoments, new Millisecond(), TimeZone.getDefault());
        this.newestAt = nMoments - 1;
    }

    
    public DynamicTimeSeriesCollection(int nSeries, int nMoments,
            TimeZone zone) {
        this(nSeries, nMoments, new Millisecond(), zone);
        this.newestAt = nMoments - 1;
    }

    
    public DynamicTimeSeriesCollection(int nSeries, int nMoments,
            RegularTimePeriod timeSample) {
        this(nSeries, nMoments, timeSample, TimeZone.getDefault());
    }

    
    public DynamicTimeSeriesCollection(int nSeries, int nMoments,
            RegularTimePeriod timeSample, TimeZone zone) {

        // the first initialization must precede creation of the ValueSet array:
        this.maximumItemCount = nMoments;  // establishes length of each array
        this.historyCount = nMoments;
        this.seriesKeys = new Comparable[nSeries];
        // initialize the members of "seriesNames" array so they won't be null:
        for (int i = 0; i < nSeries; i++) {
            this.seriesKeys[i] = "";
        }
        this.newestAt = nMoments - 1;
        this.valueHistory = new ValueSequence[nSeries];
        this.timePeriodClass = timeSample.getClass();

        /// Expand the following for all defined TimePeriods:
        if (this.timePeriodClass == Millisecond.class) {
            this.pointsInTime = new Millisecond[nMoments];
        } else if (this.timePeriodClass == Second.class) {
            this.pointsInTime = new Second[nMoments];
        } else if (this.timePeriodClass == Minute.class) {
            this.pointsInTime = new Minute[nMoments];
        } else if (this.timePeriodClass == Hour.class) {
            this.pointsInTime = new Hour[nMoments];
        }
        ///  .. etc....
        this.workingCalendar = Calendar.getInstance(zone);
        this.position = START;
        this.domainIsPointsInTime = true;
    }

    
    public synchronized long setTimeBase(RegularTimePeriod start) {
        if (this.pointsInTime[0] == null) {
            this.pointsInTime[0] = start;
            for (int i = 1; i < this.historyCount; i++) {
                this.pointsInTime[i] = this.pointsInTime[i - 1].next();
            }
        }
        long oldestL = this.pointsInTime[0].getFirstMillisecond(
                this.workingCalendar);
        long nextL = this.pointsInTime[1].getFirstMillisecond(
                this.workingCalendar);
        this.deltaTime = nextL - oldestL;
        this.oldestAt = 0;
        this.newestAt = this.historyCount - 1;
        findDomainLimits();
        return this.deltaTime;
    }

    
    protected void findDomainLimits() {
        long startL = getOldestTime().getFirstMillisecond(this.workingCalendar);
        long endL;
        if (this.domainIsPointsInTime) {
            endL = getNewestTime().getFirstMillisecond(this.workingCalendar);
        }
        else {
            endL = getNewestTime().getLastMillisecond(this.workingCalendar);
        }
        this.domainStart = startL;
        this.domainEnd = endL;
        this.domainRange = new Range(startL, endL);
    }

    
    public int getPosition() {
        return this.position;
    }

    
    public void setPosition(int position) {
        this.position = position;
    }

    
    public void addSeries(float[] values, int seriesNumber, 
            Comparable seriesKey) {

        invalidateRangeInfo();
        int i;
        if (values == null) {
            throw new IllegalArgumentException("TimeSeriesDataset.addSeries(): "
                + "cannot add null array of values.");
        }
        if (seriesNumber >= this.valueHistory.length) {
            throw new IllegalArgumentException("TimeSeriesDataset.addSeries(): "
                + "cannot add more series than specified in c'tor");
        }
        if (this.valueHistory[seriesNumber] == null) {
            this.valueHistory[seriesNumber]
                = new ValueSequence(this.historyCount);
            this.seriesCount++;
        }
        // But if that series array already exists, just overwrite its contents

        // Avoid IndexOutOfBoundsException:
        int srcLength = values.length;
        int copyLength = this.historyCount;
        boolean fillNeeded = false;
        if (srcLength < this.historyCount) {
            fillNeeded = true;
            copyLength = srcLength;
        }
        //{
        for (i = 0; i < copyLength; i++) { // deep copy from values[], caller
                                           // can safely discard that array
            this.valueHistory[seriesNumber].enterData(i, values[i]);
        }
        if (fillNeeded) {
            for (i = copyLength; i < this.historyCount; i++) {
                this.valueHistory[seriesNumber].enterData(i, 0.0f);
            }
        }
      //}
        if (seriesKey != null) {
            this.seriesKeys[seriesNumber] = seriesKey;
        }
        fireSeriesChanged();
    }

    
    public void setSeriesKey(int seriesNumber, Comparable key) {
        this.seriesKeys[seriesNumber] = key;
    }

    
    public void addValue(int seriesNumber, int index, float value) {
        invalidateRangeInfo();
        if (seriesNumber >= this.valueHistory.length) {
            throw new IllegalArgumentException(
                "TimeSeriesDataset.addValue(): series #"
                + seriesNumber + "unspecified in c'tor"
            );
        }
        if (this.valueHistory[seriesNumber] == null) {
            this.valueHistory[seriesNumber]
                = new ValueSequence(this.historyCount);
            this.seriesCount++;
        }
        // But if that series array already exists, just overwrite its contents
        //synchronized(this)
        //{
            this.valueHistory[seriesNumber].enterData(index, value);
        //}
        fireSeriesChanged();
    }

    
    @Override
    public int getSeriesCount() {
        return this.seriesCount;
    }

    
    @Override
    public int getItemCount(int series) {  // all arrays equal length,
                                           // so ignore argument:
        return this.historyCount;
    }

    // Methods for managing the FIFO's:

    
    protected int translateGet(int toFetch) {
        if (this.oldestAt == 0) {
            return toFetch;  // no translation needed
        }
        // else  [implicit here]
        int newIndex = toFetch + this.oldestAt;
        if (newIndex >= this.historyCount) {
            newIndex -= this.historyCount;
        }
        return newIndex;
    }

    
    public int offsetFromNewest(int delta) {
        return wrapOffset(this.newestAt + delta);
    }

    
    public int offsetFromOldest(int delta) {
        return wrapOffset(this.oldestAt + delta);
    }

    
    protected int wrapOffset(int protoIndex) {
        int tmp = protoIndex;
        if (tmp >= this.historyCount) {
            tmp -= this.historyCount;
        }
        else if (tmp < 0) {
            tmp += this.historyCount;
        }
        return tmp;
    }

    
    public synchronized RegularTimePeriod advanceTime() {
        RegularTimePeriod nextInstant = this.pointsInTime[this.newestAt].next();
        this.newestAt = this.oldestAt;  // newestAt takes value previously held
                                        // by oldestAT
        /***
         * The next 10 lines or so should be expanded if data can be negative
         ***/
        // if the oldest data contained a maximum Y-value, invalidate the stored
        //   Y-max and Y-range data:
        boolean extremaChanged = false;
        float oldMax = 0.0f;
        if (this.maxValue != null) {
            oldMax = this.maxValue;
        }
        for (int s = 0; s < getSeriesCount(); s++) {
            if (this.valueHistory[s].getData(this.oldestAt) == oldMax) {
                extremaChanged = true;
            }
            if (extremaChanged) {
                break;
            }
        }  /*** If data can be < 0, add code here to check the minimum    **/
        if (extremaChanged) {
            invalidateRangeInfo();
        }
        //  wipe the next (about to be used) set of data slots
        float wiper = (float) 0.0;
        for (int s = 0; s < getSeriesCount(); s++) {
            this.valueHistory[s].enterData(this.newestAt, wiper);
        }
        // Update the array of TimePeriods:
        this.pointsInTime[this.newestAt] = nextInstant;
        // Now advance "oldestAt", wrapping at end of the array
        this.oldestAt++;
        if (this.oldestAt >= this.historyCount) {
            this.oldestAt = 0;
        }
        // Update the domain limits:
        long startL = this.domainStart;  //(time is kept in msec)
        this.domainStart = startL + this.deltaTime;
        long endL = this.domainEnd;
        this.domainEnd = endL + this.deltaTime;
        this.domainRange = new Range(startL, endL);
        fireSeriesChanged();
        return nextInstant;
    }

    //  If data can be < 0, the next 2 methods should be modified

    
    public void invalidateRangeInfo() {
        this.maxValue = null;
        this.valueRange = null;
    }

    
    protected double findMaxValue() {
        double max = 0.0f;
        for (int s = 0; s < getSeriesCount(); s++) {
            for (int i = 0; i < this.historyCount; i++) {
                double tmp = getYValue(s, i);
                if (tmp > max) {
                    max = tmp;
                }
            }
        }
        return max;
    }

    

    
    public int getOldestIndex() {
        return this.oldestAt;
    }

    
    public int getNewestIndex() {
        return this.newestAt;
    }

    // appendData() writes new data at the index position given by newestAt/
    // When adding new data dynamically, use advanceTime(), followed by this:
    
    public void appendData(float[] newData) {
        int nDataPoints = newData.length;
        if (nDataPoints > this.valueHistory.length) {
            throw new IllegalArgumentException(
                    "More data than series to put them in");
        }
        int s;   // index to select the "series"
        for (s = 0; s < nDataPoints; s++) {
            // check whether the "valueHistory" array member exists; if not,
            // create them:
            if (this.valueHistory[s] == null) {
                this.valueHistory[s] = new ValueSequence(this.historyCount);
            }
            this.valueHistory[s].enterData(this.newestAt, newData[s]);
        }
        fireSeriesChanged();
    }

    
    public void appendData(float[] newData, int insertionIndex, int refresh) {
        int nDataPoints = newData.length;
        if (nDataPoints > this.valueHistory.length) {
            throw new IllegalArgumentException(
                    "More data than series to put them in");
        }
        for (int s = 0; s < nDataPoints; s++) {
            if (this.valueHistory[s] == null) {
                this.valueHistory[s] = new ValueSequence(this.historyCount);
            }
            this.valueHistory[s].enterData(insertionIndex, newData[s]);
        }
        if (refresh > 0) {
            insertionIndex++;
            if (insertionIndex % refresh == 0) {
                fireSeriesChanged();
            }
        }
    }

    
    public RegularTimePeriod getNewestTime() {
        return this.pointsInTime[this.newestAt];
    }

    
    public RegularTimePeriod getOldestTime() {
        return this.pointsInTime[this.oldestAt];
    }

    
    // getXxx() ftns can ignore the "series" argument:
    // Don't synchronize this!! Instead, synchronize the loop that calls it.
    @Override
    public Number getX(int series, int item) {
        RegularTimePeriod tp = this.pointsInTime[translateGet(item)];
        return getX(tp);
    }

    
    @Override
    public double getYValue(int series, int item) {
        // Don't synchronize this!!
        // Instead, synchronize the loop that calls it.
        ValueSequence values = this.valueHistory[series];
        return values.getData(translateGet(item));
    }

    
    @Override
    public Number getY(int series, int item) {
        return getYValue(series, item);
    }

    
    @Override
    public Number getStartX(int series, int item) {
        RegularTimePeriod tp = this.pointsInTime[translateGet(item)];
        return tp.getFirstMillisecond(this.workingCalendar);
    }

    
    @Override
    public Number getEndX(int series, int item) {
        RegularTimePeriod tp = this.pointsInTime[translateGet(item)];
        return tp.getLastMillisecond(this.workingCalendar);
    }

    
    @Override
    public Number getStartY(int series, int item) {
        return getY(series, item);
    }

    
    @Override
    public Number getEndY(int series, int item) {
        return getY(series, item);
    }

    

    
    @Override
    public Comparable getSeriesKey(int series) {
        return this.seriesKeys[series];
    }

    
    protected void fireSeriesChanged() {
        seriesChanged(new SeriesChangeEvent(this));
    }

    // The next 3 functions override the base-class implementation of
    // the DomainInfo interface.  Using saved limits (updated by
    // each updateTime() call), improves performance.
    //

    
    @Override
    public double getDomainLowerBound(boolean includeInterval) {
        return this.domainStart.doubleValue();
        // a Long kept updated by advanceTime()
    }

    
    @Override
    public double getDomainUpperBound(boolean includeInterval) {
        return this.domainEnd.doubleValue();
        // a Long kept updated by advanceTime()
    }

    
    @Override
    public Range getDomainBounds(boolean includeInterval) {
        if (this.domainRange == null) {
            findDomainLimits();
        }
        return this.domainRange;
    }

    
    private long getX(RegularTimePeriod period) {
        switch (this.position) {
            case (START) :
                return period.getFirstMillisecond(this.workingCalendar);
            case (MIDDLE) :
                return period.getMiddleMillisecond(this.workingCalendar);
            case (END) :
                return period.getLastMillisecond(this.workingCalendar);
            default:
                return period.getMiddleMillisecond(this.workingCalendar);
        }
     }

    // The next 3 functions implement the RangeInfo interface.
    // Using saved limits (updated by each updateTime() call) significantly
    // improves performance.  WARNING: this code makes the simplifying
    // assumption that data is never negative.  Expand as needed for the
    // general case.

    
    @Override
    public double getRangeLowerBound(boolean includeInterval) {
        double result = Double.NaN;
        if (this.minValue != null) {
            result = this.minValue.doubleValue();
        }
        return result;
    }

    
    @Override
    public double getRangeUpperBound(boolean includeInterval) {
        double result = Double.NaN;
        if (this.maxValue != null) {
            result = this.maxValue.doubleValue();
        }
        return result;
    }

    
    @Override
    public Range getRangeBounds(boolean includeInterval) {
        if (this.valueRange == null) {
            double max = getRangeUpperBound(includeInterval);
            this.valueRange = new Range(0.0, max);
        }
        return this.valueRange;
    }

}
