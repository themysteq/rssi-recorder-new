package pl.mysteq.software.rssirecordernew.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import pl.mysteq.software.rssirecordernew.R;
import pl.mysteq.software.rssirecordernew.events.AutoScanCompletedEvent;
import pl.mysteq.software.rssirecordernew.events.PerformWifiScanEvent;
import pl.mysteq.software.rssirecordernew.events.SubmitAutoScanEvent;
import pl.mysteq.software.rssirecordernew.events.WifiScanCompletedEvent;
import pl.mysteq.software.rssirecordernew.extendables.SectorPoint;
import pl.mysteq.software.rssirecordernew.managers.MyWifiScannerManager;
import pl.mysteq.software.rssirecordernew.managers.PlansFileManager;
import pl.mysteq.software.rssirecordernew.managers.SynchronizerManager;
import pl.mysteq.software.rssirecordernew.structures.MeasureBundle;
import pl.mysteq.software.rssirecordernew.structures.MeasurePoint;
import pl.mysteq.software.rssirecordernew.structures.PlanBundle;

import static pl.mysteq.software.rssirecordernew.activities.ScanningActivity.LogTAG;
import static pl.mysteq.software.rssirecordernew.activities.ScanningActivity.OFFSET_VALUE_KEY;
import static pl.mysteq.software.rssirecordernew.activities.ScanningActivity.finalRotation;
import static pl.mysteq.software.rssirecordernew.managers.PlansFileManager.SHAREDPREF;

public class RecordPathActivity extends Activity implements SensorEventListener {

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
    Sensor rotatiometer;
    SensorManager mSensorManager ;
    SharedPreferences sharedPreferences;
    private int mAzimuth = 0; // degree
    private float degrees;
    private float finalRotation;
    private float calibrationOffset = 0;
    float[] orientation = new float[3];
    float[] rMat = new float[9];
    String finalRotationText;
    String orientationText;
    MeasureBundle measureBundle = null;
    MyWifiScannerManager scannerManagerInstance = null;
    boolean recording = false;
    private static final String LogTAG = "RecordPathActivity";

    ProgressDialog dialog = null;

    @BindView(R.id.startRecordButton) Button startRecordButton;
    @BindView(R.id.slowScanSwitch) Switch slowScanSwitch;
    @BindView(R.id.recordDirectionButton) ImageButton recordDirectionButton;
    @BindView(R.id.recordFirstTextView) TextView recordFirstTextView;
    @BindView(R.id.recordSecondTextView) TextView reccordSecondTextView;
    @BindView(R.id.recordLeftTextView) TextView recordLeftTextView;
    @BindView(R.id.recordRightTextView) TextView recordRightTextView;
    @BindView(R.id.recordSaveButton) Button recordSaveButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_path);
        ButterKnife.bind(this);

        prepareAll();
        EventBus.getDefault().register(this);

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if( sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR ){
            // calculate th rotation matrix
            SensorManager.getRotationMatrixFromVector( rMat, sensorEvent.values );
            // get the azimuth value (orientation[0]) in degree
            mAzimuth = (int) ( Math.toDegrees( SensorManager.getOrientation( rMat, orientation )[0] ) + 360 ) % 360;
            degrees = (360 + mAzimuth)%360;
        }
        finalRotation = degrees+calibrationOffset;
        //directionImageButtton.setRotation(finalRotation);
        recordDirectionButton.setRotation(finalRotation);

        finalRotationText = String.format("degree: %s", Float.toString(finalRotation));
        recordFirstTextView.setText(finalRotationText);
        orientationText = String.format("orientation: %s", MeasurePoint.rotationToString(finalRotation));
        reccordSecondTextView.setText(orientationText);
        scannerManagerInstance.getAutoScanManager().setExternalRotation(finalRotation);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume()
    {
        Log.d(LogTAG,"RUNTIME onResume()");
        super.onResume();
        mSensorManager.registerListener(this, rotatiometer, SensorManager.SENSOR_DELAY_FASTEST);
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
        EventBus.getDefault().unregister(this);
        scannerManagerInstance.cleanup();
        try {
            unregisterReceiver(broadcast);
        }
        catch (IllegalArgumentException ex){
            Log.w(LogTAG,"BroadcastReceiver already unregistered");
        }
        super.onStop();

    }

    private void prepareAll(){
        sharedPreferences = getSharedPreferences(SHAREDPREF,MODE_PRIVATE);
        calibrationOffset = sharedPreferences.getInt(OFFSET_VALUE_KEY,0);
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        rotatiometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        scannerManagerInstance = MyWifiScannerManager.getInstance().init(this);
        scannerManagerInstance.getAutoScanManager().setUseExternalRotation(true);
        scannerManagerInstance.getAutoScanManager().setExternalOffset(calibrationOffset);
        scannerManagerInstance.scan();


        dialog = new ProgressDialog(this,ProgressDialog.STYLE_SPINNER);
        dialog.setCancelable(false);
        dialog.setMessage("Stopping...");
    }

    @OnClick(R.id.startRecordButton)
    void startStopRecordClick(){
        if(recording){
            //stop record beacuse during record
            dialog.show();
            scannerManagerInstance.getAutoScanManager().setMAX_MEASURES_PER_DIRECTION(0);
        }
        else {
            //not recording, start
            Log.d(LogTAG,"Start record");
            scannerManagerInstance.getSectorManager().clearAll();
            startRecordButton.setText("Stop record");
            slowScanSwitch.setEnabled(false);
            measureBundle = new MeasureBundle("path_record");
            String filename = "record_"+measureBundle.getUuid();
            //measureBundle.setUuid(UUID.randomUUID().toString());
            File path = SynchronizerManager.getInstance().getAppExternalSynchronizerTempFolder();
            measureBundle.setFilepath(filename);

            //scannerManagerInstance.getAutoScanManager().setSlow_scan();
            recording = true;
            scannerManagerInstance.getSectorManager().setCurrentSectorPoint(new SectorPoint(-1,-1));
            scannerManagerInstance.getAutoScanManager().setMAX_MEASURES_PER_DIRECTION(999);
            scannerManagerInstance.getAutoScanManager().start();

            recordRightTextView.setText(String.format("name: %s", measureBundle.getUuid()));
            EventBus.getDefault().post(new PerformWifiScanEvent());
        }
    }
    @OnCheckedChanged(R.id.slowScanSwitch)
    public void slowaScanChanged(boolean isChecked)
    {
        scannerManagerInstance.getAutoScanManager().setSlow_scan(isChecked);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void refreshAutoScanStats(SubmitAutoScanEvent event){
        Log.d(LogTAG,"refreshAutoScanStats()");
        String count = Integer.toString(event.counter);
        recordLeftTextView.setText(String.format("count: %s",count));
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void grabScansAndSave(AutoScanCompletedEvent event){

        Log.d(LogTAG,"grabScansAndSave()");

        startRecordButton.setText("Start record");
        slowScanSwitch.setEnabled(true);
        //recordLeftTextView.setText(String.format("count: %s",0));
        dialog.dismiss();
        recording = false;
    }

    @OnClick(R.id.recordSaveButton)
    void saveMeasuresToFile()
    {
       // ArrayList<MeasurePoint> measures = scannerManagerInstance.getAutoScanManager().getMeasurePoints();
          //  String _count = Integer.toString(measures.size());
         //
       // measureBundle.setMeasures(measures);
        scannerManagerInstance.saveToFile(measureBundle);
        Toast.makeText(this,"Saved", Toast.LENGTH_SHORT).show();

    }
}
