package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;

public interface ProjectileCreator {
    Projectile create(Vector2 pos, Vector2 dir, float speed, float dmg, float size,
                      TextureRegion tex, EffectManager em, String trailType, float trailInterval,
                      float lifetime, float critChance, float critDamageMult, float impactKnockback,
                      Entity owner);
}
