package pl.mysteq.software.rssirecordernew.events;

/**
 * Created by mysteq on 2017-04-09.
 */

public class SaveMeasuresEvent {
    public String fullpath;
    public String plan_name;
    public SaveMeasuresEvent(String fullpath,String planName){
        this.fullpath = fullpath;
        this.plan_name = planName;
    }
}

