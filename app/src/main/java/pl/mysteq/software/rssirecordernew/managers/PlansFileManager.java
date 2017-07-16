package pl.mysteq.software.rssirecordernew.managers;

import android.os.Environment;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import pl.mysteq.software.rssirecordernew.events.AddPlanEvent;
import pl.mysteq.software.rssirecordernew.events.BundlesReloadedEvent;
import pl.mysteq.software.rssirecordernew.events.CreateBundleEvent;
import pl.mysteq.software.rssirecordernew.events.ReloadBundlesEvent;
import pl.mysteq.software.rssirecordernew.structures.MeasureBundle;
import pl.mysteq.software.rssirecordernew.structures.MeasurePoint;
import pl.mysteq.software.rssirecordernew.structures.PlanBundle;

/**
 * Created by mysteq on 2017-04-09.
 */

public final class PlansFileManager {

    private static PlansFileManager instance = null;
    private  static final String LogTAG = "PlansFileManager";

    public static final String SHAREDPREF = "selected_bundle_shared_pref_key";

    public static final String plan_interfix = ".plan";
    public static final String bundle_suffix = ".bundle.json";
    public static final String measure_suffix = ".measure.json";
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

    public FilenameFilter bundleFilter = null;
    public FilenameFilter planFilter = null;
    public FilenameFilter measureFilter = null;

    protected ArrayList<PlanBundle> bundlesContainerList = null;

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
                return filename.matches(".*\\.plan\\.(jpg)|(png)|(bmp)$");
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

        bundlesContainerList = new ArrayList<PlanBundle>();

