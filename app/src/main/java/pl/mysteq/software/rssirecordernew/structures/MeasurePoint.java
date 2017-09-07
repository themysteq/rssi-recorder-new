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

    @SerializedName("rotation")
    public int rotation;

    @SerializedName("sector")
    public Point sector;

    public MeasurePoint()
    {
        super();
    }
    public MeasurePoint(ArrayList<CustomScanResult> _scanResultArrayList, Point point, int _rotation){
        super(point);
        scanResultArrayList = new ArrayList<CustomScanResult>(_scanResultArrayList);
        this.rotation = _rotation;
        this.sector = PlanBundle.getSectorFromPointOnImage(point);
    }
    public MeasurePoint(MeasurePoint measurePoint){
        super(measurePoint);
        this.scanResultArrayList = (ArrayList<CustomScanResult>) measurePoint.scanResultArrayList.clone();
        this.rotation = measurePoint.rotation;

    }

    public ArrayList<CustomScanResult> getScanResultArrayList() {
        return this.scanResultArrayList;
    }

    public static String rotationToString(float _rotationDegrees)
    {
        float margin = 35.f;
        if (_rotationDegrees > (360-margin) || _rotationDegrees < (margin)) { return "^12_00^";}
        else if(_rotationDegrees > (90-margin) && _rotationDegrees < (90+margin)) {return ">3_00>";}
        else if( _rotationDegrees > (180-margin) && _rotationDegrees < (180+margin)) {return "v6_00v";}
        else if( _rotationDegrees > (270-margin) && _rotationDegrees < (270+margin)){ return "<9_00<";}
        else{ return "????";}

    }
    public static float rotationStickTo(float _rotationDegrees)
    {
        float margin = 35.f;
        if (_rotationDegrees > (360-margin) || _rotationDegrees < (margin)) { return 0.f;}
        else if(_rotationDegrees > (90-margin) && _rotationDegrees < (90+margin)) {return 90.f;}
        else if( _rotationDegrees > (180-margin) && _rotationDegrees < (180+margin)) {return 180.f;}
        else if( _rotationDegrees > (270-margin) && _rotationDegrees < (270+margin)){ return 270.f;}
        else{ return _rotationDegrees ;}

    }

}
