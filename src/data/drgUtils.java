package data;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain.RingParams;
import com.fs.starfarer.api.impl.campaign.terrain.RingSystemTerrainPlugin;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class drgUtils
{
    public static float lerp(float a, float b, float amount)
    {
        return a*(1f-amount) + b*amount;
    }

    public static Vector2f lerp(Vector2f a, Vector2f b, float amount)
    {
        return new Vector2f(lerp(a.x, b.x, amount), lerp(a.y, b.y, amount));
    }

    public static float randBetween(float a, float b)
    {
        return lerp(a, b, Misc.random.nextFloat());
    }
    /**
     * Draws an arc explosion thingy.
     * @param source projectile to draw arcs from
     */
    public static void plasmaEffects(DamagingProjectileAPI source, Color color, int numArcs, float expRadius)
    {
        final Color FRINGE_COLOR = new Color(46, 91, 255);
        final int NUM_ARCS = 4;
        Vector2f from = MathUtils.getRandomPointInCircle(source.getLocation(), 20f);
        CombatEngineAPI engine = Global.getCombatEngine();
        for (int i = 0; i < NUM_ARCS; i++)
        {
            engine.spawnEmpArcVisual(from, null, MathUtils.getRandomPointInCircle(from, expRadius * 0.66f), null,
                    10f, FRINGE_COLOR, Color.white);
        }
        if (expRadius > 0f)
        {
            engine.addSmoothParticle(source.getLocation(), Misc.ZERO, expRadius, 1.3f, 1.2f, color);
            engine.addSmoothParticle(source.getLocation(), Misc.ZERO, expRadius * 0.5f, 1.3f, 0.5f, Color.white);
        }
    }

    // code provided by tomatopaste
    public static boolean isEntityInArc(CombatEntityAPI entity, Vector2f center, float centerAngle, float arcDeviation)
    {
        if (entity instanceof ShipAPI)
        {
            Vector2f point = getNearestPointOnShipBounds((ShipAPI) entity, center);
            return Misc.isInArc(centerAngle, arcDeviation * 2f, center, point);
        } else
        {
            return Misc.isInArc(centerAngle, arcDeviation * 2f, center,
                    getNearestPointOnCollisionRadius(center, entity));
        }
    }

    public static Vector2f getNearestPointOnShipBounds(ShipAPI ship, Vector2f point)
    {
        BoundsAPI bounds = ship.getExactBounds();
        if (bounds == null)
        {
            return getNearestPointOnCollisionRadius(point, ship);
        } else
        {
            Vector2f closest = ship.getLocation();
            float distSquared = 0f;
            for (BoundsAPI.SegmentAPI segment : bounds.getSegments())
            {
                Vector2f tmpcp = MathUtils.getNearestPointOnLine(point, segment.getP1(), segment.getP2());
                float distSquaredTemp = MathUtils.getDistanceSquared(tmpcp, point);
                if (distSquaredTemp < distSquared)
                {
                    distSquared = distSquaredTemp;
                    closest = tmpcp;
                }
            }
            return closest;
        }
    }

    public static Vector2f getNearestPointOnCollisionRadius(Vector2f point, CombatEntityAPI entity)
    {
        return MathUtils.getPointOnCircumference(entity.getLocation(), entity.getCollisionRadius(),
                VectorUtils.getAngle(entity.getLocation(), point));
    }

    // Direct copy of MagicLib's method. Replicated here since a hard dependency for just a single method seems a bit excessive.
    /**
     * Creates sharp lensflares, more suited to very thin short lived flares
     * Not CPU intensive
     *
     * @param engine
     * Combat engine.
     * @param origin
     * Source of the Flare. Can be anything but CANNOT BE NULL.
     * @param point
     * Absolute coordinates of the flare.
     * @param thickness
     * Thickness of the flare in pixels. Work best between 3 and 10.
     * @param length
     * Length of the flare's branches in pixels.
     * Works great between 50 and 300 but can easily be longer/shorter.
     * @param angle
     * Angle of the flare. 0 means horizontal.
     * Remember that real Anamorphic flares are always horizontal.
     * @param fringeColor
     * Fringe color of the flare.
     * @param coreColor
     * Core color of the flare.
     */
    public static void createSharpFlare(CombatEngineAPI engine, ShipAPI origin, Vector2f point, float thickness, float length, float angle, Color fringeColor, Color coreColor){

        Vector2f offset = new Vector2f(0,thickness);
        VectorUtils.rotate(offset, MathUtils.clampAngle(angle), offset);
        Vector2f.add(offset, point, offset);

        engine.spawnEmpArc(
                origin,
                point,
                new SimpleEntity(point),
                new SimpleEntity(offset),
                DamageType.FRAGMENTATION,
                0f,
                0f,
                100f,
                null,
                length,
                fringeColor,
                coreColor
        );
    }

    /**
     * Blends two colors
     * @param c1 color 1
     * @param c2 color 2
     * @param ratio what percent of color 1 the result should have
     * @return blended color
     */
    public static Color blendColors( Color c1, Color c2, float ratio ) {

        float iRatio = 1.0f - ratio;

        int a1 = c1.getAlpha();
        int r1 = c1.getRed();
        int g1 = c1.getGreen();
        int b1 = c1.getBlue();

        int a2 = c2.getAlpha();
        int r2 = c2.getRed();
        int g2 = c2.getGreen();
        int b2 = c2.getBlue();

        int a = (int)((a1 * iRatio) + (a2 * ratio));
        int r = (int)((r1 * iRatio) + (r2 * ratio));
        int g = (int)((g1 * iRatio) + (g2 * ratio));
        int b = (int)((b1 * iRatio) + (b2 * ratio));

        return new Color(r,g,b,a);
    }
}
