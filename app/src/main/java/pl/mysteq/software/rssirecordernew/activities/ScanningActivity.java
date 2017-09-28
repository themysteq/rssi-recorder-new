package pl.mysteq.software.rssirecordernew.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.OnTouch;
import pl.mysteq.software.rssirecordernew.R;
import pl.mysteq.software.rssirecordernew.events.RefreshStatisticsEvent;
import pl.mysteq.software.rssirecordernew.events.ReloadBundlesEvent;
import pl.mysteq.software.rssirecordernew.events.SubmitAutoScanEvent;
import pl.mysteq.software.rssirecordernew.events.WifiScanCompletedEvent;
import pl.mysteq.software.rssirecordernew.extendables.SectorPoint;
import pl.mysteq.software.rssirecordernew.managers.ImageManipulationManager;
import pl.mysteq.software.rssirecordernew.managers.MyWifiScannerManager;
import pl.mysteq.software.rssirecordernew.managers.PlansFileManager;
import pl.mysteq.software.rssirecordernew.structures.CustomScanResult;
import pl.mysteq.software.rssirecordernew.structures.MeasureBundle;
import pl.mysteq.software.rssirecordernew.structures.MeasurePoint;
import pl.mysteq.software.rssirecordernew.structures.PlanBundle;
import pl.mysteq.software.rssirecordernew.structures.Sector;

import static pl.mysteq.software.rssirecordernew.managers.PlansFileManager.SHAREDPREF;

public class ScanningActivity extends Activity implements SensorEventListener {


