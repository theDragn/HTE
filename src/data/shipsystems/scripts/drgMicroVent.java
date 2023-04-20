package data.shipsystems.scripts;


import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;


// venting? idk that's pretty sus
public class drgMicroVent extends BaseShipSystemScript
{

    public static final float VENT_BOOST = 1.5f; // vent rate = dissipation * this
    private boolean runOnce = false;
    private boolean reactivateShield = false;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, ShipSystemStatsScript.State state, float effectLevel)
    {

        stats.getFluxDissipation().modifyMult(id, VENT_BOOST);
        ShipAPI ship = (ShipAPI)stats.getEntity();
        if (!runOnce)
        {
            runOnce = true;
            reactivateShield = ship.getShield().isOn();
        }
        ship.giveCommand(ShipCommand.VENT_FLUX, null, 1);
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id)
    {
        runOnce = false;
        stats.getFluxDissipation().unmodify(id);
        ShipAPI ship = (ShipAPI)stats.getEntity();
        ship.getFluxTracker().stopVenting();
        if (reactivateShield)
            ship.getShield().toggleOn();
        reactivateShield = false;
    }

    @Override
    public ShipSystemStatsScript.StatusData getStatusData(int index, ShipSystemStatsScript.State state, float effectLevel) {
        return null;
    }

}
