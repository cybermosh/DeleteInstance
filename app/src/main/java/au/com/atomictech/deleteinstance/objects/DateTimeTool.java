package au.com.atomictech.deleteinstance.objects;

import android.content.Context;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class DateTimeTool {

    Calendar calendar;
    String YearPart = "yyyy";
    String MonthPart = "MM";
    String DayPart = "dd";

    Context context;

    public long day_length = (1000 * 60 * 60 * 24); //Used for adding days to a date
    public long minute_length = (1000 * 60); //Used for adding days to a date

    public DateTimeTool(Context context) {
        super();

        this.context = context;

        calendar = new GregorianCalendar();

        calendar.setTimeZone(findTimeZone());

    }

    public String convertLongTimeToString(Long _time){

        calendar.setTimeInMillis(_time);

        SimpleDateFormat f = new SimpleDateFormat("hh:mm a");

        return f.format(calendar.getTime());

    }

    public String convertLongGMTTimeToString(Long _time){

        GregorianCalendar calendar = new GregorianCalendar();

        calendar.setTimeInMillis(_time);

        SimpleDateFormat f = new SimpleDateFormat("hh:mm a");

        return f.format(calendar.getTime());

    }

    public String convertLongDateTimeToString(Long _date_time){

        calendar.setTimeInMillis(_date_time);

        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy hh:mm a");

        return f.format(calendar.getTime());

    }

    public String convertLongDateTimeToFullString(Long _date_time){

        calendar.setTimeInMillis(_date_time);

        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy hh:mm ssss a");

        return f.format(calendar.getTime());

    }

    public String convertLongGMTTimeToString(long _time) {

        Calendar calendar = new GregorianCalendar();

        calendar.setTime(new Date(_time));

        Date date = calendar.getTime();

        SimpleDateFormat format = new SimpleDateFormat("hh:mm a");

        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        return format.format(date);

    }

    public Long convertStringGMTTimeToLong(String _time) {

        Calendar gmt_calendar = new GregorianCalendar();

        SimpleDateFormat f = new SimpleDateFormat("hh:mm a");

        f.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            gmt_calendar.setTime(f.parse(_time));
        } catch (ParseException e) {
            //showText("Error parsing date");
            e.printStackTrace();
        }

        Date date = gmt_calendar.getTime();

        return date.getTime();

    }

    public Long convertStringGMTDateToLong(String _date) {

        Calendar calendar = new GregorianCalendar();

        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyy");

        f.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            calendar.setTime(f.parse(_date));
        } catch (ParseException e) {
            //showText("Error parsing date");
            e.printStackTrace();
        }

        Date date = calendar.getTime();

        return date.getTime();

    }

    public Long convertString24HourGMTTimeToLong(String _time) {

        Calendar calendar = new GregorianCalendar();

        SimpleDateFormat f = new SimpleDateFormat("HH:mm");

        f.setTimeZone(TimeZone.getTimeZone("GMT"));

        try {
            calendar.setTime(f.parse(_time));
        } catch (ParseException e) {
            //showText("Error parsing date");
            e.printStackTrace();
        }

        Date date = calendar.getTime();

        return date.getTime();

    }


    public Long convertStringTimeToLong(String _time){

        SimpleDateFormat f = new SimpleDateFormat("hh:mm a");

        try {
            calendar.setTime(f.parse(_time));
        } catch (ParseException e) {
            //showText("Error parsing date");
            e.printStackTrace();
        }

        return calendar.getTimeInMillis();

    }

    public String convertStringTimePartToString(String _time, String time_part){

        SimpleDateFormat f = new SimpleDateFormat(time_part);

        try {
            calendar.setTime(f.parse(_time));
        } catch (ParseException e) {

            e.printStackTrace();
        }

        return f.format(calendar.getTime());

    }

    public String convertLongDateToString(Long _date){

        calendar.setTimeInMillis(_date);

        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");

        return f.format( calendar.getTime());

    }

    public String convertLongDateToEXDATE(Long _date){

        //Calendar exdate_calendar = new GregorianCalendar();

        calendar.setTimeInMillis(_date);

        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");

        //f.setTimeZone(TimeZone.getTimeZone("UTC"));

        long l_date = removeMilliSeconds(calendar.getTimeInMillis());

        return f.format(l_date);

    }

    public Long convertStringDateToLong(String _time){

        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");

        try {
            calendar.setTime(f.parse(_time));
        } catch (ParseException e) {
            //showText("Error parsing date");
            e.printStackTrace();
        }

        return calendar.getTimeInMillis();

    }

    public Long convertStringDateTimeToLong(String _time){

        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy hh:mm a");

        try {
            calendar.setTime(f.parse(_time));
        } catch (ParseException e) {
            //showText("Error parsing date");
            e.printStackTrace();
        }

        return calendar.getTimeInMillis();

    }

    public Long convertStringRRuleDateToLong(String _date){

        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");

        try {
            calendar.setTime(f.parse(_date));
        } catch (ParseException e) {
            //showText("Error parsing date");
            e.printStackTrace();
        }

        return calendar.getTimeInMillis();

    }


    public String convertLongDateToShortString(Long _date){

        calendar.setTimeInMillis(_date);

        SimpleDateFormat f = new SimpleDateFormat("dd/MM");

        return f.format(calendar.getTime());

    }

    public int getDateTimePart(long _datetime, String format ){

        calendar.setTimeInMillis(_datetime);

        SimpleDateFormat f = new SimpleDateFormat(format);

        return Integer.parseInt(f.format(calendar.getTime()));

    }

    public int getDateTimeGMTPartToInt(long _datetime, String format ){

        calendar.setTimeInMillis(_datetime);

        SimpleDateFormat f = new SimpleDateFormat(format);

        f.setTimeZone(TimeZone.getTimeZone("GMT"));

        return Integer.parseInt(f.format(calendar.getTime()));

    }

    public String getDateTimeGMTPartToString(long _datetime, String format ){

        calendar.setTimeInMillis(_datetime);

        SimpleDateFormat f = new SimpleDateFormat(format);

        f.setTimeZone(TimeZone.getTimeZone("GMT"));

        return f.format(calendar.getTime());

    }

    public Long getTodaysDate(){

        Calendar date_time_cal = new GregorianCalendar();

        date_time_cal.setTime(Calendar.getInstance().getTime());

        Calendar date_cal = new GregorianCalendar();
        date_cal.setTimeZone(findTimeZone());

        int year = getDateTimePart(date_time_cal.getTimeInMillis() ,"yyyy");
        int month = getDateTimePart(date_time_cal.getTimeInMillis() ,"MM");
        int day = getDateTimePart(date_time_cal.getTimeInMillis() ,"dd");

        date_cal.set(year, month -1, day);

        long _date = new GregorianCalendar(year, month - 1, day).getTimeInMillis();

        //Log.v("=dtt:getTodaysDate", "l_date= " + _date);
        //Log.v("=dtt:getTodaysDate", "s_date= " + convertLongDateTimeToString(_date));

        return _date;

    }

    public Long getCurrentHour(){

        Calendar time_cal = new GregorianCalendar();
        time_cal.setTimeZone(findTimeZone());

        int min = getDateTimePart(time_cal.getTimeInMillis(), "mm");

        long _hour = time_cal.getTimeInMillis() - (min * minute_length);

        return _hour;

    }

    public TimeZone findTimeZone()
    {

        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        return tz;

    }

    public long addTimeToDate(long _date, long _time) {

        int day =  getDateTimePart(_date, "dd");
        int month =  getDateTimePart(_date, "MM");
        int year =  getDateTimePart(_date, "yyyy");

        int hour =  getDateTimePart(_time, "HH");
        int minute =  getDateTimePart(_time, "mm");

        GregorianCalendar date_cal = new GregorianCalendar();
        date_cal.setTimeZone(findTimeZone());

        date_cal.set(year, month - 1, day, hour, minute);

        //Log.v("=ch:AddTimeDate", "pre_l_date= " + date_cal.getTimeInMillis());

        //long l_date_time = removeMilliSeconds(_date + _time);

        long l_date_time = removeMilliSeconds(date_cal.getTimeInMillis());

        //Log.v("=ch:AddTimeDate", "post_l_date= " + l_date);

        return l_date_time;

    }

    public long removeMilliSeconds(long _time)
    {
        long droppedMillis = (_time/ 100000);

        droppedMillis = (droppedMillis * 100000);

        return droppedMillis;
    }


    public int getDayOfWeekIndex(long input_date){

        Date output_date = new Date(input_date);

        Calendar c = Calendar.getInstance();
        c.setTime(output_date);

        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        return dayOfWeek;
    }

    public String getBYDAY(int DayIndex)
    {

        Map<Integer, String> mp=new HashMap<>();

        mp.put(1, "SU");
        mp.put(2, "MO");
        mp.put(3, "TU");
        mp.put(4, "WE");
        mp.put(5, "TH");
        mp.put(6, "FR");
        mp.put(7, "SA");

        return mp.get(DayIndex);

    }


}
