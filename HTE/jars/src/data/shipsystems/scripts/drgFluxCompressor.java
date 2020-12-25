package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.combat.ShipAPI;

public class drgFluxCompressor extends BaseShipSystemScript {

	public static final float 
		HARD_FLUX_REDUCTION = 0.2f,    // removes this fraction of hard flux
        SOFT_FLUX_REDUCTION = 0.4f,     // removes this fraction of soft flux
        MAX_FLUX_LEVEL_BOOST = 1.0f,   // maximum boost scaling at this level 
        BASE_SPEED_FLAT = 50f,          // base flat bonus to top speed
        BASE_MANEUVERING_PERCENT = 125f,// base percent bonus to maneuvering
        BOOST_PERCENT = 2.5f;           // base is multiplied by this at maximum flux level

    private boolean isActive = false;
    private float fluxOnActivation = 0f; // percentage of flux at activation
    private float boostMult = 0f;

    @Override
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        ShipAPI ship = (ShipAPI)stats.getEntity();

        // this if statement gets run only when it's initially activated
        if (!isActive)
        {
            isActive = true;
            FluxTrackerAPI fluxtracker = ship.getFluxTracker();
            fluxOnActivation = fluxtracker.getFluxLevel();
            if (fluxOnActivation > MAX_FLUX_LEVEL_BOOST)
                fluxOnActivation = MAX_FLUX_LEVEL_BOOST;
            boostMult = 1.0f + BOOST_PERCENT * (fluxOnActivation / MAX_FLUX_LEVEL_BOOST);
            fluxtracker.setHardFlux(fluxtracker.getHardFlux() * (1 - HARD_FLUX_REDUCTION));
            fluxtracker.setCurrFlux(fluxtracker.getCurrFlux() * (1 - SOFT_FLUX_REDUCTION));
        }
        stats.getMaxSpeed().modifyFlat(id, BASE_SPEED_FLAT * boostMult * effectLevel);
        stats.getAcceleration().modifyPercent(id, BASE_MANEUVERING_PERCENT * boostMult * (effectLevel + 0.5f));
        stats.getDeceleration().modifyPercent(id, BASE_MANEUVERING_PERCENT * boostMult * (effectLevel + 0.5f)); // these effectLevels need to be higher than the others or you just yeet yourself
        stats.getTurnAcceleration().modifyPercent(id, BASE_MANEUVERING_PERCENT * boostMult * effectLevel);
        stats.getMaxTurnRate().modifyPercent(id, BASE_MANEUVERING_PERCENT * boostMult * effectLevel);
    }
    
    @Override
	public void unapply(MutableShipStatsAPI stats, String id) {
        stats.getMaxSpeed().unmodify(id);
        stats.getAcceleration().unmodify(id);
        stats.getDeceleration().unmodify(id);
        stats.getTurnAcceleration().unmodify(id);
        stats.getMaxTurnRate().unmodify(id);
        boostMult = 0f;
        fluxOnActivation = 0f;
        isActive = false;
	}
    
    @Override
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) 
			return new StatusData("+" + (int)(BASE_SPEED_FLAT * boostMult * effectLevel) + " top speed", false);
		if (index == 1) 
			return new StatusData("+" + (int)(BASE_MANEUVERING_PERCENT * boostMult * effectLevel) + "% maneuverability", false);
		return null;
	}

}
