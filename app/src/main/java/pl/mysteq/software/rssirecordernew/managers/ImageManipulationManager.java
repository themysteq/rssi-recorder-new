package pl.mysteq.software.rssirecordernew.managers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by mysteq on 2017-04-22.
 */

public class ImageManipulationManager {
    Canvas canvas;
    Paint pointPaint;
    Bitmap bitmap;
    public ImageManipulationManager(){
            this.canvas = new Canvas();
        this.pointPaint = new Paint();
        this.pointPaint.setColor(Color.GREEN);
        this.pointPaint.setStyle(Paint.Style.STROKE);
        this.pointPaint.setStrokeWidth(2f);
        this.pointPaint.setStrokeJoin(Paint.Join.ROUND);
    }
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
    public  void setBitmap(Bitmap _bitmap){
        this.bitmap = _bitmap.copy(_bitmap.getConfig(),true);
        this.canvas.setBitmap(this.bitmap);
    }
    /*public void setBitmap(File filepath){
        this.bitmap = BitmapFactory.decodeFile(filepath.getAbsolutePath());
        this.canvas.setBitmap(this.bitmap);
    }
    */
    public void drawPoint(Point point){
        Log.d(LogTAG, String.format("draw point x: %d, y: %d",point.x,point.y ));
        this.canvas.drawPoint(point.x,point.y,this.pointPaint);
    }
    public void setBlankBitmap(Bitmap bitmap){
        this.bitmap = Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        this.canvas.setBitmap(this.bitmap);
    }
    public Bitmap getBitmap()
    {

        return this.bitmap;
    }
}
