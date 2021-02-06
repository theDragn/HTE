package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class drgWeaponOverdrive extends BaseShipSystemScript
{

    public static final float FIRE_RATE_MULT = 1.5f;
    public static final float MISSILE_FIRE_RATE_MULT = 2f;
    public static final float FLUX_REDUCTION = 33f;
    public static final float MISSILE_COOLDOWN_PER_SEC = 0.2f;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel)
    {
        stats.getBallisticRoFMult().modifyMult(id, FIRE_RATE_MULT);
        stats.getEnergyRoFMult().modifyMult(id, FIRE_RATE_MULT);
        stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -FLUX_REDUCTION);
        stats.getEnergyWeaponFluxCostMod().modifyPercent(id, -FLUX_REDUCTION);
        stats.getMissileRoFMult().modifyMult(id, MISSILE_FIRE_RATE_MULT);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id)
    {
        stats.getBallisticRoFMult().unmodify(id);
        stats.getEnergyRoFMult().unmodify(id);
        stats.getBallisticWeaponFluxCostMod().unmodify(id);
        stats.getEnergyWeaponFluxCostMod().unmodify(id);
        stats.getMissileRoFMult().unmodify(id);
    }

    @Override
    public StatusData getStatusData(int index, State state, float effectLevel)
    {
        if (index == 0)
            return new StatusData("weapon rate of fire +" + (int) (FIRE_RATE_MULT * 100f - 100f) + "%", false);
        if (index == 1)
            return new StatusData("weapon flux use -" + (int) (FLUX_REDUCTION) + "%", false);
        return null;
    }

}
