package pl.mysteq.software.rssirecordernew;


import android.graphics.Point;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowLog;

import java.util.ArrayList;

import pl.mysteq.software.rssirecordernew.extendables.SectorPoint;
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


    @Before
    public void setUp()
    {
        ShadowLog.stream = System.out;

    }



    @Test
    public void countingSectors_isOkay(){

       // Robolectric.bindShadowClass(ShadowLog.class);

        SectorManager sectorManager = new SectorManager();
        Sector sector1 = new Sector(new SectorPoint(3,4));
        Sector sector2 = new Sector(new SectorPoint(3,5));
        //Sector sector3 = new Sector(new Point(3,5));
        sectorManager.insertSector(sector1);
        sectorManager.insertSector(sector2);
        //sectorManager.insertSector(sector3);
        assertEquals(2,sectorManager.countAllSectors());

    }

    @Test
    public void getSector_isOkay(){
        SectorManager sectorManager = new SectorManager();
        Sector sector1 = new Sector(new SectorPoint(1,7));
        sectorManager.insertSector(sector1);
        assertNotNull(sectorManager.getSector(new SectorPoint(1,7)));

    }
    @Test
    public void replacingSector_isNotRepleacable(){
        SectorManager sectorManager = new SectorManager();
        Sector sector1 = new Sector(new SectorPoint(3,4));
        Sector sector2 = new Sector(new SectorPoint(3,5));
        sector2.insertMeasurePoint(new MeasurePoint());
        sectorManager.insertSector(sector1);
        sectorManager.insertSector(sector2);
        //Sector sector3 = new Sector(new Point(3,5));
        assertFalse(sectorManager.getSector(sector2.getCoordinates()).isEmptySector());
    }
    @Test
    public void queryingEmptyRow(){
        SectorManager sectorManager = new SectorManager();
        Sector sector1 = new Sector(new SectorPoint(4,5));
        //Sector sector2 = new Sector(new Point(4,6));
        sectorManager.insertSector(sector1);
        assertNotNull(sectorManager.getSector(new SectorPoint(4,6)));
    }

    @Test(expected = IllegalStateException.class)
    public void replaceSector_ShouldReturnException()
    {
        SectorManager sectorManager = new SectorManager();
        Sector sector1 = new Sector(new SectorPoint(4,5));
        sectorManager.insertSector(sector1);
       sectorManager.insertSector(new Sector(new SectorPoint(4,5)));

       // sectorManager.insertSector(sector1);
       // sectorManager.insertSector(sector2);
       // assertNotNull(sectorManager.getSector(new Point(4,6)));
    }

    @Test
    public void queryingEmptySector(){
        SectorManager sectorManager = new SectorManager();
        Sector sector1 = new Sector( new SectorPoint(5,10));
        sectorManager.insertSector(sector1);
        assertNotNull( sectorManager.getSector(new SectorPoint(5,8)));
    }

    @Test
    public void testTimeOfCreation(){
        SectorManager sectorManager = new SectorManager();
    }


    @Test
    public void testMeasures(){

        SectorManager sectorManager = new SectorManager();
        sectorManager.setCurrentSectorPoint(new SectorPoint(100,100));
        Sector sector =  sectorManager.getCurrentSector();
        sector.insertMeasurePoint(new MeasurePoint());
        sector.insertMeasurePoint(new MeasurePoint());
        ArrayList<MeasurePoint> ms = sectorManager.getAllMeasures();

        assertEquals(ms.size(),2);

    }

}
