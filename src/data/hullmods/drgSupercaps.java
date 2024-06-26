package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class drgSupercaps extends BaseHullMod
{

    public static final float ENERGY_WEP_DAMAGE_BONUS = 50f;
    public static final float ENERGY_WEP_FLUX_BONUS = 50f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id)
    {
        stats.getEnergyWeaponDamageMult().modifyPercent(id, ENERGY_WEP_DAMAGE_BONUS);
        stats.getEnergyWeaponFluxCostMod().modifyPercent(id, ENERGY_WEP_FLUX_BONUS);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize)
    {
        if (index == 0)
            return (int) ENERGY_WEP_DAMAGE_BONUS + "%";
        if (index == 1)
            return (int) ENERGY_WEP_FLUX_BONUS + "%";
        return null;
    }

}
