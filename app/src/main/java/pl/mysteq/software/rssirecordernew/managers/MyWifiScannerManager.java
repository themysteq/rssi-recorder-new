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

import pl.mysteq.software.rssirecordernew.events.AutoScanCompletedEvent;
import pl.mysteq.software.rssirecordernew.events.PerformWifiScanEvent;
import pl.mysteq.software.rssirecordernew.events.RefreshStatisticsEvent;
import pl.mysteq.software.rssirecordernew.events.SaveMeasuresEvent;
import pl.mysteq.software.rssirecordernew.events.WifiScanCompletedEvent;
import pl.mysteq.software.rssirecordernew.structures.CustomScanResult;
import pl.mysteq.software.rssirecordernew.structures.MeasureBundle;
import pl.mysteq.software.rssirecordernew.structures.MeasurePoint;
import pl.mysteq.software.rssirecordernew.structures.PlanBundle;
import pl.mysteq.software.rssirecordernew.structures.Sector;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by mysteq on 2017-04-22.
 */

public final class MyWifiScannerManager {

    public static final String LogTAG = "MyWifiScannerManager";
    private ArrayList<CustomScanResult> lastScanResults = null;
    private Context context;
    private SectorManager sectorManager = null;
    private AutoScanManager autoScanManager = null;
    private WifiManager wifiManager;
    private WifiManager.WifiLock wifiLock;
    boolean initialized = false;

   // private ArrayList<MeasurePoint> measurePointArrayList = null;
    private static MyWifiScannerManager instance = null;

    public static MyWifiScannerManager getInstance(){
        if(instance == null) instance = getSync();
        return instance;
    }
    private static synchronized MyWifiScannerManager getSync(){
        if(instance==null) instance = new MyWifiScannerManager();
        Log.v(LogTAG,"new MyWifiScannerManager instance");
        return instance;
    }

    public MyWifiScannerManager init(Context _context) {
        //if(initialized) {
        //   this.measurePointArrayList = new ArrayList<MeasurePoint>();
        //  return this;
        //}
        if(initialized){
            Log.w(LogTAG,"Reinitialized");
        }
        else{
            Log.w(LogTAG,"First initialize");
        }
        this.context = _context.getApplicationContext();
       // this.measurePointArrayList = new ArrayList<MeasurePoint>();
        wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, "RSSI_RECORDER_WIFI_LOCK");
        wifiLock.acquire();
        sectorManager = new SectorManager();
        autoScanManager = new AutoScanManager();
        if(! initialized) {
            Log.d(LogTAG,"registering to EventBus");
            EventBus.getDefault().register(this);
        }
        Log.d(LogTAG,"Initialize done");
        initialized = true;
        return instance;
    }
    public void stop(){
        if (wifiLock.isHeld()) {
            wifiLock.release();
        }
    }

    public void scan(){
        //FIXME: #1 potencjalnie null pointer exception lub cos takiego!
        Log.d(LogTAG,"scan()");
        if( ! wifiLock.isHeld()) {
            Log.w(LogTAG,"wifiLock lost, acquiring again");
            wifiLock.acquire();
        }
        Log.v(LogTAG,"starting scan");
        this.wifiManager.startScan();

    }
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void scanDone(WifiScanCompletedEvent event)
    {
        Log.d(LogTAG,"scanDone");
        ArrayList<ScanResult> scanResults = (ArrayList<ScanResult>) this.wifiManager.getScanResults();
        lastScanResults = new ArrayList<>();

        if (scanResults.size() < 1){
            Log.w(LogTAG,"scanResults are empty!");
        }
        for(ScanResult scanResult : scanResults)
        {
            Log.v(LogTAG,"scanResult: "+scanResult.toString());
            lastScanResults.add(new CustomScanResult(scanResult));

        }
    }

    public ArrayList<CustomScanResult> getLastScanResult(){
        return lastScanResults;
    }
