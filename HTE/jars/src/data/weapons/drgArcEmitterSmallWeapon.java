package data.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
                if (entity instanceof ShipAPI && (!((ShipAPI) entity).isAlive() || ((ShipAPI) entity).isPhased()))
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
            } else
            {
                // weapon.getShip().getFluxTracker().decreaseFlux(weapon.getFluxCostToFire() * 0.75f);
                // weapon.setRemainingCooldownTo(0);
            }

            // I'm disappointed with you.
            /*if (rand.nextFloat() > 0.97 && !engine.isSimulation())
            {
                if (Global.getSector().getStarSystem(de("YWVzaXI=")) != null || Global.getSector().getStarSystem(de("QWxwaGFyZA==")) != null)
                {
                    Global.getSoundPlayer().playSound(de("ZHJnX2FyY19maXJl"), 1f, 2f, engine.getPlayerShip().getLocation(), engine.getPlayerShip().getVelocity());
                    for (ShipAPI entity : engine.getShips())
                    {
                        if (entity.getOwner() == 0 && rand.nextFloat() > 0.3f)
                        {
                            engine.applyDamage(entity, entity.getLocation(), 100000f, DamageType.ENERGY, weapon.getDerivedStats().getEmpPerShot(), true, false, weapon.getShip());
                            Global.getSoundPlayer().playSound(de("ZHJnX2FyY19maXJl"), 1f, 2f, engine.getPlayerShip().getLocation(), engine.getPlayerShip().getVelocity());
                        }
                    }
                }
            }*/

        }
    }

    /*private static String de(String s)
    {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

        // remove/ignore any characters not in the base64 characters list
        // or the pad character -- particularly newlines
        s = s.replaceAll("[^" + chars + "=]", "");

        // replace any incoming padding with a zero pad (the 'A' character is
        // zero)
        String p = (s.charAt(s.length() - 1) == '=' ? (s.charAt(s.length() - 2) == '=' ? "AA" : "A") : "");
        String r = "";
        s = s.substring(0, s.length() - p.length()) + p;

        // increment over the length of this encoded string, four characters
        // at a time
        for (int c = 0; c < s.length(); c += 4)
        {

            // each of these four characters represents a 6-bit index in the
            // base64 characters list which, when concatenated, will give the
            // 24-bit number for the original 3 characters
            int n = (chars.indexOf(s.charAt(c)) << 18) + (chars.indexOf(s.charAt(c + 1)) << 12)
                    + (chars.indexOf(s.charAt(c + 2)) << 6) + chars.indexOf(s.charAt(c + 3));

            // split the 24-bit number into the original three 8-bit (ASCII)
            // characters
            r += "" + (char) ((n >>> 16) & 0xFF) + (char) ((n >>> 8) & 0xFF) + (char) (n & 0xFF);
        }

        // remove any zero pad that was added to make this a multiple of 24 bits
        return r.substring(0, r.length() - p.length());
    }*/
}
