package pl.mysteq.software.rssirecordernew.events;

import android.widget.ProgressBar;

/**
 * Created by mysteq on 2017-09-10.
 */

public class ProgressEvent {

    public float progress = 0;
    public boolean indeterminate = true;
    public boolean enabled = true;
    public String text = "";
    public ProgressEvent()
    {}
    public ProgressEvent(float _progress,boolean _indeterminate, boolean _enabled){
        this.progress = _progress;
        this.indeterminate = _indeterminate;
        this.enabled = _enabled;
    }
    public ProgressEvent setText(String _text){
        this.text = _text;
        return  this;
    }
}
