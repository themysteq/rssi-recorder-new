package pl.mysteq.software.rssirecordernew.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import pl.mysteq.software.rssirecordernew.R;

import static pl.mysteq.software.rssirecordernew.managers.PlansFileManager.SHAREDPREF;
import static pl.mysteq.software.rssirecordernew.structures.PlanBundle.SELECTED_PLANBUNDLE_KEY;

public class MainActivity extends Activity {

    public static final int INTENT_RESULT_CODE_CHOOSE_PLAN = 2001;
    private static final String LogTAG = "MainActivity";
    String selectedBundle = null;
    //widgets
    Button planManagerButton;
    TextView selectedPlanTextView  = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INTENT_RESULT_CODE_CHOOSE_PLAN )
        {
            String selectedBundleName = data.getStringExtra("bundle_name");
            SharedPreferences sharedPreferences = getSharedPreferences(SHAREDPREF,MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(SELECTED_PLANBUNDLE_KEY,selectedBundleName);
            editor.commit();
            selectedBundle = selectedBundleName;
            if(resultCode == Activity.RESULT_OK) {
                Log.d(LogTAG, "onActivityResult");
                selectedPlanTextView.setText(selectedBundle);
                Log.d(LogTAG, "Received bundle name: " + selectedBundleName);
            }
            else
            {
                selectedPlanTextView.setText("-- none --");
                Log.w(LogTAG,"No bundle selected!");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
        planManagerButton = (Button)findViewById(R.id.openPlanManagerButton);
        selectedPlanTextView = (TextView) findViewById(R.id.selectedPlanTextView);
        SharedPreferences sharedPreferences = getSharedPreferences(SHAREDPREF,MODE_PRIVATE);
        selectedBundle = sharedPreferences.getString(SELECTED_PLANBUNDLE_KEY,null);
        selectedPlanTextView.setText(selectedBundle);
    }
}
