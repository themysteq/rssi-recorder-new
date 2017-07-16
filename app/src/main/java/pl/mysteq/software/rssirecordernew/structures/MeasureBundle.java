package pl.mysteq.software.rssirecordernew.structures;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Created by mysteq on 2017-05-01.
 */

public class MeasureBundle {
    public static final String LogTAG = "MeasureBundle";
    String filename_suffix = ".measure.json";
    FilenameFilter measureFilter = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String filename) {
            Log.d(LogTAG, String.format("measureFilter: %s", filename));
            return filename.matches(".*\\.measure\\.json$");
        }
    };
    @SerializedName("uuid")
    public String uuid;

    @SerializedName("measures")
    public ArrayList<MeasurePoint> measures;

    @SerializedName("filepath")
    public String filepath;

    @SerializedName("for_plan")
    public String forPlan;

    @SerializedName("last_changed")
    public Date lastChanged;

    public MeasureBundle(String forPlanName){
        this.forPlan = forPlanName;
        this.uuid = UUID.randomUUID().toString();
        measures = new ArrayList<MeasurePoint>();
        filepath = uuid+filename_suffix;
    }

    public ArrayList<MeasurePoint> getMeasures() {
        return measures;
    }

    public String getFilepath() {
        return filepath;
    }

    public String getUuid() {
        return uuid;
    }

    public Date getLastChanged() {
        return lastChanged;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    public void setLastChanged(Date lastChanged) {
        this.lastChanged = lastChanged;
    }

    public void setMeasures(ArrayList<MeasurePoint> measures) {
        this.measures = measures;
    }
}
