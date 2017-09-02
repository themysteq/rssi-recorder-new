package pl.mysteq.software.rssirecordernew.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import butterknife.BindView;
import pl.mysteq.software.rssirecordernew.R;

/**
 * Created by mysteq on 2017-07-16.
 */

public final class CalibratorDialog extends AlertDialog.Builder {

    public SeekBar degreeSeekBar;
    public TextView degreesTextView;
    public ImageView arrowImageView;

  public CalibratorDialog(Context context){
      super(context);
      LayoutInflater layoutInflater = LayoutInflater.from(context);
      View view = layoutInflater.inflate(R.layout.dialog_calibrator,null);
      setView(view);
      degreeSeekBar = (SeekBar) view.findViewById(R.id.calibratorSeekBarOffset);
      degreesTextView = (TextView) view.findViewById(R.id.calibratorDegreeTextView);
      arrowImageView = (ImageView) view.findViewById(R.id.calibratorArrowImageView);

      arrowImageView.setImageResource(R.drawable.red_arrow);

      setTitle("Calibrator");
      setCancelable(false);
      setPositiveButton("OKEJ",null);
      setNegativeButton("KANCEL",null);


  }


}
