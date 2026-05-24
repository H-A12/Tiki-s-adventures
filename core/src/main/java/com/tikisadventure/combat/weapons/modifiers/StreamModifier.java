package com.tikisadventure.combat.weapons.modifiers;

import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.combat.weapons.WeaponModifier;

//Configurar el arma para disparar en ráfaga continua
public class StreamModifier implements WeaponModifier {
    private final float visualFireRate;
    private final boolean blockCritLeech;

    public StreamModifier(float visualFireRate, boolean blockCritLeech) {
        this.visualFireRate = visualFireRate;
        this.blockCritLeech = blockCritLeech;
    }

    @Override
    public void apply(Weapon weapon) {
        weapon.setVisualFireRate(visualFireRate);
        weapon.setBlockCritLeech(blockCritLeech);
    }
}
