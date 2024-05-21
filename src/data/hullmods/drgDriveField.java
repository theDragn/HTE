package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import plugins.drgFleetStatManager;
import plugins.drgModPlugin;

public class drgDriveField extends BaseHullMod {

    public static final float 
        ACCELERATION_BONUS = 0.75f,
        PROFILE_BONUS = 0.2f;

    @Override
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (index == 0) 
            return (int)(ACCELERATION_BONUS * 100f) + "%";
        if (index == 1)
            return (int)(PROFILE_BONUS * 100f) + "%";
		return null;
	}
}



