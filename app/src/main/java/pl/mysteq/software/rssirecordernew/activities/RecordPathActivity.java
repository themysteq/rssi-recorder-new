package pl.mysteq.software.rssirecordernew.activities;

import android.app.Activity;
import android.hardware.Sensor;
import android.os.Bundle;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import pl.mysteq.software.rssirecordernew.R;

public class RecordPathActivity extends Activity {

    Sensor rotatiometer;

    @BindView(R.id.startRecordButton) Button startRecordButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record_path);

        ButterKnife.bind(this);

    }
}
