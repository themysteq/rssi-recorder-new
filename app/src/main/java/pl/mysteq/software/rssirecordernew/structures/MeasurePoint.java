package pl.mysteq.software.rssirecordernew.structures;

import android.graphics.Point;
import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import pl.mysteq.software.rssirecordernew.extendables.SectorPoint;

/**
 * Created by mysteq on 2017-04-22.
 */

public class MeasurePoint extends Point {
    @SerializedName("scan_results")
    public ArrayList<CustomScanResult> scanResultArrayList;

    @SerializedName("rotation")
    public int rotation;

    @SerializedName("sector")
    public SectorPoint sector;

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
    public MeasurePoint(ArrayList<CustomScanResult> _scanResultArrayList, SectorPoint _sectorPoint, int _rotation) {
        super(-3,-3);
        scanResultArrayList = new ArrayList<CustomScanResult>(_scanResultArrayList);
        this.rotation = _rotation;
        this.sector = _sectorPoint;
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

        float margin = 30.f;
        float value = (360+_rotationDegrees)%360;

       // Log.v("rotationToString",Float.toString(value));
        if (value > (360-margin) || value < (margin)) { return "/\\";}
        else if(value > (90-margin) && value < (90+margin)) {return "-->>";}
        else if( value > (180-margin) && value < (180+margin)) {return "\\/";}
        else if( value > (270-margin) && value < (270+margin)){ return "<<--";}
        else{ return "????";}

        /*
        if (( value > 0-margin) && (_rotationDegrees < 0+margin)){ return "/\\";}
        else if (( _rotationDegrees > 90-margin) && (_rotationDegrees < 90+margin)){return "-->>";}
        else if (( _rotationDegrees > 270-margin) && (_rotationDegrees < 270+margin)) { return "<<--";}
        else if (( _rotationDegrees (margin/2))
        */

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
