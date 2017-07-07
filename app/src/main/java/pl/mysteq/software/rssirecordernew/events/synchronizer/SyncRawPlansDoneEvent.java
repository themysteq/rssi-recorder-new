package pl.mysteq.software.rssirecordernew.events.synchronizer;

/**
 * Created by mysteq on 2017-07-07.
 */

public class SyncRawPlansDoneEvent extends SyncEvent {
    public SyncRawPlansDoneEvent(String _hostname, int _port) {
        super(_hostname, _port);
    }
}
