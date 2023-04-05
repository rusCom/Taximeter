package org.toptaxi.taximeter.tools;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Calendar;

public class DateTimeTools {

    public static boolean isToday(LocalDateTime dateTime){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate now = LocalDate.now();
            LocalDate checkDate = dateTime.toLocalDate();
            if (now.equals(checkDate)){return true;}
            return false;
        }
        return false;
    }
    public static boolean isTomorrow(LocalDateTime dateTime){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            LocalDate now = LocalDate.now().plusDays(1);
            LocalDate checkDate = dateTime.toLocalDate();
            if (now.equals(checkDate)){return true;}
            return false;
        }
        return false;
    }

    public static String getTime(Calendar calendar){
        String result = "";
        if (calendar != null){
            String hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
            if (calendar.get(Calendar.HOUR_OF_DAY) < 10){hour = "0" + calendar.get(Calendar.HOUR_OF_DAY);}
            String minute = String.valueOf(calendar.get(Calendar.MINUTE));
            if (calendar.get(Calendar.MINUTE) < 10){minute = "0" + calendar.get(Calendar.MINUTE);}
            result = hour + ":" + minute;
        }
        return result;
    }

    public static boolean isTomorrow(Calendar date){
        Calendar curDate = Calendar.getInstance();
        curDate.setTimeInMillis(System.currentTimeMillis());
        return (date.get(Calendar.DAY_OF_YEAR) == (curDate.get(Calendar.DAY_OF_YEAR) + 1))
                & (date.get(Calendar.YEAR) == curDate.get(Calendar.YEAR));
    }

    public static boolean isAfterTomorrow(Calendar date){
        Calendar curDate = Calendar.getInstance();
        curDate.setTimeInMillis(System.currentTimeMillis());
        return (date.get(Calendar.DAY_OF_YEAR) == (curDate.get(Calendar.DAY_OF_YEAR) + 2))
                & (date.get(Calendar.YEAR) == curDate.get(Calendar.YEAR));
    }

    public static boolean isCurDate(Calendar date){
        Calendar curDate = Calendar.getInstance();
        curDate.setTimeInMillis(System.currentTimeMillis());
        return (date.get(Calendar.DAY_OF_YEAR) == curDate.get(Calendar.DAY_OF_YEAR))
                & (date.get(Calendar.YEAR) == curDate.get(Calendar.YEAR));
    }

    public static String getSklonMonthName(Calendar date){
        switch (date.get(Calendar.MONTH)){
            case 0:return "января";
            case 1:return "февраля";
            case 2:return "марта";
            case 3:return "апреля";
            case 4:return "майя";
            case 5:return "июня";
            case 6:return "июля";
            case 7:return "августа";
            case 8:return "сентября";
            case 9:return "октября";
            case 10:return "ноября";
            case 11:return "декабря";
            default:return "ХЗ";
        }
    }
}
