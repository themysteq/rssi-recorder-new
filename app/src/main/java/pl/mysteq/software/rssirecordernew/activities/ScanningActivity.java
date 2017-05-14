package pl.mysteq.software.rssirecordernew.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
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
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

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
    ImageManipulationManager markupsImageManipulationManager = null;
    ImageManipulationManager buildingPlanManipulationManager = null;
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
    static final float alpha = 0.5f;
    float azimut;
    float degrees;
    float degressWithOffset = 0;
    float lockedDegree = 0;
    float px,py;
    float offset = 0;
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
    String measureFullPath = null;

    TextView degreesTextView = null;
    TextView offsetTextView = null;

    boolean lockOrient = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        //bindings



        mostRightButton.setEnabled(false);
/*
        mostRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lockOrient )
                {
                    //offset = degrees;
                }
                lockOrient = ! lockOrient;
               // mostRightButton.setEnabled(!mostRightButton.isEnabled());
                Log.d(LogTAG, String.format("degrees with offset: %f | offset: %f", degressWithOffset,offset));
            }
        });
*/

        zoomBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int scale = progress+1;
                //markupMeasuresImageView.getImageMatrix()
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
                    Point pointOnView = getRelativePosition(markupMeasuresImageView.getRootView().findViewById(R.id.frameLayoutWithImages),e );
                    Point pointOnImage = ImageManipulationManager.calculatePointOnImage(markupMeasuresImageView,pointOnView);
                    px = (buildingPlanImageView.getScrollX()+buildingPlanImageView.getWidth()/(2*buildingPlanImageView.getScaleX()));
                    py = (buildingPlanImageView.getScrollY()+buildingPlanImageView.getHeight())/(2*buildingPlanImageView.getScaleY());
                    //Log.d(LogTAG, String.format("px: %d, py: %d", px,py));
                    //px = pointOnImage.x;
                    //py = pointOnImage.y;
                    Log.d(LogTAG, String.format("px: %f, py: %f", px,py));
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
                    //Log.d(LogTAG, String.format("markup onSingleTapConfirmed. X: %d, Y: %d", pointOnView.x, pointOnView.y));
                    //gdzie tak na prawde klikłeś w obrazek

                    Point pointOnImage = ImageManipulationManager.calculatePointOnImage(markupMeasuresImageView,pointOnView);
                   // px = pointOnImage.x;
                   // py = pointOnImage.y;
                    Log.d(LogTAG, String.format("On image: X: %d Y: %d, rotation: %f, px: %f, py: %f", pointOnImage.x,pointOnImage.y,degressWithOffset,px,py));


                    scannerManagerInstance.addMeasurePoint(customScanResults,pointOnImage,Math.round(degressWithOffset) );
                    markupsImageManipulationManager.drawPoint(pointOnImage);
                    markupMeasuresImageView.setImageBitmap(markupsImageManipulationManager.getBitmap());

                    //buildingPlanImageView.setImageBitmap(buildingPlanManipulationManager.getBitmap());
                    //markupMeasuresImageView.setImageBitmap(markupsImageManipulationManager.getBitmap());




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
        try {
            EventBus.getDefault().register(this);
        }
        catch (org.greenrobot.eventbus.EventBusException ex){
            Log.w(LogTAG,"OOoops. "+ex.getMessage());
        }
        //String planName = getIntent().getStringExtra("PLAN_NAME");
        //plansFileManager.generateNewMeasureFile()
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
        try {
            registerReceiver(broadcast,filter);
        }
        catch (org.greenrobot.eventbus.EventBusException ex){
            Log.w(LogTAG,"OOoops. "+ex.getMessage());
        }



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

        scannerManagerInstance.saveToFile(new File(measureFullPath),planName);
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
        measureFullPath = getIntent().getStringExtra("MEASURE_FULLPATH");
        Log.d(LogTAG,"Plan name: "+planName);
        PlanBundle planBundle = plansFileManager.getBundleByName(planName);
        File planfile = plansFileManager.getBundlePlanFile(planBundle);
        Log.d(LogTAG,"plan filepath: "+planfile.getAbsolutePath());
        markupsImageManipulationManager = new ImageManipulationManager();
        buildingPlanManipulationManager = new ImageManipulationManager();
        buildingPlanManipulationManager.setBitmap(BitmapFactory.decodeFile(planfile.getAbsolutePath()));
        buildingBitmap = buildingPlanManipulationManager.getBitmap();
        px = (buildingPlanImageView.getScrollX()+buildingPlanImageView.getWidth()/(2*buildingPlanImageView.getScaleX()));
        py = (buildingPlanImageView.getScrollY()+buildingPlanImageView.getHeight())/(2*buildingPlanImageView.getScaleY());
        //buildingPlanManipulationManager.setBitmap(buildingBitmap);
        //buildingPlanImageView.setImageBitmap(buildingPlanManipulationManager.getBitmap());
        //measuresBitmap = Bitmap.createBitmap(buildingBitmap.getWidth(),buildingBitmap.getHeight(),Bitmap.Config.ARGB_8888);
        markupsImageManipulationManager.setBlankBitmap(buildingPlanManipulationManager.getBitmap());

        buildingPlanImageView.setEnabled(false);
        markupMeasuresImageView.setEnabled(false);
        scannerManagerInstance = MyWifiScannerManager.getInstance().init(getApplicationContext());
        markupMeasuresImageView.setImageBitmap(markupsImageManipulationManager.getBitmap());
        buildingPlanImageView.setImageBitmap(buildingPlanManipulationManager.getBitmap());
        scannerManagerInstance.scan();
        mostRightButton = (Button) findViewById(R.id.mostRightButton);

        degreesTextView = (TextView) findViewById(R.id.degreesTextView);
        offsetTextView = (TextView) findViewById(R.id.offsetTextView);



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
                degrees = ((float)Math.toDegrees(azimut)+180.f);
                //degrees +=180.f;
                //
                lockedDegree = -degrees-offset;
                if( !lockOrient) {
                    //Log.d(LogTAG, String.format("azimuth: %f", degrees));
                    degressWithOffset = (-degrees-offset );
                    rotateBuildingView(degressWithOffset,px,py);

                }
                else{
                    offset = degressWithOffset + lockedDegree;
                }


            }


        }

        degreesTextView.setText(String.format("%.2f",degressWithOffset));
        offsetTextView.setText(String.format("%.2f",lockedDegree));

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
    public void rotateBuildingView(float _degreesWithOffset, float _px, float _py){
        //Log.d(LogTAG, String.format("Rotating: %f %f %f",degrees, _px,_py ));
        //markupsImageManipulationManager.rotate(_degrees,_px,_py);
        //buildingPlanManipulationManager.rotate(_degrees,_px,_py);
        Matrix rotateMatrix = new Matrix();
        //degressWithOffset = -_degrees+offset;
        //rotateMatrix.setTranslate(_px,_py);
        rotateMatrix.postRotate(_degreesWithOffset,_px,_py);

        //rotateMatrix.preTranslate(_px,_py);

        buildingPlanImageView.setImageMatrix(rotateMatrix);
        markupMeasuresImageView.setImageMatrix(rotateMatrix);
        //markupMeasuresImageView.setImageMatrix();
        //markupMeasuresImageView.invalidate();
        //buildingPlanImageView.invalidate();
        //markupMeasuresImageView.setImageBitmap(markupsImageManipulationManager.getBitmap());
        //buildingPlanImageView.setImageBitmap(buildingPlanManipulationManager.getBitmap());

    }

}
