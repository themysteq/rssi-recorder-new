package pl.mysteq.software.rssirecordernew.managers;

import android.app.DownloadManager;
import android.os.Environment;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pl.mysteq.software.rssirecordernew.events.PlansReloadedEvent;
import pl.mysteq.software.rssirecordernew.events.synchronizer.SyncBundlesDoneEvent;
import pl.mysteq.software.rssirecordernew.events.synchronizer.SyncBundlesEvent;
import pl.mysteq.software.rssirecordernew.events.synchronizer.SyncMeasuresEvent;
import pl.mysteq.software.rssirecordernew.events.synchronizer.SyncPlansDoneEvent;
import pl.mysteq.software.rssirecordernew.events.synchronizer.SyncPlansEvent;
import pl.mysteq.software.rssirecordernew.structures.PlanBundle;

import static pl.mysteq.software.rssirecordernew.managers.PlansFileManager.external_storage_app_folder_name;

/**
 * Created by mysteq on 2017-05-20.
 */

public class SynchronizerManager {

    private static final String LogTAG = "SynchronizerManager";
    private static SynchronizerManager instance = null;

    public static final String synchronizer_folder_name = "/synchronizer";
    public static final String synchronizer_plans_subfolder_name = "/plans";
    public static final String synchronizer_bundles_subfolder_name = "/bundles";
    public static final String synchronizer_measures_subfolder_name ="/measures";

    public static final String serverURL = "http://192.168.10.123/";

    private File appExternalBaseFolder = null;
    private File appExternalSynchronizerFolder = null;
    private File appExternalSynchronizerPlansFolder = null;
    private File appExternalSynchronizerBundlesFolder = null;
    private File appExternalSynchronizerMeasuresFolder = null;


    public SynchronizerManager(){
        appExternalBaseFolder = createExternalSubFolder(Environment.getExternalStorageDirectory(),external_storage_app_folder_name);
        appExternalSynchronizerFolder = createExternalSubFolder(appExternalBaseFolder,synchronizer_folder_name);
        appExternalSynchronizerBundlesFolder = createExternalSubFolder(appExternalSynchronizerFolder,synchronizer_bundles_subfolder_name);
        appExternalSynchronizerMeasuresFolder = createExternalSubFolder(appExternalSynchronizerFolder,synchronizer_measures_subfolder_name);
        appExternalSynchronizerPlansFolder = createExternalSubFolder(appExternalSynchronizerFolder,synchronizer_plans_subfolder_name);
        EventBus.getDefault().register(this);

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

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void OnMessage(SyncPlansEvent event){
        Log.d(LogTAG,"SyncPlansEvent received");
        syncPlansWithServer();
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void OnMessage(SyncBundlesEvent event){
        Log.d(LogTAG,"SyncBundlesEvent received");
        ArrayList<PlanBundle> planBundles = PlansFileManager.getInstance().getAllBundles();
        ArrayList<String> names = new ArrayList<String>();
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(serverURL+"/bundles").build();
        String bundlesJSON = null;
        try {
            Response response = okHttpClient.newCall(request).execute();
            bundlesJSON = response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //for (PlanBundle planBundle : planBundles ){
        //
        //}
            EventBus.getDefault().post(new SyncBundlesDoneEvent(bundlesJSON));

    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void OnMessage(SyncMeasuresEvent event){
        Log.d(LogTAG,"SyncMeasuresEvent received");
    }

    public void syncPlansWithServer(){
        File plansDir = PlansFileManager.getInstance().getAppExternalPlansFolder();
        File[] plans = plansDir.listFiles(PlansFileManager.getInstance().planFilter);
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(serverURL+"/plans").build();
        String plansJSON = null;

        try {
           Response response = okHttpClient.newCall(request).execute();
           plansJSON = response.body().string();
            Log.d(LogTAG,"plansJson:");
            Log.d(LogTAG,plansJSON);
        }
        catch (IOException | NullPointerException ex)
        {
            Log.d(LogTAG,ex.getMessage());
        }

        for (File plan : plans) {
            Log.d(LogTAG,"plan: "+plan.getName());
        }

        EventBus.getDefault().post(new SyncPlansDoneEvent(plansJSON));
    }

    /*
    public void syncBundlesWithServer() {

    }

    public void syncMeasuresWithServer() {

    }
    */


}
