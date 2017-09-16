package pl.mysteq.software.rssirecordernew.structures;

import android.graphics.Point;
import android.graphics.Rect;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import pl.mysteq.software.rssirecordernew.extendables.SectorPoint;

/**
 * Created by mysteq on 2017-04-09.
 */

public class PlanBundle {

    public static final String SELECTED_PLANBUNDLE_KEY = "plan_bundle_key";
    public static final int SECTOR_X_SIZE = 50;
    public static final int SECTOR_Y_SIZE = 50;

    @SerializedName("building_plan_filename")
    private String buildingPlanFileName = "";

    @SerializedName("measures_per_plan_paths")
    private ArrayList<String> measuresFileNames = new ArrayList<String>();

    @SerializedName("plan_bundle_name")
    private String planBundleName ;

    @SerializedName("comment")
    private String comment;

    public PlanBundle(){}

    public String getPlanBundleName() {
        return planBundleName;
    }

    public void setPlanBundleName(String planBundleName) {
        this.planBundleName = planBundleName;
    }

    public String getBuildingPlanFileName() {
        return buildingPlanFileName;
    }

    public void setBuildingPlanFileName(String buildingPlanFileName) {
        this.buildingPlanFileName = buildingPlanFileName;
    }

    public ArrayList<String> getMeasuresFileNames() {
        return measuresFileNames;
    }

    public void setMeasuresFileNames(ArrayList<String> measuresFileNames) {
        this.measuresFileNames = measuresFileNames;
    }
    public void addMeasureFilename(String filename){
        this.measuresFileNames.add(filename);
    }
    public static SectorPoint getSectorFromPointOnImage(Point pointOnImage){
       // SectorPoint sectorPoint = new SectorPoint();

        int sector_x = ((Double)(Math.floor(pointOnImage.x/PlanBundle.SECTOR_X_SIZE))).intValue();
        int sector_y = ((Double)(Math.floor(pointOnImage.y/PlanBundle.SECTOR_Y_SIZE))).intValue();
       // sectorPoint.set(sector_x,sector_y);
        return new SectorPoint(sector_x,sector_y);
    }

}