        EventBus.getDefault().register(this);
        Log.d(LogTAG,"constructed");
    }

    public ArrayList<PlanBundle> getBundles(){
        return this.bundlesContainerList;
    }
    @Override
    protected void finalize() throws Throwable {
        EventBus.getDefault().unregister(this);
        super.finalize();
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

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onMessage(AddPlanEvent event){
        Log.d(LogTAG,String.format("Received AddPlanEvent: %s", event.getImageFilePath()));
        //wez plik wejsciowy, skopiuj pod nazwa zwiazana z zawartoscia
        File srcFile = new File(event.getImageFilePath());
        File planFile = FileManager.copyFileNamedDigest(this.appExternalPlansFolder, plan_interfix, srcFile);
        //String digest = destFile.getName().split("\\.")[0];
        Log.d(LogTAG,String.format("Sending event CreateBundleEvent: %s %s",planFile.getAbsolutePath(),event.getName()));
        EventBus.getDefault().post(new CreateBundleEvent(planFile, event.getName()));


    }
    /*
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onMessage(ReloadPlansEvent event)
    {
        Log.d(LogTAG,"Received ReloadPlansEvent");
        //przeczytaj wszystkie plany
        //zobacz czy sa do nich bundle
        File[] plans = this.appExternalPlansFolder.listFiles(this.planFilter);
        Log.d(LogTAG,String.format("Plans count: %d",plans.length));
        for(File plan : plans)
        {
            //cos typu da39a3ee5e6b4b0d3255bfef95601890afd80709.plan.jpg
            String digest = plan.getName().split("\\.")[0]; //da39a3ee5e6b4b0d3255bfef95601890afd80709
            String bundleName = String.format("%s%s",digest,bundle_suffix);
            File bundleFile = new File(this.appExternalBundlesFolder,bundleName);
            if(bundleFile.exists()) {
                Log.d(LogTAG,String.format("Bundle %s exists",bundleFile.getName()));
            } else {
                Log.w(LogTAG,String.format("Bundle %s should be created",bundleFile.getName()));
                //creating bundle
            }

        }
    }
    */
    @Subscribe(threadMode = ThreadMode.ASYNC,sticky = true)
    public void onMessage(ReloadBundlesEvent event){
        Log.d(LogTAG,"Received ReloadBundlesEvent");
        PlanBundle planBundle = null;
        JsonPlanBundleReader jsonPlanBundleReader = new JsonPlanBundleReader();
        File[] bundles = this.appExternalBundlesFolder.listFiles(this.bundleFilter);
        this.bundlesContainerList.clear();
        for (File file : bundles) {
            planBundle = jsonPlanBundleReader.run(file);
            Log.d(LogTAG,"Read from storage bundle: "+planBundle.getPlanBundleName());
            this.bundlesContainerList.add(planBundle);
        }
        EventBus.getDefault().post(new BundlesReloadedEvent());
    }

    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onMessage(CreateBundleEvent event){
        Log.d(LogTAG,String.format("Received CreateBundleEvent: %s %s",event.getPlanFile().getName(),event.getName()));
        String digest = event.getPlanFile().getName().split("\\.")[0];
        PlanBundle planBundle = new PlanBundle();
        File bundleFile = new File(this.appExternalBundlesFolder,String.format("%s%s",digest,bundle_suffix));
        if(bundleFile.exists()) {
            Log.d(LogTAG,"bundleExists: "+bundleFile.getName());
            return;
        }
        planBundle.setBuildingPlanFileName(event.getPlanFile().getName());
        planBundle.setPlanBundleName(event.getName() == null ? digest : event.getName());
        planBundle.setMeasuresFileNames(new ArrayList<String>());
        //TODO: singleton instance!
        JsonPlanBundleWriter jsonPlanBundleWriter = new JsonPlanBundleWriter(planBundle,bundleFile);
        jsonPlanBundleWriter.run();
        Log.d(LogTAG,"Json wrote data!");
        EventBus.getDefault().post(new ReloadBundlesEvent());

    }

    public PlanBundle getBundleByName(String name){
        for (PlanBundle planBundle : this.bundlesContainerList)
        {
            if(planBundle.getPlanBundleName().equals(name))
            {
                return planBundle;
            }
        }
        return null;
    }
    public ArrayList<PlanBundle> getAllBundles(){
        return this.bundlesContainerList;
    }
    public File getBundlePlanFile(PlanBundle planBundle){
        return new File(getAppExternalPlansFolder(),planBundle.getBuildingPlanFileName());
    }


public MeasureBundle generateNewMeasureBundle(String _planBundleName){
       // String uuid = UUID.randomUUID().toString();
       // String filepath = uuid+measure_suffix;
        //PlanBundle _planBundle = null;
        PlanBundle tempPlanBundle = null;
        JsonPlanBundleReader jsonPlanBundleReader = new JsonPlanBundleReader();

        File[] bundles = this.appExternalBundlesFolder.listFiles(this.bundleFilter);
       // PlanBundle planBundle = null;
        for (File bundle : bundles){
            tempPlanBundle = jsonPlanBundleReader.run(bundle);
            if(tempPlanBundle.getPlanBundleName().equals(_planBundleName)){
                MeasureBundle measureBundle = new MeasureBundle(_planBundleName);
                //tempPlanBundle.addMeasureFilename(filepath);
                tempPlanBundle.addMeasureFilename(measureBundle.getFilepath());
                JsonPlanBundleWriter jsonPlanBundleWriter = new JsonPlanBundleWriter(tempPlanBundle,bundle);
                jsonPlanBundleWriter.run();
                //File measureFile = new File(getAppExternalMeasuresFolder(),filepath);
                JsonMeasuresWriter jsonMeasuresWriter = new JsonMeasuresWriter(measureBundle);
                jsonMeasuresWriter.run();

                return measureBundle;
            }
        }
        return null;
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void saveMeasures(ArrayList<MeasurePoint> measurePoints,String filename){
       Log.d(LogTAG,"Saving measures!: ");
        throw new RuntimeException("saving measures via old method!");
        //File fullPath = new File(getAppExternalMeasuresFolder(),filepath);
       // JsonMeasuresWriter jsonMeasuresWriter = new JsonMeasuresWriter(measurePoints,fullPath);
      //  jsonMeasuresWriter.run();
       // Log.d(LogTAG,"measures saved to: "+fullPath.getAbsolutePath());
    }

    public File getMeasureFile(String measureName){
        return new File(getAppExternalMeasuresFolder(),measureName);
    }






}
