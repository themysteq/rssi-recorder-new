package pl.mysteq.software.rssirecordernew.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

public class ScanningActivity extends Activity implements SensorEventListener {


    //private variables
    public static final String LogTAG = "ScanningActivity";
    ImageManipulationManager imageManipulationManager = null;
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
    float[] mGravity;
    float[] mGeomagnetic;
    static final float alpha = 0.25f;
    float azimut;
    float degrees;
    Bitmap buildingBitmap = null;
    Bitmap measuresBitmap = null;
    MyWifiScannerManager scannerManagerInstance = null;
    PlansFileManager plansFileManager = null;
    String planName = null;
    ArrayList<CustomScanResult> customScanResults;

    SensorManager mSensorManager ;
    Sensor accelerometer;
    Sensor magnetometer;
    //widgets
    SeekBar zoomBar = null;
    ImageView markupMeasuresImageView = null;
    ImageView buildingPlanImageView = null;
    Button scanButton = null;
    Button mostRightButton = null;
    Canvas measuresCanvas = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        //bindings







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
                    for (CustomScanResult result :  new ArrayList<CustomScanResult>(customScanResults)){
                        Log.i(LogTAG,String.format("ssid: %s, bssid: %s, rssi: %d",result.SSID,result.BSSID,result.level));
                    }

                    Point pointOnView = getRelativePosition(markupMeasuresImageView.getRootView().findViewById(R.id.frameLayoutWithImages),e );
                    Log.d(LogTAG, String.format("markup onSingleTapConfirmed. X: %d, Y: %d", pointOnView.x, pointOnView.y));
                    //gdzie tak na prawde klikłeś w obrazek

                    Point pointOnImage = ImageManipulationManager.calculatePointOnImage(markupMeasuresImageView,pointOnView);
                    Log.d(LogTAG, String.format("On image: X: %d Y: %d", pointOnImage.x,pointOnImage.y));

                    scannerManagerInstance.addMeasurePoint(customScanResults,pointOnImage);
                    imageManipulationManager.drawPoint(pointOnImage);
                    markupMeasuresImageView.setImageBitmap(imageManipulationManager.getBitmap());
                    //markupMeasuresImageView.setImageBitmap(imageManipulationManager.getBitmap());




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
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        registerReceiver(broadcast,filter);


    }
    @Override
    protected void onPause()
    {
        super.onPause();
        mSensorManager.unregisterListener(this);
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
        Log.d(LogTAG,"calling onStop");
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
        imageManipulationManager = new ImageManipulationManager();

        buildingBitmap = BitmapFactory.decodeFile(planfile.getAbsolutePath());
        buildingPlanImageView.setImageBitmap(buildingBitmap);
        //measuresBitmap = Bitmap.createBitmap(buildingBitmap.getWidth(),buildingBitmap.getHeight(),Bitmap.Config.ARGB_8888);
        imageManipulationManager.setBlankBitmap(buildingBitmap);
        buildingPlanImageView.setEnabled(false);
        markupMeasuresImageView.setEnabled(false);
        scannerManagerInstance = MyWifiScannerManager.getInstance().init(getApplicationContext());
        markupMeasuresImageView.setImageBitmap(imageManipulationManager.getBitmap());
        scannerManagerInstance.scan();
        mostRightButton = (Button) findViewById(R.id.mostRightButton);
        //markupMeasuresImageView.setImageBitmap(measuresBitmap);
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


    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = lowPass(event.values.clone(),mGravity);
            //mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = lowPass(event.values.clone(),mGeomagnetic);
            //mGeomagnetic = event.values;
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                degrees = (float)Math.toDegrees(azimut);
                degrees +=180.f;
                Log.d(LogTAG, String.format("azimuth: %f", degrees));

            }
        }
        //mCustomDrawableView.invalidate();
    }
    protected float[] lowPass( float[] input, float[] output ) {

        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + alpha * (input[i] - output[i]);
        }
        return output;
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
