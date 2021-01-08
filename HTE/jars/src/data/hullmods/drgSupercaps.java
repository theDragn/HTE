package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class drgSupercaps extends BaseHullMod {
        
    public static final float 
        ENERGY_WEP_DAMAGE_BONUS = 66f,
        BEAM_DAMAGE_BONUS = 25f,
        ENERGY_WEP_FLUX_BONUS = 25f;

    @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        
        // this is probably hugely fucky if another mod has something that modifies weapon damage
		stats.getEnergyWeaponDamageMult().modifyPercent(id, ENERGY_WEP_DAMAGE_BONUS);
        stats.getEnergyWeaponFluxCostMod().modifyPercent(id, ENERGY_WEP_FLUX_BONUS);
        
        // apply the inverse of the buff as a debuff to beams so that their damage is unaffected
        stats.getBeamWeaponDamageMult().modifyMult(id, (1f/(1f+ENERGY_WEP_DAMAGE_BONUS / 100f)));

        // then apply the beam buff
        stats.getBeamWeaponDamageMult().modifyPercent(id, BEAM_DAMAGE_BONUS);


	}

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) 
            return (int)ENERGY_WEP_DAMAGE_BONUS + "%";
        if (index == 1) 
            return (int)BEAM_DAMAGE_BONUS + "%";
        if (index == 2)
            return (int)ENERGY_WEP_FLUX_BONUS + "%";
        return null;
    }

}
