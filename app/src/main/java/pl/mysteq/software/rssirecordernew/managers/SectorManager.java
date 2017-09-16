package pl.mysteq.software.rssirecordernew.managers;

import android.graphics.Point;
import android.icu.util.Measure;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import pl.mysteq.software.rssirecordernew.extendables.SectorPoint;
import pl.mysteq.software.rssirecordernew.structures.MeasurePoint;
import pl.mysteq.software.rssirecordernew.structures.Sector;

/**
 * Created by mysteq on 2017-09-05.
 */

public class SectorManager {

    private HashMap<Integer,HashMap<Integer,Sector>> sectors;
    private SectorPoint currentSectorPoint = null;
    private int measuresCounter = 0;
    //private Sector currentSector = null;
    public static final String LogTAG = "SectorManager";
    public SectorManager(){
        Log.d(LogTAG,"creating");
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
    public Sector getSector(SectorPoint coordinates){
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
    public ArrayList<Sector> getAllSectorsArrayList(){
        ArrayList<Sector> sectorArrayList = new ArrayList<>();
        Set<Integer> columns = sectors.keySet();
        for(Integer x : columns){
            for(Integer y : sectors.get(x).keySet())
            {
                sectorArrayList.add(sectors.get(x).get(y));
            }
        }
        return  sectorArrayList;
    }
    public void insertMeasureToCurrentSector(MeasurePoint _measurePoint){
           getCurrentSector().insertMeasurePoint(_measurePoint);
            measuresCounter++;
    }
    public void insertMeasureToSector(SectorPoint _sectorPoint, MeasurePoint _measurePoint){
        Log.v(LogTAG, String.format("insertMeasureToSector(): %s", _sectorPoint.toString()));
        getSector(_sectorPoint).insertMeasurePoint(_measurePoint);
        measuresCounter++;
    }

    public void setCurrentSectorPoint(SectorPoint currentSector) {
        Log.v(LogTAG, String.format("setCurrentSectorPoint(): %s", currentSector.toString()));
        this.currentSectorPoint = currentSector;
    }

    public SectorPoint getCurrentSectorPoint() {
        return currentSectorPoint;
    }
    public Sector getCurrentSector() {
        return getSector(currentSectorPoint);
    }

    public ArrayList<MeasurePoint> getAllMeasures(){
        //FIXME: Could be hard!
        Log.d(LogTAG,"getAllMeasures() start");
        ArrayList<MeasurePoint> allPoints = new ArrayList<>();
        Set<Integer> columns = sectors.keySet();
        for(Integer x : columns){
            for(Integer y : sectors.get(x).keySet())
            {
                allPoints.addAll(sectors.get(x).get(y).getMeasurePoints());
            }
        }
        Log.d(LogTAG,"getAllMeasures() finish");
        //FIXME: n^3 algorithm. *slow clap*
        return  allPoints;
    }
    public void loadAllMeasures(ArrayList<MeasurePoint> _allMeasures){
        Log.d(LogTAG,"loadAllMeasures() size: "+Integer.toString(_allMeasures.size()));
        for (MeasurePoint measure : _allMeasures){
            getSector(measure.sector).insertMeasurePoint(measure);
            measuresCounter++;
        }
    }
    public int measuresPerSectorSize(SectorPoint _sectorPoint){
        return getSector(_sectorPoint).size();
    }
    public int measuresPerCurrentSectorSize(){
        return getCurrentSector().size();
    }

    public int getMeasuresCounter() {
        return measuresCounter;
    }
    public void clearCurrent() {
        getCurrentSector().clear();
    }
}
