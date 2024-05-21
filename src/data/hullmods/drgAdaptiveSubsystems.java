package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import data.drgUtils;

import java.awt.Color;
import java.util.EnumSet;


public class drgAdaptiveSubsystems extends BaseHullMod
{
    private static final float EFFECT_RAMP_TIME = 2.0f;
    private static final String KEY = "hte_equality_sys";
    // Supercruise
    public static final String SUPERCRUISE_MODE_NAME = "Supercruise";
    private static final float SUPERCRUISE_MANUEVER_MULT = 2.0f;
    private static final float SUPERCRUISE_SPEED_BONUS = 30f;
    //private static final float SUPERCRUISE_ROF_DAMAGE_MULT = 0.0f;
    private static final Color SUPERCRUISE_ENGINE_COLOR = new Color(100,255,100,255);

    // Siege
    public static final String SIEGE_MODE_NAME = "Siege";
    //private static final float SIEGE_ROF_MULT = 1.0f;
    private static final float SIEGE_DAMAGE_MULT = 2.0f;
    private static final float SIEGE_FLUX_MULT = 0.85f;
    private static final float SIEGE_SPEED_MULT = 0.2f; //zero-flux boost
    private static final Color SIEGE_WEAPON_GLOW_COLOR = Color.RED;

    // Bastion
    public static final String BASTION_MODE_NAME = "Bastion";
    private static final float BASTION_DEFENSES_DAMAGE_MULT = 0.75f; // shield and armor, but not hull
    //private static final float BASTION_SHIELD_SPEED_MULT = 1.75f;
    //private static final float BASTION_ARMOR_EFFECTIVENESS_MULT = 1.35f;
    //private static final float BASTION_WEAPON_DAMAGE_MULT = 0.8f;
    private static final float BASTION_PD_DAMAGE_MULT = 1.5f;
    private static final Color BASTION_SHIELD_CORE_COLOR = new Color(147,0,255,100);

    private static final String id = "drgAdaptive";
    public static final String DRG_AS_ICON = "graphics/icons/buffs/drg_systemswapbuff.png";
    public static final String DRG_AS_BUFF_ID = "drg_as1";
    public static final String DRG_AS_DEBUFF_ID = "drg_as2";

    private enum AdaptiveMode
    {
        SPEED,
        WEAPONS,
        DEFENSES
    }

    // gets stored in custom data
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
        ShipTracker shipTracker;
        if (ship.getCustomData().containsKey(KEY))
        {
            shipTracker = (ShipTracker) ship.getCustomData().get(KEY);
        } else {
            if (ship.getShield() != null && !ship.getShield().getType().equals(ShieldAPI.ShieldType.NONE))
                shipTracker = new ShipTracker(ship.getShield().getInnerColor());
            else
                shipTracker = new ShipTracker(Color.black);
            ship.setCustomData(KEY, shipTracker);
        }
        ShipSystemAPI system = ship.getSystem();
        MutableShipStatsAPI stats = ship.getMutableStats();
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

        // supercruise buff
        stats.getMaxSpeed().modifyFlat(id, SUPERCRUISE_SPEED_BONUS*speedEffectLevel );
        stats.getAcceleration().modifyMult(id, (SUPERCRUISE_MANUEVER_MULT *2-1f)*speedEffectLevel+1f);
        stats.getDeceleration().modifyMult(id, (SUPERCRUISE_MANUEVER_MULT *2-1f)*speedEffectLevel+1f);
        stats.getTurnAcceleration().modifyMult(id, (SUPERCRUISE_MANUEVER_MULT -1f)*speedEffectLevel+1f);

        ship.getEngineController().fadeToOtherColor(this, SUPERCRUISE_ENGINE_COLOR, new Color(0,0,0,0), speedEffectLevel, 0.67f);
        ship.getEngineController().extendFlame(this, 1.2f * speedEffectLevel, speedEffectLevel, speedEffectLevel);

        // siege buffs
        stats.getHitStrengthBonus().modifyMult(id, (SIEGE_DAMAGE_MULT-1f) * weaponEffectLevel + 1f);
        stats.getEnergyWeaponFluxCostMod().modifyMult(id, (SIEGE_FLUX_MULT-1f) * weaponEffectLevel + 1f);
        stats.getBallisticWeaponFluxCostMod().modifyMult(id, (SIEGE_FLUX_MULT-1f) * weaponEffectLevel + 1f);
        stats.getBeamWeaponDamageMult().modifyMult(id, (1f / ((SIEGE_DAMAGE_MULT-1f) * weaponEffectLevel + 1f)) * ((SIEGE_DAMAGE_MULT-1f) * 0.5f * weaponEffectLevel + 1f));
        stats.getBeamWeaponFluxCostMult().modifyMult(id, 1f / ((SIEGE_DAMAGE_MULT - 1f) * weaponEffectLevel + 1f));
        stats.getZeroFluxSpeedBoost().modifyMult(id, (SIEGE_SPEED_MULT-1f) * weaponEffectLevel + 1f);

        ship.setWeaponGlow(weaponEffectLevel, SIEGE_WEAPON_GLOW_COLOR, EnumSet.of(WeaponAPI.WeaponType.ENERGY));

