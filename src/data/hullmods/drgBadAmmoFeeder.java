package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.WeaponBaseRangeModifier;


public class drgBadAmmoFeeder extends BaseHullMod {

    public static final float
            BALLISTIC_RANGE_MOD = -200f,
            BALLISTIC_FLUX_INCREASE = 0f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getBallisticWeaponFluxCostMod().modifyPercent(id, BALLISTIC_FLUX_INCREASE);
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id)
    {
        ship.addListener(new TigersharkRangeMod());
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0)
            return (int)BALLISTIC_FLUX_INCREASE + "%";
        if (index == 1)
            return (int)-BALLISTIC_RANGE_MOD + "";
        return null;
    }

    static class TigersharkRangeMod implements WeaponBaseRangeModifier
    {
        @Override
        public float getWeaponBaseRangePercentMod(ShipAPI ship, WeaponAPI weapon)
        {
            return 0;
        }

        @Override
        public float getWeaponBaseRangeMultMod(ShipAPI ship, WeaponAPI weapon)
        {
            return 1f;
        }

        @Override
        public float getWeaponBaseRangeFlatMod(ShipAPI ship, WeaponAPI weapon)
        {
            if (weapon.getType() == WeaponAPI.WeaponType.BALLISTIC)
                return BALLISTIC_RANGE_MOD;
            else
                return 0f;
        }
    }
}