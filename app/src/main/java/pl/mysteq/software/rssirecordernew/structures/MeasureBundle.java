package pl.mysteq.software.rssirecordernew.structures;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
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

    @SerializedName("filename")
    public String filename;

    @SerializedName("for_plan")
    public String forPlan;

    public MeasureBundle(String forPlanName){
        this.forPlan = forPlanName;
        this.uuid = UUID.randomUUID().toString();
        measures = new ArrayList<MeasurePoint>();
        filename = uuid+filename_suffix;
    }

    public ArrayList<MeasurePoint> getMeasures() {
        return measures;
    }

    public String getFilename() {
        return filename;
    }

    public String getUuid() {
        return uuid;
    }

    public void setMeasures(ArrayList<MeasurePoint> measures) {
        this.measures = measures;
    }
}
