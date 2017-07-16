package pl.mysteq.software.rssirecordernew.events.synchronizer;

/**
 * Created by mysteq on 2017-05-21.
 */

public class SyncBundlesDoneEvent {
    public String bundles = null;
    public SyncBundlesDoneEvent() {}

    public SyncBundlesDoneEvent(String bundles) {
        this.bundles = bundles;
    }

}
