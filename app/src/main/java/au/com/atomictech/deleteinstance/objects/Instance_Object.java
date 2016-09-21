package au.com.atomictech.deleteinstance.objects;

public class Instance_Object {


    long session_id;
    long instance_id;
    long begin_time;
    long end_time;
    long begin_date;
    long begin;

    public long getBegin() {
        return begin;
    }

    public void setBegin(long begin) {
        this.begin = begin;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    long end;


    public long getBegin_date() {
        return begin_date;
    }

    public void setBegin_date(long begin_date) {
        this.begin_date = begin_date;
    }

    public long getEnd_date() {
        return end_date;
    }

    public void setEnd_date(long end_date) {
        this.end_date = end_date;
    }

    long end_date;

    public long getSession_id() {
        return session_id;
    }

    public void setSession_id(long session_id) {
        this.session_id = session_id;
    }

    public long getInstance_id() {
        return instance_id;
    }

    public void setInstance_id(long instance_id) {
        this.instance_id = instance_id;
    }

    public long getBegin_time() {
        return begin_time;
    }

    public void setBegin_time(long begin_time) {
        this.begin_time = begin_time;
    }

    public long getEnd_time() {
        return end_time;
    }

    public void setEnd_time(long end_time) {
        this.end_time = end_time;
    }
}
