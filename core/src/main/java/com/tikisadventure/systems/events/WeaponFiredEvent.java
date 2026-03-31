package com.tikisadventure.systems.events;

import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.entities.base.Entity;

public class WeaponFiredEvent implements Event {
    public final Weapon weapon;
    public final Entity target;

    public WeaponFiredEvent(Weapon weapon, Entity target) {
        this.weapon = weapon;
        this.target = target;
    }
}
