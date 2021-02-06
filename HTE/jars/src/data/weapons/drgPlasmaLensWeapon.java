package data.weapons;

import com.fs.starfarer.api.combat.*;
import org.lazywizard.lazylib.combat.CombatUtils;
import data.weapons.proj.drgPlasmaLensProjectile;

import java.util.*;

public class drgPlasmaLensWeapon implements EveryFrameWeaponEffectPlugin {

    private List<DamagingProjectileAPI> alreadyRegisteredProjectiles = new ArrayList<>();

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {

        if (engine.isPaused()) {
            return;
        }


        // targets for guidance script
        ShipAPI source = weapon.getShip();
        ShipAPI target = null;

        if(source.getWeaponGroupFor(weapon)!=null ){
            //WEAPON IN AUTOFIRE
            if(source.getWeaponGroupFor(weapon).isAutofiring()  //weapon group is autofiring
                    && source.getSelectedGroupAPI()!=source.getWeaponGroupFor(weapon)){ //weapon group is not the selected group
                target = source.getWeaponGroupFor(weapon).getAutofirePlugin(weapon).getTargetShip();
            }
            else {
                target = source.getShipTarget();
            }
        }


        //And clean up our registered projectile list
        List<DamagingProjectileAPI> cloneList = new ArrayList<>(alreadyRegisteredProjectiles);
        for (DamagingProjectileAPI proj : cloneList) {
            if (!engine.isEntityInPlay(proj) || proj.didDamage()) {
                alreadyRegisteredProjectiles.remove(proj);
            }
        }

        for (DamagingProjectileAPI proj : CombatUtils.getProjectilesWithinRange(weapon.getLocation(), 200f)) {
            if (proj.getWeapon() == weapon && !alreadyRegisteredProjectiles.contains(proj) && engine.isEntityInPlay(proj) && !proj.didDamage()) {
                engine.addPlugin(new drgPlasmaLensProjectile(proj, target));
                alreadyRegisteredProjectiles.add(proj);
            }
        }
    }
}
