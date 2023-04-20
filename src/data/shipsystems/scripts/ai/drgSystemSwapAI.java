package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import plugins.drgModPlugin;

import java.util.HashMap;
import java.util.List;

public class drgSystemSwapAI implements ShipSystemAIScript
{
    private IntervalUtil timer;
    private ShipSystemAPI system;
    private ShipAPI ship;
    private int desiredMode;

    // used for threat weighting
    private HashMap<ShipAPI.HullSize, Float> mults = new HashMap<>();

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        // load things from settings
        drgModPlugin.checkPluginLoading();
        // longer AI interval than normal, because we want to let modes actually kick in before recalculating
        timer = new IntervalUtil(drgModPlugin.AI_UPDATE_INTERVAL, drgModPlugin.AI_UPDATE_INTERVAL * 2f);

        // initialize variables
        this.ship = ship;
        this.system = system;
        desiredMode = 1;
        mults.put(ShipAPI.HullSize.CAPITAL_SHIP, 1.5f);
        mults.put(ShipAPI.HullSize.CRUISER, 1.25f);
        mults.put(ShipAPI.HullSize.DESTROYER, 1f);
        mults.put(ShipAPI.HullSize.FRIGATE, 0.75f);
        mults.put(ShipAPI.HullSize.FIGHTER, 0f); // don't turn on the system to shoot fighters

        // find the range of our largest, shortest-range weapon
        // presumably we should only swap out of bastion and into siege when the shortest-range heavy weapon is in range

    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target)
    {
        if (ship == null || !ship.isAlive() || Global.getCombatEngine().isPaused())
            return;

        timer.advance(amount);
        int currentMode = system.getAmmo();
        if (timer.intervalElapsed())
        {
            float analysisRange = getLongestRange(ship);
            float threat = getThreatWeight(analysisRange, ship);

            if (threat < 10 && ship.getFluxLevel() < 0.05f)
                desiredMode = 1;
            else if (threat > 75 || ship.getFluxLevel() > 0.5f)
                desiredMode = 3;
            else
                desiredMode = 2;

        }
        if (currentMode != desiredMode && system.getState() == ShipSystemAPI.SystemState.IDLE)
        {
            ship.useSystem();
        }
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
                weight *= 1.5f;
            }
            if (enemy.getHullLevel() < 0.4f) {
                weight *= 1.5f;
            }
            if (enemy.getFluxLevel() > 0.5f) {
                weight *= 1.25f;
            }

            threatWeightTotal += weight;
        }
        return threatWeightTotal;
    }

    private float getLongestRange(ShipAPI ship)
    {
        float longestRange = 0f;
        List<WeaponAPI> weapons = ship.getAllWeapons();
        WeaponSize largestWeaponSize = WeaponSize.SMALL;
        for (WeaponAPI weapon : weapons)
        {
            WeaponSize size = weapon.getSize();
            if (largestWeaponSize == WeaponSize.SMALL && weapon.getSize() != largestWeaponSize)
                largestWeaponSize = weapon.getSize();
            if (largestWeaponSize == WeaponSize.MEDIUM && weapon.getSize() == WeaponSize.LARGE)
                largestWeaponSize = WeaponSize.LARGE;
        }
        for (WeaponAPI weapon : weapons) {
            if (weapon.getType() != WeaponType.MISSILE && weapon.getSize() == largestWeaponSize && !weapon.hasAIHint(WeaponAPI.AIHints.PD)) // probably needs adjustment if the ship has different mounts
            {
                float range = weapon.getRange();
                if (range > longestRange)
                    longestRange = range;
            }
        }
        if (longestRange < 100f) // something fucked up, default to "normal" range of 600 + rangemod
            longestRange = 600 * ship.getMutableStats().getEnergyWeaponRangeBonus().computeEffective(1);
        return longestRange;
    }
}