    //private variables
    public static final String LogTAG = "ScanningActivity";
    ImageManipulationManager markupsImageManipulationManager = null;
    ImageManipulationManager buildingPlanManipulationManager = null;
    //AutoScanManager autoScanManager = null;
    private IntentFilter filter = new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    private BroadcastReceiver broadcast = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LogTAG,"onReceive called, WifiScanCompletedEvent");
            EventBus.getDefault().post(new WifiScanCompletedEvent());
            //scannerManagerInstance.scanDone();
            //unregisterReceiver(this);
            //Log.d(LogTAG,intent.getDataString());
        }

    };
    SharedPreferences sharedPreferences = null;
    //public static final String SHAREDPREF_SCANNING = "rssi_recorder_scanning_prefs";
    public static final String OFFSET_VALUE_KEY = "offset_value";
    //float[] mGravity;
    //float[] mGeomagnetic;
    static final float alpha = 0.5f;
    //float azimut;
    public static float degrees;
    //float lockedDegree = 0;
    float px,py;
    //float offset = 0;
    private int mAzimuth = 0; // degree

    float[] orientation = new float[3];
    float[] rMat = new float[9];

    Bitmap buildingBitmap = null;
    Bitmap measuresBitmap = null;
    MyWifiScannerManager scannerManagerInstance = null;
    PlansFileManager plansFileManager = null;
    //SectorManager sectorManager = null;
    String planName = null;
    String measureUUID = null;
    String measureName = null;
    MeasureBundle measureBundle = null;
    ArrayList<CustomScanResult> customScanResults;

    Bitmap sourceBitmap = null;

    int currentMeasuresCounter = 0;
    int perDirectionMeasures = 0;

    public static float calibrationOffset = 0;
   // float calbratedRotation = 0;
    public static float finalRotation = 0;

    float fixedRotation = 0;
    //oolean lockedRotation = false;


    SensorManager mSensorManager ;
    Sensor accelerometer;
    Sensor magnetometer;
    Sensor rotatiometer;

    Canvas measuresCanvas = null;
    String measureFullPath = null;

    ProgressDialog progressDialog = null;
    AlertDialog aboutCalibrationDialog = null;


    @BindView(R.id.counterSummaryTextView) TextView counterTextView;
    @BindView(R.id.rightButton) Button startAutoMeasureButton;
    @BindView(R.id.selectedSectorTextView) TextView selectedSectorTextView;
    @BindView(R.id.directionImageButton)ImageButton directionImageButtton;
    @BindView(R.id.zoomSeekBar) SeekBar zoomBar;
    @BindView(R.id.leftButton) Button scanButton;
    @BindView(R.id.markupMeasuresImageView) ImageView markupMeasuresImageView;
    @BindView(R.id.buildingPlanImageView) ImageView buildingPlanImageView;
    @BindView(R.id.mostRightButton) Button mostRightButton;
    @BindView(R.id.degreesTextView) TextView degreesTextView;
    @BindView(R.id.directionTextView) TextView offsetTextView;

    @Override
    protected void onDestroy() {
        Log.d(LogTAG,"RUNTIME onDestroy()");
        super.onDestroy();
        markupMeasuresImageView.setImageBitmap(null);
        buildingPlanImageView.setImageBitmap(null);
        buildingPlanManipulationManager.recycle();
        markupsImageManipulationManager.recycle();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LogTAG,"RUNTIME onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);
        ButterKnife.bind(this);
        initAllElements();
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        rotatiometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mostRightButton.setEnabled(false);


        progressDialog.dismiss();
        aboutCalibrationDialog.show();
        counterTextView.setText(String.format("Measures. Current:%d Total:%d",  currentMeasuresCounter, scannerManagerInstance.getSectorManager().getMeasuresCounter()));

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
                    Log.v(LogTAG, "markup onScroll");

                    markupMeasuresImageView.scrollBy((int) distanceX, (int) distanceY);
                    return true;
                }
                /*
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    Log.d(LogTAG,"single tap - scan");
                    scannerManagerInstance.scan();
                    Point pointOnView = getRelativePosition(markupMeasuresImageView.getRootView().findViewById(R.id.frameLayoutWithImages),e );
                    Point pointOnImage = ImageManipulationManager.calculatePointOnImage(markupMeasuresImageView,pointOnView);
                    px = (buildingPlanImageView.getScrollX()+buildingPlanImageView.getWidth()/(2*buildingPlanImageView.getScaleX()));
                    py = (buildingPlanImageView.getScrollY()+buildingPlanImageView.getHeight())/(2*buildingPlanImageView.getScaleY());

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
                    for (CustomScanResult result : new ArrayList<CustomScanResult>(customScanResults)) {
                        Log.i(LogTAG, String.format("ssid: %s, bssid: %s, rssi: %d", result.SSID, result.BSSID, result.level));
                    }

                    Point pointOnView = getRelativePosition(markupMeasuresImageView.getRootView().findViewById(R.id.frameLayoutWithImages), e);
                    //gdzie tak na prawde klikłeś w obrazek

                    Point pointOnImage = ImageManipulationManager.calculatePointOnImage(markupMeasuresImageView, pointOnView);


                    Log.d(LogTAG, String.format("On image: X: %d Y: %d, px: %f, py: %f", pointOnImage.x, pointOnImage.y, px, py));



                    if ( ! scannerManagerInstance.getAutoScanManager().isRunning()){
                        scannerManagerInstance.addMeasurePoint(customScanResults, pointOnImage, Math.round(finalRotation));
                    markupsImageManipulationManager.drawPoint(pointOnImage);
                    markupMeasuresImageView.setImageBitmap(markupsImageManipulationManager.getBitmap());
                    //Update counters and display them
                    currentMeasuresCounter++;
                    counterTextView.setText(String.format("Measures. Current:%d Total:%d", currentMeasuresCounter, scannerManagerInstance.getSectorManager().getMeasuresCounter()));
                }
                else {
                        Toast.makeText(getApplicationContext(),"Autoscanning is running!",Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                */
                @Override
                public void onLongPress(MotionEvent e) {
                    Point pointOnView = getRelativePosition(markupMeasuresImageView.getRootView().findViewById(R.id.frameLayoutWithImages),e );
                    Point pointOnImage = ImageManipulationManager.calculatePointOnImage(markupMeasuresImageView,pointOnView);
                    Log.d(LogTAG, String.format("Long press: x: %d, y: %d", pointOnImage.x,pointOnImage.y));
                    selectSector(pointOnImage);
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
                    Log.v(LogTAG, "buildingplan onScroll");
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
        Log.d(LogTAG,"RUNTIME onStart()");
        super.onStart();
        try {
            EventBus.getDefault().register(this);
        }
        catch (org.greenrobot.eventbus.EventBusException ex){
            Log.w(LogTAG,"OOoops. "+ex.getMessage());
        }
    }
    @Override
    protected void onResume()
    {
        Log.d(LogTAG,"RUNTIME onResume()");
        super.onResume();
        mSensorManager.registerListener(this, rotatiometer, SensorManager.SENSOR_DELAY_FASTEST);
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
        Log.d(LogTAG,"RUNTIME onPause()");
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onStop()
    {
        Log.d(LogTAG,"RUNTIME onStop()");
      //  EventBus.getDefault().unregister(this);
        try {
            unregisterReceiver(broadcast);
        }
        catch (IllegalArgumentException ex){
            Log.w(LogTAG,"BroadcastReceiver already unregistered");
        }
        Log.d(LogTAG,"scannerManagerInstance.saveToFile : "+measureBundle.getFilepath());
        measureBundle.setLastChanged(new Date());
        scannerManagerInstance.saveToFile(measureBundle); // FIXME
        scannerManagerInstance.stop();
        EventBus.getDefault().post(new ReloadBundlesEvent());
        Log.d(LogTAG,"calling onStop");
        sourceBitmap.recycle();
        super.onStop();
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        Log.d(LogTAG,"RUNTIME onContentChanged()");
        sharedPreferences = getSharedPreferences(SHAREDPREF,MODE_PRIVATE);
        Log.d(LogTAG,"Getting shared preferences");
        calibrationOffset = sharedPreferences.getInt(OFFSET_VALUE_KEY,0);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setMessage("Remember about the calibration setup!");
        alertDialogBuilder.setTitle("See settings for calibration. Current offset: "+Integer.toString(Math.round(calibrationOffset)));
        alertDialogBuilder.setPositiveButton("Continue",null);
        progressDialog = new ProgressDialog(this,ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        aboutCalibrationDialog = alertDialogBuilder.create();
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
    public void onMessage(WifiScanCompletedEvent event){
        buildingPlanImageView.setEnabled(true);
        markupMeasuresImageView.setEnabled(true);
    }
    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onMessage(SubmitAutoScanEvent event){
        counterTextView.setText(String.format("Measures. Current:%d Total submitted:%d", event.counter, scannerManagerInstance.getSectorManager().getMeasuresCounter()));
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        if( event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR ){
            // calculate th rotation matrix
            SensorManager.getRotationMatrixFromVector( rMat, event.values );
            // get the azimuth value (orientation[0]) in degree
            mAzimuth = (int) ( Math.toDegrees( SensorManager.getOrientation( rMat, orientation )[0] ) + 360 ) % 360;
                degrees = (360 + mAzimuth)%360;
        }
        finalRotation = degrees+calibrationOffset;
        //directionImageButtton.setRotation(finalRotation);
        directionImageButtton.setRotation(finalRotation);
        degreesTextView.setText(Float.toString(finalRotation));
        offsetTextView.setText(MeasurePoint.rotationToString(finalRotation));
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @OnClick(R.id.rightButton) void startAutoMeasure()
    {
       Log.d(LogTAG,"startAutoMeasure onClick()");

        if(scannerManagerInstance.getAutoScanManager().isPrepared()) {
            perDirectionMeasures = 0;
            //startAutoMeasureButton.setText("Scanning...");
            Toast.makeText(getApplicationContext(),"Scanning...",Toast.LENGTH_LONG).show();
            scannerManagerInstance.getAutoScanManager().start();
        } else {
            Toast.makeText(getApplicationContext(),"Long press first on sector",Toast.LENGTH_SHORT).show();
        }
    }

    public void selectSector(Point pointOnImage) {

        SectorPoint selectedSector = PlanBundle.getSectorFromPointOnImage(pointOnImage);
        scannerManagerInstance.getSectorManager().setCurrentSectorPoint(selectedSector);
        scannerManagerInstance.getLastScanResult();
        markupMeasuresImageView.setImageBitmap(markupsImageManipulationManager.getWithSectorBitmap());
        EventBus.getDefault().post(new RefreshStatisticsEvent());
    }


    @OnClick(R.id.directionImageButton) void changeScanDirection() {
        fixedRotation = (fixedRotation+90.f)%360.f;
        directionImageButtton.setRotation(fixedRotation);
        Log.d(LogTAG, String.format("old calibration offset = %.0f", calibrationOffset));
        calibrationOffset = (fixedRotation - degrees)%360.f;
        Log.d(LogTAG, String.format("new calibration offset = %.0f", calibrationOffset));
    }
    @OnLongClick(R.id.directionImageButton) boolean saveOffset() {
        Log.d(LogTAG,"saveOffset()");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(OFFSET_VALUE_KEY,Math.round(calibrationOffset));
        editor.apply();
        return true; //long click is not simple click
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnMessage(RefreshStatisticsEvent event){
        Log.v(LogTAG,"RefreshStatisticsEvent received");
        Sector _sector = scannerManagerInstance.getSectorManager().getCurrentSector();
        counterTextView.setText(String.format("Measures. Current:%d Total submitted:%d",
                //_sector.size(),
                0,
                scannerManagerInstance.getSectorManager().getMeasuresCounter() ));
        selectedSectorTextView.setText(String.format("X%d_Y%d: %d", _sector.getCoordinates().x,_sector.getCoordinates().y, _sector.size()));
    }
    private void initAllElements(){
        plansFileManager = PlansFileManager.getInstance();
        planName = getIntent().getStringExtra("PLAN_NAME");
        measureFullPath = getIntent().getStringExtra("MEASURE_FULLPATH");
        measureUUID = getIntent().getStringExtra("MEASURE_UUID");
        measureName = getIntent().getStringExtra("MEASURE_NAME");
        Log.d(LogTAG,"Plan name: "+planName);
        measureBundle = new MeasureBundle(planName);
        measureBundle.setUuid(measureUUID);
        measureBundle.setFilepath(measureFullPath);
        PlanBundle planBundle = plansFileManager.getBundleByName(planName);
        File planfile = plansFileManager.getBundlePlanFile(planBundle);
        Log.d(LogTAG,"plan filepath: "+planfile.getAbsolutePath());
        markupsImageManipulationManager = new ImageManipulationManager();
        buildingPlanManipulationManager = new ImageManipulationManager();
        sourceBitmap = BitmapFactory.decodeFile(planfile.getAbsolutePath());
        buildingPlanManipulationManager.setBitmap(sourceBitmap);
        buildingBitmap = buildingPlanManipulationManager.getBitmap();
        px = (buildingPlanImageView.getScrollX()+buildingPlanImageView.getWidth()/(2*buildingPlanImageView.getScaleX()));
        py = (buildingPlanImageView.getScrollY()+buildingPlanImageView.getHeight())/(2*buildingPlanImageView.getScaleY());
        markupsImageManipulationManager.setBlankBitmap(buildingPlanManipulationManager.getBitmap());
        buildingPlanImageView.setEnabled(false);
        markupMeasuresImageView.setEnabled(false);
        scannerManagerInstance = MyWifiScannerManager.getInstance().init(getApplicationContext());
        markupMeasuresImageView.setImageBitmap(markupsImageManipulationManager.getBitmap());
        Log.d(LogTAG,"Redrawing sectors");
        //buildingPlanManipulationManager.redrawSectors();
        scannerManagerInstance.loadFromFile(new File(PlansFileManager.getInstance().getAppExternalMeasuresFolder(),measureFullPath));
        buildingPlanManipulationManager.redrawSectors();
        buildingPlanImageView.setImageBitmap(buildingPlanManipulationManager.getBitmap());
        scannerManagerInstance.scan();


    }
}
