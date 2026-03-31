package com.tikisadventure.systems.events;

import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.entities.base.Entity;

public class ProjectileImpactEvent implements Event {
    public final Projectile projectile;
    public final Entity target;

    public ProjectileImpactEvent(Projectile projectile, Entity target) {
        this.projectile = projectile;
        this.target = target;
    }
}
