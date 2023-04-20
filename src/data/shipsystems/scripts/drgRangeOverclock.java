package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class drgRangeOverclock extends BaseShipSystemScript {

	public static final float 
		NEW_RANGE = 1600f,
		SPEED_PENALTY = 0.66f;
	
    @Override
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        stats.getEnergyWeaponRangeBonus().modifyMult(id, 1f + effectLevel);
        stats.getWeaponRangeThreshold().modifyFlat(id, NEW_RANGE);
		stats.getWeaponRangeMultPastThreshold().modifyMult(id, 0.0f);
		stats.getMaxSpeed().modifyMult(id, 1 - SPEED_PENALTY);
    }
    
    @Override
	public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getEnergyWeaponRangeBonus().unmodify(id);
        stats.getWeaponRangeThreshold().unmodify(id);
		stats.getWeaponRangeMultPastThreshold().unmodify(id);
		stats.getMaxSpeed().unmodify(id);
	}
    
    @Override
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) 
			return new StatusData("weapon range increased", false);
		if (index == 1) 
			return new StatusData("generating hard flux", true);
		if (index == 2) 
			return new StatusData("-" + (int)(100f * SPEED_PENALTY) + "% top speed", true);

		return null;
	}

}
