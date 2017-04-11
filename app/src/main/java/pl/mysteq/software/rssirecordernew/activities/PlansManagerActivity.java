package pl.mysteq.software.rssirecordernew.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import pl.mysteq.software.rssirecordernew.R;
import pl.mysteq.software.rssirecordernew.events.AddPlanEvent;
import pl.mysteq.software.rssirecordernew.events.BundlesReloadedEvent;
import pl.mysteq.software.rssirecordernew.events.ReloadBundlesEvent;
import pl.mysteq.software.rssirecordernew.events.ReloadPlansEvent;
import pl.mysteq.software.rssirecordernew.managers.BundlesArrayAdapter;
import pl.mysteq.software.rssirecordernew.managers.PlansFileManager;

public class PlansManagerActivity extends Activity {

    //class wide variables
    private static final String LogTAG = "PlansManagerActivity";
    private DialogProperties chooseNewPlanProperties;
    private FilePickerDialog chooseNewPlanDialog;
    private PlansFileManager plansFileManager;

    BundlesArrayAdapter arrayAdapter = null;

    //widgets
    Button addNewPlanButton ;
    ListView plansListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plans_manager);

        addNewPlanButton = (Button) findViewById(R.id.addNewPlanButton);


        addNewPlanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chooseNewPlanDialog = new FilePickerDialog(v.getContext(),chooseNewPlanProperties);
                chooseNewPlanDialog.setDialogSelectionListener(new DialogSelectionListener() {
                    @Override
                    public void onSelectedFilePaths(String[] files) {
                        if(files.length ==1){
                            Log.d(LogTAG,"Sending event: "+files[0]);
                            EventBus.getDefault().post(new AddPlanEvent(files[0],null));
                            EventBus.getDefault().post(new ReloadPlansEvent());

                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),"Select failed.",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                chooseNewPlanDialog.setTitle("Pick a plan");
                chooseNewPlanDialog.show();
            }
        });



    }

    @Override
    public void onContentChanged() {
        super.onContentChanged();

        plansFileManager = PlansFileManager.getInstance();
        plansListView = (ListView) findViewById(R.id.plansListView);

        chooseNewPlanProperties = new DialogProperties();
        chooseNewPlanProperties.selection_type = DialogConfigs.SINGLE_MODE;
        chooseNewPlanProperties.selection_type=DialogConfigs.FILE_SELECT;
        chooseNewPlanProperties.root = Environment.getExternalStorageDirectory();
        chooseNewPlanProperties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        chooseNewPlanProperties.offset = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        chooseNewPlanProperties.extensions = PlansFileManager.plans_extensions.split(":");

        arrayAdapter = new BundlesArrayAdapter(this,plansFileManager.getBundles());
        plansListView.setAdapter(arrayAdapter);

        EventBus.getDefault().post(new ReloadBundlesEvent());

    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    //FIXME: do poprawki bo chyba nie ten tryb
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessage(BundlesReloadedEvent event){
        Log.d(LogTAG, "received BundlesReloadedEvent");
        Log.d(LogTAG,String.format("Bundles: %d",plansFileManager.getBundles().size()));
        arrayAdapter.notifyDataSetChanged();
    }

}