        // bastion buffs
        stats.getShieldAbsorptionMult().modifyMult(id, 1f - ((1f - BASTION_DEFENSES_DAMAGE_MULT) * defenseEffectLevel));
        stats.getArmorDamageTakenMult().modifyMult(id, 1f - ((1f - BASTION_DEFENSES_DAMAGE_MULT) * defenseEffectLevel));
        stats.getDamageToFighters().modifyMult(id,(BASTION_PD_DAMAGE_MULT-1f) * defenseEffectLevel + 1f);
        stats.getDamageToMissiles().modifyMult(id,(BASTION_PD_DAMAGE_MULT-1f) * defenseEffectLevel + 1f);
        if (ship.getShield() != null && !ship.getShield().getType().equals(ShieldAPI.ShieldType.NONE))
            ship.getShield().setInnerColor(drgUtils.blendColors(initialShieldColor,BASTION_SHIELD_CORE_COLOR,defenseEffectLevel));


        // display buff/debuff info
        if (ship == Global.getCombatEngine().getPlayerShip())
        {
            String modeName = getModeName(currentMode);
            String buffText = "";
            String debuffText = "";
            switch (currentMode)
            {
                case SPEED:
                    buffText = "speed and maneuvering increased";
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
            //Global.getCombatEngine().maintainStatusForPlayerShip(DRG_AS_DEBUFF_ID, DRG_AS_ICON, modeName + " Mode", debuffText, true);
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
        text.addPara("+" + (int)((SUPERCRUISE_MANUEVER_MULT -1f) * 100f) + "%% maneuverability.", 0, Color.GREEN, "+"+(int)((SUPERCRUISE_MANUEVER_MULT -1f) * 100f) + "%");
        text.addPara("+" + (int)SUPERCRUISE_SPEED_BONUS + " maximum speed.", 0, Color.GREEN, "+"+(int)SUPERCRUISE_SPEED_BONUS);
        //text.addPara("-" + (int)((1f-SUPERCRUISE_ROF_DAMAGE_MULT) * 100f) + "%% rate of fire for non-missile weapons.", 0, Color.RED, "-" + (int)((1f-SUPERCRUISE_ROF_DAMAGE_MULT) * 100f) + "%");
        //text.addPara("-" + (int)((1f-SUPERCRUISE_ROF_DAMAGE_MULT) * 100f) + "%% damage and flux cost for beam weapons.", 0, Color.RED, "-" + (int)((1f-SUPERCRUISE_ROF_DAMAGE_MULT) * 100f) + "%");
        tooltip.addImageWithText(pad);

        // Offense
        text = tooltip.beginImageWithText("graphics/hullmods/adaptive/adaptive_weapons.png", 36);
        text.addPara(SIEGE_MODE_NAME, 0, Color.ORANGE, SIEGE_MODE_NAME);
        //text.addPara("+" + (int)Math.ceil((SIEGE_DAMAGE_MULT - 1f) * 100f) + "%% damage and flux cost for non-missile projectile weapons.", 0, Color.GREEN, "+" + (int)Math.ceil((SIEGE_DAMAGE_MULT - 1f) * 100f) + "%");
        //text.addPara("+" + (int)Math.ceil((SIEGE_DAMAGE_MULT- 1f) * 0.5f * 100f) + "%% damage for beam weapons.", 0, Color.GREEN, "+" + (int)Math.ceil((SIEGE_DAMAGE_MULT- 1f) * 0.5f * 100f) + "%");
        text.addPara("+" + (int)Math.ceil((SIEGE_DAMAGE_MULT - 1f) * 100f) + "%% hit strength for armor damage reduction calculation.", 0, Color.GREEN, "+" + (int)Math.ceil((SIEGE_DAMAGE_MULT - 1f) * 100f) + "%");
        text.addPara("" + (int)Math.floor((1f-SIEGE_FLUX_MULT) * 100f) + "%% reduced weapon flux costs.", 0, Color.GREEN, "-" + (int)Math.floor((1f-SIEGE_FLUX_MULT) * 100f) + "%");
        //text.addPara("Zero-flux boost disabled.", 0, Color.RED, "disabled.");
        text.addPara("-" + (int)((1f-SIEGE_SPEED_MULT) * 100f) + "%% zero-flux boost.", 0, Color.RED, "-" + (int)((1f-SIEGE_SPEED_MULT) * 100f) + "%");
        tooltip.addImageWithText(pad);

        // Defense
        text = tooltip.beginImageWithText("graphics/hullmods/adaptive/adaptive_shields.png", 36);
        text.addPara(BASTION_MODE_NAME, 0, Color.ORANGE, BASTION_MODE_NAME);
        text.addPara("-" + (int)((1f - BASTION_DEFENSES_DAMAGE_MULT) * 100f) + "%% shield and armor damage taken.", 0, Color.GREEN, "-" + (int)((1 - BASTION_DEFENSES_DAMAGE_MULT) * 100f) + "%");
        text.addPara("+" + (int)((BASTION_PD_DAMAGE_MULT- 1f) * 100f) + "%% damage to missiles and fighters.", 0, Color.GREEN, "+" + (int)((BASTION_PD_DAMAGE_MULT- 1f) * 100f) + "%");

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