package world;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.campaign.econ.MarketConditionAPI;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor;
import com.fs.starfarer.api.impl.campaign.procgen.StarGenDataSpec;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin.CoronaParams;

import java.util.Arrays;
import java.util.ArrayList;

import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.drgUtils;
import plugins.drgModPlugin;

import java.awt.Color;

public class drgZarmazd
{
    public void generate(SectorAPI sector)
    {
        if (!drgModPlugin.ZARMAZD_ENABLED)
            return;
        StarSystemAPI system = sector.createStarSystem("Zarmazd");
        system.getLocation().set(drgModPlugin.ZARMAZD_X, drgModPlugin.ZARMAZD_Y);
        system.setBackgroundTextureFilename("graphics/backgrounds/background6.jpg");
        // blatantly stolen code
        PlanetAPI star = system.initStar("drg_zarmazd_star", StarTypes.BLACK_HOLE, 225f, 0);
        star.getSpec().setBlackHole(true);
        system.setLightColor(new Color(135, 110, 110));

        StarGenDataSpec starData = (StarGenDataSpec) Global.getSettings().getSpec(StarGenDataSpec.class, star.getSpec().getPlanetType(), false);
        float corona = (float) (star.getRadius() * (starData.getCoronaMult() + starData.getCoronaVar()));
        //if (corona < starData.getCoronaMin()) {
            corona = starData.getCoronaMin();
        //}
        SectorEntityToken eventHorizon = system.addTerrain(Terrain.EVENT_HORIZON,
                new CoronaParams(star.getRadius() + corona, (star.getRadius() + corona) / 2f,
                        star, starData.getSolarWind(),
                        (float) (starData.getMinFlare() + (starData.getMaxFlare() - starData.getMinFlare()) * Math.random()),
                        starData.getCrLossMult()));

        eventHorizon.setCircularOrbit(star, 0, 0, 100);

        float orbitRadius = 3100f;
        float bandWidth = 350f;
        int numBands = 15;

        for (float i = 0; i < numBands; i++) {
            float radius = orbitRadius - i * bandWidth * 0.25f - i * bandWidth * 0.1f;
            float orbitDays = radius / (30f + 10f * Misc.random.nextFloat());
            WeightedRandomPicker<String> rings = new WeightedRandomPicker<>();
            rings.add("rings_dust0");
            rings.add("rings_dust0");
            rings.add("rings_ice0");
            rings.add("rings_special0");
            rings.add("rings_special0");
            String ring = rings.pick();
            RingBandAPI visual = system.addRingBand(star, "misc", ring, 256f, 0, new Color(120,0,120), bandWidth,
                    radius + bandWidth / 2f, -orbitDays);
            float spiralFactor = 2f + Misc.random.nextFloat() * 7f;
            visual.setSpiral(true);
            visual.setMinSpiralRadius(star.getRadius());
            visual.setSpiralFactor(spiralFactor);
        }
        SectorEntityToken ring = system.addTerrain(Terrain.RING, new BaseRingTerrain.RingParams(orbitRadius-300f, orbitRadius / 2f, star, "Zarmazd Accretion Disk"));
        ring.addTag(Tags.ACCRETION_DISK);
        ring.setCircularOrbit(star, 0, 0, -100);



        // add our one lonely planet
        PlanetAPI planet = system.addPlanet("drg_svarog_planet", star, "Svarog", "tundra", 0f, 140f, 4000f, 270f);
        planet.setCustomDescriptionId("drg_svarog");
        // add comm relay for stability
        SectorEntityToken commRelay = system.addCustomEntity(
            "drg_zarmazd_relay",
            "Comm Relay",
            Entities.COMM_RELAY,
            Factions.INDEPENDENT
        );
        commRelay.setCircularOrbit(star, 90f, 4500f, 270f);

        SectorEntityToken navRelay = system.addCustomEntity(
                "drg_zarmazd_relay",
                "Nav Relay",
                Entities.NAV_BUOY,
                Factions.INDEPENDENT
        );
        navRelay.setCircularOrbit(star, 270f, 4500f, 270f);

        // a lil asteroid belt to hold up our asteroid pants
        system.addAsteroidBelt(
            star, //orbit focus
            80, //number of asteroid entities
            3000, //orbit radius is 500 gap for outer randomly generated entity above
            300, //width of band
            190, //minimum and maximum visual orbit speeds of asteroids
            220,
            Terrain.ASTEROID_BELT, //ID of the terrain type that appears in the section above the abilities bar
            "Zarmazd Asteroid Belt" //display name
        );

            //add a ring texture. it will go under the asteroid entities generated above
        system.addRingBand(star,
            "misc", //used to access band texture, this is the name of a category in settings.json
            "rings_asteroids0", //specific texture id in category misc in settings.json
            256f, //texture width, can be used for scaling shenanigans
            2,
            Color.white, //colour tint
            256f, //band width in game
            3300, //same as above
            200f,
            null,
            null
        );

        //and our lonely market for our lonely planet :( all by itself, it needs you to be its friend
        MarketAPI market = Global.getFactory().createMarket("drg_svarog_market", "Svarog", 5);
        market.setPrimaryEntity(planet);
        planet.setMarket(market);
        planet.setFaction(Factions.INDEPENDENT);
        market.setFactionId(Factions.INDEPENDENT);
        market.getTariff().modifyFlat("gen", market.getFaction().getTariffFraction());
        market.addSubmarket(Submarkets.SUBMARKET_OPEN);
        market.addSubmarket(Submarkets.SUBMARKET_STORAGE);
        market.addSubmarket(Submarkets.GENERIC_MILITARY);
        if (drgModPlugin.SVAROG_OUTPOST_MARKET_ENABLED)
        {
            market.addSubmarket("drg_SvarogMarket");
        }
        market.addSubmarket(Submarkets.SUBMARKET_BLACK);

        market.addCondition(Conditions.COLD);
        market.addCondition(Conditions.HABITABLE);
        market.addCondition(Conditions.LOW_GRAVITY);
        market.addCondition(Conditions.TECTONIC_ACTIVITY);
        market.addCondition(Conditions.POPULATION_5);

        market.addIndustry(Industries.POPULATION);
        market.addIndustry(Industries.MEGAPORT);
        market.addIndustry(Industries.ORBITALWORKS, new ArrayList<>(Arrays.asList(Items.CORRUPTED_NANOFORGE)));
        market.addIndustry(Industries.BATTLESTATION_HIGH);
        market.addIndustry(Industries.REFINING);
        market.addIndustry(Industries.HEAVYBATTERIES);
        market.addIndustry(Industries.MILITARYBASE);

        for (MarketConditionAPI mc : market.getConditions())
        {
            mc.setSurveyed(true);
        }
        market.setSurveyLevel(MarketAPI.SurveyLevel.FULL);
        market.reapplyIndustries();
        Global.getSector().getEconomy().addMarket(market, true);

        // autogenerate jump points that will appear in hyperspace and in system
        JumpPointAPI jumpPoint1 = Global.getFactory().createJumpPoint("drg_zarm_jp1", "Svarog Jump Point");
        OrbitAPI orbit = Global.getFactory().createCircularOrbit(star, 0, 5000, 270);
        jumpPoint1.setOrbit(orbit);
        jumpPoint1.setRelatedPlanet(planet);
        jumpPoint1.setStandardWormholeToHyperspaceVisual();
        system.addEntity(jumpPoint1);
        // the following is hyperspace cleanup code that will remove hyperstorm clouds around this system's location in hyperspace
        // don't need to worry about this, it's more or less copied from vanilla
        system.autogenerateHyperspaceJumpPoints();
        system.generateAnchorIfNeeded();
        // set up hyperspace editor plugin
        HyperspaceTerrainPlugin hyperspaceTerrainPlugin = (HyperspaceTerrainPlugin) Misc.getHyperspaceTerrain().getPlugin(); // get instance of hyperspace terrain
        NebulaEditor nebulaEditor = new NebulaEditor(hyperspaceTerrainPlugin); // object used to make changes to hyperspace nebula

        // set up radiuses in hyperspace of system
        float minHyperspaceRadius = hyperspaceTerrainPlugin.getTileSize() * 2.5f;
        float maxHyperspaceRadius = system.getMaxRadiusInHyperspace();

        // hyperstorm-b-gone (around system in hyperspace)
        nebulaEditor.clearArc(system.getLocation().x, system.getLocation().y, 0,
                minHyperspaceRadius + maxHyperspaceRadius, 0f, 360f, 0.25f);
    }
}
