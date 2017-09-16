package pl.mysteq.software.rssirecordernew.algorithm;

import java.util.ArrayList;
import java.util.HashMap;

import pl.mysteq.software.rssirecordernew.structures.CustomScanResult;
import pl.mysteq.software.rssirecordernew.structures.MeasurePoint;
import pl.mysteq.software.rssirecordernew.structures.Sector;

/**
 * Created by mysteq on 2017-09-10.
 */

public class AlgorithmSingleton {

    private Sector currentSector = null;
    private MeasurePoint currentMeasurePoint = null;
    private ArrayList<MeasurePoint> measurePointArrayList = null;
    private HashMap<Integer,HashMap<Integer,Sector>> sectorHashMap = null;
    private HashMap<String,CustomScanResult> _ScanResultHashMap = null;


    public void setSectorHashMap(HashMap<Integer, HashMap<Integer, Sector>> sectorHashMap) {
        this.sectorHashMap = sectorHashMap;
    }

    public void setMeasurePointArrayList(ArrayList<MeasurePoint> measurePointArrayList) {
        this.measurePointArrayList = measurePointArrayList;
    }

    public void indexMeasures() {

    }
}
