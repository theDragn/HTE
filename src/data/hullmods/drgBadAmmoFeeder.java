package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;


public class drgBadAmmoFeeder extends BaseHullMod {

    public static final float
            BALLISTIC_ROF_PENALTY = 15,
            BALLISTIC_FLUX_INCREASE = 15,
            BALLISTIC_RANGE_PENALTY = 15;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponFluxCostMod().modifyPercent(id, BALLISTIC_FLUX_INCREASE);
        stats.getBallisticRoFMult().modifyPercent(id, BALLISTIC_ROF_PENALTY * -1f);
        stats.getBallisticWeaponRangeBonus().modifyPercent(id, BALLISTIC_RANGE_PENALTY * -1f);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0)
            return "-" + (int)BALLISTIC_ROF_PENALTY + "%";
        if (index == 1)
            return (int)BALLISTIC_FLUX_INCREASE + "%";
        if (index == 2)
            return "-" + (int)BALLISTIC_RANGE_PENALTY + "%";
        return null;
    }
}