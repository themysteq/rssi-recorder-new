package pl.mysteq.software.rssirecordernew.managers;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import pl.mysteq.software.rssirecordernew.structures.MeasureBundle;
import pl.mysteq.software.rssirecordernew.structures.MeasurePoint;
import pl.mysteq.software.rssirecordernew.structures.PlanBundle;

/**
 * Created by mysteq on 2017-04-24.
 */

public class JsonMeasuresWriter {
    public static final String LogTAG = "JsonMeasuresWriter";

    private ArrayList<MeasurePoint> measurePoints;
    private File measureFile;
    private MeasureBundle measureBundle;
    // private ArrayList<String> measuresFiles;

    public JsonMeasuresWriter(MeasureBundle _measureBundle){
        //this.measurePoints = _measureBundle.getMeasures();
        //FIXME: this is bad and you should feel bad
        this.measureFile = new File(PlansFileManager.getInstance().getAppExternalMeasuresFolder(),_measureBundle.getFilename());
        this.measureBundle = _measureBundle;
        // /this.measuresFiles = measuresFiles;
    }

    protected Void run(){
        try {
            Gson gson = new Gson();
            Writer writer = new FileWriter(measureFile);
           /* if(measurePoints == null)
            {
                measurePoints = new ArrayList<MeasurePoint>();
                Log.w(LogTAG,"Writing empty list");
            }
            */
            gson.toJson(measureBundle,new TypeToken<MeasureBundle>(){}.getType(),writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }
}
