package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class drgSupercaps extends BaseHullMod {
        
    public static final float 
        ENERGY_WEP_DAMAGE_BONUS = 66,
        ENERGY_WEP_FLUX_BONUS = 25;

    @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getEnergyWeaponDamageMult().modifyPercent(id, ENERGY_WEP_DAMAGE_BONUS);
        stats.getEnergyWeaponFluxCostMod().modifyPercent(id, ENERGY_WEP_FLUX_BONUS);
        
        // apply a proportionate debuff to beams so that their damage is unaffected
        stats.getBeamWeaponDamageMult().modifyMult(id, (1f/(1f+ENERGY_WEP_DAMAGE_BONUS / 100f)));
        stats.getBeamWeaponFluxCostMult().modifyMult(id, (1f/(1f+ENERGY_WEP_FLUX_BONUS / 100f)));
        // beam kiting for eternity is shit
        // use a real gun you lazy fuck
	}

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) 
            return (int)ENERGY_WEP_DAMAGE_BONUS + "%";
        if (index == 1) 
            return (int)ENERGY_WEP_FLUX_BONUS + "%";
        return null;
    }

}
