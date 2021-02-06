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
    public static final float RELOAD_TIME = 50f;

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
    public boolean isApplicableToShip(ShipAPI ship) {
        return false;
    }
    
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (Global.getCombatEngine().isPaused() || ship.isHulk()) 
        {
            return;
        }

        timer.advance(amount);

        if (timer.intervalElapsed())
        {
            float ammoMult = ship.getMutableStats().getMissileAmmoBonus().computeEffective(100) / 100f;
            for (WeaponAPI weapon : ship.getUsableWeapons())
            {
                if (weapon.getType() == WeaponType.MISSILE && weapon.usesAmmo() && weapon.getMaxAmmo() / ammoMult > 1)
                {
                    weapon.setAmmo(Math.min(weapon.getAmmo() + (int)Math.ceil(weapon.getMaxAmmo() * RELOAD_PERCENT / ammoMult), weapon.getMaxAmmo()));
                }
            }
        }
    }
}
