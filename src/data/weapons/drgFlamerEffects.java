package data.weapons;


import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import data.drgUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;
import plugins.drgModPlugin;

// note from dragn: this was hacked together to make it work with twin-barreled weapons
// it would have been good practice to make it work with an arbitrary number of barrels
// I did not follow good practices :)
public class drgFlamerEffects extends BaseCombatLayeredRenderingPlugin implements OnFireEffectPlugin,
        OnHitEffectPlugin,
        EveryFrameWeaponEffectPlugin
{
    private static final float SOFT_FLUX_FRACTION = 0.4f;
    private static final Color START_COLOR = new Color(0,0,255, 50);
    private static final float FADE_TIME = 1f;
    private boolean runOnce = false;
    // used to track which barrel we're on, if there's more than one
    private int barrel = 0;

    // need one for each barrel to properly track the streams
    // do NOT cross the streams
    // I should really have a list of lists or something but I'm lazy
    protected List<drgFlamerEffects> trailsOne;
    protected List<drgFlamerEffects> trailsTwo;


    public drgFlamerEffects()
    {
    }

    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon)
    {
        if (engine.isPaused() || weapon == null)
            return;

        if (!runOnce)
        {
            runOnce = true;
            ShipAPI ship = weapon.getShip();
            if (!ship.hasListenerOfClass(drgFlamerDamageMod.class))
            {
                ship.addListener(new drgFlamerDamageMod());
            }
        }

        if (trailsOne == null || drgModPlugin.LOW_GRAPHICS_MODE) return;

        Iterator<drgFlamerEffects> iter = trailsOne.iterator();
        while (iter.hasNext())
        {
            if (iter.next().isExpired()) iter.remove();
        }
        if (trailsTwo != null)
        {
            Iterator<drgFlamerEffects> iterTwo = trailsTwo.iterator();
            while (iterTwo.hasNext())
            {
                if (iterTwo.next().isExpired()) iterTwo.remove();
            }
        }
        // sound loop playback
        if (weapon.getShip() != null)
        {
            float maxRange = weapon.getRange();
            ShipAPI ship = weapon.getShip();
            Vector2f com = new Vector2f();
            float weight = 0f;
            float totalDist = 0f;
            Vector2f source = weapon.getLocation();
            for (drgFlamerEffects curr : trailsOne)
            {
                if (curr.proj != null)
                {
                    Vector2f.add(com, curr.proj.getLocation(), com);
                    weight += curr.proj.getBrightness();
                    totalDist += Misc.getDistance(source, curr.proj.getLocation());
                }
            }
            if (trailsTwo != null)
            {
                for (drgFlamerEffects curr : trailsTwo)
                {
                    if (curr.proj != null)
                    {
                        Vector2f.add(com, curr.proj.getLocation(), com);
                        weight += curr.proj.getBrightness();
                        totalDist += Misc.getDistance(source, curr.proj.getLocation());
                    }
                }
            }
            if (weight > 0.1f)
            {
                com.scale(1f / weight);
                float volume = Math.min(weight, 1f);
                if (trailsOne.size() > 0)
                {
                    float extraSize = trailsTwo != null ? trailsTwo.size() : 0;
                    totalDist /= (float) trailsOne.size() + extraSize;
                    float mult = totalDist / Math.max(maxRange, 1f);
                    mult = 1f - mult;
                    if (mult > 1f) mult = 1f;
                    if (mult < 0f) mult = 0f;
                    mult = (float) Math.sqrt(mult);
                    volume *= mult;
                }
                Global.getSoundPlayer().playLoop("drg_flamer_loop", ship, 1f, volume, com, ship.getVelocity());
            }
        }


        //System.out.println("Trails: " + trails.size());
        float numIter = 1f; // more doesn't actually change anything
        amount /= numIter;
        // drag along the previous projectile, starting with the most recently launched; new ones are added at the start
        // note: prev is fired before and so is in front of proj
        for (int i = 0; i < numIter; i++)
        {
            for (drgFlamerEffects trail : trailsOne)
            {
                //trail.proj.setFacing(trail.proj.getFacing() + 180f * amount);
                if (trail.prev != null && !trail.prev.isExpired() && Global.getCombatEngine().isEntityInPlay(trail.prev))
                {
                    float dist1 = Misc.getDistance(trail.prev.getLocation(), trail.proj.getLocation());
                    if (dist1 < trail.proj.getProjectileSpec().getLength() * 1f)
                    {
                        float maxSpeed = trail.prev.getMoveSpeed() * 0.5f;// * Math.max(0.5f, 1f - trail.prev.getElapsed() * 0.5f);
                        // goal here is to prevent longer shot series (e.g. from Paragon) from moving too unnaturally
                        float e = trail.prev.getElapsed();
                        float t = 0.5f;
                        if (e > t)
                        {
                            maxSpeed *= Math.max(0.25f, 1f - (e - t) * 0.5f);
                        }
                        if (dist1 < 20f && e > t)
                        {
                            maxSpeed *= dist1 / 20f;
                        }

                        Vector2f driftTo = Misc.closestPointOnLineToPoint(trail.proj.getLocation(), trail.proj.getTailEnd(), trail.prev.getLocation());
                        float dist = Misc.getDistance(driftTo, trail.prev.getLocation());
                        Vector2f diff = Vector2f.sub(driftTo, trail.prev.getLocation(), new Vector2f());
                        diff = Misc.normalise(diff);
                        diff.scale(Math.min(dist, maxSpeed * amount));
                        Vector2f.add(trail.prev.getLocation(), diff, trail.prev.getLocation());
                        Vector2f.add(trail.prev.getTailEnd(), diff, trail.prev.getTailEnd());
                    }
                }
            }
            if (trailsTwo != null)
            {
                for (drgFlamerEffects trail : trailsTwo)
                {
                    //trail.proj.setFacing(trail.proj.getFacing() + 180f * amount);
                    if (trail.prev != null && !trail.prev.isExpired() && Global.getCombatEngine().isEntityInPlay(trail.prev))
                    {
                        float dist1 = Misc.getDistance(trail.prev.getLocation(), trail.proj.getLocation());
                        if (dist1 < trail.proj.getProjectileSpec().getLength() * 1f)
                        {
                            float maxSpeed = trail.prev.getMoveSpeed() * 0.5f;// * Math.max(0.5f, 1f - trail.prev.getElapsed() * 0.5f);
                            // goal here is to prevent longer shot series (e.g. from Paragon) from moving too unnaturally
                            float e = trail.prev.getElapsed();
                            float t = 0.5f;
                            if (e > t)
                            {
                                maxSpeed *= Math.max(0.25f, 1f - (e - t) * 0.5f);
                            }
                            if (dist1 < 20f && e > t)
                            {
                                maxSpeed *= dist1 / 20f;
                            }

                            Vector2f driftTo = Misc.closestPointOnLineToPoint(trail.proj.getLocation(), trail.proj.getTailEnd(), trail.prev.getLocation());
                            float dist = Misc.getDistance(driftTo, trail.prev.getLocation());
                            Vector2f diff = Vector2f.sub(driftTo, trail.prev.getLocation(), new Vector2f());
                            diff = Misc.normalise(diff);
                            diff.scale(Math.min(dist, maxSpeed * amount));
                            Vector2f.add(trail.prev.getLocation(), diff, trail.prev.getLocation());
                            Vector2f.add(trail.prev.getTailEnd(), diff, trail.prev.getTailEnd());
                        }
                    }
                }
            }
        }
    }


    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine)
    {
        if (drgModPlugin.LOW_GRAPHICS_MODE)
            return;
        Color color = projectile.getProjectileSpec().getFringeColor();
//		Color inverted = NSLanceEffect.getColorForDarkening(color);
//		inverted = Misc.setAlpha(inverted, 50);
//		Color inverted = new Color(255, 255, 100, 50);

        Vector2f vel = new Vector2f();
        if (target instanceof ShipAPI)
        {
            vel.set(target.getVelocity());
        }

        float size = projectile.getProjectileSpec().getWidth() * 1f;
        //size = Misc.getHitGlowSize(size, projectile.getDamage().getBaseDamage(), damageResult);
        float sizeMult = Misc.getHitGlowSize(100f, projectile.getDamage().getBaseDamage(), damageResult) / 100f;
//		sizeMult = 1.5f;
//		System.out.println(sizeMult);
        float dur = 1f;
        float rampUp = 0f;
        engine.addNebulaParticle(point, vel, size, 5f + 3f * sizeMult,
                rampUp, 0f, dur, color);
//		engine.addNegativeNebulaParticle(point, vel, size, 2f,
//										rampUp, 0f, dur, inverted);
//		engine.addNegativeParticle(point, vel, size,
//								   rampUp, dur, inverted);

        /*Misc.playSound(damageResult, point, vel,
                "cryoflamer_hit_shield_light",
                "cryoflamer_hit_shield_solid",
                "cryoflamer_hit_shield_heavy",
                "cryoflamer_hit_light",
                "cryoflamer_hit_solid",
                "cryoflamer_hit_heavy");*/
    }

    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine)
    {
        if (drgModPlugin.LOW_GRAPHICS_MODE)
            return;
        ShipAPI ship = weapon.getShip();
        if (ship != null)
        {
            for (int i = 0; i < 3; i++) {
                Vector2f point = MathUtils.getRandomPointInCone(projectile.getLocation(), 30f, weapon.getCurrAngle() - 50f, weapon.getCurrAngle() + 50f);
                Vector2f vel = (Vector2f) VectorUtils.getDirectionalVector(weapon.getLocation(), point).scale(MathUtils.getDistance(point, weapon.getLocation()) * (2.5f*Misc.random.nextFloat()+1f));
                Vector2f.add(vel, ship.getVelocity(), vel);
                float size = (float) (10f + (Math.random() * 10f));
                engine.addNebulaParticle(point, vel, 10f, 8f, 0f, 0f, 1f, projectile.getProjectileSpec().getFringeColor());
            }
        }
        if (weapon.getSpec().getHardpointAngleOffsets().size() == 1)
        {
            String prevKey = "drgflamer_prev_" + weapon.getShip().getId() + "_" + weapon.getSlot().getId();
            DamagingProjectileAPI prev = (DamagingProjectileAPI) engine.getCustomData().get(prevKey);

            drgFlamerEffects trail = new drgFlamerEffects(projectile, prev);
            CombatEntityAPI e = engine.addLayeredRenderingPlugin(trail);
            e.getLocation().set(projectile.getLocation());

            engine.getCustomData().put(prevKey, projectile);

            if (trailsOne == null)
            {
                trailsOne = new ArrayList<drgFlamerEffects>();
            }
            trailsOne.add(0, trail);
        } else
        {
            if (barrel == 0)
            {
                barrel++;
                String prevKey = "drgflamer_prev1_" + weapon.getShip().getId() + "_" + weapon.getSlot().getId();
                DamagingProjectileAPI prev = (DamagingProjectileAPI) engine.getCustomData().get(prevKey);

                drgFlamerEffects trail = new drgFlamerEffects(projectile, prev);
                CombatEntityAPI e = engine.addLayeredRenderingPlugin(trail);
                e.getLocation().set(projectile.getLocation());

                engine.getCustomData().put(prevKey, projectile);

                if (trailsOne == null)
                {
                    trailsOne = new ArrayList<drgFlamerEffects>();
                }
                trailsOne.add(0, trail);
            } else if (barrel == 1)
            {
                barrel = 0;
                String prevKey = "drgflamer_prev2_" + weapon.getShip().getId() + "_" + weapon.getSlot().getId();
                DamagingProjectileAPI prev = (DamagingProjectileAPI) engine.getCustomData().get(prevKey);

                drgFlamerEffects trail = new drgFlamerEffects(projectile, prev);
                CombatEntityAPI e = engine.addLayeredRenderingPlugin(trail);
                e.getLocation().set(projectile.getLocation());

                engine.getCustomData().put(prevKey, projectile);

                if (trailsTwo == null)
                {
                    trailsTwo = new ArrayList<drgFlamerEffects>();
                }
                trailsTwo.add(0, trail);
            }
        }
    }


    public static class ParticleData
    {
        public SpriteAPI sprite;
        public Vector2f offset = new Vector2f();
        public Vector2f vel = new Vector2f();
        public float scale = 1f;
        public DamagingProjectileAPI proj;
        public float scaleIncreaseRate = 1f;
        public float turnDir = 1f;
        public float angle = 1f;
        public FaderUtil fader;
        public float elapsed = 0f;

        public ParticleData(DamagingProjectileAPI proj)
        {
            this.proj = proj;
            sprite = Global.getSettings().getSprite("misc", "nebula_particles");
            //sprite = Global.getSettings().getSprite("misc", "dust_particles");
            float i = Misc.random.nextInt(4);
            float j = Misc.random.nextInt(4);
            sprite.setTexWidth(0.25f);
            sprite.setTexHeight(0.25f);
            sprite.setTexX(i * 0.25f);
            sprite.setTexY(j * 0.25f);
            sprite.setAdditiveBlend();

            angle = (float) Math.random() * 360f;

            float maxDur = proj.getWeapon().getRange() / proj.getWeapon().getProjectileSpeed();
            scaleIncreaseRate = 2f / maxDur;
            scale = 1f;
//			scale = 0.1f;
//			scaleIncreaseRate = 2.9f / maxDur;
//			scale = 0.1f;
//			scaleIncreaseRate = 2.5f / maxDur;
//			scale = 0.5f;

            turnDir = Math.signum((float) Math.random() - 0.5f) * 60f * (float) Math.random();
            //turnDir = 0f;

            float driftDir = (float) Math.random() * 360f;
            vel = Misc.getUnitVectorAtDegreeAngle(driftDir);
            vel.scale(proj.getProjectileSpec().getWidth() / maxDur * 0.33f);

//			offset.x += vel.x * 1f;
//			offset.y += vel.y * 1f;
            fader = new FaderUtil(0f, 0.25f, 0.5f);
            fader.fadeIn();
        }

        public void advance(float amount)
        {
            scale += scaleIncreaseRate * amount;
            if (scale < 1f)
            {
                scale += scaleIncreaseRate * amount * 1f;
            }
            offset.x += vel.x * amount;
            offset.y += vel.y * amount;

            angle += turnDir * amount;
            elapsed += amount;
            fader.advance(amount);
        }
    }

    protected List<ParticleData> particles = new ArrayList<ParticleData>();

    protected DamagingProjectileAPI proj;
    protected DamagingProjectileAPI prev;
    protected float baseFacing = 0f;

    public drgFlamerEffects(DamagingProjectileAPI proj, DamagingProjectileAPI prev)
    {
        this.proj = proj;
        this.prev = prev;

        baseFacing = proj.getFacing();

        int num = 7;
        for (int i = 0; i < num; i++)
        {
            particles.add(new ParticleData(proj));
        }

        float length = proj.getProjectileSpec().getLength();
        float width = proj.getProjectileSpec().getWidth();

        float index = 0;
        for (ParticleData p : particles)
        {
            float f = index / (particles.size() - 1);
            Vector2f dir = Misc.getUnitVectorAtDegreeAngle(proj.getFacing() + 180f);
            dir.scale(length * f);
            Vector2f.add(p.offset, dir, p.offset);

            p.offset = Misc.getPointWithinRadius(p.offset, width * 0.5f);
            //p.scale = 0.25f + 0.75f * (1 - f);

            index++;
        }
    }

    public float getRenderRadius()
    {
        return 300f;
    }


    protected EnumSet<CombatEngineLayers> layers = EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);

    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers()
    {
        return layers;
    }


    public void init(CombatEntityAPI entity)
    {
        super.init(entity);
    }

    public void advance(float amount)
    {
        if (Global.getCombatEngine().isPaused() || drgModPlugin.LOW_GRAPHICS_MODE) return;

        entity.getLocation().set(proj.getLocation());

        for (ParticleData p : particles)
        {
            p.advance(amount);
        }
    }


    public boolean isExpired()
    {
        return proj.isExpired() || !Global.getCombatEngine().isEntityInPlay(proj);
    }

    public void render(CombatEngineLayers layer, ViewportAPI viewport)
    {
        float x = entity.getLocation().x;
        float y = entity.getLocation().y;

        //Color color = new Color(100,150,255,50);
        Color color = proj.getProjectileSpec().getFringeColor();
        color = Misc.setAlpha(color, 50);
        float b = proj.getBrightness();
        b *= viewport.getAlphaMult();

        for (ParticleData p : particles)
        {
            float size = proj.getProjectileSpec().getWidth() * 0.6f;
            size *= p.scale;

            float alphaMult = 1f;
            Vector2f offset = p.offset;
            float diff = Misc.getAngleDiff(baseFacing, proj.getFacing());
            if (Math.abs(diff) > 0.1f)
            {
                offset = Misc.rotateAroundOrigin(offset, diff);
            }
            Vector2f loc = new Vector2f(x + offset.x, y + offset.y);

            p.sprite.setAngle(p.angle);
            p.sprite.setSize(size, size);
            p.sprite.setAlphaMult(b * alphaMult * p.fader.getBrightness());
            p.sprite.setColor(color);
            p.sprite.renderAtCenter(loc.x, loc.y);
        }
    }

    public static class drgFlamerDamageMod implements DamageDealtModifier
    {
        public drgFlamerDamageMod()
        {

        }

        /**
         * @param param     This is the thing that's dealing the damage. DamagingProjectileAPI, BeamAPI, or MissileAPI
         * @param target    What the damage is getting applied to
         * @param damage    Damage amount and type.
         * @param point     Location where the hit is occurring.
         * @param shieldHit True if the damage is being applied to shields.
         * @return Should return the ID of the modifier. This is the same as the id argument for any of the stat modifiers.
         */
        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit)
        {
            // in this case, we're only modifying the damage from TPR projectiles on armor hits
            if (param instanceof DamagingProjectileAPI && shieldHit && target instanceof ShipAPI)
            {
                ShipAPI targetShip = (ShipAPI) target;
                WeaponAPI weapon = ((DamagingProjectileAPI) param).getWeapon();
                if (weapon == null)
                    return null;
                if (weapon.getId().contains("drg_flamer"))
                {
                    //if (shieldHit)
                    //{
                        damage.getModifier().modifyMult("drg_flamer", 1f - SOFT_FLUX_FRACTION);
                        targetShip.getFluxTracker().increaseFlux(targetShip.getMutableStats().getEnergyShieldDamageTakenMult().computeMultMod() * ((DamagingProjectileAPI) param).getDamageAmount() * SOFT_FLUX_FRACTION, false);
                        return "drg_flamer";
                    /*} else {
                        if (Misc.random.nextFloat() <= 0.1f)
                        {
                            damage.getModifier().modifyMult("drg_flamer", 3f);
                            return  "drg_flamer";
                        }
                    }*/
                }
            }
            return null;
        }
    }
}




