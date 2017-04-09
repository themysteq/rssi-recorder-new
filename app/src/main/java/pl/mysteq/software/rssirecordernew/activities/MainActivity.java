package pl.mysteq.software.rssirecordernew.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import pl.mysteq.software.rssirecordernew.R;
import pl.mysteq.software.rssirecordernew.managers.PlansFileManager;

public class MainActivity extends Activity {

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
                startActivity(plansManagerIntent);
            }
        });
    }

}
