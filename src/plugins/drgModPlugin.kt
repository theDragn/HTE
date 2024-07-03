package plugins

import com.fs.starfarer.api.BaseModPlugin
import java.lang.RuntimeException
import java.lang.ClassNotFoundException
import com.fs.starfarer.api.impl.campaign.procgen.ProcgenUsedNames
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.MissileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.PluginPick
import com.fs.starfarer.api.combat.MissileAIPlugin
import data.weapons.proj.ai.drgKneecapperMissileAI
import com.fs.starfarer.api.campaign.CampaignPlugin
import data.weapons.proj.ai.drgFakeBreachAI
import org.dark.shaders.light.LightData
import org.dark.shaders.util.TextureData
import kotlin.Throws
import java.io.IOException
import org.json.JSONException
import java.lang.Exception
import java.util.HashMap

class drgModPlugin : BaseModPlugin()
{
    override fun onApplicationLoad()
    {
        val hasLazyLib = Global.getSettings().modManager.isModEnabled("lw_lazylib")
        if (!hasLazyLib) throw RuntimeException(
            "HTE requires LazyLib.\nGet it at http://fractalsoftworks.com/forum/index.php?topic=5444"
        )


        val hasMagicLib = Global.getSettings().modManager.isModEnabled("MagicLib")
        if (!hasMagicLib) throw RuntimeException(
            "HTE requires MagicLib.\nGet it at http://fractalsoftworks.com/forum/index.php?topic=13718"
        )

        if (Global.getSettings().modManager.isModEnabled("shaderLib"))
        {
            TextureData.readTextureDataCSV("data/config/hte_texture_data.csv")
            LightData.readLightDataCSV("data/config/hte_light_data.csv")
        }

        // old crashcode preserved for posterity
        // I guess ngo is basically dead and buried so there's not much reason to continue dying on this hill
        // rip matt damon, the john brown of starsector modding
        /*
        var b = false
        try
        {
            Global.getSettings().scriptClassLoader.loadClass(xd("ZGF0YS5zY3JpcHRzLk5HT01vZFBsdWdpbg==")) // tries loading target mod plugin class
            b = true // if it's not there, it'll throw an error and won't reach this line
        } catch (e: ClassNotFoundException)
        {
        }
        if (b) // b is only true if it could successfully load the targeted mod plugin
        {
            throw RuntimeException(xd("SFRFIGVycm9yOiBUaGlzIG1vZCBpcyBub3QgY29tcGF0aWJsZSB3aXRoIE5HTy4gUGxlYXNlIGRpc2FibGUgSFRFIG9yIHRoZSBjb25mbGljdGluZyBtb2QgYW5kIHJlc3RhcnQgeW91ciBnYW1lLg=="))
        }
        */

        try
        {
            loadHTEsettings()
        } catch (e: Exception)
        {
            System.out.println(e);
            // could probably make it fail gracefully but that would likely cause confusing behavior
            // crash early and notify the user rather than allowing confusion- it's easy to fix by redownloading
            throw RuntimeException(
                "HTE encountered a \"bruh moment\".\nAnd by that I mean there was an issue with the settings file."
            )
        }
    }

    override fun onNewGame()
    {
        drgSectorGenerator().generate(Global.getSector())
        ProcgenUsedNames.notifyUsed("Svarog")
    }

    override fun onGameLoad(newGame: Boolean)
    {
        val sector = Global.getSector()
        if (!sector.hasScript(drgFleetStatManager::class.java))
        {
            sector.addScript(drgFleetStatManager() as EveryFrameScript)
        }
    }

    override fun pickMissileAI(missile: MissileAPI, launchingShip: ShipAPI): PluginPick<MissileAIPlugin>?
    {
        return when (missile.projectileSpecId)
        {
            "drg_kneecapper_rocket" -> return PluginPick(drgKneecapperMissileAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC)
            "drg_fake_breach" -> PluginPick(drgFakeBreachAI(missile, launchingShip), CampaignPlugin.PickPriority.MOD_SPECIFIC)
            else -> null
        }
    }

