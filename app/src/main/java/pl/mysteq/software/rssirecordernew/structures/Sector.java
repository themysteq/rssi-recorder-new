package pl.mysteq.software.rssirecordernew.structures;

import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by mysteq on 2017-09-05.
 */

public class Sector {
    public static final int FULL_SECTOR = 44;
    private static final String LogTAG = "Sector";
    private ArrayList<MeasurePoint> measurePoints;
    private Point coordinates;

    public Sector(){
        this.measurePoints = new ArrayList<>();
        Log.v(LogTAG,"constructed new empty Sector.");
    }
    public Sector(Point _coordinates){
        this.coordinates = _coordinates;
        this.measurePoints = new ArrayList<>();
        Log.v(LogTAG, String.format("constructed new Sector: x=%d,y=%d", _coordinates.x,_coordinates.y));
    }


    public boolean isFullSector(){
        return measurePoints.size() == FULL_SECTOR;
    }
    public boolean isEmptySector(){
        return measurePoints.size() == 0;
    }

    public void insertMeasurePoint(MeasurePoint _measurePoint){
        this.measurePoints.add(_measurePoint);
    }

    public int getPointCount() {
        return measurePoints.size();
    }
    public int size(){
        return  measurePoints.size();
    }

    public Point getCoordinates() {
        return coordinates;
    }
}
