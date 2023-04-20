package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.drgUtils;

import java.awt.Color;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;


public class drgAdaptiveSubsystems extends BaseHullMod
{
    private Map<ShipAPI, ShipTracker> shipTrackerMap = new HashMap<>();
    private static final float EFFECT_RAMP_TIME = 2.0f;

    // Supercruise
    public static final String SUPERCRUISE_MODE_NAME = "Supercruise";
    private static final float SUPERCRUISE_ZERO_FLUX_BOOST_MULT = 2.0f;
    private static final float SUPERCRUISE_ZERO_FLUX_AMOUNT_FLAT = 0.05f;
    private static final float SUPERCRUISE_ROF_DAMAGE_MULT = 0.5f;
    private static final Color SUPERCRUISE_ENGINE_COLOR = new Color(100,255,100,255);

    // Siege
    public static final String SIEGE_MODE_NAME = "Siege";
    private static final float SIEGE_ROF_MULT = 0.8f;
    private static final float SIEGE_DAMAGE_MULT = 1.3f; // also flux cost mult
    private static final float SIEGE_SPEED_MULT = 0.2f; // total and zero-flux boost
    private static final Color SIEGE_WEAPON_GLOW_COLOR = Color.RED;

    // Bastion
    public static final String BASTION_MODE_NAME = "Bastion";
    private static final float BASTION_DEFENSES_DAMAGE_MULT = 0.75f; // shield and armor, but not hull
    private static final float BASTION_SHIELD_SPEED_MULT = 1.75f;
    private static final float BASTION_ARMOR_EFFECTIVENESS_MULT = 1.35f;
    private static final float BASTION_WEAPON_DAMAGE_MULT = 0.8f;
    private static final float BASTION_PD_DAMAGE_MULT = 1.33f;
    private static final Color BASTION_SHIELD_CORE_COLOR = new Color(147,0,255,100);

    private static final String id = "drgAdaptive";
    public static final String DRG_AS_ICON = "graphics/icons/buffs/drg_systemswapbuff.png";
    public static final String DRG_AS_BUFF_ID = "drg_as1";
    public static final String DRG_AS_DEBUFF_ID = "drg_as2";

    private static int storedHashCode = 0;
    //private IntervalUtil timer = new IntervalUtil(60f, 120f); //

    private enum AdaptiveMode
    {
        SPEED,
        WEAPONS,
        DEFENSES
    }

    // since there's only one hullmod object, we make a map of these objects to track variables on a per-ship basis
    private class ShipTracker
    {
        public Color initialShieldColor;
        public float speedEffectLevel = 0f;
        public float weaponEffectLevel = 0f;
        public float defenseEffectLevel = 0f;

