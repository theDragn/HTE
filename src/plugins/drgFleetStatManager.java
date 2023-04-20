package plugins;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;

import data.hullmods.drgDriveField;

public class drgFleetStatManager implements EveryFrameScript
{

    private static float accelBonus = 0;
    private static IntervalUtil timer = new IntervalUtil(0.25f,0.5f);

    public boolean runWhilePaused()
    {
        return false;
    }

    public boolean isDone()
    {
        return false;
    }

    public void advance(float amount)
    {   
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        calc(playerFleet);

        // really no need to check every frame
        timer.advance(amount);
        if (timer.intervalElapsed())
        {
            for (CampaignFleetAPI fleet : Misc.getVisibleFleets(playerFleet, true))
            {
                // only these factions should be able to get the Gloom, so there's no need to iterate over anyone else
                if (fleet.getFaction().getId().equals(Factions.TRITACHYON) || fleet.getFaction().getId().equals(Factions.PIRATES) || fleet.getFaction().getId().equals("science_fuckers"))
                    calc(fleet);
            }
        }
    }
    // can't set a static variable inside a non-static function
    private static void setAccelBonus(float bonus)
    {
        accelBonus = bonus;
    }

    // called when the hullmod text gets displayed
    public static float getBonus()
    {
        CampaignFleetAPI playerFleet = Global.getSector().getPlayerFleet();
        calc(playerFleet);

        return accelBonus;
    }

    private static float calc(CampaignFleetAPI fleet)
    {
        boolean shouldGetProfileBonus = true;

        if (fleet.hasAbility("sustained_burn"))
        {
            if (fleet.getAbility("sustained_burn").isActive())
            {
                shouldGetProfileBonus = false;
            }
        }
        if (fleet.hasAbility("emergency_burn"))
        {
            if (fleet.getAbility("emergency_burn").isActive())
            {
                shouldGetProfileBonus = false;
            }
        }

        int numBoosters = 0;
        for (FleetMemberAPI member : fleet.getFleetData().getMembersListCopy())
        {
            if (member.getVariant().hasHullMod("drg_drivefield"))
                numBoosters++;
        }
        float accelBonus = Math.min(numBoosters * drgDriveField.ACCELERATION_BONUS, drgDriveField.ACCELERATION_CAP);
        float profileBonus = Math.min(numBoosters * drgDriveField.PROFILE_BONUS, drgDriveField.PROFILE_CAP);
        if (accelBonus > 0)
        {
            fleet.getStats().getAccelerationMult().modifyMult("drg_drivefield", 1f + accelBonus, "Drive Field Compressor");
            setAccelBonus(accelBonus);
        } else {
            fleet.getStats().getAccelerationMult().unmodify("drg_drivefield");
        }
        if (shouldGetProfileBonus)
        {
            fleet.getStats().getDetectedRangeMod().modifyMult("drg_drivefield", 1f - profileBonus, "Drive Field Compressor");
        } else {
            fleet.getStats().getDetectedRangeMod().unmodify("drg_drivefield");
        }
        return accelBonus;
    }
}
