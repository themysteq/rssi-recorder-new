package pl.mysteq.software.rssirecordernew.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.mysteq.software.rssirecordernew.R;
import pl.mysteq.software.rssirecordernew.events.synchronizer.SyncBundlesDoneEvent;
import pl.mysteq.software.rssirecordernew.events.synchronizer.SyncBundlesEvent;
import pl.mysteq.software.rssirecordernew.events.synchronizer.SyncMeasuresDoneEvent;
import pl.mysteq.software.rssirecordernew.events.synchronizer.SyncMeasuresEvent;
import pl.mysteq.software.rssirecordernew.events.synchronizer.SyncPlansDoneEvent;
import pl.mysteq.software.rssirecordernew.events.synchronizer.SyncPlansEvent;
import pl.mysteq.software.rssirecordernew.events.synchronizer.SyncRawPlansDoneEvent;
import pl.mysteq.software.rssirecordernew.events.synchronizer.SyncRawPlansEvent;
import pl.mysteq.software.rssirecordernew.managers.SynchronizerManager;

import static pl.mysteq.software.rssirecordernew.managers.PlansFileManager.SHAREDPREF;

public class SynchronizerActivity extends Activity {

    private static final String LogTAG = "SynchronizerActivity";

    public SynchronizerManager synchronizerManagerInstance;

    SharedPreferences sharedPreferences = null;
    String hostname = null;
    Integer port = null;

    @BindView(R.id.editText) EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synchronizer);
        synchronizerManagerInstance = SynchronizerManager.getInstance();
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        sharedPreferences = getSharedPreferences(SHAREDPREF,MODE_PRIVATE);
        Log.d(LogTAG,"Getting shared preferences");
        hostname = sharedPreferences.getString("SYNC_HOSTNAME","localhost");
        port = sharedPreferences.getInt("SYNC_PORT",5000);

    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
    }

    @OnClick(R.id.synchronizeBundlesButton) void syncBundles(){
        Log.d(LogTAG,"syncBundles()");
        EventBus.getDefault().post(new SyncBundlesEvent(hostname,port));

    }

    @OnClick(R.id.synchronizeMeasuresButton) void syncMeasures(){
        Log.d(LogTAG,"syncMeasures()");
        EventBus.getDefault().post(new SyncMeasuresEvent(hostname,port));
    }

    @OnClick(R.id.synchronizePlansButton) void syncPlans(){
        Log.d(LogTAG,"syncPlans()");
        EventBus.getDefault().post(new SyncPlansEvent(hostname,port));
    }
    @OnClick(R.id.synchronizeRawPlansButton) void syncRawPlans(){
        Log.d(LogTAG,"syncRawPlans()");
        EventBus.getDefault().post(new SyncRawPlansEvent(hostname,port));
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void syncPlansDone(SyncPlansDoneEvent event){
        Log.d(LogTAG,"SyncPlansDoneEvent done");
        if(event.plans != null) editText.setText(event.plans);
        else editText.setText("@NONE@");

        Toast.makeText(getApplicationContext(),"sync plans done",Toast.LENGTH_SHORT).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void syncBundlesDone(SyncBundlesDoneEvent event){
        Log.d(LogTAG,"SyncBundlesDoneEventt done");
        if(event.bundles != null) editText.setText(event.bundles);
        else editText.setText("@NONE@");

        Toast.makeText(getApplicationContext(),"sync bundles done",Toast.LENGTH_SHORT).show();
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void syncRawPlansDone(SyncRawPlansDoneEvent event)
    {
        Log.d(LogTAG,"SyncRawPlansDoneEvent done");
        Toast.makeText(getApplicationContext(),"plans downloaded",Toast.LENGTH_SHORT).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void syncMeasuresDone(SyncMeasuresDoneEvent event){
        Log.d(LogTAG,"SyncMeasuresDoneEvent done");
        Toast.makeText(getApplicationContext(),"measures synced",Toast.LENGTH_SHORT).show();
    }

}