        public ShipTracker(Color initialShieldColor)
        {
            this.initialShieldColor = initialShieldColor;
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id)
    {
        shipTrackerMap.clear();
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount)
    {
        // sanity checks and setup
        if (Global.getCombatEngine().isPaused() || ship.isHulk())
        {
            return;
        }
        // checking haschode every frame doesn't seem to be very performance intensive
        /*if (Global.getCombatEngine().hashCode() != storedHashCode)
        {
            ship.getSystem().setAmmo(1);
            shipTrackerMap.clear();
            storedHashCode = Global.getCombatEngine().hashCode();
        }*/
        if (!shipTrackerMap.containsKey(ship))
        {
            if (ship.getShield() != null && !ship.getShield().getType().equals(ShieldAPI.ShieldType.NONE))
                shipTrackerMap.put(ship,new ShipTracker(ship.getShield().getInnerColor()));
            else
                shipTrackerMap.put(ship,new ShipTracker(Color.black));
        }
        ShipSystemAPI system = ship.getSystem();
        MutableShipStatsAPI stats = ship.getMutableStats();
        ShipTracker shipTracker = shipTrackerMap.get(ship);
        float speedEffectLevel = shipTracker.speedEffectLevel;
        float weaponEffectLevel = shipTracker.weaponEffectLevel;
        float defenseEffectLevel = shipTracker.defenseEffectLevel;
        Color initialShieldColor = shipTracker.initialShieldColor;

        if (system == null || stats == null)
            return;

        // calculate the buff level (this is what handles the ramp when you swap between systems)
        AdaptiveMode currentMode = getMode(system);
        if (currentMode == null) return;
        switch (currentMode)
        {
            case SPEED:
                speedEffectLevel += amount / EFFECT_RAMP_TIME;
                weaponEffectLevel -= amount / EFFECT_RAMP_TIME;
                defenseEffectLevel -= amount / EFFECT_RAMP_TIME;
                break;
            case WEAPONS:
                speedEffectLevel -= amount / EFFECT_RAMP_TIME;
                weaponEffectLevel += amount / EFFECT_RAMP_TIME;
                defenseEffectLevel -= amount / EFFECT_RAMP_TIME;
                break;
            case DEFENSES:
                speedEffectLevel -= amount / EFFECT_RAMP_TIME;
                weaponEffectLevel -= amount / EFFECT_RAMP_TIME;
                defenseEffectLevel += amount / EFFECT_RAMP_TIME;
                break;
        }
        if (speedEffectLevel > 1f)
            speedEffectLevel = 1f;
        if (speedEffectLevel < 0f)
            speedEffectLevel = 0f;
        if (weaponEffectLevel > 1f)
            weaponEffectLevel = 1f;
        if (weaponEffectLevel < 0f)
            weaponEffectLevel = 0f;
        if (defenseEffectLevel > 1f)
            defenseEffectLevel = 1f;
        if (defenseEffectLevel < 0f)
            defenseEffectLevel = 0f;

        // store our new buff values in the tracker for next frame, and then apply all the buffs and effects
        shipTracker.speedEffectLevel = speedEffectLevel;
        shipTracker.weaponEffectLevel = weaponEffectLevel;
        shipTracker.defenseEffectLevel = defenseEffectLevel;
        // speed buffs and effects
        //if (speedEffectLevel > 0)
        //{
            stats.getZeroFluxSpeedBoost().modifyMult(id,(SUPERCRUISE_ZERO_FLUX_BOOST_MULT-1f)*speedEffectLevel+1f);
            stats.getAcceleration().modifyMult(id, (SUPERCRUISE_ZERO_FLUX_BOOST_MULT*2-1f)*speedEffectLevel+1f);
            stats.getDeceleration().modifyMult(id, (SUPERCRUISE_ZERO_FLUX_BOOST_MULT*2-1f)*speedEffectLevel+1f);
            stats.getTurnAcceleration().modifyMult(id, (SUPERCRUISE_ZERO_FLUX_BOOST_MULT-1f)*speedEffectLevel+1f);
            stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, SUPERCRUISE_ZERO_FLUX_AMOUNT_FLAT*speedEffectLevel);
            stats.getBeamWeaponDamageMult().modifyMult(id+"spd",1f - ((1f - SUPERCRUISE_ROF_DAMAGE_MULT) * speedEffectLevel));
            stats.getBeamWeaponFluxCostMult().modifyMult(id+"spd",1f - ((1f - SUPERCRUISE_ROF_DAMAGE_MULT) * speedEffectLevel));
            stats.getEnergyRoFMult().modifyMult(id+"spd",1f - ((1f - SUPERCRUISE_ROF_DAMAGE_MULT) * speedEffectLevel));
            stats.getBallisticRoFMult().modifyMult(id+"spd",1f - ((1f - SUPERCRUISE_ROF_DAMAGE_MULT) * speedEffectLevel));
    /*    }
        else
        {
            stats.getZeroFluxSpeedBoost().unmodify(id);
            stats.getAcceleration().unmodify(id);
            stats.getDeceleration().unmodify(id);
            stats.getTurnAcceleration().unmodify(id);
            //stats.getZeroFluxMinimumFluxLevel().unmodify(id);
            stats.getBeamWeaponFluxCostMult().unmodify(id+"spd");
            stats.getBeamWeaponDamageMult().unmodify(id+"spd");
            stats.getEnergyRoFMult().unmodify(id+"spd");
            stats.getBallisticRoFMult().unmodify(id+"spd");
        }*/
        ship.getEngineController().fadeToOtherColor(this, SUPERCRUISE_ENGINE_COLOR, SUPERCRUISE_ENGINE_COLOR, speedEffectLevel, 0.67f);
        ship.getEngineController().extendFlame(this, 1.2f * speedEffectLevel, speedEffectLevel, speedEffectLevel);

