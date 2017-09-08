package pl.mysteq.software.rssirecordernew.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.os.CountDownTimer;
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
import pl.mysteq.software.rssirecordernew.R;
import pl.mysteq.software.rssirecordernew.events.ReloadBundlesEvent;
import pl.mysteq.software.rssirecordernew.events.SubmitAutoScanEvent;
import pl.mysteq.software.rssirecordernew.events.WifiScanCompletedEvent;
import pl.mysteq.software.rssirecordernew.managers.AutoScanManager;
import pl.mysteq.software.rssirecordernew.managers.ImageManipulationManager;
import pl.mysteq.software.rssirecordernew.managers.MyWifiScannerManager;
import pl.mysteq.software.rssirecordernew.managers.PlansFileManager;
import pl.mysteq.software.rssirecordernew.structures.CustomScanResult;
import pl.mysteq.software.rssirecordernew.structures.MeasureBundle;
import pl.mysteq.software.rssirecordernew.structures.MeasurePoint;
import pl.mysteq.software.rssirecordernew.structures.PlanBundle;

public class ScanningActivity extends Activity implements SensorEventListener {


    //private variables
    public static final String LogTAG = "ScanningActivity";
    ImageManipulationManager markupsImageManipulationManager = null;
    ImageManipulationManager buildingPlanManipulationManager = null;
    AutoScanManager autoScanManager = null;
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
    String measureUUID = null;
    String measureName = null;
    MeasureBundle measureBundle = null;
    ArrayList<CustomScanResult> customScanResults;

    int currentMeasuresCounter = 0;
    int perDirectionMeasures = 0;

    float calibrationOffset = 0;
    float calbratedRotation = 0;
    float finalRotation = 0;

    float fixedRotation = 0;
    boolean lockedRotation = false;


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
    ProgressDialog progressDialog = null;
    AlertDialog aboutCalibrationDialog = null;
    CalibratorDialog calibratorDialog = null;

