
package org.jfree.chart.date;

import java.util.Calendar;
import java.util.Date;


public class SpreadsheetDate extends SerialDate {

    
    private static final long serialVersionUID = -2039586705374454461L;
    
    
    private final int serial;

    
    private final int day;

    
    private final int month;

    
    private final int year;

    
    public SpreadsheetDate(int day, int month, int year) {

        if ((year >= 1900) && (year <= 9999)) {
            this.year = year;
        }
        else {
            throw new IllegalArgumentException(
                "The 'year' argument must be in range 1900 to 9999.");
        }

        if ((month >= MonthConstants.JANUARY) 
                && (month <= MonthConstants.DECEMBER)) {
            this.month = month;
        }
        else {
            throw new IllegalArgumentException(
                "The 'month' argument must be in the range 1 to 12.");
        }

        if ((day >= 1) && (day <= SerialDate.lastDayOfMonth(month, year))) {
            this.day = day;
        } else {
            throw new IllegalArgumentException("Invalid 'day' argument.");
        }

        // the serial number needs to be synchronised with the day-month-year...
        this.serial = calcSerial(day, month, year);
    }

    
    public SpreadsheetDate(int serial) {

        if ((serial >= SERIAL_LOWER_BOUND) && (serial <= SERIAL_UPPER_BOUND)) {
            this.serial = serial;
        }
        else {
            throw new IllegalArgumentException(
                "SpreadsheetDate: Serial must be in range 2 to 2958465.");
        }

        // the day-month-year needs to be synchronised with the serial number...
        // get the year from the serial date
        final int days = this.serial - SERIAL_LOWER_BOUND;
        // overestimated because we ignored leap days
        final int overestimatedYYYY = 1900 + (days / 365);
        final int leaps = SerialDate.leapYearCount(overestimatedYYYY);
        final int nonleapdays = days - leaps;
        // underestimated because we overestimated years
        int underestimatedYYYY = 1900 + (nonleapdays / 365);

        if (underestimatedYYYY == overestimatedYYYY) {
            this.year = underestimatedYYYY;
        } else {
            int ss1 = calcSerial(1, 1, underestimatedYYYY);
            while (ss1 <= this.serial) {
                underestimatedYYYY = underestimatedYYYY + 1;
                ss1 = calcSerial(1, 1, underestimatedYYYY);
            }
            this.year = underestimatedYYYY - 1;
        }

        final int ss2 = calcSerial(1, 1, this.year);

        int[] daysToEndOfPrecedingMonth 
                = AGGREGATE_DAYS_TO_END_OF_PRECEDING_MONTH;

        if (isLeapYear(this.year)) {
            daysToEndOfPrecedingMonth 
                    = LEAP_YEAR_AGGREGATE_DAYS_TO_END_OF_PRECEDING_MONTH;
        }

        // get the month from the serial date
        int mm = 1;
        int sss = ss2 + daysToEndOfPrecedingMonth[mm] - 1;
        while (sss < this.serial) {
            mm = mm + 1;
            sss = ss2 + daysToEndOfPrecedingMonth[mm] - 1;
        }
        this.month = mm - 1;

        // what's left is d(+1);
        this.day = this.serial - ss2 
                 - daysToEndOfPrecedingMonth[this.month] + 1;

    }

    
    @Override
    public int toSerial() {
        return this.serial;
    }

    
    @Override
    public Date toDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(getYYYY(), getMonth() - 1, getDayOfMonth(), 0, 0, 0);
        return calendar.getTime();
    }

    
    @Override
    public int getYYYY() {
        return this.year;
    }

    
    @Override
    public int getMonth() {
        return this.month;
    }

    
    @Override
    public int getDayOfMonth() {
        return this.day;
    }

    
    @Override
    public int getDayOfWeek() {
        return (this.serial + 6) % 7 + 1;
    }

    
    @Override
    public boolean equals(Object object) {

        if (object instanceof SerialDate) {
            SerialDate s = (SerialDate) object;
            return (s.toSerial() == this.toSerial());
        } else {
            return false;
        }

    }

    
    @Override
    public int hashCode() {
        return toSerial();
    }

    
    @Override
    public int compare(SerialDate other) {
        return this.serial - other.toSerial();
    }

    
    @Override
    public int compareTo(Object other) {
        return compare((SerialDate) other);    
    }
    
    
    @Override
    public boolean isOn(SerialDate other) {
        return (this.serial == other.toSerial());
    }

    
    @Override
    public boolean isBefore(SerialDate other) {
        return (this.serial < other.toSerial());
    }

    
    @Override
    public boolean isOnOrBefore(SerialDate other) {
        return (this.serial <= other.toSerial());
    }

    
    @Override
    public boolean isAfter(SerialDate other) {
        return (this.serial > other.toSerial());
    }

    
    @Override
    public boolean isOnOrAfter(SerialDate other) {
        return (this.serial >= other.toSerial());
    }

    
    @Override
    public boolean isInRange(SerialDate d1, SerialDate d2) {
        return isInRange(d1, d2, SerialDate.INCLUDE_BOTH);
    }

    
    @Override
    public boolean isInRange(SerialDate d1, SerialDate d2, int include) {
        int s1 = d1.toSerial();
        int s2 = d2.toSerial();
        int start = Math.min(s1, s2);
        int end = Math.max(s1, s2);
        
        int s = toSerial();
        if (include == SerialDate.INCLUDE_BOTH) {
            return (s >= start && s <= end);
        }
        else if (include == SerialDate.INCLUDE_FIRST) {
            return (s >= start && s < end);            
        }
        else if (include == SerialDate.INCLUDE_SECOND) {
            return (s > start && s <= end);            
        }
        else {
            return (s > start && s < end);            
        }    
    }

    
    private int calcSerial(int d, int m, int y) {
        int yy = ((y - 1900) * 365) + SerialDate.leapYearCount(y - 1);
        int mm = SerialDate.AGGREGATE_DAYS_TO_END_OF_PRECEDING_MONTH[m];
        if (m > MonthConstants.FEBRUARY) {
            if (SerialDate.isLeapYear(y)) {
                mm = mm + 1;
            }
        }
        int dd = d;
        return yy + mm + dd + 1;
    }

}

