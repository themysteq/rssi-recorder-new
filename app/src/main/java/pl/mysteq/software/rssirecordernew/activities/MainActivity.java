package pl.mysteq.software.rssirecordernew.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import pl.mysteq.software.rssirecordernew.R;
import pl.mysteq.software.rssirecordernew.managers.PlansFileManager;

public class MainActivity extends Activity {

    public static final int INTENT_RESULT_CODE_CHOOSE_PLAN = 2001;
    private static final String LogTAG = "MainActivity";
    //widgets
    Button planManagerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        planManagerButton = (Button)findViewById(R.id.openPlanManagerButton);

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
        if (requestCode == INTENT_RESULT_CODE_CHOOSE_PLAN)
        {
            Log.d(LogTAG,"onActivityResult");
            String selectedBundleName = data.getStringExtra("bundle_name");
            Log.d(LogTAG,"Received bundle name: "+selectedBundleName);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
