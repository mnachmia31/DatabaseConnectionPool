package com.mnachmia31.util;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

/**
* Utility class to perform operations on date times.
*
* @author      Michael Nachmias <michael.nachmias@gmail.com>
* @version     1.0
* @since       2014-10-24
* @see DatabaseProperties
*/ 
public class DateTimeUtil {

    /**
     * Gets the time difference between a date and the current time in seconds
     * 
     * @param date The date you want to compare to the current time
     * @return int The difference between a date and the current time in seconds
     */
    public static int getTimeDiff(DateTime dateTime) {
        DateTime now = DateTime.now();
        return Seconds.secondsBetween(dateTime, now).getSeconds();
    }
}