package pl.mysteq.software.rssirecordernew.managers;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import pl.mysteq.software.rssirecordernew.events.WifiScanCompleted;
import pl.mysteq.software.rssirecordernew.structures.CustomScanResult;
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

    public MyWifiScannerManager init(Context _context){
        this.context = _context.getApplicationContext();
        this.measurePointArrayList = new ArrayList<MeasurePoint>();
        wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY,"RSSI_RECORDER_WIFI_LOCK");
        wifiLock.acquire();
        EventBus.getDefault().register(this);
        Log.d(LogTAG,"Initialized");
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
            Log.d(LogTAG,"scanResult: "+scanResult.toString());
            lastScanResults.add(new CustomScanResult(scanResult));

        }
    }

    public ArrayList<CustomScanResult> getLastScanResult(){
        return lastScanResults;
    }


}
