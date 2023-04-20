package plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.RepLevel;
import com.fs.starfarer.api.campaign.SubmarketPlugin;
import com.fs.starfarer.api.campaign.econ.SubmarketAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class drgSvarogMarketPlugin extends BaseSubmarketPlugin
{
    private static final RepLevel MIN_STANDING = RepLevel.COOPERATIVE;

    @Override
    public void init(SubmarketAPI submarket)
    {
        super.init(submarket);
    }

    @Override
    public float getTariff()
    {
        return drgModPlugin.SVAROG_TARIFF;
    }

    @Override
    public String getTooltipAppendix(CoreUIAPI ui)
    {
        RepLevel level = this.submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction("player"));
        if (!level.isAtWorst(MIN_STANDING))
            return "Requires: " + this.submarket.getFaction().getDisplayName() + " - "
                    + MIN_STANDING.getDisplayName().toLowerCase();
        if (!Global.getSector().getPlayerFleet().isTransponderOn())
            return "Requires: Fleet identification. Turn on your transponder.";
        return super.getTooltipAppendix(ui);
    }

    @Override
    public boolean isEnabled(CoreUIAPI ui)
    {
        if (!Global.getSector().getPlayerFleet().isTransponderOn())
            return false;
        RepLevel level = this.submarket.getFaction().getRelationshipLevel(Global.getSector().getFaction("player"));
        return level.isAtWorst(MIN_STANDING);
    }

    @Override
    public void updateCargoPrePlayerInteraction()
    {
        WeightedRandomPicker<ShipHullSpecAPI> picker = getHullPicker();
        this.sinceLastCargoUpdate = 0.0F;
        if (okToUpdateShipsAndWeapons())
        {            
            this.sinceSWUpdate = 0.0F;

            // add weapons from a mix of tritach, indy, and persean
            pruneWeapons(0.0F);
            int weapons = drgModPlugin.SVAROG_NUM_WEAPONS / 2;
            int fighters = drgModPlugin.SVAROG_NUM_WINGS / 2;
            addWeapons(weapons, weapons + 4, 4, Factions.PERSEAN);
            addFighters(fighters, fighters + 2, 3, Factions.PERSEAN);
            addWeapons(weapons, weapons + 4, 4, Factions.TRITACHYON);
            addFighters(fighters, fighters + 2, 3, Factions.TRITACHYON);

            int bonusHulls = Misc.random.nextInt(drgModPlugin.SVAROG_NUM_HULLS);
            getCargo().getMothballedShips().clear();
            for (int i = 0; i < drgModPlugin.SVAROG_NUM_HULLS + bonusHulls; i++)
            {
                ShipHullSpecAPI hull = null;
                hull = picker.pick();
                addShip(hull.getHullId() + "_Hull", false, 250f);
            }
            addHullMods(4, 5);
        }
        getCargo().sort();
    }

    private WeightedRandomPicker<ShipHullSpecAPI> getHullPicker()
    {
        WeightedRandomPicker<ShipHullSpecAPI> picker = new WeightedRandomPicker<>();
        for (ShipHullSpecAPI hull : Global.getSettings().getAllShipHullSpecs())
        {
            float hullWeight = 0;
            float tagWeight = 0;
            if (drgModPlugin.HULL_WEIGHTS.containsKey(hull.getHullSize()))
            {
                hullWeight = drgModPlugin.HULL_WEIGHTS.get(hull.getHullSize());
                for (String tag : hull.getTags())
                {
                    if (drgModPlugin.TAG_WEIGHTS.containsKey(tag) && drgModPlugin.TAG_WEIGHTS.get(tag) > tagWeight)
                    {
                        tagWeight = drgModPlugin.TAG_WEIGHTS.get(tag);
                    }
                }
            }
            if (hullWeight * tagWeight > 0)
            {
                picker.add(hull, hullWeight * tagWeight);
            }
        }
        return picker;
    }

    @Override
    public boolean isIllegalOnSubmarket(CargoStackAPI stack, SubmarketPlugin.TransferAction action)
    {
        if (action == SubmarketPlugin.TransferAction.PLAYER_SELL)
            return true;
        return false;
    }

    @Override
    public boolean isIllegalOnSubmarket(String commodityId, SubmarketPlugin.TransferAction action)
    {
        if (action == SubmarketPlugin.TransferAction.PLAYER_SELL)
            return true;
        return false;
    }

    @Override
    public boolean isIllegalOnSubmarket(FleetMemberAPI member, SubmarketPlugin.TransferAction action)
    {
        if (action == SubmarketPlugin.TransferAction.PLAYER_SELL)
            return true;
        return false;
    }

    @Override
    public String getIllegalTransferText(CargoStackAPI stack, SubmarketPlugin.TransferAction action)
    {
        return "No sales";
    }

    @Override
    public String getIllegalTransferText(FleetMemberAPI member, SubmarketPlugin.TransferAction action)
    {
        return "No sales";
    }

    protected Object writeReplace()
    {
        if (okToUpdateShipsAndWeapons())
        {
            pruneWeapons(0.0F);
            getCargo().getMothballedShips().clear();
        }
        return this;
    }
}
