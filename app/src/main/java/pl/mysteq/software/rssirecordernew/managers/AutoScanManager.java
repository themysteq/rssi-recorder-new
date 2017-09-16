package pl.mysteq.software.rssirecordernew.managers;

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
import pl.mysteq.software.rssirecordernew.structures.MeasurePoint;

/**
 * Created by mysteq on 2017-09-02.
 */

public class AutoScanManager {
    private  int MAX_MEASURES_PER_DIRECTION = 10;
    private  static final  int DELAY = 0;
    private  static final  int PERIOD = 6000;
    private static final String LogTAG = "AutoScanManager";
    private int perDirectionMeasuresCounter = 0;
    private boolean useExternalRotation = false;
   // private int allMeasuresCounter = 0;
   // private boolean prepared = false;
    private boolean running = false;
    //private TimerTask timerTask = null;
    private Timer timer = null;
    private float externalRotation = 0;
    private float externalOffset = 0;
    private boolean slow_scan = false;
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
        if (slow_scan) {
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Log.d(LogTAG, "new PerformWifiScanEvent");
                    EventBus.getDefault().post(new PerformWifiScanEvent());
                }
            }, DELAY, PERIOD);
        }
        else{
            EventBus.getDefault().post(new PerformWifiScanEvent());
        }


    }
    public void abort(){
        running = false;
        if(timer != null) timer.cancel();
        perDirectionMeasuresCounter = 0;
    }
    public void finish(){
        Log.d(LogTAG,"finish()");
        if(running) {
            running = false;
            if (timer != null) timer.cancel();
     /*   for (MeasurePoint _mp: measurePoints
             ) {
            //FIXME: ROTATION!!
            //TODO: ROTATION!!
            MyWifiScannerManager.getInstance().addMeasurePoint(_mp.scanResultArrayList,_mp,-1);
        }
*/

            perDirectionMeasuresCounter = 0;
            EventBus.getDefault().post(new AutoScanCompletedEvent());
        }else
        {
            throw new IllegalStateException("Finishing already finished!");
        }
    }
    public void pause(){}

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void scanCompleted(WifiScanCompletedEvent event){

        if(this.running) {
          ArrayList<CustomScanResult> results = MyWifiScannerManager.getInstance().getLastScanResult();
          //  Log.d(LogTAG, "--->");
         //   Log.d(LogTAG,results.toString());
          //  Log.d(LogTAG, "<---");
            float _final_rotation = 0;
            float _final_offset = 0;
            if (useExternalRotation){
                _final_rotation = externalRotation;
                _final_offset = externalOffset;
            }else
            {
                _final_rotation = ScanningActivity.finalRotation;
                _final_offset = ScanningActivity.calibrationOffset;
            }
            MeasurePoint measurePoint =
                    new MeasurePoint(results,
                    MyWifiScannerManager.getInstance().getSectorManager().getCurrentSectorPoint(),
                    Math.round(_final_rotation));

            measurePoint.setDirection(MeasurePoint.getDirection(_final_rotation));
            measurePoint.setOffset(Math.round(_final_offset));
            measurePoints.add(measurePoint);
            perDirectionMeasuresCounter++;
            EventBus.getDefault().post(new SubmitAutoScanEvent(perDirectionMeasuresCounter));

            Log.d(LogTAG, String.format("scanCompleted received: %d", perDirectionMeasuresCounter));
            if(! slow_scan) {
                EventBus.getDefault().post(new PerformWifiScanEvent());
            }
        }
        else{
            Log.d(LogTAG, "scanCompleted received but AutoScanManager not running");
        }
        //FIXME: race condition!
        if(perDirectionMeasuresCounter >= MAX_MEASURES_PER_DIRECTION){
            this.finish();
            // EventBus.getDefault().post(new AutoScanCompletedEvent());
        }

    }

    public boolean isRunning() {
        return running;
    }

    public ArrayList<MeasurePoint> getMeasurePoints() {
        return measurePoints;
    }

    public void setSlow_scan(boolean slow_scan) {
        this.slow_scan = slow_scan;
    }

    public void setMAX_MEASURES_PER_DIRECTION(int MAX_MEASURES_PER_DIRECTION) {
        this.MAX_MEASURES_PER_DIRECTION = MAX_MEASURES_PER_DIRECTION;
    }

    public void setExternalParams(float externalRotation, float externalOffset) {
        this.externalRotation = externalRotation;
        this.externalOffset = externalOffset;
    }

    public void setUseExternalRotation(boolean useExternalRotation) {
        this.useExternalRotation = useExternalRotation;
    }

    public void setExternalOffset(float externalOffset) {
        this.externalOffset = externalOffset;
    }

    public void setExternalRotation(float externalRotation) {
        this.externalRotation = externalRotation;
    }
}
