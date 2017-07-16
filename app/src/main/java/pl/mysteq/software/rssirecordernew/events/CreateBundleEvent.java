package pl.mysteq.software.rssirecordernew.events;

import java.io.File;

/**
 * Created by mysteq on 2017-04-10.
 */

public class CreateBundleEvent {
    private File planFile;
    private String name;

    public CreateBundleEvent(File planFile, String name){
        this.planFile = planFile;
        this.name = name;
    }
    public File getPlanFile(){ return this.planFile; }
    public String getName(){ return this.name; }
}
