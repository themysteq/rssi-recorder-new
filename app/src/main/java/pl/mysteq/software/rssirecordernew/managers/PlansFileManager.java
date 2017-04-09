package pl.mysteq.software.rssirecordernew.managers;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by mysteq on 2017-04-09.
 */

public final class PlansFileManager {

    private static PlansFileManager instance = null;
    private  static final String LogTAG = "PlansFileManager";

    public static final String external_storage_app_folder_name = "/rssi-recorder-new";
    public static final String bundles_subfolder_name = "/bundles";
    public static final String plans_subfolder_name = "/plans";
    public static final String measures_subfolder_name = "/measures";
    public static final String plans_extensions = "jpg:png";

    public File getAppExternalMeasuresFolder() {
        return appExternalMeasuresFolder;
    }

    public File getAppExternalPlansFolder() {
        return appExternalPlansFolder;
    }

    public File getAppExternalBundlesFolder() {
        return appExternalBundlesFolder;
    }

    public File getAppExternalBaseFolder() {
        return appExternalBaseFolder;
    }

    private File appExternalBaseFolder = null;
    private File appExternalBundlesFolder = null;
    private File appExternalPlansFolder = null;
    private File appExternalMeasuresFolder = null;

    private FilenameFilter bundleFilter = null;
    private FilenameFilter planFilter = null;
    private FilenameFilter measureFilter = null;

    private PlansFileManager(){
        Log.d(LogTAG,"constructing...");
        bundleFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                Log.d(LogTAG, String.format("bundleFilter: %s", filename));
                return filename.matches(".*\\.bundle\\.json$");
            }
        };
        planFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                Log.d(LogTAG, String.format("planFilter: %s", filename));
                return filename.matches(".*\\.plan\\.(png)|(bmp)$");
            }
        };
        measureFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                Log.d(LogTAG, String.format("measureFilter: %s", filename));
                return filename.matches(".*\\.measure\\.json$");
            }
        };
        appExternalBaseFolder = createExternalSubFolder(Environment.getExternalStorageDirectory(),external_storage_app_folder_name);
        appExternalBundlesFolder = createExternalSubFolder(appExternalBaseFolder,bundles_subfolder_name);
        appExternalPlansFolder = createExternalSubFolder(appExternalBaseFolder,plans_subfolder_name);
        appExternalMeasuresFolder = createExternalSubFolder(appExternalBaseFolder,measures_subfolder_name);

        Log.d(LogTAG,"constructed");
    }

    private static synchronized PlansFileManager getSync(){
        if(instance == null) instance = new PlansFileManager();
        return instance;
    }

    public static PlansFileManager getInstance(){
        if(instance == null) instance = getSync();
        return instance;
    }

    private File createExternalSubFolder(File root, String subfolder){
        // nie moge zapisac do external storage

        if (! root.canWrite()) {
            throw new SecurityException(Environment.getExternalStorageDirectory().getAbsolutePath());
        }
        String fullPath = root.getPath().concat(subfolder);
        File folder = new File(fullPath);

        //dodac sprawdzanie czy ma uprawnienia
        if (! folder.exists()) {
            Boolean result =  folder.mkdir();
            Log.d(LogTAG,String.format(" creating: %s with result: %b",folder.getAbsolutePath(),result));
            if (! result){
                throw  new SecurityException(String.format("Can't create folder %s",folder.getAbsolutePath()));
            }
        }
        else {
            Log.d(LogTAG,"exists: "+folder.getAbsolutePath());
        }
        return folder;
    }

}
