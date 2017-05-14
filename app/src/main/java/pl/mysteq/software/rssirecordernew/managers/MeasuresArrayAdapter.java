package pl.mysteq.software.rssirecordernew.managers;

import android.content.Context;
import android.icu.util.Measure;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

import pl.mysteq.software.rssirecordernew.R;
import pl.mysteq.software.rssirecordernew.structures.MeasurePoint;
import pl.mysteq.software.rssirecordernew.structures.PlanBundle;

/**
 * Created by mysteq on 2017-04-11.
 */

public class MeasuresArrayAdapter extends ArrayAdapter {
    private ArrayList<ArrayList<MeasurePoint>> items;

    public MeasuresArrayAdapter(Context context, ArrayList<ArrayList<MeasurePoint>> objects) {
        super(context,0 ,objects);
        this.items = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // return super.getView(position, convertView, parent);

        ArrayList<MeasurePoint> measuresPoint = items.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.measures_list_item, parent, false);
        }
        TextView measuresNameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
        TextView measuresTextView = (TextView) convertView.findViewById(R.id.countTextView);
        //TextView planImageTextView = (TextView) convertView.findViewById(R.id.planImageTextView);



       // measuresNameTextView.setText(planBundle.getPlanBundleName());
        measuresNameTextView.setText("HEHEHE");
       // measuresTextView.setText(String.format(Locale.GERMAN,"%d",planBundle.getMeasuresFileNames().size()));
        measuresTextView.setText("hehe 2");

       // planImageTextView.setText(planBundle.getBuildingPlanName());

        return convertView;

    }

}