package data.weapons.proj;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import java.awt.Color;
import java.util.Random;

import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.util.Misc;
import data.drgUtils;
import org.lwjgl.util.vector.Vector2f;

public class drgKneecapperBombletOnHitEffect implements OnHitEffectPlugin
{   
    private static final int MAX_ARCS_ARMOR = 2;
    private static final int MAX_ARCS_SHIELDS = 1;
    private static final float ARC_EMP_FRACTION = 0.75f;
    private static final float ARC_DAMAGE_FRACTION = 0.33f;
    private static final float ARC_CHANCE_ARMOR = 0.6f;
    private static final float ARC_CHANCE_SHIELDS = 0.15f;
    private static final float MIN_FLUX_TO_PEN_SHIELDS = 0.10f;
    private static final Color BOOM_COLOR = new Color(46,91,255); // used for arcs and explosion colors

    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine)
    {
        if (shieldHit && target instanceof ShipAPI)
        {
            for (int i = 0; i < MAX_ARCS_SHIELDS; i++)
            {
                if (Misc.random.nextFloat() < ARC_CHANCE_SHIELDS * ((ShipAPI)target).getFluxLevel() && ((ShipAPI)target).getFluxLevel() > MIN_FLUX_TO_PEN_SHIELDS)
                    engine.spawnEmpArcPierceShields(projectile.getSource(), point, target, target,
                        DamageType.ENERGY, 
                        projectile.getDamageAmount() * ARC_DAMAGE_FRACTION,
                        projectile.getEmpAmount() * ARC_EMP_FRACTION, // emp 
                        100000f, // max range 
                        "tachyon_lance_emp_impact",
                        20f, // thickness
                        BOOM_COLOR,
                        Color.white
                    );
            }
        } else if (!shieldHit && target instanceof ShipAPI) {
            for (int i = 0; i < MAX_ARCS_ARMOR; i++)
            {
                if (Misc.random.nextFloat() > (1 - ARC_CHANCE_ARMOR))
                    engine.spawnEmpArc(projectile.getSource(), point, target, target,
                        DamageType.ENERGY, 
                        projectile.getDamageAmount() * ARC_DAMAGE_FRACTION,
                        projectile.getEmpAmount() * ARC_EMP_FRACTION, // emp 
                        100000f, // max range 
                        "tachyon_lance_emp_impact",
                        20f, // thickness
                        BOOM_COLOR,
                        Color.white
                    );
            }
        }
        drgUtils.plasmaEffects(projectile, BOOM_COLOR,3,80f);
    }
}
