package pl.mysteq.software.rssirecordernew.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.mysteq.software.rssirecordernew.R;
import pl.mysteq.software.rssirecordernew.algorithm.AlgorithmSingleton;
import pl.mysteq.software.rssirecordernew.managers.MyWifiScannerManager;
import pl.mysteq.software.rssirecordernew.managers.PlansFileManager;
import pl.mysteq.software.rssirecordernew.structures.MeasurePoint;

public class AlgorithmActivity extends Activity {

    private static final String LogTAG = "AlgorithmActivity";

    private String planName;
    private String measureUUID;
    private String measureName;
    private String measureFullPath;

    private AlgorithmSingleton algorithmSingleton = null;

    private MyWifiScannerManager scannerManager = null;

    @BindView(R.id.firstButton) Button firstButton;
    @BindView(R.id.secondButton) Button secondButton;
    @BindView(R.id.summaryTextView) TextView summaryTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_algorithm);
        ButterKnife.bind(this);
        Log.d(LogTAG,"preparing");
        prepare();
        Log.d(LogTAG,"prepared");

    }

    public void prepare()
    {
        planName = getIntent().getStringExtra("PLAN_NAME");
        measureFullPath = getIntent().getStringExtra("MEASURE_FULLPATH");
        measureUUID = getIntent().getStringExtra("MEASURE_UUID");
        measureName = getIntent().getStringExtra("MEASURE_NAME");
        scannerManager =  MyWifiScannerManager.getInstance().init(this);
        scannerManager.loadFromFile(new File(PlansFileManager.getInstance().getAppExternalMeasuresFolder(),measureFullPath));

        algorithmSingleton = new AlgorithmSingleton();
    }

    @OnClick(R.id.secondButton)
    public void secondButtonClick(){
        Log.d(LogTAG,"secondButtonClick()");

        ArrayList<MeasurePoint> _measures = scannerManager.getSectorManager().getAllMeasures();
        algorithmSingleton.setMeasurePointArrayList(_measures);


    }
}
