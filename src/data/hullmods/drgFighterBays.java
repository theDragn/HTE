package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class drgFighterBays extends BaseHullMod {

    public static final int 
        BOMBER_COST_FLAT = 10001,
        CREW_LOSS_REDUCTION = 100;
    
    // you might be wondering why I use floats some times and ints other times
    // don't worry, I wonder too

    @Override
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getDynamic().getMod(Stats.BOMBER_COST_MOD).modifyFlat(id, BOMBER_COST_FLAT);
	}
    

    @Override
    public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
        fighter.getMutableStats().getCrewLossMult().modifyMult(id, 0f);
    }

    @Override
	public String getDescriptionParam(int index, HullSize hullSize, ShipAPI ship) {
        if (index == 0)
            return "preventing any casualties due to fighter losses.";
        if (index == 1)
            return "incapable of fabricating the heavy weaponry used in bombers.";
		return null;
	}
	
	@Override
	public boolean affectsOPCosts() {
		return true;
	}
}