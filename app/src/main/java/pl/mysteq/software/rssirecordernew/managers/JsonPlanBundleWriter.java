package pl.mysteq.software.rssirecordernew.managers;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import pl.mysteq.software.rssirecordernew.structures.PlanBundle;

/**
 * Created by mysteq on 2017-04-10.
 */

public class JsonPlanBundleWriter {
    public static final String LogTAG = "JsonPlanBundleWriter";

    private PlanBundle planBundle;
    private File bundleFile;
   // private ArrayList<String> measuresFiles;

    public JsonPlanBundleWriter(PlanBundle planBundle,File bundleFile ){
        this.planBundle = planBundle;
        this.bundleFile = bundleFile;
        //this.measuresFiles = measuresFiles;
    }

    protected Void run(){
        try {
            Gson gson = new Gson();
            Writer writer = new FileWriter(bundleFile);
            gson.toJson(planBundle,PlanBundle.class,writer);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }
}
