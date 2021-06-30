package com.huateng.common.util;

import java.text.ParseException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DateUtil {

    public static final String DATE_FORMAT_SHORT = "yyyy-MM-dd";
    public static final String DATE_FORMAT_FULL = "yyyy-MM-dd HH:mm:ss";

    public static final String DATE_YEAR = "yyyy";
    public static final String DATE_YEAR_MONTH = "yyyyMM";
    public static final String DATE_FORMAT_COMPACT = "yyyyMMdd";
    public static final String DATE_FORMAT_TIME = "HHmmss";
    public static final String DATE_FORMAT_COMPACT_FULL = "yyyyMMddHHmmss";
    public static final String DATE_FORMAT_FULL_MSE = "yyyyMMddHHmmssSSS";
    public static final String DATE_FORMAT_FULL_MSEL = "yyyyMMddHHmmssSSSS";

    private DateUtil(){

    }
    public enum dayOfWeek  {
        one("1","周日"),
        two("2","周一"),
        three("3","周二"),
        four("4","周三"),
        five("5","周四"),
        six("6","周五"),
        seven("7","周六");

        private final String value;
        private final String desc;
        dayOfWeek(String value,String desc) {
            this.value = value;
            this.desc = desc;
        }
        public String getValue() {
            return value;
        }

        public String getDesc() {
            return desc;
        }
        //根据value获取描述
        public static String getDescByValue(String value){
            dayOfWeek[] arr = dayOfWeek.values();
            String desc = "";
            for(dayOfWeek state:arr){
                if(state.value.equals(value)){
                    desc = state.getDesc();
                    break;
                }
            }
            return desc;
        }
    }


    /**
     * 获取时区
     * @return ZoneId
     */
    public static ZoneId getZoneId(){
        return ZoneOffset.systemDefault();
    }

    public static ZoneOffset getZoneOffset() {
        return OffsetDateTime.now().getOffset();
    }

    /**
     * 获取当前时间
     * @return LocalTime
     */
    public static LocalTime getCurrentLocalTime(){
        return LocalTime.now(getZoneId());
    }

    /**
     * 获取系统当前日期
     * @return LocalDate
     */
    public static LocalDate getCurrentLocalDate() {
        return LocalDate.now(getZoneId());
    }

    /**
     * 获取系统当前日期时间
     * @return LocalDateTime
     */
    public static LocalDateTime getCurrentLocalDateTime() {
        return LocalDateTime.now(getZoneId());
    }

    /**
     * 根据格式获取当前时间
     * @param format 格式
     * @return String
     */
    public static String getCurrentLocalDateTime(String format) {
        return LocalDateTime.now(getZoneId()).format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * 根据指定时间返回指定格式
     * @param localDateTime 指定时间
     * @param format 指定格式
     * @return String
     */
    public static String getCurrentLocalDateTime(LocalDateTime localDateTime, String format) {
        return localDateTime.format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * 得到当前日期时间 yyyy-MM-dd HH:mm:ss格式
     * @return String
     */
    public static String getCurrentDateFull() {
        return getCurrentLocalDateTime(DATE_FORMAT_FULL);
    }

    public static String getCurrentDateyyyy() {
        return getCurrentLocalDateTime(DATE_YEAR);
    }

    public static String getCurrentDateyyyyMM() {
        return getCurrentLocalDateTime(DATE_YEAR_MONTH);
    }

    public static String getCurrentDateyyyyMMdd() {
        return getCurrentLocalDateTime(DATE_FORMAT_COMPACT);
    }

    public static String getCurrentDateHHmmss() {
        return getCurrentLocalDateTime(DATE_FORMAT_TIME);
    }

    public static String getCurrentDateyyyyMMddHHmmss() {
        return getCurrentLocalDateTime(DATE_FORMAT_COMPACT_FULL);
    }

    public static String getTimeStr(LocalDateTime dateTime,String formatter){
        return dateTime.format(DateTimeFormatter.ofPattern(formatter));
    }

    /**
     * 获取时间戳
     * @return String
     */
    public static String getCurrentTimeStamp() {
        return LocalDateTime.now(getZoneId()).format(DateTimeFormatter.ofPattern(DATE_FORMAT_FULL_MSEL));
    }

    /**
     * 字符串转年月日的Date
     * @param theDate 日期字符
     * @param timeDtf 时间格式
     * @return Date
     */
    public static Date string2Date4LocalDate(String theDate, String timeDtf){
        LocalDate localDate = string2LocalDate(theDate, timeDtf);
        ZonedDateTime zonedDateTime = localDate.atStartOfDay(getZoneId());
        return Date.from(zonedDateTime.toInstant());
    }

    /**
     * 字符串转换年月日的Date并增加天数
     * @param theDate 日期字符
     * @param day 天数
     * @param timeDtf 时间格式
     * @return
     */
    public static Date string2Date4LocalDateAddDay(String theDate,long day, String timeDtf) {
        LocalDate localDate = string2LocalDate(theDate, timeDtf);
        ZonedDateTime zonedDateTime = localDate.plusDays(day).atStartOfDay(getZoneId());
        return Date.from(zonedDateTime.toInstant());
    }

    /**
     * 字符串转年月日时分秒的Date
     * @param theDate 日期字符
     * @param timeDtf 时间格式
     * @return Date
     */
    public static Date string2Date4LocalDateTime(String theDate, String timeDtf){
        LocalDateTime localDateTime = string2LocalDateTime(theDate, timeDtf);
        ZonedDateTime zonedDateTime = localDateTime.atZone(getZoneId());
        return Date.from(zonedDateTime.toInstant());
    }

    public static Date LocalDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from( localDateTime.atZone( getZoneId()).toInstant());
    }


    /**
     * date转成字符
     * @param theDate 日期字符
     * @param timeDtf 时间格式
     * @return String
     */
    public static String date2String(Date theDate, String timeDtf) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(theDate.toInstant(), getZoneId());
        return localDateTime.format(DateTimeFormatter.ofPattern(timeDtf));
    }

    /**
     * 格式转换
     * @param theDate 日期字符
     * @param timeDtf1 旧格式
     * @param timeDtf2 新格式
     * @return String
     */
    public static String changeFormat4LocalDate(String theDate,String timeDtf1,String timeDtf2){
        LocalDate localDate = string2LocalDate(theDate, timeDtf1);
        return localDate.format(DateTimeFormatter.ofPattern(timeDtf2));
    }

    /**
     * 格式转换
     * @param theDate 日期字符
     * @param format1 旧格式
     * @param format2 新格式
     * @return String
     */
    public static String changeFormat4LocalDateTime(String theDate,String format1,String format2){
        LocalDateTime localDate = string2LocalDateTime(theDate, format1);
        return localDate.format(DateTimeFormatter.ofPattern(format2));
    }

    /**
     * 日期字符转时间
     * @param theDate 日期字符
     * @param timeDtf 格式
     * @return LocalTime
     */
    public static LocalTime string2LocalTime(String theDate,String timeDtf){
        return LocalTime.parse(theDate,DateTimeFormatter.ofPattern(timeDtf));
    }

    /**
     * 日期字符转日期
     * @param theDate 日期字符
     * @param timeDtf 格式
     * @return LocalDate
     */
    public static LocalDate string2LocalDate(String theDate,String timeDtf){
        return LocalDate.parse(theDate,DateTimeFormatter.ofPattern(timeDtf));
    }

    /**
     * 日期字符转日期时间
     * @param theDate 日期字符
     * @param timeDtf 格式
     * @return LocalDateTime
     */
    public static LocalDateTime string2LocalDateTime(String theDate,String timeDtf){
        return LocalDateTime.parse(theDate,DateTimeFormatter.ofPattern(timeDtf));
    }


    /**
     * 判断是否是今天
     * @param theDate 日期字符
     * @param timeDtf 格式
     * @return boolean
     */
    public static boolean isCurrentDay(String theDate,String timeDtf) {
        boolean bRet = false;
        LocalDate localDate = string2LocalDate(theDate, timeDtf);
        if (LocalDate.now().getYear() == localDate.getYear()) {
            MonthDay monthDay = MonthDay.from(localDate);
            MonthDay today = MonthDay.from(LocalDate.now());
            return monthDay.equals(today);
        }
        return bRet;
    }

    /**
     * 获取几小时后的时间
     * @param hour 偏移小时数
     * @param timeDtf 时间格式
     * @return String
     */
    public static String getPlusHours4LocalTime(int hour, String timeDtf) {
        return LocalTime.now().plusHours(hour).format(DateTimeFormatter.ofPattern(timeDtf));
    }

    /**
     * 获取指定日期几天后的时间
     * @param theDate 日期字符
     * @param hour 偏移小时数
     * @param timeDtf 时间格式
     * @return String
     */
    public static String getPlusHours4LocalTime(String theDate, int hour, String timeDtf) {
        return string2LocalTime(theDate, timeDtf).plusHours(hour).format(DateTimeFormatter.ofPattern(timeDtf));
    }

    /**
     * 获取几小时后的时间
     * @param hour 偏移小时数
     * @param timeDtf 格式
     * @return String
     */
    public static String getPlusHours4LocalDateTime(int hour, String timeDtf) {
        return LocalDateTime.now().plusHours(hour).format(DateTimeFormatter.ofPattern(timeDtf));
    }

    /**
     * 获取几小时后的时间
     * @param theDate 日期字符
     * @param hour 偏移小时数
     * @param timeDtf 格式
     * @return String
     */
    public static String getPlusHours4LocalDateTime(String theDate,int hour, String timeDtf) {
        return string2LocalDateTime(theDate, timeDtf).plusHours(hour).format(DateTimeFormatter.ofPattern(timeDtf));
    }

    /**
     * 获取几天后的时间
     * @param day 偏移天数
     * @param timeDtf 格式
     * @return String
     */
    public static String getPlusDays4LocalDate(int day,String timeDtf) {
        return LocalDate.now().plusDays(day).format(DateTimeFormatter.ofPattern(timeDtf));
    }

    /**
     * 获取几天后的时间
     * @param theDate 日期字符
     * @param day 偏移天数
     * @param timeDtf 格式
     * @return String
     */
    public static String getPlusDays4LocalDate(String theDate,int day,String timeDtf) {
        return string2LocalDate(theDate,timeDtf).plusDays(day).format(DateTimeFormatter.ofPattern(timeDtf));
    }


    /**
     * 获取几天后的时间
     * @param day 偏移天数
     * @param timeDtf 格式
     * @return String
     */
    public static String getPlusDays4LocalDateTime(int day,String timeDtf) {
        return LocalDateTime.now().plusDays(day).format(DateTimeFormatter.ofPattern(timeDtf));
    }

    /**
     * 获取几天后的时间
     * @param theDate 日期字符
     * @param day 偏移天数
     * @param timeDtf 格式
     * @return String
     */
    public static String getPlusDays4LocalDateTime(String theDate,int day,String timeDtf) {
        return string2LocalDateTime(theDate,timeDtf).plusDays(day).format(DateTimeFormatter.ofPattern(timeDtf));
    }

    /**
     * 获取几个月后的时间
     * @param month 偏移月数
     * @param timeDtf 格式
     * @return String
     */
    public static String getPlusMonths4LocalDate(long month,String timeDtf) {
        return LocalDate.now().plusMonths(month).format(DateTimeFormatter.ofPattern(timeDtf));
    }

    /**
     * 获取几个月后的时间
     * @param month 偏移月数
     * @param timeDtf 格式
     * @return String
     */
    public static String getPlusMonths4LocalDateTime(long month,String timeDtf) {
        return LocalDateTime.now().plusMonths(month).format(DateTimeFormatter.ofPattern(timeDtf));
    }

    /**
     * 获取几个月后的时间
     * @param theDate 日期字符
     * @param month 偏移月数
     * @param timeDtf 格式
     * @return String
     */
    public static String getPlusMonths4LocalDate(String theDate,long month,String timeDtf) {
        return string2LocalDate(theDate,timeDtf).plusMonths(month).format(DateTimeFormatter.ofPattern(timeDtf));
    }

    /**
     * 获取几个月后的时间
     * @param theDate 日期字符
     * @param month 偏移月数
     * @param timeDtf 格式
     * @return String
     */
    public static String getPlusMonths4LocalDateTime(String theDate,long month,String timeDtf) {
        return string2LocalDateTime(theDate,timeDtf).plusMonths(month).format(DateTimeFormatter.ofPattern(timeDtf));
    }

    /**
     * 比较日期
     * @param theDate 日期字符1
     * @param otherDate 日期字符2
     * @param timeDtf 格式
     * @return boolean
     */
    public static boolean isBefore4LocalTime(String theDate, String otherDate, String timeDtf) {
        LocalTime localTime1 = string2LocalTime(theDate, timeDtf);
        LocalTime localTime2 = string2LocalTime(otherDate, timeDtf);
        return localTime1.isBefore(localTime2);
    }

    /**
     * 比较日期
     * @param theDate 日期字符1
     * @param otherDate 日期字符2
     * @param timeDtf 格式
     * @return boolean
     */
    public static boolean isBefore4LocalDate(String theDate, String otherDate, String timeDtf) {
        LocalDate localDateTime1 = string2LocalDate(theDate, timeDtf);
        LocalDate localDateTime2 = string2LocalDate(otherDate, timeDtf);
        return localDateTime1.isBefore(localDateTime2);
    }

    /**
     * 比较日期
     * @param theDate 日期字符1
     * @param otherDate 日期字符2
     * @param timeDtf 格式
     * @return boolean
     */
    public static boolean isBefore4LocalDateTime(String theDate, String otherDate, String timeDtf) {
        LocalDateTime localDateTime1 = string2LocalDateTime(theDate, timeDtf);
        LocalDateTime localDateTime2 = string2LocalDateTime(otherDate, timeDtf);
        return localDateTime1.isBefore(localDateTime2);
    }

    /**
     * 计算两个日期的毫秒差
     * @param theDate 日期字符1
     * @param otherDate 日期字符2
     * @param timeDtf 格式
     * @return
     */
    public static long getSubtractMilli4LocalDateTime(String theDate, String otherDate, String timeDtf) {
        LocalDateTime localDateTime1 = string2LocalDateTime(theDate, timeDtf);
        LocalDateTime localDateTime2 = string2LocalDateTime(otherDate, timeDtf);
        long result1 = localDateTime1.atZone(getZoneId()).toInstant().toEpochMilli();
        long result2 = localDateTime2.atZone(getZoneId()).toInstant().toEpochMilli();
        return result1-result2;
    }

    /**
     * 计算两个日期相差几天
     * @param theDate 日期字符1
     * @param otherDate 日期字符2
     * @param timeDtf 格式
     * @return
     */
    public static long getSubtractDays(String theDate, String otherDate, String timeDtf) {
        LocalDate startDate = string2LocalDate(theDate,timeDtf);
        LocalDate endDate = string2LocalDate(otherDate,timeDtf);
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * 将秒值转换为小时分秒
     * @param sec 毫秒值
     * @return String
     */
    public static String changeTime(long sec) {
        String temp = "";
        if (sec < 60) {
            temp = "" + sec + "秒";
        } else if (sec < 3600) {
            temp = "" + sec / 60 + "分" + sec % 60 + "秒";
        } else {
            temp = "" + sec / 3600 + "小时" + (sec % 3600) / 60 + "分" + sec % 60 + "秒";
        }
        return temp;
    }

    /**
     * 计算两个时间的时间差 天小时分秒
     * @param time1 时间1
     * @param time2 时间1
     * @return String
     */
    public static String getTimeDiff(Date time1, Date time2){
        long l = time1.getTime() - time2.getTime();
        String returnStr = "";
        long day = l / (24 * 60 * 60 * 1000);
        if (day > 0) {
            returnStr += (day + "天");
        }
        long hour = (l / (60 * 60 * 1000) - day * 24);
        if (hour > 0) {
            returnStr += (hour + "小时");
        }
        long min = ((l / (60 * 1000)) - day * 24 * 60 - hour * 60);
        if (min > 0) {
            returnStr += (min + "分");
        }
        long s = (l / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        if (s > 0) {
            returnStr += (s + "秒");
        }
        return returnStr;
    }

    /**
     * 指定日期年的最后一天（yyyy-MM-dd）
     * @param curDate 指定日期
     * @param curTimeDtf 指定格式
     * @param firstOrLast true:第一天，false:最后一天
     * @param timeDtf 结果日期格式
     * @return
     */
    public static String getLastDayOfYear(String curDate,String curTimeDtf, boolean firstOrLast,String timeDtf) {
        if (firstOrLast) {
            return LocalDate.parse(curDate, DateTimeFormatter.ofPattern(curTimeDtf)).with(TemporalAdjusters.firstDayOfYear()).format(DateTimeFormatter.ofPattern(timeDtf));
        } else {
            return LocalDate.parse(curDate, DateTimeFormatter.ofPattern(curTimeDtf)).with(TemporalAdjusters.lastDayOfYear()).format(DateTimeFormatter.ofPattern(timeDtf));
        }
    }

    /**
     * 转换日期
     * @return
     */
    public static Date parseyyyyMMdd(String date) throws ParseException {
        return string2Date4LocalDate(date,DATE_FORMAT_COMPACT);
    }

    /**
     * 计算目前是一年中的第几周 从星期一开始
     * @param curDate
     * @param curTimeDtf
     * @return
     */
    public static int getYearOfWeek(String curDate,String curTimeDtf){
        return string2LocalDate(curDate, curTimeDtf).get(WeekFields.of(DayOfWeek.MONDAY,1).weekOfWeekBasedYear());
    }

    /**
     * 计算目前是月份中的第几周  从星期一开始
     * @param curDate
     * @param curTimeDtf
     * @return
     */
    public static int getMonthOfWeek(String curDate,String curTimeDtf){
        return string2LocalDate(curDate, curTimeDtf).get(WeekFields.of(DayOfWeek.MONDAY,1).weekOfMonth());
    }

    /**
     * 获取月份
     * @param curDate
     * @param curTimeDtf
     * @return
     */
    public static int getMonth(String curDate,String curTimeDtf){
        return string2LocalDate(curDate, curTimeDtf).getMonth().getValue();
    }

    /**
     * 获取星期几
     * @param curDate
     * @param curTimeDtf
     * @param dayOfWeek
     * @return
     */
    public static int getWeek(String curDate,String curTimeDtf,DayOfWeek dayOfWeek){
        return string2LocalDate(curDate, curTimeDtf).getDayOfWeek().get(WeekFields.of(dayOfWeek,1).dayOfWeek());
    }

    /**
     * 获取月份几号
     * @param curDate
     * @param curTimeDtf
     * @return
     */
    public static int getDate(String curDate,String curTimeDtf){
        return string2LocalDate(curDate, curTimeDtf).getDayOfMonth();
    }

    /**
     * 是否是生日当周
     * @param birthday
     * @param curDate
     * @param curTimeDtf
     * @return
     */
    public static String isBirthdayWeek(String birthday,String curDate,String curTimeDtf){
        Map<String,String> dateMap = new HashMap<>(7);
        LocalDate localDate = string2LocalDate(birthday, curTimeDtf);

        int wk = localDate.getDayOfWeek().get(WeekFields.of(DayOfWeek.MONDAY, 1).dayOfWeek());
        for (int i = wk-1; i > 0; i--) {
            dateMap.put(localDate.plusDays(-i).format(DateTimeFormatter.ofPattern(curTimeDtf)),"true");
        }
        for (int i = 7; wk <= 7; wk++) {
            dateMap.put(localDate.plusDays(7-wk).format(DateTimeFormatter.ofPattern(curTimeDtf)),"true");
        }

        if(dateMap.get(curDate) == null){
            return "";
        }else {
            return "y";
        }
    }

    /**
     * 获取毫秒值
     * @param localDateTime
     * @return
     */
    public static long getMillis(LocalDateTime localDateTime){
        return localDateTime.atZone(getZoneId()).toInstant().toEpochMilli();
    }

}