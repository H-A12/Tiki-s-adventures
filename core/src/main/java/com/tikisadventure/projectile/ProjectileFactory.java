package com.tikisadventure.projectile;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.entities.Entity;

// IMPORTANTE: Importamos todos los comportamientos de la subcarpeta
import com.tikisadventure.projectile.behaviors.StandardPhysics;
import com.tikisadventure.projectile.behaviors.WaveMovement;
import com.tikisadventure.projectile.behaviors.LifetimeBehavior;

public class ProjectileFactory {

    public static Projectile createBullet(Entity owner, Vector2 pos, Vector2 dir, float speed, float dmg, float size, TextureRegion tex) {
        Projectile p = new Projectile(owner, pos, dir, speed, dmg, size, tex);

        // Ahora sí reconocerá StandardPhysics
        p.addBehavior(new StandardPhysics());

        return p;
    }



}
