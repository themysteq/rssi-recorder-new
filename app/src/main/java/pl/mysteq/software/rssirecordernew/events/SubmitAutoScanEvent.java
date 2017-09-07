package pl.mysteq.software.rssirecordernew.events;

import java.util.HashMap;
import java.util.Objects;

/**
 * Created by mysteq on 2017-09-03.
 */

public class SubmitAutoScanEvent {
    public int counter = 0;
    public SubmitAutoScanEvent(){}
    public SubmitAutoScanEvent(int counter){
        this.counter = counter;
    }
}
