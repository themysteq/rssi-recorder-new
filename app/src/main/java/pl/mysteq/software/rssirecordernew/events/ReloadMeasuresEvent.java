package pl.mysteq.software.rssirecordernew.events;

import android.util.Log;

/**
 * Created by mysteq on 2017-05-16.
 */

public class ReloadMeasuresEvent {
        private final static String LogTAG = "ReloadMeasuresEvent";
        public String plan_name = null;
    public ReloadMeasuresEvent()
        {
            Log.d(LogTAG,"event created");
        }
        public ReloadMeasuresEvent(String plan_name){
            this.plan_name = plan_name;
        }
}
