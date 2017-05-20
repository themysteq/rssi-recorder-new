package pl.mysteq.software.rssirecordernew.managers;

import android.os.Environment;
import android.util.Log;

import java.io.File;

import static pl.mysteq.software.rssirecordernew.managers.PlansFileManager.external_storage_app_folder_name;

/**
 * Created by mysteq on 2017-05-20.
 */

public class SynchronizerManager {

    private static final String LogTAG = "SynchronizerManager";
    private static SynchronizerManager instance = null;

    public static final String synchronizer_folder_name = "/synchronizer";

    private File appExternalBaseFolder = null;
    private File appExternalSynchronizerFolder = null;



    public SynchronizerManager(){
    appExternalBaseFolder = createExternalSubFolder(Environment.getExternalStorageDirectory(),external_storage_app_folder_name);
    appExternalSynchronizerFolder = createExternalSubFolder(appExternalBaseFolder,synchronizer_folder_name);
    }
    private static synchronized SynchronizerManager getSync(){
        if(instance == null) instance = new SynchronizerManager();
        return instance;
    }

    public static SynchronizerManager getInstance(){
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
