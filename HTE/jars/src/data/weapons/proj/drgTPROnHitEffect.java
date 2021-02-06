package data.weapons.proj;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

public class drgTPROnHitEffect implements OnHitEffectPlugin
{

    private static final float EXPLOSION_DAMAGE_MULT = 0.25f;

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit,
            CombatEngineAPI engine)
    {
        if (target instanceof ShipAPI)
        {
            ShipAPI sourceShip = projectile.getSource();
            if (sourceShip != null)
            {
                WeaponAPI weaponFrom = projectile.getWeapon();
                float explosionDamage = projectile.getDamageAmount() * EXPLOSION_DAMAGE_MULT;
                if (weaponFrom != null && !shieldHit)
                {
                    engine.addSmoothParticle(point, new Vector2f(), 250, 1.5f, 0.5f, Color.blue);
                    engine.addSmoothParticle(point, new Vector2f(), 200, 2f, 0.33f, Color.cyan);
                    engine.applyDamage(target, point, explosionDamage, DamageType.ENERGY, 0, false, false, sourceShip);
                    Global.getSoundPlayer().playSound("hit_solid", 1f, 1f, target.getLocation(), target.getVelocity());
                }
            }
        }
    }
}
