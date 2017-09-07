package pl.mysteq.software.rssirecordernew;


import android.graphics.Point;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import pl.mysteq.software.rssirecordernew.managers.SectorManager;
import pl.mysteq.software.rssirecordernew.structures.MeasurePoint;
import pl.mysteq.software.rssirecordernew.structures.Sector;

import static org.junit.Assert.*;
/**
 * Created by mysteq on 2017-09-05.
 */

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class SectorManagerTest {


    @Test
    public void countingSectors_isOkay(){

        SectorManager sectorManager = new SectorManager();
        Sector sector1 = new Sector(new Point(3,4));
        Sector sector2 = new Sector(new Point(3,5));
        Sector sector3 = new Sector(new Point(3,5));
        sectorManager.insertSector(sector1);
        sectorManager.insertSector(sector2);
        sectorManager.insertSector(sector3);
        assertEquals(2,sectorManager.countAllSectors());

    }

    @Test
    public void getSector_isOkay(){
        SectorManager sectorManager = new SectorManager();
        Sector sector1 = new Sector(new Point(1,7));

    }
    @Test
    public void replacingSector_isNotRepleacable(){
        SectorManager sectorManager = new SectorManager();
        Sector sector1 = new Sector(new Point(3,4));
        Sector sector2 = new Sector(new Point(3,5));
        sector2.insertMeasurePoint(new MeasurePoint());
        sectorManager.insertSector(sector1);
        sectorManager.insertSector(sector2);
        //Sector sector3 = new Sector(new Point(3,5));
        assertFalse(sectorManager.getSector(sector2.getCoordinates()).isEmptySector());
    }
}
