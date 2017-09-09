package pl.mysteq.software.rssirecordernew;


import android.graphics.Point;
import android.os.Environment;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLog;

import pl.mysteq.software.rssirecordernew.managers.SectorManager;
import pl.mysteq.software.rssirecordernew.structures.MeasurePoint;
import pl.mysteq.software.rssirecordernew.structures.Sector;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by mysteq on 2017-09-05.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class ExternalStorageTest {


    @Before
    public void setUp() {
        ShadowLog.stream = System.out;

    }

    @Test
    public void getExternalStorage() {

    }




}
