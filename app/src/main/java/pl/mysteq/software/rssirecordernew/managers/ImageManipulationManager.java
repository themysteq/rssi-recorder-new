package pl.mysteq.software.rssirecordernew.managers;

import android.graphics.Matrix;
import android.graphics.Point;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by mysteq on 2017-04-22.
 */

public class ImageManipulationManager {

    public static final String LogTAG = "ImageManipulationManage";
    public static Point calculatePointOnImage(ImageView planImageView, Point point){

        View v = planImageView.getRootView();

        //Point point =
        Matrix inverse = new Matrix();
        planImageView.getMatrix().invert(inverse);
        Float flo_x = Float.parseFloat(((Integer) point.x).toString());
        Float flo_y = Float.parseFloat(((Integer) point.y).toString());
        Log.d(LogTAG, String.format("X: %f, Y: %f", flo_x, flo_y));
        float[] touchPoint = new float[]{flo_x, flo_y};
        inverse.mapPoints(touchPoint);
        int xCoord = Integer.valueOf((int) touchPoint[0]);
        int yCoord = Integer.valueOf((int) touchPoint[1]);

        int calcX = planImageView.getScrollX() + xCoord;
        int calcY = planImageView.getScrollY() + yCoord;

        Log.i(LogTAG,String.format("Pixel X: %d, Y: %d",calcX,calcY));
        return new Point(calcX,calcY);

    }
}
