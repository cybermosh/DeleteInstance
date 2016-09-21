package au.com.atomictech.deleteinstance.database;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import au.com.atomictech.deleteinstance.objects.DateTimeTool;
import au.com.atomictech.deleteinstance.objects.Instance_Object;
import au.com.atomictech.deleteinstance.objects.Session_Object;

public class CalHandler extends ContentResolver {

    //public String ACCOUNT_NAME = "test@email.com";
    //public String ACCOUNT_OWNER = "text@email.com";
    //public String CALENDAR_NAME = "testCalendar";

    public String ACCOUNT_NAME = "application.clienttime@gmail.com";
    public String ACCOUNT_OWNER = "application.clienttime@gmail.com";
    public String CALENDAR_NAME = "ClientTime";

    private static final int PROJECTION_ACCOUNT_NAME = 1;
    private static final int PROJECTION_DISPLAY_NAME = 2;
    private static final int PROJECTION_OWNER_ACCOUNT = 3;

    DateTimeTool dateTimeTool;
    Cursor cur;
    ContentResolver cr;

    long calendar_id;

    Context context;

    public CalHandler(Context context) {
        super(context);

        this.context = context;

        cr = context.getContentResolver();

        dateTimeTool = new DateTimeTool(context);

    }

    /**
     * You must include CALENDAR_ID and DTSTART.
     * For non-recurring events, you must include DTEND.
     *
     * @param session_object
     * @return
     */

