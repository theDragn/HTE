package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import plugins.drgFleetStatManager;
import plugins.drgModPlugin;

public class drgDriveField extends BaseHullMod {

    public static final float 
        ACCELERATION_BONUS = 0.5f,
        ACCELERATION_CAP = 2.0f,
        PROFILE_BONUS = 0.075f,
        PROFILE_CAP = 0.3f,
        MAINTENANCE_INCREASE = 0.5f;

    // plugins.drgFleetStatManager applies the fleetwide stat changes
    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) 
    {
        drgModPlugin.checkPluginLoading();
        stats.getSuppliesPerMonth().modifyMult(id, 1f + MAINTENANCE_INCREASE);
	}    

    @Override
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (index == 0) 
            return (int)(ACCELERATION_BONUS * 100f) + "%";
        if (index == 1) 
            return (int)(ACCELERATION_CAP * 100f) + "%";
        if (index == 2) 
            return (int)(PROFILE_BONUS * 100f) + "%";
        if (index == 3) 
            return (int)(PROFILE_CAP * 100f) + "%";
        if (index == 4) 
            return (int)(MAINTENANCE_INCREASE * 100f) + "%";
        if (index == 5)
            try
            {
                return (int) (drgFleetStatManager.getBonus() * 100f) + "%";
            } catch (NullPointerException e)
            {
                return ("0% (in mission mode)");
            }
		return null;
	}
}



