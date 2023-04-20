package data.shipsystems.scripts.ai;
//import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lwjgl.util.vector.Vector2f;
import org.lazywizard.lazylib.combat.*;
import java.util.List;
import java.util.HashMap;
//import java.util.Random;
import plugins.drgModPlugin;

public class drgRangeOverclockAI implements ShipSystemAIScript {
   
    // constants
    private static final float SYSTEM_MAX_RANGE = 1600f; // maximum range of weapons when affected by the ship system
    // private static float MAX_FLUX_TO_ACTIVATE; // will not activate system if flux is higher than this fraction
    // private static float FLUX_TO_DEACTIVATE; // will deactivate system when flux reaches this fraction
    // these are both pulled from settings now
    
    private IntervalUtil timer;
    //private final Random rand = new Random();
    private float averageRangeSystem = 0;
    private float averageRangeNormal;
    private FluxTrackerAPI fluxTracker;
    private ShipSystemAPI system;
    private ShipAPI ship;
    //private CombatEngineAPI engine;

    // used for threat weighting
    private HashMap<ShipAPI.HullSize, Float> mults = new HashMap<>();


    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        // load things from settings
        drgModPlugin.checkPluginLoading();
        timer = new IntervalUtil(drgModPlugin.AI_UPDATE_INTERVAL, drgModPlugin.AI_UPDATE_INTERVAL * 2f);

        // initialize variables
        this.ship = ship;
        fluxTracker = ship.getFluxTracker();
        this.system = system;
        //this.engine = engine;
        averageRangeSystem = 0;
        mults.put(ShipAPI.HullSize.CAPITAL_SHIP, 1.5f);
        mults.put(ShipAPI.HullSize.CRUISER, 1.25f);
        mults.put(ShipAPI.HullSize.DESTROYER, 1f);
        mults.put(ShipAPI.HullSize.FRIGATE, 0.75f);
        mults.put(ShipAPI.HullSize.FIGHTER, 0f); // don't turn on the system to shoot fighters

        // figure out the range of our longest-ranged weapon once the system is active
        int weaponCount = 0;
        List<WeaponAPI> weapons = ship.getAllWeapons();
        for (WeaponAPI weapon : weapons) {
            if (weapon.getType() != WeaponType.MISSILE)
            { 
                float range = weapon.getRange();
                // don't count long-range beams with an ITU when figuring out effective weapon range, since system activation won't give them much benefit
                if (range < 1350f) 
                {
                    averageRangeSystem += weapon.getRange();
                    weaponCount++;
                }
            }
        }
        if (weaponCount != 0)
        {
            averageRangeSystem = averageRangeSystem / weaponCount;
        } else
        {
            // no weapons that aren't long-range beams
            averageRangeSystem = 1350f;
        }
        averageRangeSystem *= 2f;
        if (averageRangeSystem > SYSTEM_MAX_RANGE)
            averageRangeSystem = SYSTEM_MAX_RANGE;
        averageRangeNormal = averageRangeSystem / 2f;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        // so that it doesn't get called every frame
        timer.advance(amount);

        if (!timer.intervalElapsed()) {
            return;
        }

        float nearConcern = getThreatWeight(averageRangeNormal, ship);
        // subtract concern within normal range to get the concern of targets within system-boosted range
        float farConcern = getThreatWeight(averageRangeSystem, ship) - nearConcern;

        // if system isn't active and system is off cooldown
        if (!system.isStateActive() && system.getCooldownRemaining() == 0)
        {
            if (fluxTracker.getFluxLevel() < drgModPlugin.AI_RANGECLOCK_ACTIVATE && farConcern > nearConcern)
            {
                ship.useSystem();
            }
        }
        // if it is active 
        else if (system.isStateActive())
        {
            if (fluxTracker.getFluxLevel() > drgModPlugin.AI_RANGECLOCK_DEACTIVATE || nearConcern > farConcern || farConcern == 0)
            {
                ship.useSystem();
            }
        }

        // antifascist skeleton goes doot doot
        // no longer active :(
        // I know for a fact that at least one person got dooted, so it was all worth it
        /*if (rand.nextFloat() > 0.995 && !engine.isSimulation())
        {
            if (Global.getSector().getStarSystem(de("YWVzaXI=")) != null || Global.getSector().getStarSystem(de("QWxwaGFyZA==")) != null)
            {
                for (ShipAPI entity : engine.getShips())
                {
                    if (entity.getOwner() == 0 && rand.nextFloat() > 0.3)
                    {
                        engine.applyDamage(entity, entity.getLocation(), 100000f, DamageType.ENERGY, 50, true, false, ship);
                        Global.getSoundPlayer().playSound(de("ZHJnX2FyY19maXJl"), 1f, 1.5f, engine.getPlayerShip().getLocation(), engine.getPlayerShip().getVelocity());
                    }
                }
            }
        }*/
    }

    private float getThreatWeight(float range, ShipAPI ship)
    {
        float threatWeightTotal = 0f;
        for (ShipAPI enemy : AIUtils.getNearbyEnemies(ship, range)) {
            if (enemy == null || enemy.getFleetMember() == null) {
                continue;
            }

            float weight = enemy.getFleetMember().getDeploymentCostSupplies();
            weight *= mults.get(enemy.getHullSize());
            if (enemy.getFluxTracker().isOverloadedOrVenting()) {
                weight *= 0.75f;
            }
            if (enemy.getHullLevel() < 0.4f) {
                weight *= 0.5f;
            }
            if (enemy.getFluxLevel() > 0.5f) {
                weight *= 0.5f;
            }
            if (enemy.getEngineController().isFlamedOut()) {
                weight *= 0.5f;
            }

            threatWeightTotal += weight;
        }
        return threatWeightTotal;
    }
}
