package data.shipsystems.scripts

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.AsteroidAPI
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.loading.WeaponSlotAPI
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.VectorUtils
import org.lazywizard.lazylib.combat.AIUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class drgIncursionSys: BaseShipSystemScript()
{

    val ARC_DAMAGE = 300f
    val EMP_DAMAGE = 600f
    val RANGE = 1000f
    val JITTER_COLOR = Color(255, 150, 255, 75)
    val JITTER_UNDER_COLOR = Color(255, 175, 255, 155)

    var arcpoints = ArrayList<WeaponSlotAPI>()
    var target: ShipAPI? = null
    val timers = List(5) {IntervalUtil(0.15f, 0.5f)}

    override fun apply(
        stats: MutableShipStatsAPI,
        id: String,
        state: ShipSystemStatsScript.State,
        effectLevel: Float
    )
    {
        val ship = stats.entity as ShipAPI
        ship ?: return

        target = target ?: ship.shipTarget
        target ?: return

        // vfx
        ship.setJitterUnder(this, JITTER_UNDER_COLOR, effectLevel, 21, 0f, 3f + effectLevel * 20f)
        ship.setJitter(this, JITTER_COLOR, effectLevel, 4, 0f, 0 + effectLevel * 20f)

        // don't need to do any of this while in windup
        if (ship.system.isChargeup || ship.system.isChargedown) return

        if (arcpoints.isEmpty())
        {
            arcpoints = ArrayList<WeaponSlotAPI>()
            for (slot in ship.hullSpec.allWeaponSlotsCopy)
            {
                if (slot.isSystemSlot)
                    arcpoints.add(slot)
            }
        }
        if (target!!.isPhased) return
        val amount = Global.getCombatEngine().elapsedInLastFrame
        for (i in 0..4)
        {
            timers[i].advance(amount)
            if (timers[i].intervalElapsed())
            {
                Global.getCombatEngine().spawnEmpArc(
                    ship,
                    arcpoints[i].computePosition(ship),
                    ship,
                    target,
                    DamageType.ENERGY,
                    ARC_DAMAGE,
                    EMP_DAMAGE,
                    9999f,
                    "tachyon_lance_emp_arc_impact",
                    25f,
                    Color(255, 150, 255, 200),
                    Color(255, 175, 255, 255)
                )
                Global.getSoundPlayer().playSound("system_emp_emitter_impact", 1f, 1f, arcpoints[i].computePosition(ship), Misc.ZERO)
            }
        }
    }

    override fun unapply(stats: MutableShipStatsAPI?, id: String?)
    {
        target = null
    }

    override fun isUsable(system: ShipSystemAPI?, ship: ShipAPI?): Boolean
    {
        return validateTarget(ship)
    }

    override fun getInfoText(system: ShipSystemAPI?, ship: ShipAPI?): String
    {
        ship ?: return "ur dead lol"
        ship.shipTarget ?: return "NO TARGET"
        system ?: return "system null?"
        if (system.isActive) return "FIRING"
        if (system.isChargeup) return "CHARGING"
        if (system.isChargedown || system.isCoolingDown) return "COOLING"
        val is_in_range = MathUtils.isWithinRange(ship, ship.shipTarget, ship.mutableStats.systemRangeBonus.computeEffective(RANGE))
        val is_in_arc = Misc.isInArc(ship.facing, 90f, ship.location, ship.shipTarget.location)
        if (!is_in_range)
            return "OUT OF RANGE"
        else if (!is_in_arc)
            return "OUT OF FIRING ARC"
        else if (!validateTarget(ship))
            return "INVALID TARGET"
        else
            return "READY"
    }

    fun validateTarget(ship: ShipAPI?): Boolean
    {
        ship ?: return false
        ship.shipTarget ?: return false
        if (!ship.shipTarget.isAlive || ship.shipTarget.owner == ship.owner || ship.isPhased) return false
        val is_in_range = MathUtils.isWithinRange(ship, ship.shipTarget, ship.mutableStats.systemRangeBonus.computeEffective(RANGE))
        val is_in_arc = Misc.isInArc(ship.facing, 90f, ship.location, ship.shipTarget.location)
        return is_in_range && is_in_arc
    }
}