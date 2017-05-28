package pl.mysteq.software.rssirecordernew.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.ParseException;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.mysteq.software.rssirecordernew.R;

import static pl.mysteq.software.rssirecordernew.managers.PlansFileManager.SHAREDPREF;

public class SettingsActivity extends Activity {

    public static final String LogTAG = "SettingsActivity";
    @BindView(R.id.serverEditText) EditText serverEditText;
    @BindView(R.id.portEditText) EditText portEditText;

     SharedPreferences sharedPreferences = null;
     SharedPreferences.Editor editor = null;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        sharedPreferences = getSharedPreferences(SHAREDPREF,MODE_PRIVATE);
        editor = sharedPreferences.edit();
        String sharedPreferencesHostname = sharedPreferences.getString("SYNC_HOSTNAME","localhost");
        int sharedPreferencesPort = sharedPreferences.getInt("SYNC_PORT",80);
        serverEditText.setText(sharedPreferencesHostname);
        portEditText.setText(sharedPreferencesPort);

    }

    @OnClick(R.id.settingsSaveButton) void saveSettings(){

        if(validateSettings()){
            Log.d(LogTAG,"Settings validation OK");

            editor.putInt("SYNC_PORT",Integer.parseInt(portEditText.getText().toString(), 10));
            editor.putString("SYNC_HOSTNAME",serverEditText.getText().toString());
            editor.commit();
            Toast.makeText(getApplicationContext(),"Settings saved",Toast.LENGTH_SHORT);
        }else {
            Log.d(LogTAG, "Settings validation failed");
        }

    }
    private boolean validateSettings(){

        String hostname = serverEditText.getText().toString();
        int port = 80;
        try {
            port = Integer.parseInt(portEditText.getText().toString(), 10);
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


}
