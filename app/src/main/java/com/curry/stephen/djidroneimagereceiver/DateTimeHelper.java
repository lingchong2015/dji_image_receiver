package com.curry.stephen.djidroneimagereceiver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by lingchong on 16/6/23.
 */
public class DateTimeHelper {

    public static Date getDateTimeNowForDate() {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE).parse(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                    Locale.CHINESE).format(Calendar.getInstance().getTime()));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getDateTimeNow() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE).format(Calendar.getInstance().getTime());
    }

    public static String formatDate(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINESE);
        return simpleDateFormat.format(date);
    }

    public static Date getDateNowForDate() {
        try {
            return new SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE).parse(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE).format(new Date()));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getDateNow() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE);
        return simpleDateFormat.format(new Date());
    }

    public static int compareDate(String d1, String d2) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINESE);
        try {
            Date dt1 = simpleDateFormat.parse(d1);
            Date dt2 = simpleDateFormat.parse(d2);
            if (dt1.getTime() > dt2.getTime()) {
                return 1;
            } else if (dt1.getTime() < dt2.getTime()) {
                return -1;
            } else {
                return 0;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return 0;
    }

    public static String convertToDateByFormatter(String myDate, String sourceFormatter, String destinationFormatter) {
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat(sourceFormatter, Locale.CHINESE);
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat(destinationFormatter, Locale.CHINESE);
        try {
            Date date = simpleDateFormat1.parse(myDate);
            return simpleDateFormat2.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getTickNow() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * yyyy-MM-dd hh:mm:ss => hh.xx(分与秒转换为十进制).
     */
    public static float getTime(String textDateTime) {
        try {
            String[] sArray = textDateTime.split(" ");
            String[] timeArray = sArray[1].split(":");
            float hour = Float.parseFloat(timeArray[0]);
            double minute = Float.parseFloat(timeArray[1]);
            double second = Float.parseFloat(timeArray[2]);
            String textResult = String.format("%.2f", hour + minute / 60.0f + second / 3600.0f);
            return Float.parseFloat(textResult);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public static int getHour(String textDateTime, String formatter) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formatter);
        try {
            Date datetime = simpleDateFormat.parse(textDateTime);
            return datetime.getHours();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 小时、分、秒=>总秒数.
     */
    public static float getSeconds(String textDateTime) {
        try {
            String[] sArray = textDateTime.split(" ");
            String[] timeArray = sArray[1].split(":");
            return Float.parseFloat(timeArray[0]) * 3600 + Float.parseFloat(timeArray[1]) * 60 + Float.parseFloat(timeArray[2]);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 获取输入时间与当前时间之差(秒级).
     * @param textDateTime 输入时间文本.
     * @param dateTimeFormatter 输入时间文本的时间格式.
     * @return 返回时间之差(秒级), 若出现异常, 则返回-1000.
     */
    public static float getTimeElapse(String textDateTime, String dateTimeFormatter) {
        try {
            Date now = getDateTimeNowForDate();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateTimeFormatter, Locale.CHINESE);
            Date dateTime = simpleDateFormat.parse(textDateTime);
            return (now.getTime() - dateTime.getTime()) / 1000.0f;
        } catch (ParseException e) {
            e.printStackTrace();
            return -1000;
        }
    }
}
