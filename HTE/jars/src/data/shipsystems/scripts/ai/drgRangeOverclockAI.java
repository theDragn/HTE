package data.shipsystems.scripts.ai;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lwjgl.util.vector.Vector2f;
import org.lazywizard.lazylib.combat.*;
import java.util.List;
import java.util.HashMap;
import java.util.Random;

public class drgRangeOverclockAI implements ShipSystemAIScript {
   
    // constants
    private static final float 
        SYSTEM_MAX_RANGE = 1600f, // maximum range of weapons when affected by the ship system
        MAX_FLUX_TO_ACTIVATE = 0.33f, // will not activate system if flux is higher than this fraction
        FLUX_TO_DEACTIVATE = 0.75f; // will deactivate system when flux reaches this fraction
    
    private final IntervalUtil timer = new IntervalUtil(0.5f, 1f);
    private final Random rand = new Random();
    private float averageRangeSystem = 0;
    private float averageRangeNormal;
    private FluxTrackerAPI fluxTracker;
    private ShipSystemAPI system;
    private ShipAPI ship;
    private CombatEngineAPI engine;

    // used for concern weighting
    private HashMap<ShipAPI.HullSize, Float> mults = new HashMap<>();


    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        // initialize variables
        this.ship = ship;
        fluxTracker = ship.getFluxTracker();
        this.system = system;
        this.engine = engine;
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

        float nearConcern = getConcernWeight(averageRangeNormal, ship);
        // subtract concern within normal range to get the concern of targets within system-boosted range
        float farConcern = getConcernWeight(averageRangeSystem, ship) - nearConcern;

        // if system isn't active and system is off cooldown
        if (!system.isStateActive() && system.getCooldownRemaining() == 0)
        {
            if (fluxTracker.getFluxLevel() < MAX_FLUX_TO_ACTIVATE && farConcern > nearConcern)
            {
                ship.useSystem();
            }
        }
        // if it is active 
        else if (system.isStateActive())
        {
            if (fluxTracker.getFluxLevel() > FLUX_TO_DEACTIVATE || nearConcern > farConcern || farConcern == 0)
            {
                ship.useSystem();
            }
        }

        // I'm disappointed with you.
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

    // credit to that tomato guy on discord
    private float getConcernWeight(float range, ShipAPI ship)
    {
        float concernWeightTotal = 0f;
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

            concernWeightTotal += weight;
        }
        return concernWeightTotal;
    }

    /*
    private static String de(String s)
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
