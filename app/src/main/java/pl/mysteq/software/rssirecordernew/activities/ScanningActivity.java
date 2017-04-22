package pl.mysteq.software.rssirecordernew.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;

import pl.mysteq.software.rssirecordernew.R;
import pl.mysteq.software.rssirecordernew.events.WifiScanCompleted;
import pl.mysteq.software.rssirecordernew.managers.ImageManipulationManager;
import pl.mysteq.software.rssirecordernew.managers.MyWifiScannerManager;
import pl.mysteq.software.rssirecordernew.managers.PlansFileManager;
import pl.mysteq.software.rssirecordernew.structures.CustomScanResult;
import pl.mysteq.software.rssirecordernew.structures.PlanBundle;

public class ScanningActivity extends Activity {


    //private variables
    public static final String LogTAG = "ScanningActivity";
    private IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    private BroadcastReceiver broadcast = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LogTAG,"onReceive called");
            EventBus.getDefault().post(new WifiScanCompleted());
            //scannerManagerInstance.scanDone();
            //unregisterReceiver(this);
            //Log.d(LogTAG,intent.getDataString());
        }

    };
    Bitmap buildingBitmap = null;
    Bitmap measuresBitmap = null;
    MyWifiScannerManager scannerManagerInstance = null;
    PlansFileManager plansFileManager = null;
    String planName = null;
    ArrayList<CustomScanResult> customScanResults;

    //widgets
    SeekBar zoomBar = null;
    ImageView markupMeasuresImageView = null;
    ImageView buildingPlanImageView = null;
    Button scanButton = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);

        //bindings





      /*  scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LogTAG,"scan button");
                scannerManagerInstance.scan();
                registerReceiver(broadcast,filter);
                //unregisterReceiver(broadcast);
            }
        });*/

        zoomBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int scale = progress+1;
                markupMeasuresImageView.setScaleX(scale);
                markupMeasuresImageView.setScaleY(scale);

                buildingPlanImageView.setScaleX(scale);
                buildingPlanImageView.setScaleY(scale);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        markupMeasuresImageView.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(getBaseContext(), new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    Log.d(LogTAG, "markup onScroll");

                    markupMeasuresImageView.scrollBy((int) distanceX, (int) distanceY);
                    return true;
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    Log.d(LogTAG,"single tap - scan");
                    scannerManagerInstance.scan();
                    return true;
                   // return super.onSingleTapConfirmed(e);
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {


                    //business logic

                    Log.d(LogTAG, "markup onDoubleTap");
                    scannerManagerInstance.scan();
                    customScanResults = scannerManagerInstance.getLastScanResult();
                    for (CustomScanResult result : customScanResults){
                        Log.i(LogTAG,String.format("ssid: %s, bssid: %s, rssi: %d",result.SSID,result.BSSID,result.level));
                    }

                    Point pointOnImageView = getRelativePosition(markupMeasuresImageView.getRootView().findViewById(R.id.frameLayoutWithImages),e );
                    Log.d(LogTAG, String.format("markup onSingleTapConfirmed. X: %d, Y: %d", pointOnImageView.x, pointOnImageView.y));
                    //gdzie tak na prawde klikłeś w obrazek
                    Point pointOnImage = ImageManipulationManager.calculatePointOnImage(markupMeasuresImageView,pointOnImageView);
                    Log.d(LogTAG, String.format("On image: X: %d Y: %d", pointOnImage.x,pointOnImage.y));

                    return true;
                }
            });


            @Override
            public boolean onTouch(View v, MotionEvent event) {

                buildingPlanImageView.dispatchTouchEvent(event);
                gestureDetector.onTouchEvent(event);
                return true;
            }


        });

        buildingPlanImageView.setOnTouchListener(new View.OnTouchListener() {
            private GestureDetector gestureDetector = new GestureDetector(getBaseContext(), new GestureDetector.SimpleOnGestureListener() {

                @Override
                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                    Log.d(LogTAG, "bauldingplan onScroll");
                    buildingPlanImageView.scrollBy((int) distanceX, (int) distanceY);
                    return true;
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    return super.onDoubleTap(e);
                }

                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    return super.onSingleTapConfirmed(e);
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(broadcast,filter);
    }

    @Override
    protected void onStop()
    {
      //  EventBus.getDefault().unregister(this);
        try {
            unregisterReceiver(broadcast);
        }
        catch (IllegalArgumentException ex){
            Log.w(LogTAG,"BroadcastReceiver already unregistered");
        }
        scannerManagerInstance.stop();
        super.onStop();

    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();

        zoomBar = (SeekBar) findViewById(R.id.zoomSeekBar);
        scanButton = (Button) findViewById(R.id.leftButton);
        markupMeasuresImageView = (ImageView) findViewById(R.id.markupMeasuresImageView);
        buildingPlanImageView = (ImageView) findViewById(R.id.buildingPlanImageView);

        Log.d(LogTAG,"Content changed");
        plansFileManager = PlansFileManager.getInstance();
        planName = getIntent().getStringExtra("PLAN_NAME");
        Log.d(LogTAG,"Plan name: "+planName);
        PlanBundle planBundle = plansFileManager.getBundleByName(planName);
        File planfile = plansFileManager.getBundlePlanFile(planBundle);
        Log.d(LogTAG,"plan filepath: "+planfile.getAbsolutePath());

        buildingBitmap = BitmapFactory.decodeFile(planfile.getAbsolutePath());
        buildingPlanImageView.setImageBitmap(buildingBitmap);
        measuresBitmap = Bitmap.createBitmap(buildingBitmap.getWidth(),buildingBitmap.getHeight(),Bitmap.Config.ARGB_8888);
        markupMeasuresImageView.setImageBitmap(measuresBitmap);
        buildingPlanImageView.setEnabled(false);
        markupMeasuresImageView.setEnabled(false);
        scannerManagerInstance = MyWifiScannerManager.getInstance().init(getApplicationContext());
        scannerManagerInstance.scan();
        //BitmapFactory.decodeFile(planfile.getAbsolutePath());
        //
        //buildingPlanImageView.setImageBitmap(BitmapFactory.decodeFile(planfile.getAbsolutePath()));


        //buildingPlanImageView.setImageDrawable();

    }

    protected Point getRelativePosition(View v, MotionEvent event) {
        int[] location = new int[2];
        v.getLocationOnScreen(location);
        float screenX = event.getRawX();
        float screenY = event.getRawY();
        float viewX = screenX - location[0];
        float viewY = screenY - location[1];
        return new Point((int) viewX, (int) viewY);
    }
    @Subscribe
    public void onMessage(WifiScanCompleted event){
        //customScanResults = (ArrayList<CustomScanResult>) scannerManagerInstance.getLastScanResult().clone();
        buildingPlanImageView.setEnabled(true);
        markupMeasuresImageView.setEnabled(true);
    }

}