        // weapon buffs and effects
        //if (weaponEffectLevel > 0)
        //{
            stats.getHitStrengthBonus().modifyMult(id, (SIEGE_DAMAGE_MULT-1f) * weaponEffectLevel + 1f);
            stats.getEnergyWeaponDamageMult().modifyMult(id, (SIEGE_DAMAGE_MULT-1f) * weaponEffectLevel + 1f);
            stats.getEnergyWeaponFluxCostMod().modifyMult(id, (SIEGE_DAMAGE_MULT-1f) * weaponEffectLevel + 1f);
            stats.getEnergyRoFMult().modifyMult(id, 1f - ((1f - SIEGE_ROF_MULT) * weaponEffectLevel));
            stats.getBallisticWeaponDamageMult().modifyMult(id, (SIEGE_DAMAGE_MULT-1f) * weaponEffectLevel + 1f);
            stats.getBallisticWeaponFluxCostMod().modifyMult(id, (SIEGE_DAMAGE_MULT-1f) * weaponEffectLevel + 1f);
            stats.getBallisticRoFMult().modifyMult(id, 1f - ((1f - SIEGE_ROF_MULT) * weaponEffectLevel));
            stats.getZeroFluxSpeedBoost().modifyMult(id + "wep",1f-0.8f*weaponEffectLevel);
            // apply (partially) inverse buffs to beam weapons, since they count as both energy and beam
            // this prevents double-dipping in stat bonuses
            // though it does really hurt burst beams, but I don't think there's anything I can do about that
            stats.getBeamWeaponDamageMult().modifyMult(id, (1f / ((SIEGE_DAMAGE_MULT-1f) * weaponEffectLevel + 1f)) * ((SIEGE_DAMAGE_MULT-1f) * 0.5f * weaponEffectLevel + 1f));
            stats.getBeamWeaponFluxCostMult().modifyMult(id, 1f / ((SIEGE_DAMAGE_MULT - 1f) * weaponEffectLevel + 1f));
        //}
        //else
        //{
            //stats.getEnergyWeaponDamageMult().unmodify(id);
            //stats.getEnergyWeaponFluxCostMod().unmodify(id);
            //stats.getEnergyRoFMult().unmodify(id);
            //stats.getBallisticWeaponDamageMult().unmodify(id);
            //stats.getBallisticWeaponFluxCostMod().unmodify(id);
            //stats.getBallisticRoFMult().unmodify(id);
            //stats.getZeroFluxSpeedBoost().unmodify(id+"wep");
            //stats.getBeamWeaponDamageMult().unmodify(id);
            //stats.getBeamWeaponFluxCostMult().unmodify(id);
        //}
        ship.setWeaponGlow(weaponEffectLevel, SIEGE_WEAPON_GLOW_COLOR, EnumSet.of(WeaponAPI.WeaponType.ENERGY));

        /*if (defenseEffectLevel > 0)
        {*/
            stats.getShieldAbsorptionMult().modifyMult(id, 1f - ((1f - BASTION_DEFENSES_DAMAGE_MULT) * defenseEffectLevel));
            stats.getArmorDamageTakenMult().modifyMult(id, 1f - ((1f - BASTION_DEFENSES_DAMAGE_MULT) * defenseEffectLevel));
            stats.getShieldTurnRateMult().modifyMult(id, (BASTION_SHIELD_SPEED_MULT-1f) * defenseEffectLevel + 1f);
            stats.getShieldUnfoldRateMult().modifyMult(id, (BASTION_SHIELD_SPEED_MULT-1f) * defenseEffectLevel + 1f);
            stats.getEffectiveArmorBonus().modifyMult(id, (BASTION_ARMOR_EFFECTIVENESS_MULT-1f) * defenseEffectLevel + 1f);
            stats.getEnergyWeaponDamageMult().modifyMult(id+"def",1f - ((1f - BASTION_WEAPON_DAMAGE_MULT) * defenseEffectLevel));
            stats.getEnergyWeaponFluxCostMod().modifyMult(id+"def", 1f - ((1f - BASTION_WEAPON_DAMAGE_MULT) * defenseEffectLevel));
            stats.getBallisticWeaponDamageMult().modifyMult(id+"def",1f - ((1f - BASTION_WEAPON_DAMAGE_MULT) * defenseEffectLevel));
            stats.getBallisticWeaponFluxCostMod().modifyMult(id+"def", 1f - ((1f - BASTION_WEAPON_DAMAGE_MULT) * defenseEffectLevel));
            stats.getDamageToFighters().modifyMult(id,(BASTION_PD_DAMAGE_MULT-1f) * defenseEffectLevel + 1f);
            stats.getDamageToMissiles().modifyMult(id,(BASTION_PD_DAMAGE_MULT-1f) * defenseEffectLevel + 1f);
            if (ship.getShield() != null && !ship.getShield().getType().equals(ShieldAPI.ShieldType.NONE))
                ship.getShield().setInnerColor(drgUtils.blendColors(initialShieldColor,BASTION_SHIELD_CORE_COLOR,defenseEffectLevel));
        /*}
        else
        {
            stats.getShieldAbsorptionMult().unmodify(id);
            stats.getArmorDamageTakenMult().unmodify(id);
            stats.getShieldTurnRateMult().unmodify(id);
            stats.getShieldUnfoldRateMult().unmodify(id);
            stats.getEffectiveArmorBonus().unmodify(id);
            stats.getEnergyWeaponDamageMult().unmodify(id+"def");
            stats.getEnergyWeaponFluxCostMod().unmodify(id+"def");
            stats.getBallisticWeaponDamageMult().unmodify(id+"def");
            stats.getBallisticWeaponFluxCostMod().unmodify(id+"def");
            ship.getShield().setInnerColor(initialShieldColor);
        }*/

