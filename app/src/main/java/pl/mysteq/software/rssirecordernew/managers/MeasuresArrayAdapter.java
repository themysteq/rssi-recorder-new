package pl.mysteq.software.rssirecordernew.managers;

import android.content.Context;
import android.icu.util.Measure;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import pl.mysteq.software.rssirecordernew.R;
import pl.mysteq.software.rssirecordernew.structures.MeasureBundle;
import pl.mysteq.software.rssirecordernew.structures.MeasurePoint;
import pl.mysteq.software.rssirecordernew.structures.PlanBundle;

/**
 * Created by mysteq on 2017-04-11.
 */

public class MeasuresArrayAdapter extends ArrayAdapter {
    //private ArrayList<ArrayList<MeasurePoint>> items;
    private ArrayList<MeasureBundle> items;
    private static final String LogTAG = "MeasuresArrayAdapter";
    //private ArrayList<MeasurePoint> items;
    public MeasuresArrayAdapter(Context context, ArrayList<MeasureBundle> measureBundles) {
        super(context,0,measureBundles);
        Log.d(LogTAG,"items for adapter: "+measureBundles.size());
       // this.items = measureBundles;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // return super.getView(position, convertView, parent);
            Log.d(LogTAG,"getView called");
        //ArrayList<MeasurePoint> measuresPoint = items.get(position);
        MeasureBundle measureBundle = (MeasureBundle) getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.measures_list_item, parent, false);
        }
        TextView measuresNameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
        TextView measuresTextView = (TextView) convertView.findViewById(R.id.countTextView);
        TextView dateTextView = (TextView) convertView.findViewById(R.id.dateTextView);
        //TextView planImageTextView = (TextView) convertView.findViewById(R.id.planImageTextView);


        measuresNameTextView.setText(measureBundle.getUuid());
        Log.d(LogTAG,"measuresNameTextView: "+measureBundle.getUuid());
        measuresTextView.setText(String.format("%d", measureBundle.getMeasures().size()));
        SimpleDateFormat fmtOut = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        //DateFormat dateFormat = android.text.format.DateFormat.getTimeFormat(parent.getContext());

        if (measureBundle.getLastChanged() != null){
            dateTextView.setText(fmtOut.format(measureBundle.getLastChanged()));
        }
        else
            {
            Log.e(LogTAG, String.format(" %s getLastChanged() is null",measureBundle.getUuid() ));
                dateTextView.setText("n/n");
        }

       // measuresNameTextView.setText(planBundle.getPlanBundleName());
        //measuresNameTextView.setText("HEHEHE");
       // measuresTextView.setText(String.format(Locale.GERMAN,"%d",planBundle.getMeasuresFileNames().size()));
        //measuresTextView.setText("hehe 2");

       // planImageTextView.setText(planBundle.getBuildingPlanName());

        return convertView;

    }

}
