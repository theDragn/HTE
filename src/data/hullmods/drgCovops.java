package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class drgCovops extends BaseHullMod
{

    public static final float SENSOR_STRENGTH_BONUS = 50f;
    public static final float SENSOR_PROFILE_REDUCTION = 50f;
    public static final float SPEED_BOOST_FLAT = 10f;
    public static final float ZERO_FLUX_BOOST_FLAT = 10f;
    public static final float ZERO_FLUX_LEVEL_BONUS = 0.015f;
    public static final float MAINTENANCE_INCREASE = 100f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id)
    {
        
        stats.getSensorStrength().modifyPercent(id, SENSOR_STRENGTH_BONUS);
        stats.getSensorProfile().modifyPercent(id, -SENSOR_PROFILE_REDUCTION);
        stats.getMaxSpeed().modifyFlat(id, SPEED_BOOST_FLAT);
        stats.getZeroFluxSpeedBoost().modifyFlat(id, ZERO_FLUX_BOOST_FLAT);
        stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, ZERO_FLUX_LEVEL_BONUS);
        stats.getSuppliesPerMonth().modifyPercent(id, MAINTENANCE_INCREASE);

    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize)
    {
        if (index == 0)
            return (int)SENSOR_STRENGTH_BONUS + "%";
        if (index == 1)
            return (int)SENSOR_PROFILE_REDUCTION + "%";
        if (index == 2)
            return Integer.toString((int)SPEED_BOOST_FLAT);
        if (index == 3)
            return Integer.toString((int)ZERO_FLUX_BOOST_FLAT);
        if (index == 4)
            return (ZERO_FLUX_LEVEL_BONUS * 100f) + "%";
        if (index == 5)
            return (int)MAINTENANCE_INCREASE + "%";
        return null;
    }

}