        // display buff/debuff info
        if (ship == Global.getCombatEngine().getPlayerShip())
        {
            String modeName = getModeName(currentMode);
            String buffText = "";
            String debuffText = "";
            switch (currentMode)
            {
                case SPEED:
                    buffText = "zero-flux boost and maneuvering increased";
                    debuffText = "weapon power diverted";
                    break;
                case WEAPONS:
                    buffText = "weapon power amplified";
                    debuffText = "zero-flux boost disabled";
                    break;
                case DEFENSES:
                    buffText = "defenses reinforced";
                    debuffText = "weapon power diverted";
            }
            Global.getCombatEngine().maintainStatusForPlayerShip(DRG_AS_BUFF_ID, DRG_AS_ICON, modeName + " Mode", buffText, false);
            Global.getCombatEngine().maintainStatusForPlayerShip(DRG_AS_DEBUFF_ID, DRG_AS_ICON, modeName + " Mode", debuffText, true);
        }
    }

    //For the cool extra description section
    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width, boolean isForModSpec) {
        float pad = 10f;
        tooltip.addSectionHeading("Details", Alignment.MID, pad);

        // NYOOM
        TooltipMakerAPI text = tooltip.beginImageWithText("graphics/hullmods/adaptive/adaptive_engines.png", 36);
        text.addPara(SUPERCRUISE_MODE_NAME, 0, Color.ORANGE, SUPERCRUISE_MODE_NAME);
        text.addPara("+" + (int)((SUPERCRUISE_ZERO_FLUX_BOOST_MULT-1f) * 100f) + "%% zero-flux boost and maneuverability.", 0, Color.GREEN, "+"+(int)((SUPERCRUISE_ZERO_FLUX_BOOST_MULT-1f) * 100f) + "%");
        text.addPara("+" + (int)((SUPERCRUISE_ZERO_FLUX_AMOUNT_FLAT) * 100f) + "%% to zero-flux boost activation threshold.", 0, Color.GREEN, "+"+(int)((SUPERCRUISE_ZERO_FLUX_AMOUNT_FLAT) * 100f) + "%");
        text.addPara("-" + (int)((1f-SUPERCRUISE_ROF_DAMAGE_MULT) * 100f) + "%% rate of fire for non-missile weapons.", 0, Color.RED, "-" + (int)((1f-SUPERCRUISE_ROF_DAMAGE_MULT) * 100f) + "%");
        text.addPara("-" + (int)((1f-SUPERCRUISE_ROF_DAMAGE_MULT) * 100f) + "%% damage and flux cost for beam weapons.", 0, Color.RED, "-" + (int)((1f-SUPERCRUISE_ROF_DAMAGE_MULT) * 100f) + "%");
        tooltip.addImageWithText(pad);

        // Offense
        text = tooltip.beginImageWithText("graphics/hullmods/adaptive/adaptive_weapons.png", 36);
        text.addPara(SIEGE_MODE_NAME, 0, Color.ORANGE, SIEGE_MODE_NAME);
        text.addPara("+" + (int)Math.ceil((SIEGE_DAMAGE_MULT - 1f) * 100f) + "%% damage and flux cost for non-missile projectile weapons.", 0, Color.GREEN, "+" + (int)Math.ceil((SIEGE_DAMAGE_MULT - 1f) * 100f) + "%");
        text.addPara("+" + (int)Math.ceil((SIEGE_DAMAGE_MULT- 1f) * 0.5f * 100f) + "%% damage for beam weapons.", 0, Color.GREEN, "+" + (int)Math.ceil((SIEGE_DAMAGE_MULT- 1f) * 0.5f * 100f) + "%");
        text.addPara("+" + (int)Math.ceil((SIEGE_DAMAGE_MULT - 1f) * 100f) + "%% hit strength for armor damage reduction calculation.", 0, Color.GREEN, "+" + (int)Math.ceil((SIEGE_DAMAGE_MULT - 1f) * 100f) + "%");
        text.addPara("-" + (int)Math.ceil((1f-SIEGE_ROF_MULT) * 100f) + "%% rate of fire for non-missile weapons.", 0, Color.RED, "-" + (int)Math.ceil((1f-SIEGE_ROF_MULT) * 100f) + "%");
        //text.addPara("Zero-flux boost disabled.", 0, Color.RED, "disabled.");
        text.addPara("-" + (int)((1f-SIEGE_SPEED_MULT) * 100f) + "%% zero-flux boost.", 0, Color.RED, "-" + (int)((1f-SIEGE_SPEED_MULT) * 100f) + "%");
        tooltip.addImageWithText(pad);

        // Defense
        text = tooltip.beginImageWithText("graphics/hullmods/adaptive/adaptive_shields.png", 36);
        text.addPara(BASTION_MODE_NAME, 0, Color.ORANGE, BASTION_MODE_NAME);
        text.addPara("-" + (int)((1f - BASTION_DEFENSES_DAMAGE_MULT) * 100f) + "%% shield and armor damage taken.", 0, Color.GREEN, "-" + (int)((1 - BASTION_DEFENSES_DAMAGE_MULT) * 100f) + "%");
        text.addPara("+" + (int)Math.ceil((BASTION_SHIELD_SPEED_MULT - 1f) * 100f) + "%% shield rotation and deployment rate.", 0, Color.GREEN, "+" + (int)Math.ceil((BASTION_SHIELD_SPEED_MULT - 1f) * 100f) + "%");
        text.addPara("+" + (int)((BASTION_ARMOR_EFFECTIVENESS_MULT - 1f) * 100f) + "%% armor effectiveness for damage reduction calculation.", 0, Color.GREEN, "+" + (int)((BASTION_ARMOR_EFFECTIVENESS_MULT - 1f) * 100f) + "%");
        text.addPara("+" + (int)((BASTION_PD_DAMAGE_MULT- 1f) * 100f) + "%% damage to missiles and fighters.", 0, Color.GREEN, "+" + (int)((BASTION_PD_DAMAGE_MULT- 1f) * 100f) + "%");
        text.addPara("-" + (int)Math.ceil((1f - BASTION_WEAPON_DAMAGE_MULT) * 100f) + "%% damage and flux cost for non-missile weapons.", 0, Color.RED, "-" + (int)Math.ceil((1f - BASTION_WEAPON_DAMAGE_MULT) * 100f) + "%");
        tooltip.addImageWithText(pad);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize)
    {
        if (index == 0)
            return (int)EFFECT_RAMP_TIME + " seconds";
        return null;
    }

    private String getModeName(AdaptiveMode mode)
    {
        switch (mode)
        {
            case SPEED:
                return SUPERCRUISE_MODE_NAME;
            case WEAPONS:
                return SIEGE_MODE_NAME;
            case DEFENSES:
                return BASTION_MODE_NAME;
            default:
                return "damn bitch, you fucked up";
        }
    }

    private AdaptiveMode getMode(ShipSystemAPI system)
    {
        int i = system.getAmmo();
        switch (i)
        {
            case 0: return AdaptiveMode.DEFENSES;
            case 1: return AdaptiveMode.SPEED;
            case 2: return AdaptiveMode.WEAPONS;
            case 3: return AdaptiveMode.DEFENSES;
        }
        return null;
    }
}