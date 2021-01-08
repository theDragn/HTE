package data.weapons.proj;

import com.fs.starfarer.api.Global;
//import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
//import com.fs.starfarer.api.loading.DamagingExplosionSpec;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;
import data.scripts.util.MagicLensFlare;
import org.lazywizard.lazylib.MathUtils;

public class drgPlasmaLensOnHitEffect implements OnHitEffectPlugin {

    private static final float 
        EXPLOSION_DISTANCE = 52f, // distance of each explosion from the previous
        //EXPLOSION_DAMAGE = 500f, // now uses base projectile damage
        LENS_FLARE_THICKNESS = 10f,
        LENS_FLARE_LENGTH = 400f;
    private static final Color LENS_FLARE_FRINGE_COLOR = new Color(128,0,0);
    private static final Color LENS_FLARE_CORE_COLOR = Color.RED;
    private static final int 
        NUM_LENS_FLARES = 3,
        NUM_EXPLOSIONS = 4;


    // damaging explosion graphics are too big and take away from the sparkles
    // would be nice to punch through station armor but it's a fairly small thing
    /*private final DamagingExplosionSpec explosionSpec = new DamagingExplosionSpec(0.05f,
		50,
		25,
		EXPLOSION_DAMAGE,
		EXPLOSION_DAMAGE / 2,
		CollisionClass.PROJECTILE_FF,
		CollisionClass.PROJECTILE_FIGHTER,
		3,
		3,
		0.5f,
		10,
		new Color(33, 255, 122, 255),
		new Color(255, 150, 35, 255)
	);
	{
		explosionSpec.setDamageType(DamageType.HIGH_EXPLOSIVE);
	}*/


    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, CombatEngineAPI engine) {
        if (target instanceof ShipAPI) {
            ShipAPI sourceShip = projectile.getSource();

            if (sourceShip != null) {

                WeaponAPI weaponFrom = projectile.getWeapon();
                float explosionDamage = projectile.getDamageAmount() / 2f;
                if (weaponFrom != null && !shieldHit) {

                    // get projectile location and velocity, normalize it
                    Vector2f projectileVelocity = projectile.getVelocity();
                    projectileVelocity = projectileVelocity.normalise(projectileVelocity);

                    for (int i = 1; i <= NUM_EXPLOSIONS; i++)
                    {
                        Vector2f explosionLoc = new Vector2f(point.getX() + projectileVelocity.getX() * i * EXPLOSION_DISTANCE, point.getY() + projectileVelocity.getY() * i * EXPLOSION_DISTANCE);
                        //engine.spawnDamagingExplosion(explosionSpec, sourceShip, explosionLoc);
                        engine.spawnExplosion(explosionLoc, new Vector2f(projectile.getVelocity().getX() * 250f, projectile.getVelocity().getY() * 250f), new Color(225, 200, 50, 200), explosionDamage / 70f, 0.5f);
                        engine.applyDamage(target, explosionLoc, explosionDamage, DamageType.HIGH_EXPLOSIVE, 0, false, false, sourceShip);

                        for (int j = 0; j < NUM_LENS_FLARES; j++)
                        {
                            MagicLensFlare.createSharpFlare(engine, (ShipAPI)target, MathUtils.getRandomPointInCircle(explosionLoc, 50), LENS_FLARE_THICKNESS, LENS_FLARE_LENGTH, 0, LENS_FLARE_FRINGE_COLOR, LENS_FLARE_CORE_COLOR);
                        }

                        

                    }
                    Global.getSoundPlayer().playSound("hit_solid", 1f, 1f,target.getLocation(), target.getVelocity());
                }
            }
        }
    }
}
