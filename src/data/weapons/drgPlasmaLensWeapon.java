package data.weapons;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import data.weapons.proj.drgPlasmaLensProjectile;
import org.lwjgl.util.vector.Vector2f;

import java.util.*;

public class drgPlasmaLensWeapon implements OnFireEffectPlugin, EveryFrameWeaponEffectPlugin
{
    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (!weapon.getShip().hasListenerOfClass(drgPlasmaLensWeapon.class)) {
            weapon.getShip().addListener(new drgPlasmaLensListener());
        }
    }

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine)
    {
        ShipAPI source = weapon.getShip();
        ShipAPI target = null;

        if(source.getWeaponGroupFor(weapon)!=null )
        {
            //WEAPON IN AUTOFIRE
            if(source.getWeaponGroupFor(weapon).isAutofiring()  //weapon group is autofiring
                    && source.getSelectedGroupAPI()!=source.getWeaponGroupFor(weapon))
            { //weapon group is not the selected group
                target = source.getWeaponGroupFor(weapon).getAutofirePlugin(weapon).getTargetShip();
            } else {
                target = source.getShipTarget();
            }
        }
        if (target == null)
        {
            // not great but oh well
            // it's 4:55am and I'm not doing vector math
            target = AIUtils.getNearestEnemy(source);
        }
        // double damage to make AI avoid the projectile correctly
        // the listener will halve the damage again
        projectile.setDamageAmount(projectile.getDamageAmount() * 2);
        engine.addPlugin(new drgPlasmaLensProjectile(projectile, target));
    }

    private class drgPlasmaLensListener implements DamageDealtModifier {

        @Override
        public String modifyDamageDealt(Object param, CombatEntityAPI target, DamageAPI damage, Vector2f point, boolean shieldHit)
        {
            if (damage != null) damage.getModifier().modifyMult("drgplslns", 0.5f);
            return "drgplslns";
        }
    }
}
