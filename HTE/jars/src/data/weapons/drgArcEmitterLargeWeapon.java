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

public class drgArcEmitterLargeWeapon implements EveryFrameWeaponEffectPlugin
{

    // this weapon can produce multiple secondary arcs
    private static final float CHAIN_CHANCE = 0.10f;
    private static final float CHAIN_RANGE = 300f;
    private static final float BASE_RANGE = 600f; // unmodified weapon range, used for figuring out the proportional increase to chain range
    private static final float FIRING_ARC = 120f;
    private static final float RAMP_TIME = 6f; // time in seconds of continuous fire needed to reach maximum chain rate
    private static final int NUM_SECONDARY_CHAINS = 3;
    public static final Color CORE_COLOR = new Color(250, 250, 255);
    public static final Color FRINGE_COLOR = new Color(22, 22, 255);

    private boolean fireNextFrame = false;
    private final Random rand = new Random();
    private float chargeLevel = 0;

    // gets list of hostile targets (missiles and ships) within the weapon's spread
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
                if (entity instanceof ShipAPI && !((ShipAPI)entity).isAlive())
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

    // gets list of hostile targets within range of a specific point, ignoring angle
    private List<CombatEntityAPI> getSecondaryTargetList(Vector2f location, float range, WeaponAPI weapon)
    {
        List<CombatEntityAPI> targetList = new ArrayList<>();
        List<CombatEntityAPI> entities = CombatUtils.getEntitiesWithinRange(location, range);

        for (CombatEntityAPI entity : entities)
        {
            if ((entity instanceof MissileAPI || entity instanceof ShipAPI)
                    && entity.getOwner() != weapon.getShip().getOwner())
            {
                targetList.add(entity);
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
            if (weapon.getChargeLevel() > 0)
            {
                chargeLevel += amount / RAMP_TIME;
            } else
            {
                chargeLevel -= amount * 1.5f;
            }
            if (chargeLevel < 0)
                chargeLevel = 0;
            else if (chargeLevel > 1)
                chargeLevel = 1;

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

                CombatEntityAPI chainAnchor = target;
                float chainRangeBonused = (weapon.getRange() / BASE_RANGE) * CHAIN_RANGE;
                for (int i = 0; i < NUM_SECONDARY_CHAINS; i++)
                {
                    if (rand.nextFloat() <= CHAIN_CHANCE * chargeLevel * 5f)
                    {
                        targetList = getSecondaryTargetList(target.getLocation(), chainRangeBonused, weapon);
                        targetList.remove(chainAnchor);

                        if (!targetList.isEmpty())
                        {
                            CombatEntityAPI nextTarget = targetList.get(rand.nextInt(targetList.size()));
                            engine.spawnEmpArc(weapon.getShip(), target.getLocation(), chainAnchor, nextTarget,
                                    weapon.getDamageType(), arcDamage, empDamage, 10000f,
                                    "tachyon_lance_emp_arc_impact", 10f, CORE_COLOR, FRINGE_COLOR);
                            targetList.remove(nextTarget);
                        }
                    }
                }
            } else {
                // do nothing for the large weapon
                // don't refund flux because otherwise you could keep it ramped all the time
            }
        }
    }
}
