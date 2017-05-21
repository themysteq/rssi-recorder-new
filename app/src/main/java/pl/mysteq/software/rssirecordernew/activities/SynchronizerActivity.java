package pl.mysteq.software.rssirecordernew.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

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
import pl.mysteq.software.rssirecordernew.managers.SynchronizerManager;

public class SynchronizerActivity extends Activity {

    private static final String LogTAG = "SynchronizerActivity";

    public SynchronizerManager synchronizerManagerInstance;

    @BindView(R.id.editText) EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synchronizer);
        synchronizerManagerInstance = SynchronizerManager.getInstance();
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();
    }

    @OnClick(R.id.synchronizeBundlesButton) void syncBundles(){
        Log.d(LogTAG,"syncBundles()");
        EventBus.getDefault().post(new SyncBundlesEvent());

    }

    @OnClick(R.id.synchronizeMeasuresButton) void syncMeasures(){
        Log.d(LogTAG,"syncMeasures()");
        EventBus.getDefault().post(new SyncMeasuresEvent());
    }

    @OnClick(R.id.synchronizePlansButton) void syncPlans(){
        Log.d(LogTAG,"syncPlans()");
        EventBus.getDefault().post(new SyncPlansEvent());
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void syncPlansDone(SyncPlansDoneEvent event){
        if(event.plans != null) editText.setText(event.plans);
        else editText.setText("@NONE@");
    }

    @Subscribe
    public void syncBundlesDone(SyncBundlesDoneEvent event){
        if(event.bundles != null) editText.setText(event.bundles);
        else editText.setText("@NONE@");
    }

    @Subscribe
    public void syncMeasuresDone(SyncMeasuresDoneEvent event){

    }

}
