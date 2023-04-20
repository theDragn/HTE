package data.weapons.proj.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.GuidedMissileAI;
import com.fs.starfarer.api.combat.MissileAIPlugin;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;
import java.util.Random;
import java.awt.Color;

public class drgKneecapperMissileAI implements MissileAIPlugin, GuidedMissileAI
{

    private static final float SPLIT_DISTANCE = 500f;
    private static final float AIM_TIME_LIMIT = 2.5f; // engine will turn on if it spends this long in aiming stage, ignoring whether it'll actually hit anything
    private static final float ARMING_TIME = 0.66f; // won't split unless engine has been on for at least this long
    private static final float INHERIT_VELOCITY_FRACTION = 0.2f; // how much of the missile velocity do the bomblets get
    private static final float SPREAD_ARC = 30f; // spread arc for bomblets
    private static final int NUM_BOMBLETS = 5;
    private static final float VEL_DAMPING_FACTOR = 0.15f; // this is a magic number
    private static final float AIMING_THRESHOLD = 0.5f; // also a magic number
    // basically, you set the regular weapon to be a mirv, and then also create a hidden weapon that's just the bomblet
    // making the regular weapon still a mirv makes sure it displays properly
    // can't spawn projectiles without a weapon id, apparently? anyway, this works
    private static final String BOMBLET_WEAPON_ID = "drg_kneecapper_bomblet_weapon"; // this is an invisible weapon that fires your bomblet
    private static Random rand = new Random();
    private float aimTimer;
    private float totalAimingTime;
    private float flightTime;
    private boolean turningDisabled;
    private boolean hasTarget;
    private CombatEntityAPI target;
    private ShipAPI launchingShip;
    private MissileAPI missile;


    public drgKneecapperMissileAI(MissileAPI missile, ShipAPI launchingShip) {
        this.missile = missile;
        this.launchingShip = launchingShip;
        turningDisabled = false;
        target = launchingShip.getShipTarget();
        missile.setArmedWhileFizzling(true);
        aimTimer = 0f;
        totalAimingTime = 0f;
        flightTime = 0f;
        if (!isTargetValid(target))
            hasTarget = acquireTarget();
        else
            hasTarget = true;
    }

    @Override
    public void advance(float amount)
    {
        if (this.missile.isFading())
            return;
        if (hasTarget && isTargetValid(target))
        {
            if (!turningDisabled)
            {
                Vector2f relativeTargetVelocity = new Vector2f();
                Vector2f.sub(target.getVelocity(), missile.getVelocity(), relativeTargetVelocity);
                // maxSpeed * 0.75 to account for acceleration and reduced bomblet speed; might need some tinkering if you reuse this code with different speed numbers
                Vector2f interceptPoint = AIUtils.getBestInterceptPoint(missile.getLocation(), missile.getMaxSpeed() * 0.75f, target.getLocation(), relativeTargetVelocity);
                if (interceptPoint == null)
                {
                    turningDisabled = true;
                    return;
                }
                float angleDelta = MathUtils.getShortestRotation(missile.getFacing(), VectorUtils.getAngle(missile.getLocation(), interceptPoint));
                if (angleDelta < 0)
                    missile.giveCommand(ShipCommand.TURN_RIGHT);
                else if (angleDelta > 0)
                    missile.giveCommand(ShipCommand.TURN_LEFT);
                if (Math.abs(angleDelta) < Math.abs(missile.getAngularVelocity()) * VEL_DAMPING_FACTOR)
                    missile.setAngularVelocity(angleDelta / VEL_DAMPING_FACTOR);
                if (Math.abs(angleDelta) < 5f)
                {
                    aimTimer += amount;
                }
                totalAimingTime += amount;
                if (aimTimer > 0.2 || totalAimingTime > AIM_TIME_LIMIT || Math.abs(angleDelta) < AIMING_THRESHOLD)
                {
                    turningDisabled = true;
                    missile.setAngularVelocity(0);
                }
            } else {
                missile.giveCommand(ShipCommand.ACCELERATE);
                flightTime += amount;
                if (MathUtils.getDistanceSquared(missile, target) < (SPLIT_DISTANCE * SPLIT_DISTANCE) && flightTime > ARMING_TIME) // use squared distance since multiplication is way faster than square root
                {


                    for (int i = 0; i < NUM_BOMBLETS; i++) {
                        float angle = missile.getFacing() + (SPREAD_ARC/(float)NUM_BOMBLETS-1)*(i-(NUM_BOMBLETS-1)/2f) + (rand.nextFloat() - 0.5f) * 1.5f * (SPREAD_ARC / ((float)NUM_BOMBLETS-1f));
                        if (angle < 0.0F) {
                          angle += 360.0F;
                        } else if (angle >= 360.0F) {
                          angle -= 360.0F;
                        } 

                        float rotateAngle = (SPREAD_ARC/(float)NUM_BOMBLETS-1)*(i-(NUM_BOMBLETS-1)/2f) + (rand.nextFloat() - 0.5f) * (SPREAD_ARC / ((float)NUM_BOMBLETS-1f));

                        Vector2f inheritedVelocity = new Vector2f(missile.getVelocity());
                        inheritedVelocity.scale(INHERIT_VELOCITY_FRACTION + Misc.random.nextFloat() * 0.1f);
                        Vector2f location = MathUtils.getPointOnCircumference(missile.getLocation(), 6F, angle);
                        Global.getCombatEngine().spawnProjectile(missile.getSource(), missile.getWeapon(), BOMBLET_WEAPON_ID, location, angle, VectorUtils.rotate(inheritedVelocity, rotateAngle));
                      }
                    // scale velocity to average of inherited velocity and missile velocity for drawing smooth particle on split
                    missile.getVelocity().scale(0.5f * INHERIT_VELOCITY_FRACTION + 0.5f);
                    // delete our original missile
                    Global.getCombatEngine().addSmoothParticle(missile.getLocation(), missile.getVelocity(), 120f, 1.3f, 0.33f, Color.BLUE);
                    Global.getCombatEngine().removeEntity(missile);

                }
            }
        } else {
            missile.giveCommand(ShipCommand.ACCELERATE);
            return;
        }
    }

    @Override
    public CombatEntityAPI getTarget()
    {
        return target;
    }

    @Override
    public void setTarget(CombatEntityAPI target)
    {
        this.target = target;
    }

    // acquires target for the missile if there is no target selected
    // priotizes ships with higher flux levels
    // will not target fighters unless directed to do so
    private boolean acquireTarget()
    {
        if (isTargetValid(launchingShip.getShipTarget()))
        {
            target = launchingShip.getShipTarget();
            return true;
        } else
        {
            List<ShipAPI> enemies = AIUtils.getNearbyEnemies(missile, missile.getWeapon().getRange());
            float highestThreat = -1f;
            ShipAPI tempTarget = null;
            for (ShipAPI enemy : enemies)
            {
                if (enemy.getHullSize().equals(HullSize.FIGHTER))
                    continue;
                float threat = enemy.getFluxLevel();
                if (threat > highestThreat)
                {
                    tempTarget = enemy;
                    highestThreat = threat;
                }
            }
            if (isTargetValid(tempTarget))
            {
                target = tempTarget;
                return true;
            }
        }
        return false;
    }

    private boolean isTargetValid(CombatEntityAPI target)
    {
        if (target instanceof ShipAPI)
            return ((ShipAPI) target).isAlive();
        return false;
    }
}
