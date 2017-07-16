package pl.mysteq.software.rssirecordernew.events.synchronizer;

/**
 * Created by mysteq on 2017-05-21.
 */

public class SyncPlansDoneEvent {

    public String plans = null;
    public SyncPlansDoneEvent() {}

    public SyncPlansDoneEvent(String plans) {
        this.plans = plans;
    }
}
