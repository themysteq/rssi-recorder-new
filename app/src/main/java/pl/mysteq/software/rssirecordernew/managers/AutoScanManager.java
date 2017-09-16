package pl.mysteq.software.rssirecordernew.managers;

import android.graphics.Point;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import pl.mysteq.software.rssirecordernew.activities.ScanningActivity;
import pl.mysteq.software.rssirecordernew.events.AutoScanCompletedEvent;
import pl.mysteq.software.rssirecordernew.events.PerformWifiScanEvent;
import pl.mysteq.software.rssirecordernew.events.SubmitAutoScanEvent;
import pl.mysteq.software.rssirecordernew.events.WifiScanCompletedEvent;
import pl.mysteq.software.rssirecordernew.structures.CustomScanResult;
import pl.mysteq.software.rssirecordernew.managers.MyWifiScannerManager;
import pl.mysteq.software.rssirecordernew.structures.MeasurePoint;

/**
 * Created by mysteq on 2017-09-02.
 */

public class AutoScanManager {
    private static final int MAX_MEASURES_PER_DIRECTION = 11;
    private  static final  int DELAY = 0;
    private  static final  int PERIOD = 4000;
    private static final String LogTAG = "AutoScanManager";
    private int perDirectionMeasuresCounter = 0;
   // private int allMeasuresCounter = 0;
   // private boolean prepared = false;
    private boolean running = false;
    //private TimerTask timerTask = null;
    private Timer timer = null;
   // private Point hookPoint = null;
   // private Point sector = null;
    private ArrayList<MeasurePoint> measurePoints = null;
    public AutoScanManager()
    {
        EventBus.getDefault().register(this);
        this.measurePoints = new ArrayList<>();
        Log.d(LogTAG,"creating");
       // timer = new Timer();
    }

    public boolean isPrepared() {
        if(MyWifiScannerManager.getInstance().getSectorManager().getCurrentSectorPoint() == null || this.running){
            Log.w(LogTAG,"is not prepared. Already running or smth");
            return false;
        }
        else return true;
    }

    public void start(){
        //FIXME: race condition!
        measurePoints.clear();
        perDirectionMeasuresCounter = 0;
        running = true;
        timer = new Timer();
        timer.schedule( new TimerTask() {
            @Override
            public void run() {Log.d(LogTAG,"new PerformWifiScanEvent");
              EventBus.getDefault().post(new PerformWifiScanEvent());

            }
        },DELAY,PERIOD);

    }
    public void stop(){
        running = false;
        if(timer != null) timer.cancel();
     /*   for (MeasurePoint _mp: measurePoints
             ) {
            //FIXME: ROTATION!!
            //TODO: ROTATION!!
            MyWifiScannerManager.getInstance().addMeasurePoint(_mp.scanResultArrayList,_mp,-1);
        }
*/
        EventBus.getDefault().post(new AutoScanCompletedEvent());
        perDirectionMeasuresCounter = 0;
    }
    public void pause(){}

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void scanCompleted(WifiScanCompletedEvent event){
        if(perDirectionMeasuresCounter >= MAX_MEASURES_PER_DIRECTION){
            this.stop();
           // EventBus.getDefault().post(new AutoScanCompletedEvent());


        }
        if(this.running) {
          ArrayList<CustomScanResult> results = MyWifiScannerManager.getInstance().getLastScanResult();
            Log.d(LogTAG, "--->");
            Log.d(LogTAG,results.toString());
            Log.d(LogTAG, "<---");
            MeasurePoint measurePoint = new MeasurePoint(results,
                    MyWifiScannerManager.getInstance().getSectorManager().getCurrentSectorPoint(),
                    Math.round(ScanningActivity.finalRotation));
            //measurePoint.sector = MyWifiScannerManager.getInstance().getSectorManager().getCurrentSectorPoint();
            measurePoints.add(measurePoint);
            perDirectionMeasuresCounter++;
            EventBus.getDefault().post(new SubmitAutoScanEvent(perDirectionMeasuresCounter));
            Log.d(LogTAG, String.format("scanCompleted received: %d", perDirectionMeasuresCounter));
        }
        else{
            Log.d(LogTAG, "scanCompleted received but AutoScanManager not running");
        }
        //FIXME: race condition!

    }

    public boolean isRunning() {
        return running;
    }

    public ArrayList<MeasurePoint> getMeasurePoints() {
        return measurePoints;
    }
}
