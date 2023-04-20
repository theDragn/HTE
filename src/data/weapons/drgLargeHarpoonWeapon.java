package data.weapons;

import com.fs.starfarer.api.combat.*;

public class drgLargeHarpoonWeapon implements EveryFrameWeaponEffectPlugin
{
    private static final int SHOTS_TIL_MINIMUM = 5;
    private static final float ROF_BONUS = 12.5f;
    private static final float SECS_TO_COOLDOWN = 10f;
    private float shotsFired = 0;
    private float BASE_CD;
    private boolean initDone = false;
    private ShipAPI ship;


    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon)
    {
        if (engine.isPaused() || weapon == null)
        {
            return;
        }
        if (!initDone)
        {
            initDone = true;
            ship = weapon.getShip();
            BASE_CD = weapon.getCooldown();
        }

        if (weapon.getCooldownRemaining() == 0)
        {
            shotsFired -= amount * SHOTS_TIL_MINIMUM / SECS_TO_COOLDOWN;
            if (shotsFired < 0)
                shotsFired = 0;
        }

        if (weapon.isFiring())
        {
            if (weapon.getChargeLevel() != 1)
                return;
            if (shotsFired > SHOTS_TIL_MINIMUM)
                shotsFired = SHOTS_TIL_MINIMUM;
            float rof = (1/ship.getMutableStats().getEnergyRoFMult().computeMultMod());
            float rofMult = 1/(ROF_BONUS * (SHOTS_TIL_MINIMUM - shotsFired)/SHOTS_TIL_MINIMUM);
            shotsFired++;
            weapon.setRemainingCooldownTo(BASE_CD * rof * rofMult);
        }
    }
}

