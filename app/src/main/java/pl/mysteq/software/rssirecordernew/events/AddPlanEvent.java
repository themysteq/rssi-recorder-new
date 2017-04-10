package pl.mysteq.software.rssirecordernew.events;

/**
 * Created by mysteq on 2017-04-09.
 */

public class AddPlanEvent {
    private String imageFilePath;
    private String name;
    public AddPlanEvent(String pathToImageFile,String name){
        this.imageFilePath = pathToImageFile;
        this.name = name;
    }
    public String getImageFilePath(){
        return this.imageFilePath;
    }
    public String getName() { return this.name; }
}
