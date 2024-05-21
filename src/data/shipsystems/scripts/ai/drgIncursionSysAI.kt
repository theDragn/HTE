package data.shipsystems.scripts.ai

import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.util.IntervalUtil
import data.shipsystems.scripts.drgIncursionSys
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.AIUtils
import org.lwjgl.util.vector.Vector2f

class drgIncursionSysAI: ShipSystemAIScript
{
    var ship: ShipAPI? = null
    var sys: ShipSystemAPI? = null
    val timer = IntervalUtil(0.1f, 0.2f)

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?)
    {
        sys = system
        this.ship = ship
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?)
    {
        timer.advance(amount)
        ship ?: return
        ship?.shipTarget ?: return
        sys ?: return
        target ?: return
        if (!timer.intervalElapsed()) return;
        if (MathUtils.isWithinRange(ship, target, ship!!.mutableStats.systemRangeBonus.computeEffective(1000f)))
            ship!!.useSystem()
    }
}