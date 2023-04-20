package data.weapons.proj;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.combat.DisintegratorEffect;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

public class drgFissureOnHit implements OnHitEffectPlugin
{
    public static float DAMAGE = 1250;

    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit, ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if (!shieldHit && target instanceof ShipAPI) {
            dealArmorDamage(projectile, (ShipAPI) target, point);
        }
    }

    public static void dealArmorDamage(DamagingProjectileAPI projectile, ShipAPI target, Vector2f point) {
        CombatEngineAPI engine = Global.getCombatEngine();

        ArmorGridAPI grid = target.getArmorGrid();
        int[] cell = grid.getCellAtLocation(point);
        if (cell == null) return;

        int gridWidth = grid.getGrid().length;
        int gridHeight = grid.getGrid()[0].length;

        float damageTypeMult = DisintegratorEffect.getDamageTypeMult(projectile.getSource(), target);

        int numCells = 0;
        for (int i = -3; i <= 3; i++) {
            for (int j = -3; j <= 3; j++) {
                if ((i == 3 || i == -3) && (j == 3 || j == -3)) continue; // skip corners

                int cx = cell[0] + i;
                int cy = cell[1] + j;

                if (cx < 0 || cx >= gridWidth || cy < 0 || cy >= gridHeight) continue;

                numCells++;
            }
        }

        float damageDealt = 0f;
        for (int i = -3; i <= 3; i++) {
            for (int j = -3; j <= 3; j++) {
                if ((i == 3 || i == -3) && (j == 3 || j == -3)) continue; // skip corners

                int cx = cell[0] + i;
                int cy = cell[1] + j;

                if (cx < 0 || cx >= gridWidth || cy < 0 || cy >= gridHeight) continue;

                float armorInCell = grid.getArmorValue(cx, cy);
                float damage = DAMAGE * (1/(float)numCells) * damageTypeMult;
                damage = Math.min(damage, armorInCell);
                if (damage <= 0) continue;

                target.getArmorGrid().setArmorValue(cx, cy, Math.max(0, armorInCell - damage));
                damageDealt += damage;
            }
        }

        if (damageDealt > 0) {
            if (Misc.shouldShowDamageFloaty(projectile.getSource(), target)) {
                engine.addFloatingDamageText(point, damageDealt, Misc.FLOATY_ARMOR_DAMAGE_COLOR, target, projectile.getSource());
            }
            target.syncWithArmorGridState();
        }
    }
}