    public long addSession(Session_Object session_object) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            Log.v("Write permissions", "denied");
            return -1;
        }

        long dt_end = session_object.get_date() + session_object.get_duration();

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, session_object.get_date());
        //values.put(CalendarContract.Events.DTEND, dt_end);
        values.put(CalendarContract.Events.DURATION, session_object.get_P_Duration());
        values.put(CalendarContract.Events.TITLE, session_object.get_title());
        values.put(CalendarContract.Events.CALENDAR_ID, calendar_id);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, dateTimeTool.findTimeZone().getID());
        values.put(CalendarContract.Events.RRULE, session_object.get_rrule());

        long session_id = -1;

        Log.v("=ch:addSession",
                " DTSTART= " + dateTimeTool.convertLongDateTimeToString(session_object.get_date()) +
                " DTEND= " + dateTimeTool.convertLongDateTimeToString(dt_end)
        );

        Uri uri = cr.insert(buildEventUri(), values);

        if (uri == null) {
            Log.v("=ch:addSession", "Event not added");
        }
        else
        {
            session_id = Long.parseLong(uri.getLastPathSegment());

            if (session_id != -1) {

                Session_Object session_object_added = getSession(session_id);

                Log.v("=ch:addSession",
                        " so.id= " + session_object_added.get_id()
                );

            }

        }

        // return the event ID that is the last element in the Uri
        return session_id;

    }

    public long createCalendar() {

        cr = context.getContentResolver();
        ContentValues cv = buildNewCalContentValues(ACCOUNT_NAME, CALENDAR_NAME);

        Uri calUri = buildCalUri(ACCOUNT_NAME);
        //insert the calendar into the database
        cr.insert(calUri, cv);

        long calendar_id = getAppCalendarID();

        Log.v("=ch:CreateCal", "Calendar created");

        return calendar_id;

    }

    /**The main_options/basic URI for the android calendars table*/
    private static final Uri CAL_URI = CalendarContract.Calendars.CONTENT_URI;

    /**Builds the Uri for your Calendar in android database (as a Sync Adapter)*/
    private Uri buildCalUri(String ACCOUNT_NAME) {

        return CAL_URI
                .buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE,
                        CalendarContract.ACCOUNT_TYPE_LOCAL)
                .build();
    }

    /**Creates the values the new calendar will have*/
    private ContentValues buildNewCalContentValues(String ACCOUNT_NAME, String CALENDAR_NAME) {

        ContentValues cv = new ContentValues();
        cv.put(CalendarContract.Events.ACCOUNT_NAME, ACCOUNT_NAME);
        cv.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        cv.put(CalendarContract.Calendars.NAME, CALENDAR_NAME);
        cv.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CALENDAR_NAME);
        cv.put(CalendarContract.Calendars.CALENDAR_COLOR, 0xEA8561);

        cv.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        //user can only read the calendar
        //cv.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_READ);

        cv.put(CalendarContract.Calendars.OWNER_ACCOUNT, ACCOUNT_NAME);
        cv.put(CalendarContract.Calendars.VISIBLE, 1);
        cv.put(CalendarContract.Calendars.SYNC_EVENTS, 1);

        return cv;

    }

    public long getAppCalendarID() {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            Log.v("=ch:getCalID", "Permission not granted");
            return 0;
        }

        cur = null;

        calendar_id = 0;

        int INDEX_CALENDAR_ID = 0;

        Uri uri = CalendarContract.Calendars.CONTENT_URI;

        String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";

        String[] selectionArgs =
                new String[]{ACCOUNT_NAME, CalendarContract.ACCOUNT_TYPE_LOCAL, ACCOUNT_OWNER};

        final String[] EVENT_PROJECTION = new String[]{
                CalendarContract.Calendars._ID,                           // 0
                CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
                CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
        };

        cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);

        // Use the cursor to step through the returned records
        while (cur.moveToNext()) {

            // Get the field values
            calendar_id = cur.getLong(INDEX_CALENDAR_ID);

            //Log.v("=ch:getCalID", "id = " + calendar_id);

        };

        cur.close();

        return calendar_id;

    }

    public void findAllCalendars() {

        // Run query
        cur = null;

        int INDEX_CALENDAR_ID = 0;
        //cr = getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;

        // Submit the query and get a Cursor object back.
        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            Log.v("=ch:findCals", "Permission not granted");
            return;
        }

        cur = cr.query(uri, EVENT_PROJECTION, null, null, null);

        // Use the cursor to step through the returned records
        while (cur.moveToNext()) {

            calendar_id = 0;

            // Get the field values
            calendar_id = cur.getLong(INDEX_CALENDAR_ID);
            String displayName = cur.getString(PROJECTION_DISPLAY_NAME);
            String accountName = cur.getString(PROJECTION_ACCOUNT_NAME);
            String ownerName = cur.getString(PROJECTION_OWNER_ACCOUNT);

            Log.v("=getCalID",
                    " Calendar found: id =" + calendar_id +
                    " displayName =  " + displayName +
                    " accountName = " + accountName +
                    " ownerName =  " + ownerName
            );
        }

        cur.close();

    }

    /**Permanently deletes our calendar from database (along with all events)*/
    public String deleteCalendar(Context ctx, long calendar_id) {

        ContentResolver cr = ctx.getContentResolver();

        Uri calUri = ContentUris.withAppendedId(buildCalUri(ACCOUNT_NAME), calendar_id);

        String log = "";

        if  (cr.delete(calUri, null, null) > 0)
        {
            Log.v("=ch:deleteCalendar", "Calendar deleted id = " + Long.toString(calendar_id));
            log = "Calendar deleted: id = " + Long.toString(calendar_id);

        }
        else
        {
            Log.v("=ch:deleteCalendar", "Cal not deleted id = " + Long.toString(calendar_id));
            log = "Calendar not deleted: id = " + Long.toString(calendar_id);
        }

        return log;

    }

    public static final String[] EVENT_PROJECTION = new String[]{
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };

    /**The main_options/basic URI for the android calendars table*/
    private static final Uri EVENT_URI = CalendarContract.Events.CONTENT_URI;

    /**Builds the Uri for events (as a Sync Adapter)*/
    public Uri buildEventUri() {
        return EVENT_URI
                .buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE,
                        CalendarContract.ACCOUNT_TYPE_LOCAL)
                .build();
    }

    private static final Uri EVENT_EXCEPTION_URI = CalendarContract.Events.CONTENT_EXCEPTION_URI;

    public Uri buildEventExceptionUri() {
        return EVENT_EXCEPTION_URI
                .buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, ACCOUNT_NAME)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE,
                        CalendarContract.ACCOUNT_TYPE_LOCAL)
                .build();
    }

    public Session_Object getSession(long session_id) {

        cur = null;

        //Log.v("=ch:getSession", "getting Session");

        Session_Object session_object = new Session_Object();

        ContentResolver cr = context.getContentResolver();

        //Projection array for query (the values you want)
        final String[] PROJECTION = new String[] {
                CalendarContract.Events._ID, //Needed
                CalendarContract.Events.CALENDAR_ID,
                CalendarContract.Events.ORGANIZER,
                CalendarContract.Events.TITLE, //Needed
                CalendarContract.Events.EVENT_LOCATION, //Needed
                CalendarContract.Events.DESCRIPTION, //Needed
                CalendarContract.Events.EVENT_COLOR, //Needed
                CalendarContract.Events.DTSTART, //Needed
                CalendarContract.Events.DTEND, //Needed
                CalendarContract.Events.EVENT_TIMEZONE, //Duration is not needed since it's derived from the Session_Type_ID
                CalendarContract.Events.DURATION, //Duration is not needed since it's derived from the Session_Type_ID
                CalendarContract.Events.ALL_DAY, //Needed
                CalendarContract.Events.RRULE,
                CalendarContract.Events.RDATE,
                CalendarContract.Events.EXRULE,
                CalendarContract.Events.EXDATE,
                CalendarContract.Events.ORIGINAL_ID,
                CalendarContract.Events.ORIGINAL_SYNC_ID,
                CalendarContract.Events.ORIGINAL_INSTANCE_TIME,
                CalendarContract.Events.ORIGINAL_ALL_DAY,
                CalendarContract.Events.ACCESS_LEVEL,
                CalendarContract.Events.SYNC_DATA1, //Client_ID
                CalendarContract.Events.SYNC_DATA3, //Session_Type_ID
                CalendarContract.Events.STATUS
        };

        final int
                Index_EVENT_ID = 0,
                Index_CALENDAR_ID = 1,
                Index_ORGANIZER = 2,
                Index_TITLE = 3,
                Index_EVENT_LOCATION = 4,
                Index_DESCRIPTION = 5,
                Index_EVENT_COLOR = 6,
                Index_DTSTART = 7,
                Index_DTEND = 8,
                Index_EVENT_TIMEZONE = 9,
                Index_DURATION = 10,
                Index_ALL_DAY = 11,
                Index_RRULE = 12,
                Index_RDATE = 13,
                Index_EXRULE = 14,
                Index_EXDATE = 15,
                Index_ORIGINAL_ID = 16,
                Index_ORIGINAL_SYNC_ID = 17,
                Index_ORIGINAL_INSTANCE_TIME = 18,
                Index_ORIGINAL_ALL_DAY = 19,
                Index_ACCESS_LEVEL = 20,
                Index_SYNC_DATA1 = 21,
                Index_SYNC_DATA3 = 22,
                Index_STATUS = 22;

        final String selection = "("+ CalendarContract.Events.OWNER_ACCOUNT + " = ? AND " +
                CalendarContract.Events._ID + " = ?)";

        final String[] selectionArgs = new String[] {ACCOUNT_NAME, Long.toString(session_id)};

        cur = cr.query(buildEventUri(), PROJECTION, selection, selectionArgs, null);

        //at most one event will be returned because event ids are unique in the table

        if (cur.moveToFirst()) {

            session_object.set_id(cur.getInt(Index_EVENT_ID));
            session_object.set_date(cur.getLong(Index_DTSTART));
            session_object.set_title(cur.getString(Index_TITLE));

            if (cur.getString(Index_RRULE) != null) {
                session_object.set_rrule(cur.getString(Index_RRULE));
            }
            else
            {
                session_object.set_rrule("");
            }

            session_object.set_exDate(cur.getString(Index_EXDATE));

            /*

            Log.v("=ch:getSession",
                    " EVENT_ID: " + cur.getLong(Index_EVENT_ID) +
                            ", CALENDAR_ID: " + cur.getString(Index_CALENDAR_ID) +
                            ", ORGANIZER: " + cur.getString(Index_ORGANIZER) +
                            ", TITLE: " + (cur.getString(Index_TITLE)) +
                            ", EVENT_LOCATION: " + (cur.getString(Index_EVENT_LOCATION)) +
                            ", DESCRIPTION: " + (cur.getString(Index_DESCRIPTION)) +
                            ", EVENT_COLOR: " + (cur.getString(Index_EVENT_COLOR)) +
                            ", DTSTART: " + cur.getLong(Index_DTSTART) +
                            ", DTEND: " + cur.getLong(Index_DTEND) +
                            ", EVENT_TIMEZONE: " + cur.getString(Index_EVENT_TIMEZONE) +
                            ", DURATION: " + cur.getString(Index_DURATION) +
                            ", ALL_DAY: " + cur.getString(Index_ALL_DAY) +
                            ", RRULE: " + cur.getString(Index_RRULE) +
                            ", RDATE: " + cur.getString(Index_RDATE) +
                            ", EXRULE: " + cur.getString(Index_EXRULE) +
                            ", EXDATE: " + cur.getString(Index_EXDATE) +
                            ", ORIGINAL_ID: " + cur.getString(Index_ORIGINAL_ID) +
                            ", ORIGINAL_SYNC_ID: " + cur.getString(Index_ORIGINAL_SYNC_ID) +
                            ", ORIGINAL_INSTANCE_TIME: " + cur.getLong(Index_ORIGINAL_INSTANCE_TIME) +
                            ", ORIGINAL_ALL_DAY: " + cur.getString(Index_ORIGINAL_ALL_DAY) +
                            ", ACCESS_LEVEL: " + (cur.getString(Index_ACCESS_LEVEL)) +
                            ", SYNC_DATA1: " + cur.getString(Index_SYNC_DATA1) +
                            ", SYNC_DATA3: " + cur.getString(Index_SYNC_DATA3) +
                            ", STATUS: " + cur.getInt(Index_STATUS)
            );
            */

        }

        cur.close();

        return session_object;
    }

    public String findAllInstances() {

        Log.v("=Finding all", "instances");

        String log_text = "";

        cur = null;

        Calendar beginTime = Calendar.getInstance();
        Calendar endTime = Calendar.getInstance();

        final String[] PROJECTION = new String[]{
                CalendarContract.Instances.EVENT_ID,      // 0
                CalendarContract.Instances.TITLE,         // 1
                CalendarContract.Instances.BEGIN,         // 2
                CalendarContract.Instances.END,            // 3
                CalendarContract.Instances._ID,      // 4
                CalendarContract.Instances.DURATION            // 5
        };

        final int INDEX_EVENT_ID = 0;
        final int INDEX_TITLE = 1;
        final int INDEX_BEGIN = 2;
        final int INDEX_END = 3;
        final int INDEX_INSTANCE_ID = 4;
        final int INDEX_DURATION = 5;

        String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?) AND ("
                + CalendarContract.Instances.EVENT_ID + " > 0" + "))";

        String[] selectionArgs = new String[]{
                ACCOUNT_NAME, CalendarContract.ACCOUNT_TYPE_LOCAL, ACCOUNT_OWNER};

        beginTime.setTime(new Date(0));
        endTime.setTime(new GregorianCalendar(3000,1,1).getTime());

        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();

        ContentUris.appendId(builder, beginTime.getTimeInMillis());
        ContentUris.appendId(builder, endTime.getTimeInMillis());

        //Get all the events for the given week starting from week_start_date
        cur = cr.query(builder.build(),
                PROJECTION,
                selection,
                selectionArgs,
                null);

        while (cur.moveToNext()) {

            // Get the field values for the found session

           log_text+= " event_id: " + cur.getLong(INDEX_EVENT_ID) +
                    " instance_id: " + cur.getLong(INDEX_INSTANCE_ID) +
                    " Begin Date: " + dateTimeTool.convertLongDateTimeToString(cur.getLong(INDEX_BEGIN)) +
                    " End Date: " + dateTimeTool.convertLongDateTimeToString(cur.getLong(INDEX_END)) +
                    " Duration: " + cur.getLong(INDEX_DURATION) + "\n\n";

            Log.v("=ch:findAllInstances",
                    " event_id: " + cur.getLong(INDEX_EVENT_ID) +
                    " instance_id: " + cur.getLong(INDEX_INSTANCE_ID) +
                    " Begin Date: " + dateTimeTool.convertLongDateTimeToString(cur.getLong(INDEX_BEGIN)) +
                    " End Date: " + dateTimeTool.convertLongDateTimeToString(cur.getLong(INDEX_END)) +
                    " Duration: " + cur.getLong(INDEX_DURATION));
        }

        cur.close();

        return log_text;
    }

    public long getFirstInstance(long event_id) {

        cur = null;

        Log.v("=ch:getFirstInstance", "event_id = " + event_id);

        //Projection array for query (the values you want)
        final String[] projection = new String[] {
                CalendarContract.Instances.EVENT_ID, //Needed
                CalendarContract.Instances._ID, //Needed
                CalendarContract.Instances.BEGIN, //Needed
                CalendarContract.Instances.END //Needed
        };

        final int
                Index_Event_ID = 0,
                Index_Instance_ID = 1,
                Index_Begin_Time = 2,
                Index_End_Time = 3;

        final String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?) AND ("
                + CalendarContract.Instances.EVENT_ID + " = ?" + "))";

        final String[] selectionArgs = new String[]{
                ACCOUNT_NAME, CalendarContract.ACCOUNT_TYPE_LOCAL, ACCOUNT_OWNER, Long.toString(event_id)};

        ContentResolver cr = context.getContentResolver();

        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();

        ContentUris.appendId(builder, 0);
        ContentUris.appendId(builder, new GregorianCalendar(3000,1,1).getTimeInMillis());

        cur = cr.query(builder.build(), projection, selection, selectionArgs, null);

        Instance_Object instance_object = new Instance_Object();

        if (cur.moveToFirst()) {

                instance_object.setSession_id(cur.getLong(Index_Event_ID));
                instance_object.setInstance_id(cur.getLong(Index_Instance_ID));
                instance_object.setBegin(cur.getLong(Index_Begin_Time));
                instance_object.setEnd(cur.getLong(Index_End_Time));

                Log.v("=ch:getFirstInstance",
                        " instance_id: " + instance_object.getInstance_id() +
                                " event_id: " + instance_object.getSession_id() +
                                " Begin Date: " + dateTimeTool.convertLongDateToString(instance_object.getBegin_date()) +
                                " End Date: " + dateTimeTool.convertLongDateToString(instance_object.getEnd_date()) +
                                " Begin Time: " + dateTimeTool.convertLongTimeToString(instance_object.getBegin_time()) +
                                " End Time: " + dateTimeTool.convertLongTimeToString(instance_object.getEnd_time())
                );
            }

        cur.close();

        return instance_object.getInstance_id();
    }

    public static Uri asSyncAdapter(Uri uri, String account, String accountType) {
        return uri
                .buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, account)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, accountType)
                .build();
    }

    public int deleteInstance(Session_Object session_object, Instance_Object instance_object) {

        //Instances are not deleted
        //A new event is created which has the Events.STATUS changed to Events.STATUS_CANCELED

        int exception_eventID = -1;

        //Client_Object client_object = dbHandler.getClient(session_object.get_client_id());
        //String title = client_object.get_firstname() + " " + client_object.get_lastname();

        //String duration = session_object.get_P_Duration();

        long begin_date_time = instance_object.getBegin();
        long end_date_time = begin_date_time + session_object.get_duration();

        Log.v("=ch:deleteInstance", "begin_date_time= " + dateTimeTool.convertLongDateTimeToString(begin_date_time));

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            Log.v("Write permissions", "denied");


            return -1;
        }

        ContentValues values = new ContentValues();

        //values.put(CalendarContract.Events.DTSTART, begin_date_time); //Start Date for the instance
        values.put(CalendarContract.Events.ORIGINAL_INSTANCE_TIME, begin_date_time);
        values.put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CANCELED);
        //values.put(CalendarContract.Events.EVENT_TIMEZONE, dateTimeTool.findTimeZone().getID());
        //values.put(CalendarContract.Events.ORIGINAL_SYNC_ID, instance_object.getSession_id());

        //values.put(CalendarContract.Events.ORIGINAL_ID, session_object.get_id());
        //values.put(CalendarContract.Events.CALENDAR_ID, calendar_id);

        /*
        values.put(CalendarContract.Events.TITLE, title);
        values.put(CalendarContract.Events.DTSTART, begin_date_time); //Start Date for the instance
        values.put(CalendarContract.Events.DTEND, end_date_time);
        values.put(CalendarContract.Events.DURATION, duration);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, dateTimeTool.findTimeZone().getID());
        values.put(CalendarContract.Events.HAS_ALARM, "0");
        values.put(CalendarContract.Events.HAS_ATTENDEE_DATA,"0");
        values.put(CalendarContract.Events.ALL_DAY, 0);
        values.put(CalendarContract.Events.STATUS, CalendarContract.Events.STATUS_CANCELED);
        values.put(CalendarContract.Events.ORIGINAL_INSTANCE_TIME, begin_date_time);
        values.put(CalendarContract.Events.ORIGINAL_ID, session_object.get_id());
        values.put(CalendarContract.Events.ORIGINAL_ALL_DAY, 0);
        values.put(CalendarContract.Events.CALENDAR_ID, calendar_id);
        values.put(CalendarContract.Events.SYNC_DATA1, client_object.get_id());
        values.put(CalendarContract.Events.SYNC_DATA3, session_object.get_type());
        values.put(CalendarContract.Events.RRULE, "");
        */

        Uri uri = Uri.withAppendedPath(CalendarContract.Events.CONTENT_EXCEPTION_URI,
                String.valueOf(instance_object.getSession_id()));

        Uri syncUri = asSyncAdapter(uri, ACCOUNT_NAME, CalendarContract.ACCOUNT_TYPE_LOCAL );

        Uri resultUri = cr.insert(syncUri, values);

        try
        {
            exception_eventID = Integer.parseInt(resultUri.getLastPathSegment());

            Session_Object session_object_exception = getSession(exception_eventID);

        }
        catch (Exception e)
        {
        }

        return exception_eventID;

    }

    public Instance_Object getInstance(long event_id, long instance_id) {

        cur = null;

        Instance_Object instance_object = new Instance_Object();

        //Projection array for query (the values you want)
        final String[] projection = new String[] {
                CalendarContract.Instances.EVENT_ID, //Needed
                CalendarContract.Instances._ID, //Needed
                CalendarContract.Instances.BEGIN, //Needed
                CalendarContract.Instances.END //Needed
        };

        final int
                Index_Event_ID = 0,
                Index_Instance_ID = 1,
                Index_Begin_Time = 2,
                Index_End_Time = 3;

        final String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?) AND ("
                + CalendarContract.Instances.EVENT_ID + " = ?" + "))";

        final String[] selectionArgs = new String[]{
                ACCOUNT_NAME, CalendarContract.ACCOUNT_TYPE_LOCAL, ACCOUNT_OWNER, Long.toString(event_id)};

        ContentResolver cr = context.getContentResolver();

        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();

        ContentUris.appendId(builder, 0);
        ContentUris.appendId(builder, dateTimeTool.convertStringDateToLong("01/01/3000"));

        cur = cr.query(builder.build(), projection, selection, selectionArgs, null);

        //at most one event will be returned because event ids are unique in the table

        while (cur.moveToNext()) {

            if (cur.getInt(Index_Instance_ID) == instance_id) {

                instance_object.setSession_id(cur.getLong(Index_Event_ID));
                instance_object.setInstance_id(cur.getLong(Index_Instance_ID));
                instance_object.setBegin(cur.getLong(Index_Begin_Time));
                instance_object.setEnd(cur.getLong(Index_End_Time));

                Log.v("=ch:getInstance",
                    " instance_id: " + instance_object.getInstance_id() +
                    " event_id: " + instance_object.getSession_id() +
                    " Begin Date: " + dateTimeTool.convertLongDateToString(instance_object.getBegin_date()) +
                    " End Date: " + dateTimeTool.convertLongDateToString(instance_object.getEnd_date()) +
                    " Begin Time: " + dateTimeTool.convertLongTimeToString(instance_object.getBegin_time()) +
                    " End Time: " + dateTimeTool.convertLongTimeToString(instance_object.getEnd_time())
                    );
            }

        }

        cur.close();

        return instance_object;
    }


}
