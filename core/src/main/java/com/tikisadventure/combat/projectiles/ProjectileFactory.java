package com.tikisadventure.combat.projectiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.components.StandardPhysicsComponent;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;

public class ProjectileFactory implements Weapon.ProjectileCreator {

    private final EffectManager effectManager;
    private final TextureRegion bulletTexture;

    public ProjectileFactory(EffectManager em, TextureRegion bulletTex) {
        this.effectManager = em;
        this.bulletTexture = bulletTex;
    }

    @Override
    public Projectile create(Vector2 pos, Vector2 dir, float speed, float dmg, float size,
                             TextureRegion tex, EffectManager em, EffectType trailType, float trailInterval) {

        Projectile p = new Projectile(null, pos, dir, speed, dmg, size, tex, em, trailType, trailInterval);
        p.addComponent(new StandardPhysicsComponent());
        return p;
    }
}
