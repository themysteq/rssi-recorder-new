package pl.mysteq.software.rssirecordernew.managers;

import android.app.DownloadManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MultipartBody;
import pl.mysteq.software.rssirecordernew.events.AddPlanEvent;
import pl.mysteq.software.rssirecordernew.events.PlansReloadedEvent;
import pl.mysteq.software.rssirecordernew.events.ProgressEvent;
import pl.mysteq.software.rssirecordernew.events.synchronizer.SyncBundlesDoneEvent;
import pl.mysteq.software.rssirecordernew.events.synchronizer.SyncBundlesEvent;
import pl.mysteq.software.rssirecordernew.events.synchronizer.SyncEvent;
import pl.mysteq.software.rssirecordernew.events.synchronizer.SyncMeasuresDoneEvent;
import pl.mysteq.software.rssirecordernew.events.synchronizer.SyncMeasuresEvent;
import pl.mysteq.software.rssirecordernew.events.synchronizer.SyncPlansDoneEvent;
import pl.mysteq.software.rssirecordernew.events.synchronizer.SyncPlansEvent;
import pl.mysteq.software.rssirecordernew.events.synchronizer.SyncRawPlansEvent;
import pl.mysteq.software.rssirecordernew.structures.PlanBundle;

import static android.R.id.candidatesArea;
import static android.R.id.list;
import static android.content.Context.MODE_PRIVATE;
import static pl.mysteq.software.rssirecordernew.managers.PlansFileManager.SHAREDPREF;
import static pl.mysteq.software.rssirecordernew.managers.PlansFileManager.external_storage_app_folder_name;

/**
 * Created by mysteq on 2017-05-20.
 */

public class SynchronizerManager {

    private static final String LogTAG = "SynchronizerManager";
    private static SynchronizerManager instance = null;

    public static final String synchronizer_folder_name = "/synchronizer";
    public static final String synchronizer_plans_subfolder_name = "/plans";
    public static final String getSynchronizer_rawplans_subfolder_name = "/rawplans";
    public static final String synchronizer_bundles_subfolder_name = "/bundles";
    public static final String synchronizer_measures_subfolder_name ="/measures";
    public static final String synchronizer_temp_subfolder_name = "/temp";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    //public static final String serverURL = "http://localhost:80/";

    private Context context = null;

    private File appExternalBaseFolder = null;
    private File appExternalSynchronizerFolder = null;
    private File appExternalSynchronizerPlansFolder = null;
    private File appExternalSynchronizerBundlesFolder = null;
    private File appExternalSynchronizerMeasuresFolder = null;
    private File appExternalSynchronizerRawplansFolder = null;
    private File appExternalSynchronizerTempFolder = null;
    private OkHttpClient okHttpClient = null;


