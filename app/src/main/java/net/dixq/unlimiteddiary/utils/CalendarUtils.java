package net.dixq.unlimiteddiary.utils;

import java.util.Calendar;

public class CalendarUtils {
    public static String getDatOfWeek(int year, int month, int date){
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, date);
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.SUNDAY:     // Calendar.SUNDAY:1 （値。意味はない）
                return "日";
            case Calendar.MONDAY:     // Calendar.MONDAY:2
                return "月";
            case Calendar.TUESDAY:    // Calendar.TUESDAY:3
                return "火";
            case Calendar.WEDNESDAY:  // Calendar.WEDNESDAY:4
                return "水";
            case Calendar.THURSDAY:   // Calendar.THURSDAY:5
                return "木";
            case Calendar.FRIDAY:     // Calendar.FRIDAY:6
                return "金";
            case Calendar.SATURDAY:   // Calendar.SATURDAY:7
                return "土";
        }
        return null;
    }

}