    companion object
    {
        const val SETTINGS_FILE = "HTE_SETTINGS.json"
        var log = Global.getLogger(drgModPlugin::class.java)
        var loaded = false
        @JvmField
        var LOW_GRAPHICS_MODE = false
        @JvmField
        var TAG_WEIGHTS: MutableMap<String, Float> = HashMap()
        @JvmField
        var HULL_WEIGHTS: MutableMap<ShipAPI.HullSize, Float> = HashMap()
        @JvmField
        var SVAROG_OUTPOST_MARKET_ENABLED = false
        @JvmField
        var ZARMAZD_ENABLED = false
        @JvmField
        var AI_UPDATE_INTERVAL = 0f
        @JvmField
        var AI_FLUXCOMP_TRIGGER = 0f
        @JvmField
        var AI_RANGECLOCK_ACTIVATE = 0f
        @JvmField
        var AI_RANGECLOCK_DEACTIVATE = 0f
        @JvmField
        var SVAROG_TARIFF = 0f
        @JvmField
        var ZARMAZD_X = 0f
        @JvmField
        var ZARMAZD_Y = 0f
        @JvmField
        var SVAROG_NUM_WEAPONS = 0
        @JvmField
        var SVAROG_NUM_WINGS = 0
        @JvmField
        var SVAROG_NUM_HULLS = 0
        @Throws(IOException::class, JSONException::class)
        private fun loadHTEsettings()
        {
            val settings = Global.getSettings().loadJSON(SETTINGS_FILE)
            log.info("Loaded HTE settings json")
            loaded = true
            SVAROG_OUTPOST_MARKET_ENABLED = settings.getBoolean("svarogOutpostMarketEnabled")
            ZARMAZD_ENABLED = settings.getBoolean("zarmazdSystemEnabled")
            LOW_GRAPHICS_MODE = settings.getBoolean("lowGraphicsMode")
            ZARMAZD_X = settings.getDouble("zarmazdX").toFloat()
            ZARMAZD_Y = settings.getDouble("zarmazdY").toFloat()
            SVAROG_TARIFF = settings.getDouble("svarogSpecialtyMarketTariff").toFloat()
            SVAROG_NUM_WEAPONS = settings.getInt("svarogNumWeapons")
            SVAROG_NUM_WINGS = settings.getInt("svarogNumWings")
            SVAROG_NUM_HULLS = settings.getInt("svarogNumHulls")
            AI_UPDATE_INTERVAL = settings.getDouble("aiUpdateInterval").toFloat()
            AI_FLUXCOMP_TRIGGER = settings.getDouble("aiFluxCompressorThreshold").toFloat()
            AI_RANGECLOCK_ACTIVATE = settings.getDouble("aiRangeOverclockActivateMax").toFloat()
            AI_RANGECLOCK_DEACTIVATE = settings.getDouble("aiRangeOverclockDeactivateAt").toFloat()
            val tagWeightData = settings.getJSONObject("svarogTagWeights")
            val iter = tagWeightData.keys()
            while (iter.hasNext())
            {
                val tag = iter.next() as String
                val weight = tagWeightData.getDouble(tag).toFloat()
                TAG_WEIGHTS[tag] = weight
            }
            val hullWeightData = settings.getJSONObject("svarogHullWeights")
            HULL_WEIGHTS[ShipAPI.HullSize.FRIGATE] = hullWeightData.getDouble("frigate").toFloat()
            HULL_WEIGHTS[ShipAPI.HullSize.DESTROYER] = hullWeightData.getDouble("destroyer").toFloat()
            HULL_WEIGHTS[ShipAPI.HullSize.CRUISER] = hullWeightData.getDouble("cruiser").toFloat()
            HULL_WEIGHTS[ShipAPI.HullSize.CAPITAL_SHIP] = hullWeightData.getDouble("capital").toFloat()
        }

        // String de-obfuscator. Just decodes a base64 string.
        fun xd(q: String): String
        {
            var q = q
            val u = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
            q = q.replace("[^" + u + "=]".toRegex(), "")
            val t = if (q[q.length - 1] == '=') if (q[q.length - 2] == '=') "AA" else "A" else ""
            var r = ""
            q = q.substring(0, q.length - t.length) + t
            var v = 0
            while (v < q.length)
            {
                val s = ((u.indexOf(q[v]) shl 18) + (u.indexOf(q[v + 1]) shl 12)
                        + (u.indexOf(q[v + 2]) shl 6) + u.indexOf(q[v + 3]))
                r += "" + (s ushr 16 and 0xFF).toChar() + (s ushr 8 and 0xFF).toChar() + (s and 0xFF).toChar()
                v += 4
            }
            return r.substring(0, r.length - t.length)
        }
    }
}