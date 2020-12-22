package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class drgFighterBays extends BaseHullMod {

    public static final int BOMBER_COST_PERCENT = 100;
    public static final int REFIT_TIME_REDUCTION = 15;
    public static final int CREW_LOSS_REDUCTION = 25;

    @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getFighterRefitTimeMult().modifyPercent(id, REFIT_TIME_REDUCTION * -1f);
		stats.getDynamic().getMod(Stats.BOMBER_COST_MOD).modifyPercent(id, BOMBER_COST_PERCENT);
	}
    

    @Override
    public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
        fighter.getMutableStats().getCrewLossMult().modifyPercent(id, CREW_LOSS_REDUCTION * -1f);
    }

    @Override
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (index == 0) 
            return (int)REFIT_TIME_REDUCTION + "%";
        if (index == 1) 
            return CREW_LOSS_REDUCTION + "%";
        if (index == 2) 
            return BOMBER_COST_PERCENT + "%";
		return null;
	}
	
	@Override
	public boolean affectsOPCosts() {
		return true;
	}
}



