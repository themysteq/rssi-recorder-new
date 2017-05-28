package pl.mysteq.software.rssirecordernew.events.synchronizer;

import android.support.annotation.Nullable;

/**
 * Created by mysteq on 2017-05-26.
 */

public abstract class SyncEvent {
    protected  String hostname = "localhost";
    protected  String scheme = "http://";
    protected  int port  = 80;
    public SyncEvent( String _hostname, int _port){
        hostname = _hostname;
        port = _port;
    }
    public SyncEvent(){}

    public String getURL()
    {
        return scheme+hostname+":"+Integer.toString(port);
    }

}
