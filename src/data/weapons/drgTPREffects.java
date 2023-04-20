package data.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import data.drgUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;

/**
 * There will be two instances of this- one that gets created per projectile for the onHit, and one per weapon for the every frame effect
 * you could split these into two scripts if it makes it easier; there's just no reason to do that here because neither plugin has data members.
 */
public class drgTPREffects implements EveryFrameWeaponEffectPlugin, OnHitEffectPlugin
{
    private static final float ARMOR_BONUS_FRACTION = 1.25f;
    private boolean runOnce = false;

    // these are just graphical effects
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine)
    {
        if (target instanceof ShipAPI)
        {
            ShipAPI sourceShip = projectile.getSource();
            if (sourceShip != null)
            {
                if (!shieldHit)
                {
                    Global.getSoundPlayer().playSound("hit_solid", 1f, 1f, target.getLocation(), target.getVelocity());
                }
                drgUtils.plasmaEffects(projectile, new Color(0,0,230), 3, 80f);
            }
        }
    }

    // using an every frame effect to apply the listener
    // per Alex's comments in API code, always apply listeners to ships instead of the engine if possible
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon)
    {
        if (engine.isPaused() || weapon == null)
            return;
        if (!runOnce)
        {
            runOnce = true;
            ShipAPI ship = weapon.getShip();
            if (!ship.hasListenerOfClass(drgTPRDamageMod.class))
            {
                ship.addListener(new drgTPRDamageMod());
            }
        }

    }
    public static class drgTPRDamageMod implements DamageDealtModifier
    {
        public drgTPRDamageMod()
        {

        }
        /**
         * @param param
         * This is the thing that's dealing the damage. DamagingProjectileAPI, BeamAPI, or MissileAPI
         * @param target
         * What the damage is getting applied to
         * @param damage
         * Damage amount and type.
         * @param point
         * Location where the hit is occurring.
         * @param shieldHit
         * True if the damage is being applied to shields.
         * @return
         * Should return the ID of the modifier. This is the same as the id argument for any of the stat modifiers.
         */
        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit)
        {
            // in this case, we're only modifying the damage from TPR projectiles on armor hits
            if (param instanceof DamagingProjectileAPI && !shieldHit)
            {
                WeaponAPI weapon = ((DamagingProjectileAPI)param).getWeapon();
                if (weapon == null)
                    return null;
                if (weapon.getId().contains("drg_tpr"))
                {
                    damage.getModifier().modifyMult("drg_tpr", ARMOR_BONUS_FRACTION);
                    return "drg_tpr";
                }
            }
            return null;
        }
    }
}
