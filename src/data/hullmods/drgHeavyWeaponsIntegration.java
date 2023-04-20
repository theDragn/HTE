package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class drgHeavyWeaponsIntegration extends BaseHullMod {

	public static final float COST_REDUCTION_ENERGY  = 10;
    public static final float COST_REDUCTION_OTHER = 7;
	
    @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.LARGE_ENERGY_MOD).modifyFlat(id, -COST_REDUCTION_ENERGY);
        stats.getDynamic().getMod(Stats.LARGE_MISSILE_MOD).modifyFlat(id, -COST_REDUCTION_OTHER);
		stats.getDynamic().getMod(Stats.LARGE_BALLISTIC_MOD).modifyFlat(id, -COST_REDUCTION_OTHER);
	}
	
    @Override
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) 
            return "" + (int) COST_REDUCTION_ENERGY + "";
        if (index == 1) 
            return "" + (int) COST_REDUCTION_OTHER + "";
		return null;
	}

	@Override
	public boolean affectsOPCosts() {
		return true;
	}

}