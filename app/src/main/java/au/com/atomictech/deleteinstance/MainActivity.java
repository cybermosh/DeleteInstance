package au.com.atomictech.deleteinstance;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import au.com.atomictech.deleteinstance.database.CalHandler;
import au.com.atomictech.deleteinstance.objects.DateTimeTool;
import au.com.atomictech.deleteinstance.objects.Instance_Object;
import au.com.atomictech.deleteinstance.objects.Session_Object;

public class MainActivity extends AppCompatActivity {

    CalHandler mycalHandler;
    DateTimeTool dateTimeTool;

    private static final int MY_PERMISSIONS_REQUEST_READ_CALENDAR = 100;

    private static final long millisecond_length = 1000;
    private static final long minute_length = millisecond_length * 60;
    private static final long hour_length = minute_length * 60;
    private static final long day_length = hour_length * 24;

    long calendar_id = 0;

    boolean calendar_read_permission;

    TextView text_log;

    String log_text;

    Button button_start;

    Button button_delete_calendar;

    /**
     * This program is specifically made to be show the issue i have with deleting instances of
     * repeating events from googles calendar contract.
     *
     *
     * How this works is when the program is run it shows a start button.
     * Pressing start creats a new calendar using the account details set in the
     * CalHandler class.
     * It then creates a repeating event and then tries to delete the
     * first instance. But as shown the first instance is actually left alone
     * and the other instances are deleted.
     *
     * @return
     */

    public CalHandler calHandler()
    {
        return this.mycalHandler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Keep app from going into standby
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Get permissions to get write/read access to the calendar
        GetCalendarPermissions();

        //Used for showing a visual log
        text_log = (TextView) findViewById(R.id.log);

        //Class used for converting dates and times
        dateTimeTool = new DateTimeTool(this);

        //Class for accessing the calendar api
        mycalHandler = new CalHandler(this);

        loadButtons();

    }

    void loadButtons()
    {
        button_start = (Button) findViewById(R.id.start_test);

        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartApp();
            }
        });

        button_delete_calendar = (Button) findViewById(R.id.reset_calendar);

        button_delete_calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mycalHandler.findAllCalendars();
                calendar_id = mycalHandler.getAppCalendarID();
                Log.v("=m:delCalClick", "calendar_id = " + calendar_id);
                text_log.setText("");
                log(calHandler().deleteCalendar(getBaseContext(), calendar_id));

            }
        });

    }

    void StartApp() {

        if (calendar_read_permission) {

            Log.v("=APP STARTED", "---------------------------");

            //Clear visual log
            text_log.setText("");

            calHandler().findAllCalendars();

            //Check if a calendar exists using the account details set in the top of calHandler
            if (mycalHandler.getAppCalendarID() == 0) {

                //Create a new calendar and grab the id for use later on
                calendar_id = mycalHandler.createCalendar();

            }

            //First add a test session
            Session_Object session_object  = AddTestSession();

            //Show list of current instances created by the repeating session
            log(mycalHandler.findAllInstances());

            //Get the id of the first instance in the test session
            long first_instance_id = calHandler().getFirstInstance(session_object.get_id());

            //Try to delete the first instance
            DeleteFirstInstance(session_object, first_instance_id);

            log("Result from deleting first instance \n\n");

            //Monitor this to determine if only the first instance was deleted successfully
            log(mycalHandler.findAllInstances());

        }

        else
        {
            log("Permission was not granted. Test can not continue");
        }

    }

    /**
     * Add a weekly repeating event starting from today at 10am that ends in 3 weeks
     */
    Session_Object AddTestSession()
    {

        long todays_date = dateTimeTool.getTodaysDate(); //Gets current date
        long _time = hour_length * 10; //10 AM
        long date_time = todays_date + _time; // 10 AM Today
        long date_time_until_l = date_time + (day_length * 21); //3 Weeks
        String end_date_s = dateTimeTool.convertLongDateToEXDATE(date_time_until_l);
        int byday_index = dateTimeTool.getDayOfWeekIndex(date_time);
        String BYDAY = dateTimeTool.getBYDAY(byday_index);
        String RRULE = "FREQ=WEEKLY;UNTIL=" + end_date_s + ";INTERVAL=1;WKST=SU;BYDAY=" + BYDAY;
        Session_Object session_object = new Session_Object();

        session_object.set_title("Test Event");
        session_object.set_date(date_time);
        session_object.set_duration(minute_length * 30);
        session_object.set_rrule(RRULE);

        session_object.set_id(calHandler().addSession(session_object));

        return session_object;

    }

    void DeleteFirstInstance(Session_Object session_object, long instance_id)
    {

        Instance_Object instance_object = calHandler().getInstance(session_object.get_id(), instance_id);
        calHandler().deleteInstance(session_object, instance_object);

    }

    void log(String log_text)
    {
        log_text = log_text +"\n\n";
        text_log.setText(text_log.getText() + log_text);
    }

    void GetCalendarPermissions()
    {

        //Ask for permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED)
        {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_CALENDAR)) {

                //This leads to the permissions callback

            }
            else
            {

                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_CALENDAR},
                        MY_PERMISSIONS_REQUEST_READ_CALENDAR);

                // MY_PERMISSIONS_REQUEST_READ_CALENDAR is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
        else
        {
            calendar_read_permission = true;

        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CALENDAR: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //Log.v("Permission", " Granted");
                    //showText("Permission Granted");
                    calendar_read_permission = true;
                    StartApp();

                } else {

                    calendar_read_permission = false;
                    //Log.v("Permission", " Denied");
                    //showText("Please accept calendar permissions to proceed");
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }

    }


}
