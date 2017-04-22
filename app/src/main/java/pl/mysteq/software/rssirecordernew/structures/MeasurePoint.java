package pl.mysteq.software.rssirecordernew.structures;

import android.graphics.Point;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by mysteq on 2017-04-22.
 */

public class MeasurePoint extends Point {
    @SerializedName("scan_results")
    public ArrayList<CustomScanResult> scanResultArrayList;

    public MeasurePoint()
    {
        super();
    }
    public MeasurePoint(ArrayList<CustomScanResult> _scanResultArrayList, Point point){
        super(point);
        scanResultArrayList = new ArrayList<CustomScanResult>(_scanResultArrayList);
    }
    public MeasurePoint(MeasurePoint measurePoint){
        super(measurePoint);
        this.scanResultArrayList = (ArrayList<CustomScanResult>) measurePoint.scanResultArrayList.clone();

    }

    public ArrayList<CustomScanResult> getScanResultArrayList() {
        return this.scanResultArrayList;
    }
}
