package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.IntervalUtil;

public class drgMissileForge extends BaseHullMod {
    
    public static final float RELOAD_PERCENT = 0.10f;
    public static final float RELOAD_TIME = 60f;

    private IntervalUtil timer = new IntervalUtil(RELOAD_TIME, RELOAD_TIME);
    
    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0)
            return (int)(RELOAD_PERCENT * 100f) + "%";
        if (index == 1)
            return (int)(RELOAD_TIME) + " seconds";
        return null;
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id)
    {
        for (WeaponAPI wep : ship.getAllWeapons())
        {
            if (wep.getType().equals(WeaponType.MISSILE))
            {
                wep.getAmmoTracker().setAmmoPerSecond(wep.getAmmoPerSecond() + (wep.getMaxAmmo() * RELOAD_PERCENT) / 60f);
                wep.getAmmoTracker().setReloadSize(wep.getSpec().getBurstSize());
            }
        }
    }
}