    public SynchronizerManager(){
        appExternalBaseFolder = createExternalSubFolder(Environment.getExternalStorageDirectory(),external_storage_app_folder_name);
        appExternalSynchronizerFolder = createExternalSubFolder(appExternalBaseFolder,synchronizer_folder_name);
        appExternalSynchronizerBundlesFolder = createExternalSubFolder(appExternalSynchronizerFolder,synchronizer_bundles_subfolder_name);
        appExternalSynchronizerMeasuresFolder = createExternalSubFolder(appExternalSynchronizerFolder,synchronizer_measures_subfolder_name);
        appExternalSynchronizerPlansFolder = createExternalSubFolder(appExternalSynchronizerFolder,synchronizer_plans_subfolder_name);
        appExternalSynchronizerRawplansFolder  = createExternalSubFolder(appExternalSynchronizerFolder,getSynchronizer_rawplans_subfolder_name);
        appExternalSynchronizerTempFolder = createExternalSubFolder(appExternalSynchronizerFolder,synchronizer_temp_subfolder_name);
        //String sharedPreferencesHostname = sharedPreferences.getString("SYNC_HOSTNAME","localhost");
        //int sharedPreferencesPort = sharedPreferences.getInt("SYNC_PORT",80);

        EventBus.getDefault().register(this);
        okHttpClient = new OkHttpClient();

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
    public void OnMessage(SyncRawPlansEvent event){
        Log.d(LogTAG,"SyncRawPlansEvent received");
        try {
            syncRawPlansImages(event);
            recreatePlans();
            //TODO:syncPlansWithServer(event);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void OnMessage(SyncPlansEvent event){
        Log.d(LogTAG,"SyncPlansEvent received");
        syncPlans(event);
    }


    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void OnMessage(SyncBundlesEvent event){
      Log.d(LogTAG,"SyncBundlesEvent received");
      syncBundlesWithServer(event);
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void OnMessage(SyncMeasuresEvent event){
        Log.d(LogTAG,"SyncMeasuresEvent received");
        syncMeasures(event);
        //TODO:syncMeasures
    }

    private void moveFile(File src, File dst) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(src);
        FileOutputStream fileOutputStream = new FileOutputStream(dst);
        byte[] buffer = new byte[10240];
        int len;
        while ((len = fileInputStream.read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, len);
        }
        fileInputStream.close();
        fileOutputStream.close();
        src.delete();
    }
    private File downloadFile(String uri, String filename) throws IOException, NullPointerException {
        File tempFile = new File(appExternalSynchronizerTempFolder,filename);
        Log.d(LogTAG, String.format("downloading file from uri: %s", uri));
        Request request = new Request.Builder().url(uri)
                .build();
        Response response = okHttpClient.newCall(request).execute();

        FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
        if (!response.isSuccessful()) {
            throw new IOException("Failed to download file: " + response);
        }
        fileOutputStream.write(response.body().bytes());
        fileOutputStream.close();
        Log.d(LogTAG, String.format("filesize: %d", tempFile.length()));
        //response.body().close();
        Log.d(LogTAG,"download ended");
        return tempFile;
    }
    public void syncRawPlansImages(SyncEvent event) throws IOException {

        //list local raw plans
        File rawPlansDir = appExternalSynchronizerRawplansFolder;
        File[] rawPlans = rawPlansDir.listFiles();
        ArrayList<String> localRawPlans = new ArrayList<>();
        Log.d(LogTAG,"rawplans here we have: ");
        for (File file : rawPlans) {localRawPlans.add(file.getName()); Log.d(LogTAG,file.getName());}


        String syncUrl = String.format("%s%s:%d%s",event.getScheme(),event.getHostname(),event.getPort(),"/rawplans");
        Request request = new Request.Builder().url(syncUrl).build();
        String rawplansJSON = null;
       // ArrayList<String> remoteRawPlans = new ArrayList<String>();
        ArrayList<File> downloadedFiles = new ArrayList<>();
        ArrayList<String> toDownload = new ArrayList<>();
        try{
            Response response = okHttpClient.newCall(request).execute();
            rawplansJSON = response.body().string();
            Log.d(LogTAG,"rawPlansJSON:");
            Log.d(LogTAG,rawplansJSON);

            JSONArray jsonResponse = new JSONArray(rawplansJSON);
            Log.d(LogTAG,jsonResponse.toString());

            for (int i = 0; i < jsonResponse.length(); i++) {
                String filename = jsonResponse.getString(i);
                if( ! localRawPlans.contains(filename)){
                    String rawurl = String.format("%s%s:%d%s",event.getScheme(),event.getHostname(),event.getPort(),"/rawplans/"+filename);
                    downloadedFiles.add(downloadFile(rawurl,filename));
                }
            }

            for (File f : downloadedFiles){
                Log.d(LogTAG,"file: "+f.getName());
                File rawPlanFile = new File(appExternalSynchronizerRawplansFolder,f.getName());
                moveFile(f,rawPlanFile);
            }
        }
        catch (IOException ex){
            Log.d(LogTAG,ex.getMessage());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        //create new bundles based on plans
    }

    public void recreatePlans(){

        File[] rawPlans = appExternalSynchronizerRawplansFolder.listFiles();
        for(File rawplan : rawPlans){
            Log.d(LogTAG, String.format("posting AddPlanEvent: %s : %s",rawplan.getAbsolutePath(),rawplan.getName() ));
            EventBus.getDefault().post(new AddPlanEvent(rawplan.getAbsolutePath(),rawplan.getName()));
        }
    }

    private void syncMeasures(SyncEvent event){
        EventBus.getDefault().post(new ProgressEvent(0,true,true));
        File measuresDir = PlansFileManager.getInstance().getAppExternalMeasuresFolder();
        File[] measures = measuresDir.listFiles(PlansFileManager.getInstance().measureFilter);
        float current = 0;
        float max = measures.length;


        String syncUrl = String.format("%s%s:%d%s",event.getScheme(),event.getHostname(),event.getPort(),"/measures");
        Request request = new Request.Builder().url(syncUrl).addHeader("content-type","application/json").build();
        //FIXME: could be slow and memory consuming! just send file as binary?!
        String measuresJSON = null;

        try {
            Response response = okHttpClient.newCall(request).execute();
            measuresJSON  = response.body().string();
            Log.d(LogTAG,"measuresJSON!");
            //Log.d(LogTAG,bundlesJSON);
        } catch (java.net.ConnectException e) {
            Log.e(LogTAG, e.getMessage());
            Log.e(LogTAG, "can't connect");
            EventBus.getDefault().post(new ProgressEvent(0, true, false).setText(e.getMessage()));
        } catch (SocketTimeoutException e){
            e.printStackTrace();
            EventBus.getDefault().post(new ProgressEvent(0, true, false).setText(e.getMessage()));
        }
         catch (IOException e) {
            e.printStackTrace();
        }
        for (File measure : measures){
            Log.d(LogTAG,"measure: "+measure.getName());

            RequestBody body = RequestBody.create(JSON, measure);
            //FIXME: dirtyhack
            String syncUrlForMeasure = syncUrl+"/"+measure.getName();
            Request uploadRequest = new Request.Builder()
                    .url(syncUrlForMeasure)
                    .post(body)
                    .build();
            try {
                Response uploadResponse = okHttpClient.newCall(uploadRequest).execute();
                Log.d(LogTAG, uploadResponse.body().string());
            } catch (IOException e) {
                e.printStackTrace();
                EventBus.getDefault().post(new ProgressEvent(0, true, false).setText(e.getMessage()));
            }
            current = current+1;
            EventBus.getDefault().post(new ProgressEvent(current/max,false,true));
        }
        EventBus.getDefault().post(new SyncMeasuresDoneEvent());

    }

    public void syncPlans(SyncPlansEvent event){
        EventBus.getDefault().post(new ProgressEvent(0,true,true));
        File plansDir = PlansFileManager.getInstance().getAppExternalPlansFolder();

        //FIXME: filter is not working and nobody knows why!!!
        //File[] plansNoFilter = plansDir.listFiles();
       // Log.d(LogTAG, String.format("plans with no filter: %d", plansNoFilter.length ));
       // File[] plans = plansDir.listFiles(PlansFileManager.getInstance().planFilter);
        File[] plans = plansDir.listFiles();
        Log.d(LogTAG,String.format("Current plans on terminal: %d ",plans.length));
        String syncUrl = String.format("%s%s:%d%s",event.getScheme(),event.getHostname(),event.getPort(),"/plans");
        Request request = new Request.Builder().url(syncUrl).build();
        String plansRawJSON = null;
        //String rawplansJSON = null;
        float max = plans.length;
        float current = 0;

        try {
            Response response = okHttpClient.newCall(request).execute();
            plansRawJSON = response.body().string();
            JSONArray plansJSON = new JSONArray(plansRawJSON);
            Log.d(LogTAG,"plansJson:");
            Log.d(LogTAG, plansRawJSON);
            //FIXME: do not upload unwanted files
            for(File plan : plans){
                //upload every plan
                Log.d(LogTAG, String.format("Sending file: %s",plan.getName() ));
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", plan.getName(),
                                RequestBody.create(MediaType.parse("image/png"), plan))
                        .build();
              //  Thread.sleep(1000);
                Request uploadRequest = new Request.Builder()
                        .url(syncUrl)
                        .post(requestBody)
                        .addHeader("Connection","close")
                        .build();
                Response uploadResponse = okHttpClient.newCall(uploadRequest).execute();
                Log.d(LogTAG, String.format("upload response %s", uploadResponse.body().string()));
            current = current +1;
                EventBus.getDefault().post(new ProgressEvent(current/max,false,true));
            }
        }
        catch (IOException | NullPointerException ex)
        {
            ex.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
            EventBus.getDefault().post(new ProgressEvent(0,true,false).setText(e.getMessage()));
        }

        for (File plan : plans) {
            Log.d(LogTAG,"plan: "+plan.getName());
        }

        //EventBus.getDefault().post(new ProgressEvent(0,true,false));
        EventBus.getDefault().post(new SyncPlansDoneEvent(plansRawJSON));
    }


    public void syncBundlesWithServer(SyncBundlesEvent event) {
        EventBus.getDefault().post(new ProgressEvent(0,true,true));
        File bundlesDir = PlansFileManager.getInstance().getAppExternalBundlesFolder();
        File[] bundles = bundlesDir.listFiles(PlansFileManager.getInstance().bundleFilter);
        float current = 0;
        float max = bundles.length;

        String syncUrl = String.format("%s%s:%d%s",event.getScheme(),event.getHostname(),event.getPort(),"/bundles");
        Request request = new Request.Builder().url(syncUrl).addHeader("content-type","application/json").build();
        String bundlesJSON = null;
        ArrayList<String> bundles_missing_in_app = new ArrayList<>();
        ArrayList<String> bundles_missin_in_server = new ArrayList<>();
        try {
            Response response = okHttpClient.newCall(request).execute();
            bundlesJSON  = response.body().string();
            Log.d(LogTAG,"bundlesJSON:");
            Log.d(LogTAG,bundlesJSON);
        } catch (java.net.ConnectException e) {
            Log.e(LogTAG, e.getMessage());
            Log.e(LogTAG, "can't connect");
            EventBus.getDefault().post(new ProgressEvent(0,true,false).setText(e.getMessage()));
        }  catch (SocketTimeoutException e) {
            EventBus.getDefault().post(new ProgressEvent(0,true,false).setText(e.getMessage()));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        for (File bundle : bundles){
            Log.d(LogTAG,"bundle: "+bundle.getName());

            RequestBody body = RequestBody.create(JSON, bundle);
            //FIXME: dirtyhack
            String syncUrlForBundle = syncUrl+"/"+bundle.getName();
            Request uploadRequest = new Request.Builder()
                    .url(syncUrlForBundle)
                    .post(body)
                    .build();
            try {
                Response uploadResponse = okHttpClient.newCall(uploadRequest).execute();
                Log.d(LogTAG, uploadResponse.body().string());
            } catch (IOException e) {
                e.printStackTrace();
                EventBus.getDefault().post(new ProgressEvent(0,true,false).setText(e.getMessage()));

            }
            current= current+1;
            EventBus.getDefault().post(new ProgressEvent(current/max,false,true));
        }
        EventBus.getDefault().post(new SyncBundlesDoneEvent(bundlesJSON));

    }
    /*

    public void syncMeasuresWithServer() {

    }
    */


}
