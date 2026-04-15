package com.tikisadventure.combat.projectiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.combat.weapons.ProjectileCreator;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.effects.EffectManager;

public class ProjectileFactory implements ProjectileCreator {

    private final EffectManager effectManager;
    private final TextureRegion bulletTexture;

    public ProjectileFactory(EffectManager em, TextureRegion bulletTex) {
        this.effectManager = em;
        this.bulletTexture = bulletTex;
    }

    @Override
    public Projectile create(Vector2 pos, Vector2 dir, float speed, float dmg, float size,
                             TextureRegion tex, EffectManager em, String trailType, float trailSpacing,
                             float lifetime, float critChance, float critDamageMult, float impactKnockback) {

        Projectile p = new Projectile(null, pos, dir, speed, dmg, critChance, critDamageMult, size, tex, em, trailType, trailSpacing);
        p.setLifetime(lifetime);
        p.setImpactKnockback(impactKnockback);
        return p;
    }
}
