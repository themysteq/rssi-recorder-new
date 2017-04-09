package pl.mysteq.software.rssirecordernew.events;

import java.util.ArrayList;

import pl.mysteq.software.rssirecordernew.structures.PlanBundle;

/**
 * Created by mysteq on 2017-04-09.
 */

public class PlansReloadedEvent {
    private ArrayList<PlanBundle> plansList;

    public PlansReloadedEvent(ArrayList<PlanBundle> plansList){
        this.plansList = plansList;
    }
    public ArrayList<PlanBundle> getData()
    {
        return this.plansList;
    }

}
