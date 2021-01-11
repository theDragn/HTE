package data.weapons;

import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;;

public class drgArcEmitterSmallWeapon implements EveryFrameWeaponEffectPlugin
{

    // this weapon can't chain at all

    private static final float FIRING_ARC = 120f;
    public static final Color CORE_COLOR = new Color(250, 250, 255);
    public static final Color FRINGE_COLOR = new Color(22, 22, 255);

    private boolean fireNextFrame = false;
    private final Random rand = new Random();

    // gets a list of all hostile targets (missiles and ships) within the weapon's spread
    private List<CombatEntityAPI> getPrimaryTargetList(Vector2f location, float range, WeaponAPI weapon)
    {
        List<CombatEntityAPI> targetList = new ArrayList<>();
        List<CombatEntityAPI> entities = CombatUtils.getEntitiesWithinRange(location, range);

        for (CombatEntityAPI entity : entities)
        {
            if ((entity instanceof MissileAPI || entity instanceof ShipAPI)
                    && entity.getOwner() != weapon.getShip().getOwner())
            {
                // don't arc to wrecks
                if (entity instanceof ShipAPI && !((ShipAPI) entity).isAlive())
                {
                    continue;
                }
                float angleToTarget = VectorUtils.getAngle(weapon.getLocation(), entity.getLocation());

                if (Math.abs(MathUtils.getShortestRotation(angleToTarget, weapon.getCurrAngle())) <= FIRING_ARC * 0.5f)
                {
                    targetList.add(entity);
                }
            }
        }
        return targetList;
    }

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon)
    {
        if (engine.isPaused() || weapon == null)
        {
            return;
        }

        // check for dummy projectiles to see if we need to create an arc
        if (!fireNextFrame)
        {
            for (DamagingProjectileAPI proj : CombatUtils.getProjectilesWithinRange(weapon.getLocation(), 100f))
            {
                if (proj.getProjectileSpecId() == null)
                {
                    continue;
                }
                if (proj.getWeapon() == weapon)
                {
                    engine.removeEntity(proj);
                    fireNextFrame = true;
                    return;
                }
            }
            return;
        } else
        {

            // if we get here, we're firing
            fireNextFrame = false;
            float arcDamage = weapon.getDamage().getDamage();
            float empDamage = weapon.getDerivedStats().getEmpPerShot();
            float range = weapon.getRange();

            Vector2f weaponFirePoint = new Vector2f(weapon.getLocation().x, weapon.getLocation().y);
            Vector2f fireOffset = new Vector2f(0f, 0f);
            if (weapon.getSlot().isTurret())
            {
                fireOffset.x += weapon.getSpec().getTurretFireOffsets().get(0).x;
                fireOffset.y += weapon.getSpec().getTurretFireOffsets().get(0).y;
            } else if (weapon.getSlot().isHardpoint())
            {
                fireOffset.x += weapon.getSpec().getHardpointFireOffsets().get(0).x;
                fireOffset.y += weapon.getSpec().getHardpointFireOffsets().get(0).y;
            }
            fireOffset = VectorUtils.rotate(fireOffset, weapon.getCurrAngle(), new Vector2f(0f, 0f));
            weaponFirePoint.x += fireOffset.x;
            weaponFirePoint.y += fireOffset.y;

            // create list of valid targets
            List<CombatEntityAPI> targetList = getPrimaryTargetList(weaponFirePoint, range + 100f, weapon);
            if (!targetList.isEmpty())
            {
                // spawn arc if we've got a valid target
                CombatEntityAPI target = targetList.get(rand.nextInt(targetList.size()));
                engine.spawnEmpArc(weapon.getShip(), weaponFirePoint, weapon.getShip(), target, weapon.getDamageType(),
                        arcDamage, empDamage, 10000f, "tachyon_lance_emp_arc_impact", 10f, CORE_COLOR, FRINGE_COLOR);
            } else {
                weapon.getShip().getFluxTracker().decreaseFlux(weapon.getFluxCostToFire() * 0.75f);
                weapon.setRemainingCooldownTo(0);
            }
        }
    }
}
