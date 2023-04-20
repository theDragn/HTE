package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.hullmods.drgAdaptiveSubsystems;

public class drgSystemSwap extends BaseShipSystemScript
{
    int mode = 1;
    boolean firstFrame = true;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel)
    {
        if (stats.getEntity() instanceof ShipAPI && firstFrame)
        {
            firstFrame = false;
            ShipAPI ship = (ShipAPI) stats.getEntity();
            mode++;
            if (mode >= 4)
                mode = 1;
            ship.getSystem().setAmmo(mode);
        }
    }


    public void unapply(MutableShipStatsAPI stats, String id)
    {
        firstFrame = true;
        if (stats.getEntity() instanceof ShipAPI)
        {
            ShipAPI ship = (ShipAPI) stats.getEntity();
            ship.getSystem().setAmmo(mode);
        }
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship)
    {
        switch (system.getAmmo())
        {
            case 1:
                return drgAdaptiveSubsystems.SUPERCRUISE_MODE_NAME;
            case 2:
                return drgAdaptiveSubsystems.SIEGE_MODE_NAME;
            case 3:
                return drgAdaptiveSubsystems.BASTION_MODE_NAME;
            default:
                return "bruh";
        }
    }
}
