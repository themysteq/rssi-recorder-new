package pl.mysteq.software.rssirecordernew;


import android.net.wifi.ScanResult;
import android.util.Log;

import org.apache.tools.ant.taskdefs.Sleep;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import java.util.ArrayList;

import pl.mysteq.software.rssirecordernew.events.WifiScanCompletedEvent;
import pl.mysteq.software.rssirecordernew.managers.MyWifiScannerManager;
import pl.mysteq.software.rssirecordernew.structures.CustomScanResult;

import static org.junit.Assert.*;

/**
 * Created by mysteq on 2017-09-05.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class WIfiManagerTest {


    @Before
    public void setUp()
    {
        ShadowLog.stream = System.out;
        EventBus.getDefault().register(this);

    }
    @Before
    @Subscribe
    public void oops(WifiScanCompletedEvent event){
        Log.i("EventBusTest","WifiScanCompletedEvent received");
    }

    @Test
    public void creationTest()
    {
        MyWifiScannerManager scannerManager = MyWifiScannerManager.getInstance().init(RuntimeEnvironment.application);
        assertNotNull(scannerManager);
    }

    @Test
    public void singletonTest(){
        MyWifiScannerManager scannerManager1 = MyWifiScannerManager.getInstance().init(RuntimeEnvironment.application);
        MyWifiScannerManager scannerManager2 = MyWifiScannerManager.getInstance();

        assertEquals(scannerManager1,scannerManager2);
    }

    @Test
    public void reinitializingTest_shouldnotreinitialize(){
        MyWifiScannerManager scannerManager1 = MyWifiScannerManager.getInstance().init(RuntimeEnvironment.application);
        MyWifiScannerManager scannerManager2 = MyWifiScannerManager.getInstance().init(RuntimeEnvironment.application);
        assertEquals(scannerManager1,scannerManager2);

    }

    @Test
    public void scanTest(){
        EventBus.getDefault().register(this);
        MyWifiScannerManager scannerManager1 = MyWifiScannerManager.getInstance().init(RuntimeEnvironment.application);
        scannerManager1.scan();
        try {
            Thread.sleep(4000);
            ArrayList<CustomScanResult> scanResults = scannerManager1.getLastScanResult();
            Log.d("scanTest",scanResults.toString());
            assertNotNull(scanResults);

        }catch (InterruptedException e){

        }

    }







}
