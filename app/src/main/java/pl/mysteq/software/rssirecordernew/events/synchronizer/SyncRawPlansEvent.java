package pl.mysteq.software.rssirecordernew.events.synchronizer;

/**
 * Created by mysteq on 2017-07-07.
 */

public class SyncRawPlansEvent extends SyncEvent {
    public SyncRawPlansEvent(String _hostname, int _port) {
        super(_hostname, _port);
    }
}
