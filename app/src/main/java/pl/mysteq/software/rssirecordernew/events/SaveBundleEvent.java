package pl.mysteq.software.rssirecordernew.events;

/**
 * Created by mysteq on 2017-04-24.
 */

public class SaveBundleEvent {
    public String bundleName;
    public SaveBundleEvent(String _bundleName){
        this.bundleName = _bundleName;
    }
}
