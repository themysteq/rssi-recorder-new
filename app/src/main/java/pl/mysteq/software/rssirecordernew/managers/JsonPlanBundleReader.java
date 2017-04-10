package pl.mysteq.software.rssirecordernew.managers;

import android.util.Log;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import pl.mysteq.software.rssirecordernew.structures.PlanBundle;

/**
 * Created by mysteq on 2017-04-10.
 */

public class JsonPlanBundleReader {
    private static final String LogTAG = "JsonPlanBundleReader";

    protected PlanBundle run(File bundleFile){
            Log.d(LogTAG, String.format("bundle file parsing: %s", bundleFile.getAbsolutePath()));
            PlanBundle planBundle = null;
            try {
                //TODO: wywalic sleep'a
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                Reader reader = new FileReader(bundleFile);
                Gson gson = new Gson();
                planBundle = gson.fromJson(reader, PlanBundle.class);
                Log.d(LogTAG, String.format("planBundle deserialized: %s", planBundle));
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        return planBundle ;

    }

}
