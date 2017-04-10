package pl.mysteq.software.rssirecordernew.structures;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by mysteq on 2017-04-09.
 */

public class PlanBundle {

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
}
