package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class drgFighterBays extends BaseHullMod {

    public static final int 
        BOMBER_COST_FLAT = 10001,
        REFIT_TIME_REDUCTION = 20,
        CREW_LOSS_REDUCTION = 50;
    
    // you might be wondering why I use floats some times and ints other times
    // don't worry, I wonder too

    @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getFighterRefitTimeMult().modifyPercent(id, REFIT_TIME_REDUCTION * -1f);
		stats.getDynamic().getMod(Stats.BOMBER_COST_MOD).modifyFlat(id, BOMBER_COST_FLAT);
	}
    

    @Override
    public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
        fighter.getMutableStats().getCrewLossMult().modifyPercent(id, CREW_LOSS_REDUCTION * -1f);
    }

    @Override
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (index == 0) 
            return REFIT_TIME_REDUCTION + "%";
        if (index == 1) 
            return CREW_LOSS_REDUCTION + "%";
        if (index == 2) 
            return "preventing the use of bombers";
		return null;
	}
	
	@Override
	public boolean affectsOPCosts() {
		return true;
	}
}