package pl.mysteq.software.rssirecordernew.managers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

import pl.mysteq.software.rssirecordernew.structures.PlanBundle;
import pl.mysteq.software.rssirecordernew.structures.Sector;

/**
 * Created by mysteq on 2017-04-22.
 */

public class ImageManipulationManager {
    Canvas canvas;
    Canvas withSectorcanvas;
    Paint pointPaint;
    Bitmap bitmap;
    Bitmap withSectorBitmap;
    //Rect currentSector;
    ArrayList<Rect> sectors;
    public ImageManipulationManager(){
            this.canvas = new Canvas();
        this.withSectorcanvas = new Canvas();
        this.pointPaint = new Paint();
        this.pointPaint.setColor(Color.RED);
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
        if(this.bitmap != null) {
            this.bitmap.recycle();
            Log.w(LogTAG,"Recycling bitmap");
        }
       this.bitmap = _bitmap.copy(_bitmap.getConfig(),true);
       _bitmap.recycle();
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
        //bitmap.recycle();
        this.canvas.setBitmap(this.bitmap);
    }
    public Bitmap getBitmap()
    {
        return this.bitmap;
    }
    public Bitmap getWithSectorBitmap()
    {
       // this.canvas.setBitmap(this.bitmap);
        Rect drawRectSector = getRectFromSector(MyWifiScannerManager.getInstance().getSectorManager().getCurrentSector());
        if (drawRectSector != null){
            this.withSectorBitmap = Bitmap.createBitmap(this.bitmap);
            this.withSectorcanvas.setBitmap(this.withSectorBitmap);
            Paint sectorPaint = new Paint();
            sectorPaint.setColor(Color.GREEN);
            //sectorPaint.setStrokeWidth(25.0f);
            sectorPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            sectorPaint.setStrokeJoin(Paint.Join.ROUND);
            Log.d(LogTAG, String.format("drawing Rect: %s", drawRectSector.toString()));
            this.withSectorcanvas.drawRect(drawRectSector,sectorPaint);
            return this.withSectorBitmap;
        }
        else {
            Log.d(LogTAG,"current sector is null");
            return this.bitmap;
        }


    }

    // public
    public void rotate(float degrees)
    {
        this.canvas.rotate(degrees);
    }
    public void rotate(float degrees,float px,float py)
    {
        this.canvas.rotate(degrees, px, py);
    }

    public Rect getRectFromSector(Sector sector){
        if(sector == null) return null;

        int x0 = PlanBundle.SECTOR_X_SIZE*sector.getCoordinates().x;
        int y0 = PlanBundle.SECTOR_Y_SIZE*sector.getCoordinates().y;
        int x1 = x0+PlanBundle.SECTOR_X_SIZE;
        int y1 = y0+PlanBundle.SECTOR_Y_SIZE;
        return new Rect(x0,y0,x1,y1);
    }
}
