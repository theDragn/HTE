package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lwjgl.util.vector.Vector2f;

import plugins.drgModPlugin;

public class drgFluxCompressAI implements ShipSystemAIScript {

    private IntervalUtil timer;
    private FluxTrackerAPI fluxTracker;
    private ShipSystemAPI system;
    private ShipAPI ship;


    // constants
    private static final float 
        ACTIVATE_AT = 0.75f; // activate system if flux level reaches this much
        // the speed bonus is cool but it's really not the main attraction
        // the AI should really reserve it for when it's high on flux and not use it to move around

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {

        drgModPlugin.checkPluginLoading();
        timer = new IntervalUtil(drgModPlugin.AI_UPDATE_INTERVAL, drgModPlugin.AI_UPDATE_INTERVAL * 2f);
        this.ship = ship;
        fluxTracker = ship.getFluxTracker();
        this.system = system;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        // so that it doesn't get called every frame
        timer.advance(amount);

        if (!timer.intervalElapsed()) {
            return;
        }
        // if system isn't active, ship has a target, and system is off cooldown
        if (!system.isStateActive())
        {
            if (fluxTracker.getFluxLevel() > ACTIVATE_AT)
                ship.useSystem();
        }
    }
}