/*
    public void addMeasurePointToSector(ArrayList<CustomScanResult> scan,Point sector, int rotation ){
        Log.d(LogTAG, String.format("Add measure point to sector: x=%d y=%d", sector.x,sector.y));
        MeasurePoint measurePoint = new MeasurePoint();
        measurePoint.scanResultArrayList = scan;
        measurePoint.sector = sector;
        measurePoint.set(-1,-1);
        measurePoint.rotation = rotation;
        sectorManager.getSector(sector).insertMeasurePoint(measurePoint);
    }
    */
    public MeasurePoint addMeasurePoint(ArrayList<CustomScanResult> list, Point pointOnImage,int rotation){
        Log.d(LogTAG, String.format("add new measure: x: %d, y: %d, rotation: %d", pointOnImage.x,pointOnImage.y,rotation));
        MeasurePoint measurePoint = new MeasurePoint();
        measurePoint.scanResultArrayList = (ArrayList<CustomScanResult>) list.clone();
        measurePoint.set(-2,-2);
        measurePoint.rotation = rotation;
        measurePoint.sector = PlanBundle.getSectorFromPointOnImage(pointOnImage);
        this.sectorManager.insertMeasureToSector(PlanBundle.getSectorFromPointOnImage(pointOnImage),measurePoint);
        return measurePoint;
    }
    public void loadFromFile(File filepath){
        Log.d(LogTAG,"loading from file: "+filepath.getAbsolutePath());
        JsonMeasuresReader reader = new JsonMeasuresReader();
        MeasureBundle _measureBundle = reader.run(filepath);
       // ArrayList<MeasurePoint> measures =
        this.getSectorManager().loadAllMeasures( _measureBundle.getMeasures());

    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void OnMessage(SaveMeasuresEvent event){
        saveToFile(new File(event.fullpath),event.plan_name);
    }
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void OnMessage(PerformWifiScanEvent event){
        Log.d(LogTAG,"PerformWifiScanEvent received");
        this.scan();
    }

    public void saveToFile(File filepath,String planName){


        MeasureBundle measureBundle = new MeasureBundle(planName);
        Log.d(LogTAG,"rewriting ");
        //FIXME: WHAT CAN GO WRONG?!
        ArrayList<MeasurePoint> _measuresFromSectors = this.sectorManager.getAllMeasures();
       // measureBundle.setMeasures(this.measurePointArrayList);
        JsonMeasuresWriter writer = new JsonMeasuresWriter(measureBundle);
        writer.run();
        Log.d(LogTAG,"saving "+filepath);
    }
    /*
    public int getMeasuresCount(){
        return this.measurePointArrayList.size();
    }
    */
    public void saveToFile(MeasureBundle measureBundle)
    {
        ArrayList<MeasurePoint> _measuresFromSectors = this.sectorManager.getAllMeasures();
        measureBundle.setMeasures(_measuresFromSectors);
        //measureBundle.setLastChanged();
        JsonMeasuresWriter writer = new JsonMeasuresWriter(measureBundle);
        writer.run();
        Log.d(LogTAG,"saved "+measureBundle.getFilepath());
    }
    /*
    public void clear()
    {
        if(measurePointArrayList != null){
            this.measurePointArrayList.clear();
        }
        else {
            measurePointArrayList = new ArrayList<MeasurePoint>();
        }
    }
*/
    /*
    public ArrayList<MeasurePoint> getMeasurePointArrayList() {
        return measurePointArrayList;
    }
    */
    /*
    public int getSize(){
        return measurePointArrayList.size();
    }
*/
    public SectorManager getSectorManager() {
        return sectorManager;
    }

    public AutoScanManager getAutoScanManager() {
        return autoScanManager;
    }

    /*
    public void invokeScanAsync(){
        EventBus.getDefault().post(new PerformWifiScanEvent());
    }
*/

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void OnMessage(AutoScanCompletedEvent event){
        Log.d(LogTAG,"AutoScanCompletedEvent received");
        ArrayList<MeasurePoint> measures =  this.autoScanManager.getMeasurePoints();
        Sector currentSector = this.sectorManager.getCurrentSector();
        for( MeasurePoint measurePoint : measures ){
           // measurePoint.sector = this.getSectorManager().getCurrentSector().getCoordinates();
            currentSector.insertMeasurePoint(measurePoint);
        //    measurePointArrayList.add(measurePoint);
        }

        EventBus.getDefault().post(new RefreshStatisticsEvent());

    }

}
