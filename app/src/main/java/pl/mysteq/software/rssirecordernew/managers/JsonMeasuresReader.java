package pl.mysteq.software.rssirecordernew.managers;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import pl.mysteq.software.rssirecordernew.structures.MeasureBundle;
import pl.mysteq.software.rssirecordernew.structures.MeasurePoint;
import pl.mysteq.software.rssirecordernew.structures.PlanBundle;

/**
 * Created by mysteq on 2017-04-24.
 */

public class JsonMeasuresReader {
    private static final String LogTAG = "JsonMeasuresReader";

    public MeasureBundle run(File measureFile){
        Log.d(LogTAG, String.format("measure file parsing: %s", measureFile.getAbsolutePath()));
        MeasureBundle measureBundle = null;

        try {
            Reader reader = new FileReader(measureFile);
            Gson gson = new Gson();
            //measurePoints = gson.fromJson(reader, new TypeToken<List<MeasurePoint>>(){}.getType());
            measureBundle = gson.fromJson(reader,new TypeToken<MeasureBundle>(){}.getType());
            Log.d(LogTAG, String.format("planBundle deserialized: %s", measureBundle));
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return measureBundle;

    }

}