    @BindView(R.id.counterSummaryTextView) TextView counterTextView;
    @BindView(R.id.rightButton) Button startAutoMeasureButton;
    @BindView(R.id.selectedSectorTextView) TextView selectedSectorTextView;
    @BindView(R.id.directionImageButton)ImageButton directionImageButtton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanning);
        ButterKnife.bind(this);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        autoScanManager = new AutoScanManager();

        //bindings



        mostRightButton.setEnabled(false);

        scannerManagerInstance.loadFromFile(new File(PlansFileManager.getInstance().getAppExternalMeasuresFolder(),measureFullPath));
        ArrayList<MeasurePoint> measurePointArrayList = scannerManagerInstance.getMeasurePointArrayList();
        if (measurePointArrayList.size() > 0) {
            for (MeasurePoint measurePoint : measurePointArrayList)
            {
                markupsImageManipulationManager.drawPoint(measurePoint);

            }
            markupMeasuresImageView.setImageBitmap(markupsImageManipulationManager.getBitmap());
            Log.d(LogTAG,"Drawing points done");
        }
        else
        {
            Log.i(LogTAG,"Empty measure file when loading");
        }
        progressDialog.dismiss();
        aboutCalibrationDialog.show();
        counterTextView.setText(String.format("Measures. Current:%d Total:%d",  currentMeasuresCounter, scannerManagerInstance.getMeasuresCount()));


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
                    for (CustomScanResult result : new ArrayList<CustomScanResult>(customScanResults)) {
                        Log.i(LogTAG, String.format("ssid: %s, bssid: %s, rssi: %d", result.SSID, result.BSSID, result.level));
                    }

                    Point pointOnView = getRelativePosition(markupMeasuresImageView.getRootView().findViewById(R.id.frameLayoutWithImages), e);
                    //Log.d(LogTAG, String.format("markup onSingleTapConfirmed. X: %d, Y: %d", pointOnView.x, pointOnView.y));
                    //gdzie tak na prawde klikłeś w obrazek

                    Point pointOnImage = ImageManipulationManager.calculatePointOnImage(markupMeasuresImageView, pointOnView);

                    //Log.d(LogTAG, String.format("SectorX: %d SectorY: %d", sector_x,sector_y ));
                    // px = pointOnImage.x;
                    // py = pointOnImage.y;
                    Log.d(LogTAG, String.format("On image: X: %d Y: %d, rotation: %f, px: %f, py: %f", pointOnImage.x, pointOnImage.y, degressWithOffset, px, py));


                    if ( ! autoScanManager.isRunning()){
                        scannerManagerInstance.addMeasurePoint(customScanResults, pointOnImage, Math.round((degrees + calibrationOffset) % 360.f));
                    markupsImageManipulationManager.drawPoint(pointOnImage);
                    markupMeasuresImageView.setImageBitmap(markupsImageManipulationManager.getBitmap());


                    //Update counters and display them
                    currentMeasuresCounter++;
                    counterTextView.setText(String.format("Measures. Current:%d Total:%d", currentMeasuresCounter, scannerManagerInstance.getMeasuresCount()));
                }
                else {
                        Toast.makeText(getApplicationContext(),"Autoscanning is running!",Toast.LENGTH_SHORT).show();
                    }


                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    //

                    Point pointOnView = getRelativePosition(markupMeasuresImageView.getRootView().findViewById(R.id.frameLayoutWithImages),e );
                    Point pointOnImage = ImageManipulationManager.calculatePointOnImage(markupMeasuresImageView,pointOnView);
                    Log.d(LogTAG, String.format("Long press: x: %d, y: %d", pointOnImage.x,pointOnImage.y));
                    sectorSelect(pointOnImage);



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

        Log.d(LogTAG,"scannerManagerInstance.saveToFile : "+measureBundle.getFilepath());
        //scannerManagerInstance.saveToFile(new File(measureFullPath),planName);
        //measureBundle.setMeasures();
        measureBundle.setLastChanged(new Date());
        scannerManagerInstance.saveToFile(measureBundle); // FIXME
        scannerManagerInstance.stop();
        EventBus.getDefault().post(new ReloadBundlesEvent());
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

        progressDialog = new ProgressDialog(this,ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();


        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setMessage("Remember about the calibration setup!");
        alertDialogBuilder.setTitle("Calibration?");
        alertDialogBuilder.setPositiveButton("Continue",null);

        alertDialogBuilder.setNegativeButton("Calibrate", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            //    Log.d(LogTAG, String.format("Calibrate: Clicked %d button", which ));
                    //showCalibrator();
            }
        });
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
        //customScanResults = (ArrayList<CustomScanResult>) scannerManagerInstance.getLastScanResult().clone();
        buildingPlanImageView.setEnabled(true);
        markupMeasuresImageView.setEnabled(true);
    }
    @Subscribe (threadMode = ThreadMode.MAIN)
    public void onMessage(SubmitAutoScanEvent event){
        counterTextView.setText(String.format("Measures. Current:%d Total:%d", event.counter, scannerManagerInstance.getMeasuresCount()));
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


        //offsetTextView.setText(String.format("%.2f",lockedDegree));
        degreesTextView.setText(String.format("%.2f",(degrees+calibrationOffset)%360.f));
        if (lockedRotation){
            float _fixedDegrees = MeasurePoint.rotationStickTo(degrees+calibrationOffset);

            directionImageButtton.setRotation(fixedRotation);
            offsetTextView.setText(MeasurePoint.rotationToString(_fixedDegrees));
            finalRotation = fixedRotation;
        }
        else{
            //degreesTextView.setText(String.format("%.2f",(degrees+calibrationOffset)%360.f));
            directionImageButtton.setRotation(degrees+calibrationOffset);
            offsetTextView.setText(MeasurePoint.rotationToString(degrees+calibrationOffset));
            finalRotation = degrees+calibrationOffset;
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

    @OnClick(R.id.rightButton) void startAutoMeasure()
    {
       Log.d(LogTAG,"startAutoMeasure onClick()");
        if(autoScanManager.isPrepared()) {
            perDirectionMeasures = 0;
            //startAutoMeasureButton.setText("Scanning...");
            Toast.makeText(getApplicationContext(),"Scanning...",Toast.LENGTH_LONG).show();
            autoScanManager.start();

        }
        else {
            Toast.makeText(getApplicationContext(),"Long press first on sector",Toast.LENGTH_SHORT).show();
        }
    }
    public void sectorSelect(Point pointOnImage)
    {
        autoScanManager.setHookPoint(pointOnImage);
        Point selectedSector = PlanBundle.getSectorFromPointOnImage(pointOnImage);
        markupsImageManipulationManager.setCurrentSector(selectedSector);
        ArrayList<MeasurePoint> perSectorMeasurePoints = new ArrayList<>();

        for (MeasurePoint _measurePoint: autoScanManager.getMeasurePoints()
             ) {
            if (_measurePoint.sector.equals(selectedSector)){
                perSectorMeasurePoints.add(_measurePoint);
            }
        }


        selectedSectorTextView.setText(String.format("X%dY%d : %d", selectedSector.x,selectedSector.y ,perSectorMeasurePoints.size()));
        markupMeasuresImageView.setImageBitmap(markupsImageManipulationManager.getWithSectorBitmap());
    }

    @OnClick(R.id.directionImageButton) void changeScanDirection() {
        fixedRotation = (fixedRotation+90.f)%360.f;
        directionImageButtton.setRotation(fixedRotation);
        if (!lockedRotation)
        {
            Log.d(LogTAG, String.format("old calibration offset = %.0f", calibrationOffset));
            calibrationOffset = (fixedRotation - degrees)%360.f;
            Log.d(LogTAG, String.format("new calibration offset = %.0f", calibrationOffset));
        }
        Log.d(LogTAG, String.format("changeDirection(): %.1f", fixedRotation));

    }
    @OnLongClick(R.id.directionImageButton) boolean lockScanDirection() {
        Log.d(LogTAG,"lockDirection()");
        lockedRotation = ! lockedRotation;
        return true; //long click is not simple click
    }


}
