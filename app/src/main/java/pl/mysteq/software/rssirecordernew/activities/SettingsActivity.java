package pl.mysteq.software.rssirecordernew.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.ParseException;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.mysteq.software.rssirecordernew.R;
import pl.mysteq.software.rssirecordernew.events.ReloadBundlesEvent;

import static pl.mysteq.software.rssirecordernew.managers.PlansFileManager.SHAREDPREF;

public class SettingsActivity extends Activity {

    public static final String LogTAG = "SettingsActivity";
    @BindView(R.id.serverEditText) EditText serverEditText;
    @BindView(R.id.portEditText) EditText portEditText;
    @BindView(R.id.directionTextView) TextView offsetTextView;
    @BindView(R.id.offsetSeekBar) SeekBar offsetSeekBar;

     SharedPreferences sharedPreferences = null;
     SharedPreferences.Editor editor = null;
   int offsetValue = 0;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        sharedPreferences = getSharedPreferences(SHAREDPREF,MODE_PRIVATE);
        editor = sharedPreferences.edit();
        String sharedPreferencesHostname = sharedPreferences.getString("SYNC_HOSTNAME","ovh02.mysteq.pl");
        int sharedPreferencesPort = sharedPreferences.getInt("SYNC_PORT",7777);
        offsetValue = sharedPreferences.getInt(ScanningActivity.OFFSET_VALUE_KEY,0);
        serverEditText.setText(sharedPreferencesHostname);
        portEditText.setText(Integer.toString(sharedPreferencesPort) );
        offsetSeekBar.setProgress(180+offsetValue);
        offsetTextView.setText(Integer.toString(offsetValue)+'°');


        offsetSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                offsetValue = i-180;
                offsetTextView.setText(Integer.toString(offsetValue)+'°');
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @OnClick(R.id.settingsSaveButton) void saveSettings(){

        if(validateSettings()){
            Log.d(LogTAG,"Settings validation OK");

            editor.putInt("SYNC_PORT",Integer.parseInt(portEditText.getText().toString(), 10));
            editor.putString("SYNC_HOSTNAME",serverEditText.getText().toString());
            editor.putInt(ScanningActivity.OFFSET_VALUE_KEY,offsetValue);
            editor.commit();
            Toast.makeText(getApplicationContext(),"Settings saved",Toast.LENGTH_LONG);
            Log.d(LogTAG,"Settings saved");
        }else {
            Log.d(LogTAG, "Settings validation failed");
        }

    }
    private boolean validateSettings(){

        String hostname = serverEditText.getText().toString();
        int port = 80;
        try {
            port = Integer.parseInt(portEditText.getText().toString(), 10);
            //offsetValue = Integer.parseInt("0",10);
        }catch (ParseException e){
            Log.d(LogTAG,e.getMessage());
            return false;
        }
        if(hostname.isEmpty())
            return false;
        if(port > 65536 ||  port < 1)
            return false;

        return true;
    }

    @Override
    protected void onStop() {
        Log.v(LogTAG,"onStop()");
        EventBus.getDefault().post(new ReloadBundlesEvent());
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

}
