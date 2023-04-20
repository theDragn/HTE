package data.hullmods

import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import data.hullmods.drgPDspec
import com.fs.starfarer.api.combat.ShipAPI

class drgPDspec : BaseHullMod()
{
    // you might be wondering why I use floats some times and ints other times
    // don't worry, I wonder too
    override fun applyEffectsBeforeShipCreation(hullSize: HullSize, stats: MutableShipStatsAPI, id: String)
    {
        stats.nonBeamPDWeaponRangeBonus.modifyFlat(id, PD_RANGE_BONUS.toFloat())
        stats.beamPDWeaponRangeBonus.modifyFlat(id, PD_RANGE_BONUS.toFloat())
        stats.weaponRangeThreshold.modifyFlat(id, THRESHOLD_AT.toFloat())
        stats.weaponRangeMultPastThreshold.modifyMult(id, 0f)
    }

    override fun getDescriptionParam(index: Int, hullSize: HullSize, ship: ShipAPI): String?
    {
        if (index == 0) return Integer.toString(PD_RANGE_BONUS)
        return if (index == 1) Integer.toString(THRESHOLD_AT) else null
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