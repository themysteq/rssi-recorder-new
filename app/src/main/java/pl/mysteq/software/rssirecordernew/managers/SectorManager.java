package pl.mysteq.software.rssirecordernew.managers;

import android.graphics.Point;
import android.icu.util.Measure;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import pl.mysteq.software.rssirecordernew.structures.MeasurePoint;
import pl.mysteq.software.rssirecordernew.structures.Sector;

/**
 * Created by mysteq on 2017-09-05.
 */

public class SectorManager {

    private HashMap<Integer,HashMap<Integer,Sector>> sectors;
    private Point currentSectorPoint = null;
    //private Sector currentSector = null;
    public static final String LogTAG = "SectorManager";
    public SectorManager(){
        sectors = new HashMap<>(65);
    }

    public void insertSector(Sector sector){
        Log.v(LogTAG,"insertSector()");
       // if(! sectors.containsKey(sector.getCoordinates().x)) { sectors.put(sector.getCoordinates().x,null);}
        if(sectors.get(sector.getCoordinates().x) == null){
            HashMap<Integer,Sector> map = new HashMap<>(65);
            Log.v(LogTAG, String.format("created new cell for column x=%d", sector.getCoordinates().x));
            map.put(sector.getCoordinates().y,sector);
            sectors.put(sector.getCoordinates().x,map);
            Log.d(LogTAG, String.format("empty row. put sector x:%d y:%d", sector.getCoordinates().x,sector.getCoordinates().y));
        }
        else {
            if(sectors.get(sector.getCoordinates().x).get(sector.getCoordinates().y) == null) {
                sectors.get(sector.getCoordinates().x).put(sector.getCoordinates().y, sector);
            }
            else {
                throw new IllegalStateException(String.format("sector exists: x=%d y=%d", sector.getCoordinates().x,sector.getCoordinates().y));
            }


        }

    }
    public Sector getSector(Point coordinates){
        Log.v(LogTAG, String.format("getSector(%s)", coordinates.toString()));
        HashMap<Integer,Sector> secondKeyValue = sectors.get(coordinates.x);
        if( secondKeyValue != null) {
            Sector sector = secondKeyValue.get(coordinates.y);
            if (sector != null) { return sector; }
            else {
                secondKeyValue.put(coordinates.y, new Sector(coordinates));
            }

        }
        else {
            secondKeyValue = new HashMap<>(65);
            secondKeyValue.put(coordinates.y,new Sector(coordinates));
            sectors.put(coordinates.x,secondKeyValue);

        }
        return sectors.get(coordinates.x).get(coordinates.y);
    }
    public int countAllSectors()
    {
        int count = 0;
        for (HashMap<Integer,Sector>  elem: sectors.values()) {
            count += elem.size();
        }
        return count;
    }
    public void insertMeasureToSector(MeasurePoint _measurePoint){

    }

    public void setCurrentSectorPoint(Point currentSector) {
        this.currentSectorPoint = currentSector;
    }

    public Point getCurrentSectorPoint() {
        return currentSectorPoint;
    }
    public Sector getCurrentSector() {
        return getSector(currentSectorPoint);
    }
}
