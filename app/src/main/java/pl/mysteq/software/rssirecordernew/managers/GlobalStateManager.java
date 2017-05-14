package pl.mysteq.software.rssirecordernew.managers;

import pl.mysteq.software.rssirecordernew.structures.MeasureBundle;

/**
 * Created by mysteq on 2017-05-01.
 */

public class GlobalStateManager {

    PlansFileManager plansFileManager = null;
    MyWifiScannerManager myWifiScannerManager = null;
    MeasureBundle currentMeasure = null;
    public static final int STATE_MAIN_MENU = 0;
    public static final int STATE_MEASURING = 1;
    //public static final int

    private static GlobalStateManager instance = null;

    private static synchronized GlobalStateManager getSync(){
        if(instance == null) instance = new GlobalStateManager();
        return instance;
    }

    public static GlobalStateManager getInstance(){
        if(instance == null) instance = getSync();
        return instance;
    }

    public void prepareApp(){
        plansFileManager = PlansFileManager.getInstance();
        myWifiScannerManager = MyWifiScannerManager.getInstance();
    }

    public void runApp()
    {

    }
    public void stopApp()
    {

    }

    public void setState(){

    }
}
