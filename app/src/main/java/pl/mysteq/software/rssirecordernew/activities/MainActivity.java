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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        plansFileManager = PlansFileManager.getInstance();


        SharedPreferences sharedPreferences = getSharedPreferences(SHAREDPREF,MODE_PRIVATE);
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
                    newMeasureIntent.putExtra("PLAN_NAME",selectedBundle);
                    startActivityForResult(newMeasureIntent,INTENT_RESULT_CODE_SCANNING);

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
    protected void onStop()
    {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

}
