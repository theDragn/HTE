package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class drgFourth extends BaseHullMod {

    public drgFourth() {
        // identity theft code goes here
    }

    public static float
        ARC_BONUS = 20,
        UNFOLD_BONUS = 25,
        UPKEEP_REDUCTION = 20,
        SHIELD_DAMAGE_REDUCTION = 5,
        HULL_ARMOR_REDUCTION = 7;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getShieldArcBonus().modifyPercent(id, ARC_BONUS);
        stats.getShieldUnfoldRateMult().modifyPercent(id, UNFOLD_BONUS);
        stats.getShieldUpkeepMult().modifyPercent(id, UPKEEP_REDUCTION * -1f);
        stats.getShieldDamageTakenMult().modifyPercent(id, SHIELD_DAMAGE_REDUCTION * -1f);
        stats.getArmorBonus().modifyPercent(id, HULL_ARMOR_REDUCTION * -1f);
        stats.getHullBonus().modifyPercent(id, HULL_ARMOR_REDUCTION * -1f);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0)
            return (int)ARC_BONUS + "%";
        if (index == 1)
            return (int)UNFOLD_BONUS + "%";
        if (index == 2)
            return (int)UPKEEP_REDUCTION + "%";
        if (index == 3)
            return (int)SHIELD_DAMAGE_REDUCTION + "%";
        // the hullmod doesn't actually increase OP, that gets done through the .skin files
        // but the player should know that it gets an OP bonus because it's IV Battlegroup
        if (index == 4)
            return "3/5/7/10";
        if (index == 5)
            return (int)HULL_ARMOR_REDUCTION + "%";

        return null;
    }

}
