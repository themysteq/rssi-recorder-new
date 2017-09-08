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
    public static final String LogTAG = "SectorManager";
    public SectorManager(){
        sectors = new HashMap<>(65);
    }

    public void insertSector(Sector sector){
        Log.v(LogTAG,"insertSector()");
       // if(! sectors.containsKey(sector.getCoordinates().x)) { sectors.put(sector.getCoordinates().x,null);}
        if(sectors.get(sector.getCoordinates().x) == null){
            HashMap<Integer,Sector> map = new HashMap<>(65);
            map.put(sector.getCoordinates().y,sector);
            sectors.put(sector.getCoordinates().x,map);
            Log.d(LogTAG, String.format("empty row. put sector x:%d y:%d", sector.getCoordinates().x,sector.getCoordinates().y));
        }
        else {

            sectors.get(sector.getCoordinates().x).put(sector.getCoordinates().y,sector);
            Log.d(LogTAG, String.format("row exists. put sector x:%d y:%d", sector.getCoordinates().x,sector.getCoordinates().y));
        }

    }
    public Sector getSector(Point coordinates){
        Log.v(LogTAG, String.format("getSector(%s)", coordinates.toString()));
        if( sectors.get(coordinates.x) != null) {
            return sectors.get(coordinates.x).get(coordinates.y);
        }
        else return null;
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

}
