package pl.mysteq.software.rssirecordernew.events;

/**
 * Created by mysteq on 2017-04-09.
 */

public class AddPlanEvent {
    private String imageFilePath;
    public AddPlanEvent(String pathToImageFile){
        this.imageFilePath = pathToImageFile;
    }
    public String getImageFilePath(){
        return this.imageFilePath;
    }
}
