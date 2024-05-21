package data.shipsystems.scripts

import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript

class drgSpireSys: BaseShipSystemScript()
{
    var runOnce = false

    override fun apply(
        stats: MutableShipStatsAPI?,
        id: String?,
        state: ShipSystemStatsScript.State?,
        effectLevel: Float
    )
    {
        val ship = stats?.entity as ShipAPI
        ship ?: return

        for (bay in ship.launchBaysCopy)
        {
            bay.makeCurrentIntervalFast()
            bay.fastReplacements = 6
        }

    }

    override fun unapply(stats: MutableShipStatsAPI?, id: String?)
    {
        super.unapply(stats, id)
    }
}