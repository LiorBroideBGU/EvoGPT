

package org.jfree.chart.axis;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.text.TextAnchor;
import org.jfree.data.Range;


public class LogarithmicAxis extends NumberAxis {

    
    private static final long serialVersionUID = 2502918599004103054L;

    
    public static final double LOG10_VALUE = Math.log(10.0);

    
    public static final double SMALL_LOG_VALUE = 1e-100;

    
    protected boolean allowNegativesFlag = false;

    
    protected boolean strictValuesFlag = true;

    
    protected final NumberFormat numberFormatterObj
        = NumberFormat.getInstance();

    
    protected boolean expTickLabelsFlag = false;

    
    protected boolean log10TickLabelsFlag = false;

    
    protected boolean autoRangeNextLogFlag = false;

    
    protected boolean smallLogFlag = false;

    
    public LogarithmicAxis(String label) {
        super(label);
        setupNumberFmtObj();      //setup number formatter obj
    }

    
    public void setAllowNegativesFlag(boolean flgVal) {
        this.allowNegativesFlag = flgVal;
    }

    
    public boolean getAllowNegativesFlag() {
        return this.allowNegativesFlag;
    }

    
    public void setStrictValuesFlag(boolean flgVal) {
        this.strictValuesFlag = flgVal;
    }

    
    public boolean getStrictValuesFlag() {
        return this.strictValuesFlag;
    }

    
    public void setExpTickLabelsFlag(boolean flgVal) {
        this.expTickLabelsFlag = flgVal;
        setupNumberFmtObj();             //setup number formatter obj
    }

    
    public boolean getExpTickLabelsFlag() {
      return this.expTickLabelsFlag;
    }

    
    public void setLog10TickLabelsFlag(boolean flag) {
        this.log10TickLabelsFlag = flag;
    }

    
    public boolean getLog10TickLabelsFlag() {
        return this.log10TickLabelsFlag;
    }

    
    public void setAutoRangeNextLogFlag(boolean flag) {
        this.autoRangeNextLogFlag = flag;
    }

    
    public boolean getAutoRangeNextLogFlag() {
        return this.autoRangeNextLogFlag;
    }

    
    @Override
    public void setRange(Range range) {
        super.setRange(range);      // call parent method
        setupSmallLogFlag();        // setup flag based on bounds values
    }

    
    protected void setupSmallLogFlag() {
        // set flag true if negative values not allowed and the
        // lower bound is between 0 and 10:
        double lowerVal = getRange().getLowerBound();
        this.smallLogFlag = (!this.allowNegativesFlag && lowerVal < 10.0
                && lowerVal > 0.0);
    }

    
    protected void setupNumberFmtObj() {
        if (this.numberFormatterObj instanceof DecimalFormat) {
            //setup for "1e#"-style tick labels or regular
            // numeric tick labels, depending on flag:
            ((DecimalFormat) this.numberFormatterObj).applyPattern(
                    this.expTickLabelsFlag ? "0E0" : "0.###");
        }
    }

    
    protected double switchedLog10(double val) {
        return this.smallLogFlag ? Math.log(val)
                / LOG10_VALUE : adjustedLog10(val);
    }

    
    public double switchedPow10(double val) {
        return this.smallLogFlag ? Math.pow(10.0, val) : adjustedPow10(val);
    }

    
    public double adjustedLog10(double val) {
        boolean negFlag = (val < 0.0);
        if (negFlag) {
            val = -val;          // if negative then set flag and make positive
        }
        if (val < 10.0) {                // if < 10 then
            val += (10.0 - val) / 10.0;  //increase so 0 translates to 0
        }
        //return value; negate if original value was negative:
        double res = Math.log(val) / LOG10_VALUE;
        return negFlag ? (-res) : res;
    }

    
    public double adjustedPow10(double val) {
        boolean negFlag = (val < 0.0);
        if (negFlag) {
            val = -val; // if negative then set flag and make positive
        }
        double res;
        if (val < 1.0) {
            res = (Math.pow(10, val + 1.0) - 10.0) / 9.0; //invert adjustLog10
        }
        else {
            res = Math.pow(10, val);
        }
        return negFlag ? (-res) : res;
    }

    
    protected double computeLogFloor(double lower) {

        double logFloor;
        if (this.allowNegativesFlag) {
            //negative values are allowed
            if (lower > 10.0) {   //parameter value is > 10
                // The Math.log() function is based on e not 10.
                logFloor = Math.log(lower) / LOG10_VALUE;
                logFloor = Math.floor(logFloor);
                logFloor = Math.pow(10, logFloor);
            }
            else if (lower < -10.0) {   //parameter value is < -10
                //calculate log using positive value:
                logFloor = Math.log(-lower) / LOG10_VALUE;
                //calculate floor using negative value:
                logFloor = Math.floor(-logFloor);
                //calculate power using positive value; then negate
                logFloor = -Math.pow(10, -logFloor);
            }
            else {
                //parameter value is -10 > val < 10
                logFloor = Math.floor(lower);   //use as-is
            }
        }
        else {
            //negative values not allowed
            if (lower > 0.0) {   //parameter value is > 0
                // The Math.log() function is based on e not 10.
                logFloor = Math.log(lower) / LOG10_VALUE;
                logFloor = Math.floor(logFloor);
                logFloor = Math.pow(10, logFloor);
            }
            else {
                //parameter value is <= 0
                logFloor = Math.floor(lower);   //use as-is
            }
        }
        return logFloor;
    }

    
    protected double computeLogCeil(double upper) {

        double logCeil;
        if (this.allowNegativesFlag) {
            //negative values are allowed
            if (upper > 10.0) {
                //parameter value is > 10
                // The Math.log() function is based on e not 10.
                logCeil = Math.log(upper) / LOG10_VALUE;
                logCeil = Math.ceil(logCeil);
                logCeil = Math.pow(10, logCeil);
            }
            else if (upper < -10.0) {
                //parameter value is < -10
                //calculate log using positive value:
                logCeil = Math.log(-upper) / LOG10_VALUE;
                //calculate ceil using negative value:
                logCeil = Math.ceil(-logCeil);
                //calculate power using positive value; then negate
                logCeil = -Math.pow(10, -logCeil);
            }
            else {
               //parameter value is -10 > val < 10
               logCeil = Math.ceil(upper);     //use as-is
            }
        }
        else {
            //negative values not allowed
            if (upper > 0.0) {
                //parameter value is > 0
                // The Math.log() function is based on e not 10.
                logCeil = Math.log(upper) / LOG10_VALUE;
                logCeil = Math.ceil(logCeil);
                logCeil = Math.pow(10, logCeil);
            }
            else {
                //parameter value is <= 0
                logCeil = Math.ceil(upper);     //use as-is
            }
        }
        return logCeil;
    }

    
    @Override
    public void autoAdjustRange() {

        Plot plot = getPlot();
        if (plot == null) {
            return;  // no plot, no data.
        }

        if (plot instanceof ValueAxisPlot) {
            ValueAxisPlot vap = (ValueAxisPlot) plot;

            double lower;
            Range r = vap.getDataRange(this);
            if (r == null) {
                   //no real data present
                r = getDefaultAutoRange();
                lower = r.getLowerBound();    //get lower bound value
            }
            else {
                //actual data is present
                lower = r.getLowerBound();    //get lower bound value
                if (this.strictValuesFlag
                        && !this.allowNegativesFlag && lower <= 0.0) {
                    //strict flag set, allow-negatives not set and values <= 0
                    throw new RuntimeException("Values less than or equal to "
                            + "zero not allowed with logarithmic axis");
                }
            }

            //apply lower margin by decreasing lower bound:
            final double lowerMargin;
            if (lower > 0.0 && (lowerMargin = getLowerMargin()) > 0.0) {
                   //lower bound and margin OK; get log10 of lower bound
                final double logLower = (Math.log(lower) / LOG10_VALUE);
                double logAbs;      //get absolute value of log10 value
                if ((logAbs = Math.abs(logLower)) < 1.0) {
                    logAbs = 1.0;     //if less than 1.0 then make it 1.0
                }              //subtract out margin and get exponential value:
                lower = Math.pow(10, (logLower - (logAbs * lowerMargin)));
            }

            //if flag then change to log version of lowest value
            // to make range begin at a 10^n value:
            if (this.autoRangeNextLogFlag) {
                lower = computeLogFloor(lower);
            }

            if (!this.allowNegativesFlag && lower >= 0.0
                    && lower < SMALL_LOG_VALUE) {
                //negatives not allowed and lower range bound is zero
                lower = r.getLowerBound();    //use data range bound instead
            }

            double upper = r.getUpperBound();

             //apply upper margin by increasing upper bound:
            final double upperMargin;
            if (upper > 0.0 && (upperMargin = getUpperMargin()) > 0.0) {
                   //upper bound and margin OK; get log10 of upper bound
                final double logUpper = (Math.log(upper) / LOG10_VALUE);
                double logAbs;      //get absolute value of log10 value
                if ((logAbs = Math.abs(logUpper)) < 1.0) {
                    logAbs = 1.0;     //if less than 1.0 then make it 1.0
                }              //add in margin and get exponential value:
                upper = Math.pow(10, (logUpper + (logAbs * upperMargin)));
            }

            if (!this.allowNegativesFlag && upper < 1.0 && upper > 0.0
                    && lower > 0.0) {
                //negatives not allowed and upper bound between 0 & 1
                //round up to nearest significant digit for bound:
                //get negative exponent:
                double expVal = Math.log(upper) / LOG10_VALUE;
                expVal = Math.ceil(-expVal + 0.001); //get positive exponent
                expVal = Math.pow(10, expVal);      //create multiplier value
                //multiply, round up, and divide for bound value:
                upper = (expVal > 0.0) ? Math.ceil(upper * expVal) / expVal
                    : Math.ceil(upper);
            }
            else {
                //negatives allowed or upper bound not between 0 & 1
                //if flag then change to log version of highest value to
                // make range begin at a 10^n value; else use nearest int
                upper = (this.autoRangeNextLogFlag) ? computeLogCeil(upper)
                    : Math.ceil(upper);
            }
            // ensure the autorange is at least <minRange> in size...
            double minRange = getAutoRangeMinimumSize();
            if (upper - lower < minRange) {
                upper = (upper + lower + minRange) / 2;
                lower = (upper + lower - minRange) / 2;
                //if autorange still below minimum then adjust by 1%
                // (can be needed when minRange is very small):
                if (upper - lower < minRange) {
                    double absUpper = Math.abs(upper);
                    //need to account for case where upper==0.0
                    double adjVal = (absUpper > SMALL_LOG_VALUE) ? absUpper
                        / 100.0 : 0.01;
                    upper = (upper + lower + adjVal) / 2;
                    lower = (upper + lower - adjVal) / 2;
                }
            }

            setRange(new Range(lower, upper), false, false);
            setupSmallLogFlag();       //setup flag based on bounds values
        }
    }

    
    @Override
    public double valueToJava2D(double value, Rectangle2D plotArea,
                                RectangleEdge edge) {

        Range range = getRange();
        double axisMin = switchedLog10(range.getLowerBound());
        double axisMax = switchedLog10(range.getUpperBound());

        double min = 0.0;
        double max = 0.0;
        if (RectangleEdge.isTopOrBottom(edge)) {
            min = plotArea.getMinX();
            max = plotArea.getMaxX();
        }
        else if (RectangleEdge.isLeftOrRight(edge)) {
            min = plotArea.getMaxY();
            max = plotArea.getMinY();
        }

        value = switchedLog10(value);

        if (isInverted()) {
            return max - (((value - axisMin) / (axisMax - axisMin))
                    * (max - min));
        }
        else {
            return min + (((value - axisMin) / (axisMax - axisMin))
                    * (max - min));
        }

    }

    
    @Override
    public double java2DToValue(double java2DValue, Rectangle2D plotArea,
                                RectangleEdge edge) {

        Range range = getRange();
        double axisMin = switchedLog10(range.getLowerBound());
        double axisMax = switchedLog10(range.getUpperBound());

        double plotMin = 0.0;
        double plotMax = 0.0;
        if (RectangleEdge.isTopOrBottom(edge)) {
            plotMin = plotArea.getX();
            plotMax = plotArea.getMaxX();
        }
        else if (RectangleEdge.isLeftOrRight(edge)) {
            plotMin = plotArea.getMaxY();
            plotMax = plotArea.getMinY();
        }

        if (isInverted()) {
            return switchedPow10(axisMax - ((java2DValue - plotMin)
                    / (plotMax - plotMin)) * (axisMax - axisMin));
        }
        else {
            return switchedPow10(axisMin + ((java2DValue - plotMin)
                    / (plotMax - plotMin)) * (axisMax - axisMin));
        }
    }

    
    @Override
    public void zoomRange(double lowerPercent, double upperPercent) {
        double startLog = switchedLog10(getRange().getLowerBound());
        double lengthLog = switchedLog10(getRange().getUpperBound()) - startLog;
        Range adjusted;

        if (isInverted()) {
            adjusted = new Range(
                    switchedPow10(startLog + (lengthLog * (1 - upperPercent))),
                    switchedPow10(startLog + (lengthLog * (1 - lowerPercent))));
        }
        else {
            adjusted = new Range(
                    switchedPow10(startLog + (lengthLog * lowerPercent)),
                    switchedPow10(startLog + (lengthLog * upperPercent)));
        }

        setRange(adjusted);
    }

    
    @Override
    protected List refreshTicksHorizontal(Graphics2D g2, Rectangle2D dataArea,
            RectangleEdge edge) {

        List ticks = new java.util.ArrayList();
        Range range = getRange();

        //get lower bound value:
        double lowerBoundVal = range.getLowerBound();
              //if small log values and lower bound value too small
              // then set to a small value (don't allow <= 0):
        if (this.smallLogFlag && lowerBoundVal < SMALL_LOG_VALUE) {
            lowerBoundVal = SMALL_LOG_VALUE;
        }

        //get upper bound value
        double upperBoundVal = range.getUpperBound();

        //get log10 version of lower bound and round to integer:
        int iBegCount = (int) Math.rint(switchedLog10(lowerBoundVal));
        //get log10 version of upper bound and round to integer:
        int iEndCount = (int) Math.rint(switchedLog10(upperBoundVal));

        if (iBegCount == iEndCount && iBegCount > 0
                && Math.pow(10, iBegCount) > lowerBoundVal) {
              //only 1 power of 10 value, it's > 0 and its resulting
              // tick value will be larger than lower bound of data
            --iBegCount;       //decrement to generate more ticks
        }

        double currentTickValue;
        String tickLabel;
        boolean zeroTickFlag = false;
        for (int i = iBegCount; i <= iEndCount; i++) {
            //for each power of 10 value; create ten ticks
            for (int j = 0; j < 10; ++j) {
                //for each tick to be displayed
                if (this.smallLogFlag) {
                    //small log values in use; create numeric value for tick
                    currentTickValue = Math.pow(10, i) + (Math.pow(10, i) * j);
                    if (this.expTickLabelsFlag
                        || (i < 0 && currentTickValue > 0.0
                        && currentTickValue < 1.0)) {
                        //showing "1e#"-style ticks or negative exponent
                        // generating tick value between 0 & 1; show fewer
                        if (j == 0 || (i > -4 && j < 2)
                                   || currentTickValue >= upperBoundVal) {
                          //first tick of series, or not too small a value and
                          // one of first 3 ticks, or last tick to be displayed
                            // set exact number of fractional digits to be shown
                            // (no effect if showing "1e#"-style ticks):
                            this.numberFormatterObj
                                .setMaximumFractionDigits(-i);
                               //create tick label (force use of fmt obj):
                            tickLabel = makeTickLabel(currentTickValue, true);
                        }
                        else {    //no tick label to be shown
                            tickLabel = "";
                        }
                    }
                    else {     //tick value not between 0 & 1
                               //show tick label if it's the first or last in
                               // the set, or if it's 1-5; beyond that show
                               // fewer as the values get larger:
                        tickLabel = (j < 1 || (i < 1 && j < 5) || (j < 4 - i)
                                         || currentTickValue >= upperBoundVal)
                                         ? makeTickLabel(currentTickValue) : "";
                    }
                }
                else { //not small log values in use; allow for values <= 0
                    if (zeroTickFlag) {   //if did zero tick last iter then
                        --j;              //decrement to do 1.0 tick now
                    }     //calculate power-of-ten value for tick:
                    currentTickValue = (i >= 0)
                        ? Math.pow(10, i) + (Math.pow(10, i) * j)
                        : -(Math.pow(10, -i) - (Math.pow(10, -i - 1) * j));
                    if (!zeroTickFlag) {  // did not do zero tick last iteration
                        if (Math.abs(currentTickValue - 1.0) < 0.0001
                            && lowerBoundVal <= 0.0 && upperBoundVal >= 0.0) {
                            //tick value is 1.0 and 0.0 is within data range
                            currentTickValue = 0.0;     //set tick value to zero
                            zeroTickFlag = true;        //indicate zero tick
                        }
                    }
                    else {     //did zero tick last iteration
                        zeroTickFlag = false;         //clear flag
                    }               //create tick label string:
                               //show tick label if "1e#"-style and it's one
                               // of the first two, if it's the first or last
                               // in the set, or if it's 1-5; beyond that
                               // show fewer as the values get larger:
                    tickLabel = ((this.expTickLabelsFlag && j < 2)
                                || j < 1
                                || (i < 1 && j < 5) || (j < 4 - i)
                                || currentTickValue >= upperBoundVal)
                                   ? makeTickLabel(currentTickValue) : "";
                }

                if (currentTickValue > upperBoundVal) {
                    return ticks;   // if past highest data value then exit
                                    // method
                }

                if (currentTickValue >= lowerBoundVal - SMALL_LOG_VALUE) {
                    //tick value not below lowest data value
                    TextAnchor anchor;
                    TextAnchor rotationAnchor;
                    double angle = 0.0;
                    if (isVerticalTickLabels()) {
                        anchor = TextAnchor.CENTER_RIGHT;
                        rotationAnchor = TextAnchor.CENTER_RIGHT;
                        if (edge == RectangleEdge.TOP) {
                            angle = Math.PI / 2.0;
                        }
                        else {
                            angle = -Math.PI / 2.0;
                        }
                    }
                    else {
                        if (edge == RectangleEdge.TOP) {
                            anchor = TextAnchor.BOTTOM_CENTER;
                            rotationAnchor = TextAnchor.BOTTOM_CENTER;
                        }
                        else {
                            anchor = TextAnchor.TOP_CENTER;
                            rotationAnchor = TextAnchor.TOP_CENTER;
                        }
                    }

                    Tick tick = new NumberTick(currentTickValue, tickLabel, 
                            anchor, rotationAnchor, angle);
                    ticks.add(tick);
                }
            }
        }
        return ticks;

    }

    
    @Override
    protected List refreshTicksVertical(Graphics2D g2, Rectangle2D dataArea,
            RectangleEdge edge) {

        List ticks = new java.util.ArrayList();

        //get lower bound value:
        double lowerBoundVal = getRange().getLowerBound();
        //if small log values and lower bound value too small
        // then set to a small value (don't allow <= 0):
        if (this.smallLogFlag && lowerBoundVal < SMALL_LOG_VALUE) {
            lowerBoundVal = SMALL_LOG_VALUE;
        }
        //get upper bound value
        double upperBoundVal = getRange().getUpperBound();

        //get log10 version of lower bound and round to integer:
        int iBegCount = (int) Math.rint(switchedLog10(lowerBoundVal));
        //get log10 version of upper bound and round to integer:
        int iEndCount = (int) Math.rint(switchedLog10(upperBoundVal));

        if (iBegCount == iEndCount && iBegCount > 0
                && Math.pow(10, iBegCount) > lowerBoundVal) {
              //only 1 power of 10 value, it's > 0 and its resulting
              // tick value will be larger than lower bound of data
            --iBegCount;       //decrement to generate more ticks
        }

        double tickVal;
        String tickLabel;
        boolean zeroTickFlag = false;
        for (int i = iBegCount; i <= iEndCount; i++) {
            //for each tick with a label to be displayed
            int jEndCount = 10;
            if (i == iEndCount) {
                jEndCount = 1;
            }

            for (int j = 0; j < jEndCount; j++) {
                //for each tick to be displayed
                if (this.smallLogFlag) {
                    //small log values in use
                    tickVal = Math.pow(10, i) + (Math.pow(10, i) * j);
                    if (j == 0) {
                        //first tick of group; create label text
                        if (this.log10TickLabelsFlag) {
                            //if flag then
                            tickLabel = "10^" + i;   //create "log10"-type label
                        }
                        else {    //not "log10"-type label
                            if (this.expTickLabelsFlag) {
                                //if flag then
                                tickLabel = "1e" + i;  //create "1e#"-type label
                            }
                            else {    //not "1e#"-type label
                                if (i >= 0) {   // if positive exponent then
                                                // make integer
                                    NumberFormat format
                                        = getNumberFormatOverride();
                                    if (format != null) {
                                        tickLabel = format.format(tickVal);
                                    }
                                    else {
                                        tickLabel = Long.toString((long)
                                                Math.rint(tickVal));
                                    }
                                }
                                else {
                                    //negative exponent; create fractional value
                                    //set exact number of fractional digits to
                                    // be shown:
                                    this.numberFormatterObj
                                        .setMaximumFractionDigits(-i);
                                    //create tick label:
                                    tickLabel = this.numberFormatterObj.format(
                                            tickVal);
                                }
                            }
                        }
                    }
                    else {   //not first tick to be displayed
                        tickLabel = "";     //no tick label
                    }
                }
                else { //not small log values in use; allow for values <= 0
                    if (zeroTickFlag) {      //if did zero tick last iter then
                        --j;
                    }               //decrement to do 1.0 tick now
                    tickVal = (i >= 0) ? Math.pow(10, i) + (Math.pow(10, i) * j)
                             : -(Math.pow(10, -i) - (Math.pow(10, -i - 1) * j));
                    if (j == 0) {  //first tick of group
                        if (!zeroTickFlag) {     // did not do zero tick last
                                                 // iteration
                            if (i > iBegCount && i < iEndCount
                                    && Math.abs(tickVal - 1.0) < 0.0001) {
                                // not first or last tick on graph and value
                                // is 1.0
                                tickVal = 0.0;        //change value to 0.0
                                zeroTickFlag = true;  //indicate zero tick
                                tickLabel = "0";      //create label for tick
                            }
                            else {
                                //first or last tick on graph or value is 1.0
                                //create label for tick:
                                if (this.log10TickLabelsFlag) {
                                       //create "log10"-type label
                                    tickLabel = (((i < 0) ? "-" : "")
                                            + "10^" + Math.abs(i));
                                }
                                else {
                                    if (this.expTickLabelsFlag) {
                                           //create "1e#"-type label
                                        tickLabel = (((i < 0) ? "-" : "")
                                                + "1e" + Math.abs(i));
                                    }
                                    else {
                                        NumberFormat format
                                            = getNumberFormatOverride();
                                        if (format != null) {
                                            tickLabel = format.format(tickVal);
                                        }
                                        else {
                                            tickLabel =  Long.toString(
                                                    (long) Math.rint(tickVal));
                                        }
                                    }
                                }
                            }
                        }
                        else {     // did zero tick last iteration
                            tickLabel = "";         //no label
                            zeroTickFlag = false;   //clear flag
                        }
                    }
                    else {       // not first tick of group
                        tickLabel = "";           //no label
                        zeroTickFlag = false;     //make sure flag cleared
                    }
                }

                if (tickVal > upperBoundVal) {
                    return ticks;  //if past highest data value then exit method
                }

                if (tickVal >= lowerBoundVal - SMALL_LOG_VALUE) {
                    //tick value not below lowest data value
                    TextAnchor anchor;
                    TextAnchor rotationAnchor;
                    double angle = 0.0;
                    if (isVerticalTickLabels()) {
                        if (edge == RectangleEdge.LEFT) {
                            anchor = TextAnchor.BOTTOM_CENTER;
                            rotationAnchor = TextAnchor.BOTTOM_CENTER;
                            angle = -Math.PI / 2.0;
                        }
                        else {
                            anchor = TextAnchor.BOTTOM_CENTER;
                            rotationAnchor = TextAnchor.BOTTOM_CENTER;
                            angle = Math.PI / 2.0;
                        }
                    }
                    else {
                        if (edge == RectangleEdge.LEFT) {
                            anchor = TextAnchor.CENTER_RIGHT;
                            rotationAnchor = TextAnchor.CENTER_RIGHT;
                        }
                        else {
                            anchor = TextAnchor.CENTER_LEFT;
                            rotationAnchor = TextAnchor.CENTER_LEFT;
                        }
                    }
                    //create tick object and add to list:
                    ticks.add(new NumberTick(tickVal, tickLabel, anchor, 
                            rotationAnchor, angle));
                }
            }
        }
        return ticks;
    }

    
    protected String makeTickLabel(double val, boolean forceFmtFlag) {
        if (this.expTickLabelsFlag || forceFmtFlag) {
            //using exponents or force-formatter flag is set
            // (convert 'E' to lower-case 'e'):
            return this.numberFormatterObj.format(val).toLowerCase();
        }
        return getTickUnit().valueToString(val);
    }

    
    protected String makeTickLabel(double val) {
        return makeTickLabel(val, false);
    }

}
