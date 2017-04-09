package pl.mysteq.software.rssirecordernew.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.github.angads25.filepicker.controller.DialogSelectionListener;
import com.github.angads25.filepicker.model.DialogConfigs;
import com.github.angads25.filepicker.model.DialogProperties;
import com.github.angads25.filepicker.view.FilePickerDialog;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import pl.mysteq.software.rssirecordernew.R;
import pl.mysteq.software.rssirecordernew.events.AddPlanEvent;
import pl.mysteq.software.rssirecordernew.managers.PlansFileManager;

public class PlansManagerActivity extends Activity {

    //class wide variables
    private static final String LogTAG = "PlansManagerActivity";
    private DialogProperties chooseNewPlanProperties;
    private FilePickerDialog chooseNewPlanDialog;
    private PlansFileManager plansFileManager;

    //widgets
    Button addNewPlanButton ;



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
                            EventBus.getDefault().post(new AddPlanEvent(files[0]));

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

        chooseNewPlanProperties = new DialogProperties();
        chooseNewPlanProperties.selection_type = DialogConfigs.SINGLE_MODE;
        chooseNewPlanProperties.selection_type=DialogConfigs.FILE_SELECT;
        chooseNewPlanProperties.root = Environment.getExternalStorageDirectory();
        chooseNewPlanProperties.error_dir = new File(DialogConfigs.DEFAULT_DIR);
        chooseNewPlanProperties.offset = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        chooseNewPlanProperties.extensions = PlansFileManager.plans_extensions.split(":");


    }
}
