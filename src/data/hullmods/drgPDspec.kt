package data.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import org.magiclib.util.MagicIncompatibleHullmods

class drgPDspec : BaseHullMod()
{
    // the float/int stuff is to make it display as ints without too much trouble
    override fun applyEffectsBeforeShipCreation(hullSize: HullSize, stats: MutableShipStatsAPI, id: String)
    {
        stats.nonBeamPDWeaponRangeBonus.modifyFlat(id, PD_RANGE_BONUS.toFloat())
        stats.beamPDWeaponRangeBonus.modifyFlat(id, PD_RANGE_BONUS.toFloat())
        stats.weaponRangeThreshold.modifyFlat(id, THRESHOLD_AT.toFloat())
        stats.weaponRangeMultPastThreshold.modifyMult(id, 0f)
    }

    override fun getDescriptionParam(index: Int, hullSize: HullSize, ship: ShipAPI): String?
    {
        if (index == 0) return PD_RANGE_BONUS.toString()
        return if (index == 1) THRESHOLD_AT.toString() else null
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI, id: String?)
    {
        if (ship.variant.hullMods.contains("safetyoverrides"))
        {
            MagicIncompatibleHullmods.removeHullmodWithWarning(
                ship.variant,
                "safetyoverrides",
                "drg_pdspec"
            )
        }
    }

    override fun affectsOPCosts(): Boolean
    {
        return true
    }

    companion object
    {
        const val PD_RANGE_BONUS = 300
        const val THRESHOLD_AT = 1200
    }
}