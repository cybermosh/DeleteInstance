package au.com.atomictech.deleteinstance.objects;

public class Session_Object {

    private static long minute_length = 1000 * 60;

    private long _id;
    private String title;
    private long _date;
    private long _duration;
    private String _exDate;
    private String _rrule;

    public Session_Object(){

        this._rrule = "";

    }

    public String get_exDate() {
        return _exDate;
    }

    public void set_exDate(String _exDate) {
        this._exDate = _exDate;
    }

    public String get_rrule() {
        return _rrule;
    }

    public void set_rrule(String _rrule) {
        this._rrule = _rrule;
    }

    public long get_id() {
        return _id;
    }

    public void set_id(long _id) {
        this._id = _id;
    }

    public String get_title() {
        return title;
    }

    public void set_title(String _client_id) {
        this.title = _client_id;
    }

    public Long get_date() {
        return _date;
    }

    public void set_date(long _date) {
        this._date = _date;
    }

    public long get_duration() {

        return this._duration;
    }

    public String get_P_Duration()
    {
        return "PT" + (this._duration / minute_length) + "M";
    }

    public void set_duration(long _duration) {

        this._duration = _duration;
    }

}
