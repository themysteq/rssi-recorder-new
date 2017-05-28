package pl.mysteq.software.rssirecordernew.events.synchronizer;

/**
 * Created by mysteq on 2017-05-21.
 */

public class SyncBundlesEvent extends SyncEvent{
    public SyncBundlesEvent(String _hostname, int _port) {
        super(_hostname, _port);
    }
}
