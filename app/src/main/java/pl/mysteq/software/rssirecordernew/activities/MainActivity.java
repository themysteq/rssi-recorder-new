package pl.mysteq.software.rssirecordernew.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;

import pl.mysteq.software.rssirecordernew.R;
import pl.mysteq.software.rssirecordernew.events.BundlesReloadedEvent;
import pl.mysteq.software.rssirecordernew.events.ReloadBundlesEvent;
import pl.mysteq.software.rssirecordernew.managers.PlansFileManager;

import static pl.mysteq.software.rssirecordernew.managers.PlansFileManager.SHAREDPREF;
import static pl.mysteq.software.rssirecordernew.structures.PlanBundle.SELECTED_PLANBUNDLE_KEY;

public class MainActivity extends Activity {

    public static final int INTENT_RESULT_CODE_CHOOSE_PLAN = 2001;
    public static final int INTENT_RESULT_CODE_SCANNING = 2002;
    private static final String LogTAG = "MainActivity";
    String selectedBundle = null;

    //widgets
    Button planManagerButton;
    Button newMeasureButton;
    TextView selectedPlanTextView  = null;
    PlansFileManager plansFileManager = null;
    ProgressDialog progressDialog = null;
    Button continueLastMeasureButton = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        plansFileManager = PlansFileManager.getInstance();


        final SharedPreferences sharedPreferences = getSharedPreferences(SHAREDPREF,MODE_PRIVATE);
        selectedBundle = sharedPreferences.getString(SELECTED_PLANBUNDLE_KEY, null);

        planManagerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //odpal nowy plan manager

                Intent plansManagerIntent = new Intent(getBaseContext(), PlansManagerActivity.class);
                startActivityForResult(plansManagerIntent,INTENT_RESULT_CODE_CHOOSE_PLAN);
            }
        });

        newMeasureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(selectedBundle == null)
                {
                    Toast.makeText(v.getContext(),"You have to select plan!",Toast.LENGTH_LONG).show();
                }
                else
                {
                    Log.d(LogTAG, String.format("Creating new measure for plan: %s", selectedBundle));

                    //start scanning activity
                    Intent newMeasureIntent = new Intent(getBaseContext(),ScanningActivity.class);
                    //String measureUUID = UUID.randomUUID().toString();
                    File measureFile = plansFileManager.generateNewMeasureFile(selectedBundle);
                    newMeasureIntent.putExtra("PLAN_NAME",selectedBundle);
                    newMeasureIntent.putExtra("MEASURE_NAME", measureFile.getName());
                    newMeasureIntent.putExtra("MEASURE_FULLPATH",measureFile.getAbsolutePath());
                    startActivityForResult(newMeasureIntent,INTENT_RESULT_CODE_SCANNING);

                }
            }
        });
        continueLastMeasureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String measureName = getSharedPreferences(SHAREDPREF,MODE_PRIVATE).getString("LAST_MEASURE_NAME",null);
                if(measureName!= null)
                {
                    String lastBundle = getSharedPreferences(SHAREDPREF,MODE_PRIVATE).getString("LAST_MEASURE_BUNDLE",null);
                    if(lastBundle!= null && lastBundle.equals(selectedBundle)){
                        Log.d(LogTAG, String.format("Continue measure %s for plan %s", measureName,selectedBundle ));
                    }
                }
                else{
                    Log.d(LogTAG,"Can't continue");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(LogTAG, "onActivityResult");
        if (requestCode == INTENT_RESULT_CODE_CHOOSE_PLAN)
        {
            if(data != null && resultCode == Activity.RESULT_OK)
            {
                String selectedBundleName = data.getStringExtra("bundle_name");
                SharedPreferences sharedPreferences = getSharedPreferences(SHAREDPREF,MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(SELECTED_PLANBUNDLE_KEY,selectedBundleName);
                editor.commit();
                selectedBundle = selectedBundleName;

                selectedPlanTextView.setText(selectedBundle);
                Log.d(LogTAG, "Received bundle name: " + selectedBundleName);
            }else
            {
                Log.w(LogTAG,"No bundle selected!");
                if(selectedBundle == null){
                    selectedPlanTextView.setText("-- none --");
                }
                else{
                    selectedPlanTextView.setText(selectedBundle);
                    Log.d(LogTAG,"But using old value: "+selectedBundle);
                }
            }
        }
        else if( requestCode == INTENT_RESULT_CODE_SCANNING)
        {
            Log.d(LogTAG,"On result INTENT_RESULT_CODE_SCANNING");
            if(data !=null){
                String measure_fullpath = data.getStringExtra("MEASURE_FULLPATH");
                File measureFile = new File(measure_fullpath);
                String measureName = measureFile.getName();
                SharedPreferences sharedPreferences = getSharedPreferences(SHAREDPREF,MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("LAST_MEASURE_NAME",measureName);
                editor.putString("LAST_MEASURE_BUNDLE",selectedBundle);
                editor.commit();

            }
            else {
                Log.w(LogTAG,"data is null!");
            }
        }
        else
        {
            Log.d(LogTAG,"Unknown Intent result");
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        planManagerButton = (Button)findViewById(R.id.openPlanManagerButton);
        selectedPlanTextView = (TextView) findViewById(R.id.selectedPlanTextView);
        newMeasureButton = (Button) findViewById(R.id.newMeasureButton);
        continueLastMeasureButton = (Button) findViewById(R.id.continueMeasureButton);

        SharedPreferences sharedPreferences = getSharedPreferences(SHAREDPREF,MODE_PRIVATE);
        selectedBundle = sharedPreferences.getString(SELECTED_PLANBUNDLE_KEY,null);
        selectedPlanTextView.setText(selectedBundle);
        EventBus.getDefault().postSticky(new ReloadBundlesEvent());
        //Toast.makeText(getApplicationContext(),"Loading ",Toast.LENGTH_LONG);
        progressDialog = new ProgressDialog(this,ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("Loading application data");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Subscribe
    public void onMessage(BundlesReloadedEvent event){
        progressDialog.dismiss();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume()
    {
        Log.d(LogTAG,"onResume fired!");
        SharedPreferences sharedPreferences = getSharedPreferences(SHAREDPREF,MODE_PRIVATE);
        String selectedBundle = sharedPreferences.getString(SELECTED_PLANBUNDLE_KEY,null);
        String lastBundle = sharedPreferences.getString("LAST_MEASURE_BUNDLE",null);
        if (lastBundle != null)
        {
            if (lastBundle.equals(selectedBundle))
            {
                continueLastMeasureButton.setEnabled(true);
            }
        }

        super.onResume();
    }

    @Override
    protected void onStop()
    {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

}
