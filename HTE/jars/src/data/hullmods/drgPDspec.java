package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class drgPDspec extends BaseHullMod {

    public static final int 
        PD_RANGE_BONUS = 300, 
        THRESHOLD_AT = 1200;
    // you might be wondering why I use floats some times and ints other times
    // don't worry, I wonder too


    @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getNonBeamPDWeaponRangeBonus().modifyFlat(id, PD_RANGE_BONUS);
        stats.getBeamPDWeaponRangeBonus().modifyFlat(id, PD_RANGE_BONUS);
        stats.getWeaponRangeThreshold().modifyFlat(id, 1200);
        stats.getWeaponRangeMultPastThreshold().modifyMult(id, 0f);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (index == 0)
            return Integer.toString(PD_RANGE_BONUS);
        if (index == 1)
            return Integer.toString(THRESHOLD_AT);
        return null;
    }

    @Override
    public boolean affectsOPCosts() {
        return true;
    }

}
