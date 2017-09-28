package pl.mysteq.software.rssirecordernew.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;

import pl.mysteq.software.rssirecordernew.R;
import pl.mysteq.software.rssirecordernew.events.MeasuresReloadedEvent;
import pl.mysteq.software.rssirecordernew.events.ReloadBundlesEvent;
import pl.mysteq.software.rssirecordernew.events.ReloadMeasuresEvent;
import pl.mysteq.software.rssirecordernew.managers.JsonMeasuresReader;
import pl.mysteq.software.rssirecordernew.managers.JsonPlanBundleReader;
import pl.mysteq.software.rssirecordernew.managers.JsonPlanBundleWriter;
import pl.mysteq.software.rssirecordernew.managers.MeasuresArrayAdapter;
import pl.mysteq.software.rssirecordernew.managers.PlansFileManager;
import pl.mysteq.software.rssirecordernew.structures.MeasureBundle;
import pl.mysteq.software.rssirecordernew.structures.PlanBundle;

public class MeasuresActivity extends Activity {

    ListView measuresListView = null;
    TextView leftTextView = null;
    TextView rightTextView = null;

    String planName = null;
    private ArrayList<MeasureBundle> measureBundles = new ArrayList<>();

    public static final String LogTAG = "MeasuresActivity";
    MeasuresArrayAdapter arrayAdapter = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measures);
        EventBus.getDefault().register(this);
        planName = getIntent().getStringExtra("PLAN_NAME");

        measuresListView = (ListView) findViewById(R.id.measuresListView);
        leftTextView = (TextView) findViewById(R.id.leftTextView);
        rightTextView = (TextView) findViewById(R.id.rightTextView);

        rightTextView.setText(planName);


        arrayAdapter = new MeasuresArrayAdapter(this,this.measureBundles);
        measuresListView.setAdapter(arrayAdapter);
       // EventBus.getDefault().post(new ReloadBundlesEvent());
        EventBus.getDefault().post(new ReloadMeasuresEvent(planName));

        measuresListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(LogTAG, String.format("clicked item: %d, id: %d",position,id));
                MeasureBundle item = (MeasureBundle) parent.getItemAtPosition(position);
                Log.d(LogTAG,"selected name: "+item.getUuid());

                setResult(Activity.RESULT_OK,getIntent().putExtra("MEASURE_UUID",item.getUuid()));
                finish();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        EventBus.getDefault().post(new ReloadBundlesEvent());
        //EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }




    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onMessage(ReloadMeasuresEvent event){
        Log.d(LogTAG,"Reloading Measures");
      //  JsonPlanBundleReader jsonPlanBundleReader = new JsonPlanBundleReader();
        ArrayList<MeasureBundle> _measureBundles = new ArrayList<>();
        //Log.d(LogTAG,"Trying to load: "+planName);
        //FIXME: nie pytaj o plan name tylko o hash tego pliku. Czyli weź wszystkie i zaglądaj do pliku json... eh.
       // PlanBundle planBundle =  jsonPlanBundleReader.run(new File(PlansFileManager.getInstance().getAppExternalBundlesFolder(),planName));
       ArrayList<PlanBundle> bundles =  PlansFileManager.getInstance().getAllBundles();
        PlanBundle selected_bundle = null;
        Log.d(LogTAG,"selected measure: "+event.plan_name);
        for (PlanBundle single_bundle : bundles)
        {
            if(single_bundle.getPlanBundleName().equals(event.plan_name)){
                selected_bundle = single_bundle;
            }
        }
        ArrayList<String> measures_names = selected_bundle.getMeasuresFileNames();
        JsonMeasuresReader jsonMeasuresReader = new JsonMeasuresReader();
        for (String measureName: measures_names ) {
            MeasureBundle measureBundle = jsonMeasuresReader.run(new File(PlansFileManager.getInstance().getAppExternalMeasuresFolder(),measureName));
            if(measureBundle != null) {
                _measureBundles.add(measureBundle);
            } else {
                Log.e(LogTAG,"Bundle not found! " +measureName);
            }
        }
        this.measureBundles.clear();
        this.measureBundles.addAll(_measureBundles);
       // this.measureBundles.addAll(_measureBundles);
        //EventBus.getDefault().post(new MeasuresReloadedEvent());

        //arrayAdapter.notifyDataSetChanged();
        EventBus.getDefault().post(new MeasuresReloadedEvent());

    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessage(MeasuresReloadedEvent event) {
        Log.d(LogTAG,"measures per plan: "+this.measureBundles.size());
        Log.d(LogTAG,"notifyDataSetChanged");

        //arrayAdapter = new MeasuresArrayAdapter(this,this.measureBundles);
       // arrayAdapter.notifyDataSetChanged();
       // arrayAdapter.clear();
       // arrayAdapter.addAll(this.measureBundles);
        //arrayAdapter.notifyAll();
        //measuresListView.invalidate();
        arrayAdapter.notifyDataSetChanged();
       // measuresListView.setAdapter(arrayAdapter);
    }

    //@Subscribe(threadMode = ThreadMode.MAIN)
    //public void onMessage(MeasuresReloadedEvent event){

    //}
}
