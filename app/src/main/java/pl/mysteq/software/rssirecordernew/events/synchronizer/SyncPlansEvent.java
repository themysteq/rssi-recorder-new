package pl.mysteq.software.rssirecordernew.events.synchronizer;

/**
 * Created by mysteq on 2017-05-21.
 */

public class SyncPlansEvent extends SyncEvent {
    public SyncPlansEvent(String _hostname, int _port) {
        super(_hostname, _port);
    }
}
