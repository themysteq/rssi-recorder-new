package pl.mysteq.software.rssirecordernew.managers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import pl.mysteq.software.rssirecordernew.R;
import pl.mysteq.software.rssirecordernew.structures.PlanBundle;

/**
 * Created by mysteq on 2017-04-11.
 */

public class BundlesArrayAdapter extends ArrayAdapter {
    private ArrayList<PlanBundle> items;

    public BundlesArrayAdapter(Context context, ArrayList<PlanBundle> objects) {
        super(context,0 ,objects);
        this.items = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // return super.getView(position, convertView, parent);

        PlanBundle planBundle = items.get(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.bundle_list_item, parent, false);
        }
        TextView bundleNameTextView = (TextView) convertView.findViewById(R.id.nameTextView);
        TextView measuresTextView = (TextView) convertView.findViewById(R.id.countTextView);
        //TextView planImageTextView = (TextView) convertView.findViewById(R.id.planImageTextView);



        bundleNameTextView.setText(planBundle.getPlanBundleName());
        measuresTextView.setText(String.format(Locale.GERMAN,"%d",planBundle.getMeasuresFileNames().size()));
       // planImageTextView.setText(planBundle.getBuildingPlanName());

        return convertView;

    }

}
