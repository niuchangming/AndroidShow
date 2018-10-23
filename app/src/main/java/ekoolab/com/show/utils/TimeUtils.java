package ekoolab.com.show.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author Army
 * @version V_1.0.0
 * @date 2017/06/28
 * @modify Neil
 * @modifydate 2017/11/1
 * @description 时间工具类
 */
public class TimeUtils {

    public static final int NUMBER_1 = 1;
    public static final int NUMBER_2 = 2;
    public static final int NUMBER_10 = 10;
    public static final int NUMBER_60 = 60;

    /**
     * 线程安全的时间格式化:yyyy-MM-dd HH:mm:ss
     */
    public static ThreadLocal<SimpleDateFormat> YYYYMMDDHHMMSS = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        }
    };

    /**
     * 线程安全的时间格式化:YYYYmmDDHHMM
     */
    public static ThreadLocal<SimpleDateFormat> YYYYmmDDHHMM = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        }
    };

    /**
     * 线程安全的时间格式化:yyyy-MM-dd 00:00:00
     */
    public static ThreadLocal<SimpleDateFormat> YYYYMMDDHHMMSSZero = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd 00:00:00", Locale.getDefault());
        }
    };

    /**
     * MM月dd日 HH:mm
     */
    public static ThreadLocal<SimpleDateFormat> MMDDHHMM = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("MMMM-d HH:mm", Locale.getDefault());
        }
    };



    /**
     * yyyy年MM月dd日 HH:mm
     */
    public static ThreadLocal<SimpleDateFormat> YYYYMMDDHHMM = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MMMM-d HH:mm", Locale.getDefault());
        }
    };

    /**
     * yyyy-MMMM-d
     */
    public static ThreadLocal<SimpleDateFormat> YYYYMMDD = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MMMM-d", Locale.getDefault());
        }
    };

    /**
     * HH:mm
     */
    public static ThreadLocal<SimpleDateFormat> HHMM = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("HH:mm", Locale.getDefault());
        }
    };

    /**
     * MMMM dd
     */
    public static ThreadLocal<SimpleDateFormat> MMMM_dd = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("MMMM dd", Locale.getDefault());
        }
    };

    /**
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String getCurrentTime() {
        return YYYYMMDDHHMMSS.get().format(new Date());
    }


    /**
     * @return yyyy-MM-dd 00:00:00
     */
    public static String getCurrentTimeAttendance() {
        return YYYYMMDDHHMMSSZero.get().format(new Date());
    }

    public static String getTodayTime() {
        return YYYYMMDD.get().format(new Date());
    }

    public static String getFormatTime(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /**
     * 所有时间格式用这个方法<br/>
     * 规则：发布时间距当前时间10分钟之内，显示“刚刚”；<br/>
     * 1小时之内，以分钟为最小单位，显示XX分钟前；<br/>
     * 1小时以上1天以内的（即当天的），以小时为最小单位，显示：xx小时前；<br/>
     * 1天以上的，由于其时效性弱，所以通常以天为最小单位，如：昨天HH：MM<br/>
     * 两天以上，显示：xx月xx日 HH：MM<br/>
     * 1年以上，显示xxxx年xx月xx日<br/>
     */
    public static String formatTime(long timestamp) {
        Calendar currentTime = Calendar.getInstance();
        Calendar needFormatTime = Calendar.getInstance();
        Calendar midTime = Calendar.getInstance();
        needFormatTime.setTimeInMillis(timestamp);
        midTime.add(Calendar.DAY_OF_MONTH, -NUMBER_1);
        boolean flag = midTime.get(Calendar.DAY_OF_MONTH) == needFormatTime.get(Calendar.DAY_OF_MONTH)
                && (Math.abs(needFormatTime.get(Calendar.MONTH) - currentTime.get(Calendar.MONTH)) == NUMBER_1);
        // 1年以上，显示xxxx年xx月xx日 HH：MM
        if (currentTime.get(Calendar.YEAR) != needFormatTime.get(Calendar.YEAR)) {
            return YYYYMMDD.get().format(needFormatTime.getTime());
        }
        // 一个月以上，显示：xx月xx日 HH：MM
        if ((currentTime.get(Calendar.MONTH) != needFormatTime.get(Calendar.MONTH)) && !flag) {
            return MMDDHHMM.get().format(needFormatTime.getTime());
        }
        // 两天以上，显示：xx月xx日 HH：MM
        int delta = currentTime.get(Calendar.DAY_OF_MONTH) - needFormatTime.get(Calendar.DAY_OF_MONTH);
        if (delta >= NUMBER_2 && !flag) {
            return MMDDHHMM.get().format(needFormatTime.getTime());
        }
        // 1天以上的，由于其时效性弱，所以通常以天为最小单位，如：昨天HH：MM
        if (delta == 1 || flag) {
            return "yesterday " + HHMM.get().format(needFormatTime.getTime());
        }
        // 1小时以上1天以内的（即当天的），以小时为最小单位，显示：xx小时前；
        long deltaInMills = currentTime.getTimeInMillis() - needFormatTime.getTimeInMillis();
        //大于一小时
        if (deltaInMills >= 60 * 60 * 1000) {
            long hour = deltaInMills / (60 * 60 * 1000);
            return String.format(Locale.getDefault(), "%d hour%s ago", hour, hour > 1 ? "s" : "");
        }
        //小于一小时
        if (deltaInMills >= 10 * 60 * 1000) {
            return String.format(Locale.getDefault(), "%d minutes ago", deltaInMills / (60 * 1000));
        }
        // 10分钟之内，显示“刚刚”；
        return "just now";
    }

    public static boolean isSameDate(long millisFirst, long millisSecond) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        return dateFormat.format(millisFirst).equals(dateFormat.format(millisSecond));
    }

    /**
     * 把一种格式的时间转化成另一种格式
     *
     * @param date           要转化的时间
     * @param originalFormat 要转化的时间的原格式
     * @param toFormat       需要转化成的格式
     */
    public static String stringDateToStringDate(String date, ThreadLocal<SimpleDateFormat> originalFormat,
                                                ThreadLocal<SimpleDateFormat> toFormat) {
        try {
            Date originalDate = originalFormat.get().parse(date);
            return toFormat.get().format(originalDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String getTimeByTimestamp(long time) {
        return HHMM.get().format(time);
    }

    /**
     * 2017-12-29 20:18:26从这个格式的时间里获取月份
     */
    public static int getTimeOfMonth(String time) {
        if (Utils.isBlank(time) || time.length() < 7) {
            return 0;
        }
        return Integer.parseInt(time.substring(5, 7));
    }

    public static int getTimeOfYear(String time) {
        if (Utils.isBlank(time) || time.length() < 4) {
            return 0;
        }
        return Integer.parseInt(time.substring(0, 4));
    }

    /**
     * 获取上周一
     *
     * @param date
     * @return
     */
    public static Date getLastWeekMonday(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getThisWeekMonday(date));
        cal.add(Calendar.DATE, -7);
        return cal.getTime();
    }

    /**
     * 获取本周一
     *
     * @param date
     * @return
     */
    public static Date getThisWeekMonday(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        // 获得当前日期是一个星期的第几天
        int dayWeek = cal.get(Calendar.DAY_OF_WEEK);
        if (1 == dayWeek) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        // 设置一个星期的第一天，按中国的习惯一个星期的第一天是星期一
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        // 获得当前日期是一个星期的第几天
        int day = cal.get(Calendar.DAY_OF_WEEK);
        // 根据日历的规则，给当前日期减去星期几与一个星期第一天的差值
        cal.add(Calendar.DATE, cal.getFirstDayOfWeek() - day);
        return cal.getTime();
    }

    /**
     * 获取下周一
     *
     * @param date
     * @return
     */
    public static Date getNextWeekMonday(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getThisWeekMonday(date));
        cal.add(Calendar.DATE, 7);
        return cal.getTime();
    }

    public static Date string2Date(String time, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try {
            return sdf.parse(time);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }

    public static String currentDate() {
        return YYYYMMDD.get().format(new Date());
    }

    public static String getWeekByTime(String time, String[] weeks) {
        if (!Utils.isBlank(time)) {
            try {
                Date date = YYYYMMDDHHMMSS.get().parse(time);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                int weekPosition = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                return weeks[weekPosition];
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * //两个时间的差值 返回格式由最後參數決定
     *
     * @param startTime
     * @param startTimeFormat
     * @param endTime
     * @param endTimeFormat
     * @param resultTime
     * @return
     */
    public static String getTimeDifference(String startTime, ThreadLocal<SimpleDateFormat> startTimeFormat,
                                           String endTime, ThreadLocal<SimpleDateFormat> endTimeFormat, ThreadLocal<SimpleDateFormat> resultTime) {
        try {
            Date startDate = startTimeFormat.get().parse(startTime);
            Date endDate = endTimeFormat.get().parse(endTime);
            long timeResult = endDate.getTime() - startDate.getTime();

            long day = timeResult / (24 * 60 * 60 * 1000);
            long hour = (timeResult / (60 * 60 * 1000) - day * 24);
            long min = ((timeResult / (60 * 1000)) - day * 24 * 60 - hour * 60);
            long s = (timeResult / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);

            java.util.Calendar calendar = java.util.Calendar.getInstance();
            try {
                calendar.setTime(TimeUtils.YYYYMMDDHHMMSS.get().parse("1970-01-01 00:00:00"));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            calendar.add(Calendar.HOUR_OF_DAY, (int) hour);
            calendar.add(Calendar.MINUTE, (int) min);
            calendar.add(Calendar.SECOND, (int) s);
            String result = resultTime.get().format(calendar.getTime());
            return result;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static long getTimeDifferenceLength(String startTime, ThreadLocal<SimpleDateFormat> startTimeFormat,
                                               String endTime, ThreadLocal<SimpleDateFormat> endTimeFormat) {
        try {
            Date startDate = startTimeFormat.get().parse(startTime);
            Date endDate = endTimeFormat.get().parse(endTime);
            long timeLong = endDate.getTime() - startDate.getTime();
            return timeLong;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }


    /**
     * 将日期格式  转换  为时间戳
     */
    public static long getTimeStampByDate(String date, ThreadLocal<SimpleDateFormat> format) {
        try {
            Date curDate = format.get().parse(date);
            return curDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 将 时间戳  转换  为日期格式
     */
    public static String getDateStringByTimeStamp(long time) {
        return MMMM_dd.get().format(time);
    }


    /**
     * 根据时间戳获取年月日
     *
     * @param time
     * @return
     */
    public static int getTimeOfDay(String time) {

        SimpleDateFormat sdf = YYYYMMDDHHMMSS.get();
        Calendar cal = Calendar.getInstance();
        Date dt = null;
        try {
            dt = sdf.parse(time);
            cal.setTime(dt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return cal.get(Calendar.YEAR);
    }


    /**
     * 获取过去或者未来 任意天内的日期数组
     *
     * @param intervals intervals天内
     * @return 日期数组
     */
    public static List<String> getFetureDays(int intervals, String time, ThreadLocal<SimpleDateFormat> formate, ThreadLocal<SimpleDateFormat> resultFormate) {
        List<String> fetureDaysList = new ArrayList<>();
        for (int i = 0; i < intervals; i++) {
            fetureDaysList.add(getFetureDate(i, time, formate, resultFormate));
        }
        return fetureDaysList;
    }

    /**
     * 获取某个时间过去第几天的日期
     *
     * @param past
     * @return
     */
    public static String getPastDate(int past, String time, ThreadLocal<SimpleDateFormat> formate, ThreadLocal<SimpleDateFormat> resultFormate) {
        try {
            Date parse = formate.get().parse(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parse);
            calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) - past);
            Date today = calendar.getTime();
            return formate.get().format(today);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取未来 第 past 天的日期
     *
     * @param past
     * @return
     */
    public static String getFetureDate(int past, String time, ThreadLocal<SimpleDateFormat> formate, ThreadLocal<SimpleDateFormat> resultFormate) {
        try {
            Date parse = formate.get().parse(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parse);
            calendar.set(Calendar.DAY_OF_YEAR, calendar.get(Calendar.DAY_OF_YEAR) + past);
            Date today = calendar.getTime();
            return resultFormate.get().format(today);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 根据传入时间获取某个时间对应的月份最后一天 日期样式自己确定
     */
    public static String getLastDaysOfMonth(String time, ThreadLocal<SimpleDateFormat> formate, ThreadLocal<SimpleDateFormat> resultFormate) {
        Date date = null;
        try {
            date = formate.get().parse(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            // 设定当前时间为每月一号
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            // 当前日历的天数上-1变成最大值 , 此方法不会改变指定字段之外的字段
            calendar.roll(Calendar.DAY_OF_MONTH, -1);
            return resultFormate.get().format(calendar.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getXingQi(String time, ThreadLocal<SimpleDateFormat> formate) {
        final String dayNames[] = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五",
                "星期六"};
        try {
            Date parse = formate.get().parse(time);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parse);
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
            if (dayOfWeek < 0) {
                dayOfWeek = 0;
            }
            return dayNames[dayOfWeek];
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 比较两个日期的大小，日期格式为yyyy-MM-dd
     *
     * @param str1 the first date
     * @param str2 the second date
     * @return true <br/>false
     */
    public static boolean isDate2Bigger(String str1, String str2) {
        boolean isBigger = false;
        SimpleDateFormat sdf = YYYYMMDD.get();
        Date dt1 = null;
        Date dt2 = null;
        try {
            dt1 = sdf.parse(str1);
            dt2 = sdf.parse(str2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (dt1.getTime() > dt2.getTime()) {
            isBigger = false;
        } else if (dt1.getTime() <= dt2.getTime()) {
            isBigger = true;
        }
        return isBigger;
    }

    /**
     * 比较两个日期的大小，日期格式为yyyy-MM-dd HH:mm:ss
     *
     * @param str1 the first date
     * @param str2 the second date
     * @return true <br/>false
     */
    public static boolean isDate2BiggerHhMmSS(String str1, String str2) {
        boolean isBigger = false;
        SimpleDateFormat sdf = YYYYMMDDHHMMSS.get();
        Date dt1 = null;
        Date dt2 = null;
        try {
            dt1 = sdf.parse(str1);
            dt2 = sdf.parse(str2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (dt1.getTime() > dt2.getTime()) {
            isBigger = false;
        } else if (dt1.getTime() <= dt2.getTime()) {
            isBigger = true;
        }
        return isBigger;
    }

    /**
     * 获取的是yyyy-MM-dd格式的，获取给定日期当前周的第一天和最后一天的日期
     */
    public static String[] getFirstAndLastOfWeek(Date time) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(time);
        int d;
        if (cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            d = -6;
        } else {
            d = 2 - cal.get(Calendar.DAY_OF_WEEK);
        }
        cal.add(Calendar.DAY_OF_WEEK, d);
        // 所在周开始日期
        String data1 = YYYYMMDD.get().format(cal.getTime());
        cal.add(Calendar.DAY_OF_WEEK, 6);
        // 所在周结束日期
        String data2 = YYYYMMDD.get().format(cal.getTime());
        return new String[]{data1, data2};
    }

    /**
     * 获取当前时间与传入时间间隔（秒）
     *
     * @param viewStartTime long 开始时间
     * @return int 秒
     */
    public static int getIntervalTimeByNow(long viewStartTime) {
        return (int) ((System.currentTimeMillis() - viewStartTime) / 1000);
    }


    /**
     * 作业时间的默认
     *
     * @return
     */
    public static String getPublishHomeWorkTime() {
        //yyyy-MM-dd HH:mm:ss
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int day = calendar.get(Calendar.DATE);
        if (hour >= 18) {
            calendar.set(Calendar.DATE, day + 1);
            String dayAfter = YYYYMMDD.get().format(calendar.getTime());
            return dayAfter + " 22:00";
        } else {
            return String.format("%s 22:00", YYYYMMDD.get().format(calendar.getTime()));
        }
    }


    /**
     * 判断是传入的时间是否比当前的时间早
     *
     * @param time
     * @return
     */
    public static boolean isbeforeNow(String time) {
        Date date = null;
        Date currentDate = new Date();
        SimpleDateFormat format = YYYYmmDDHHMM.get();
        if (!Utils.isBlank(time)) {
            try {
                date = format.parse(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        return date.before(currentDate);
    }

    /**
     * 当前的时间和传入的时间相等
     *
     * @param time
     * @return
     */
    public static boolean isCureentTime(String time) {
        Date date = null;
        Date currentDate = new Date();
        SimpleDateFormat format = YYYYmmDDHHMM.get();
        if (!Utils.isBlank(time)) {
            try {
                date = format.parse(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date.equals(currentDate);
    }

    /**
     * 是否是当前时间之后的
     *
     * @param time
     * @return
     */
    public static boolean isAfterCurrentTime(String time) {
        Date date = null;
        Date currentDate = new Date();
        SimpleDateFormat format = YYYYmmDDHHMM.get();
        if (!Utils.isBlank(time)) {
            try {
                date = format.parse(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date.after(currentDate);
    }


    /**
     * 传入的时间和三个小时后的进行比较
     *
     * @param time
     * @return
     */
    public static boolean isCurrentThreeHourAfter(String time) {
        SimpleDateFormat format = YYYYmmDDHHMM.get();
        Date date = null;
        Date threeAfter = null;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 3);
        String c = format.format(calendar.getTime());
        if (!Utils.isBlank(time)) {
            try {
                date = format.parse(time);
                threeAfter = format.parse(c);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date.after(threeAfter);
    }

    /**
     * 判断输入日期是30天之后的
     */
    public static boolean isCurrentThirtyDayAfter(String time) {
        SimpleDateFormat format = YYYYmmDDHHMM.get();
        Date date = null;
        Date threeAfter = null;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 30);
        String c = format.format(calendar.getTime());
        if (!Utils.isBlank(time)) {
            try {
                date = format.parse(time);
                threeAfter = format.parse(c);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return date.after(threeAfter);
    }

    /**
     * 已特定的格式格式化时间戳
     */
    public static String formatTimeStemp(long timeStemp, SimpleDateFormat format) {
        Date date = new Date(timeStemp);
        return format.format(date);
    }
}
