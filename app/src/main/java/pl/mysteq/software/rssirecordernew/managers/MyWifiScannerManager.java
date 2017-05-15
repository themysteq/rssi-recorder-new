package pl.mysteq.software.rssirecordernew.managers;

import android.content.Context;
import android.graphics.Point;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;

import pl.mysteq.software.rssirecordernew.events.SaveMeasuresEvent;
import pl.mysteq.software.rssirecordernew.events.WifiScanCompleted;
import pl.mysteq.software.rssirecordernew.structures.CustomScanResult;
import pl.mysteq.software.rssirecordernew.structures.MeasureBundle;
import pl.mysteq.software.rssirecordernew.structures.MeasurePoint;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by mysteq on 2017-04-22.
 */

public final class MyWifiScannerManager {

    public static final String LogTAG = "MyWifiScannerManager";
    private ArrayList<CustomScanResult> lastScanResults = null;
    private Context context;
    private WifiManager wifiManager;
    private WifiManager.WifiLock wifiLock;
    boolean initialized = false;

    private ArrayList<MeasurePoint> measurePointArrayList = null;
    private static MyWifiScannerManager instance = null;

    public static MyWifiScannerManager getInstance(){
        if(instance == null) instance = getSync();
        return instance;
    }
    private static synchronized MyWifiScannerManager getSync(){
        if(instance==null) instance = new MyWifiScannerManager();
        return instance;
    }

    public MyWifiScannerManager init(Context _context) {
        //if(initialized) {
        //   this.measurePointArrayList = new ArrayList<MeasurePoint>();
        //  return this;
        //}
        this.context = _context.getApplicationContext();
        this.measurePointArrayList = new ArrayList<MeasurePoint>();
        wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, "RSSI_RECORDER_WIFI_LOCK");
        wifiLock.acquire();
        if(! initialized) {
            EventBus.getDefault().register(this);
        }
        Log.d(LogTAG,"Initialized");
        initialized = true;
        return instance;
    }
    public void stop(){
            wifiLock.release();
    }

    public void scan(){
        //FIXME: #1 potencjalnie null pointer exception lub cos takiego!
        Log.d(LogTAG,"scan()");
        this.wifiManager.startScan();

    }
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void scanDone(WifiScanCompleted event)
    {

        ArrayList<ScanResult> scanResults = (ArrayList<ScanResult>) this.wifiManager.getScanResults();
        lastScanResults = new ArrayList<>();

        for(ScanResult scanResult : scanResults)
        {
            //Log.d(LogTAG,"scanResult: "+scanResult.toString());
            lastScanResults.add(new CustomScanResult(scanResult));

        }
    }

    public ArrayList<CustomScanResult> getLastScanResult(){
        return lastScanResults;
    }

    public MeasurePoint addMeasurePoint(ArrayList<CustomScanResult> list, Point point,int rotation){
        Log.d(LogTAG, String.format("add new measure: x: %d, y: %d, rotation: %d", point.x,point.y,rotation));
        MeasurePoint measurePoint = new MeasurePoint();
        measurePoint.scanResultArrayList =(ArrayList<CustomScanResult>) list.clone();
        measurePoint.set(point.x,point.y);
        measurePoint.rotation = rotation;
        this.measurePointArrayList.add(measurePoint);
        return measurePoint;
    }
    public void loadFromFile(File filepath){
        JsonMeasuresReader reader = new JsonMeasuresReader();
        reader.run(filepath);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void OnMessage(SaveMeasuresEvent event){
        saveToFile(new File(event.fullpath),event.plan_name);
    }

    public void saveToFile(File filepath,String planName){


        MeasureBundle measureBundle = new MeasureBundle(planName);
        measureBundle.setMeasures(this.measurePointArrayList);
        JsonMeasuresWriter writer = new JsonMeasuresWriter(measureBundle);
        writer.run();
        Log.d(LogTAG,"saving "+filepath);
    }
    public void saveToFile(MeasureBundle measureBundle)
    {
        measureBundle.setMeasures(this.measurePointArrayList);
        JsonMeasuresWriter writer = new JsonMeasuresWriter(measureBundle);
        writer.run();
        Log.d(LogTAG,"saved "+measureBundle.getFilepath());
    }
    public void clear()
    {
        if(measurePointArrayList != null){
            this.measurePointArrayList.clear();
        }
        else {
            measurePointArrayList = new ArrayList<MeasurePoint>();
        }
    }



}
