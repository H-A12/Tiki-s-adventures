package com.tikisadventure.combat.projectiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool;
import com.tikisadventure.combat.weapons.ProjectileCreator;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.effects.EffectManager;

public class ProjectileFactory implements ProjectileCreator {

    private final EffectManager effectManager;
    private final TextureRegion bulletTexture;
    private final Pool<Projectile> projectilePool;

    public ProjectileFactory(EffectManager em, TextureRegion bulletTex, int maxProjectiles) {
        this.effectManager = em;
        this.bulletTexture = bulletTex;
        this.projectilePool = new Pool<Projectile>(maxProjectiles) {
            @Override
            protected Projectile newObject() {
                return new Projectile(null, new Vector2(), new Vector2(), 0, 0, 0, 0, 0, null, null, null, 0);
            }

            @Override
            public void free(Projectile object) {
                object.reset();
                super.free(object);
            }
        };
    }

    @Override
    public Projectile create(Vector2 pos, Vector2 dir, float speed, float dmg, float size,
                             TextureRegion tex, EffectManager em, String trailType, float trailSpacing,
                             float lifetime, float critChance, float critDamageMult, float impactKnockback) {

        Projectile p = projectilePool.obtain();
        p.setOwner(null);
        p.setPosition(pos);
        p.setLastTrailPos(pos);
        p.setDirection(dir);
        p.setSpeed(speed);
        p.setDamage(dmg);
        p.setRadius(size);
        p.setSprite(tex != null ? tex : bulletTexture);
        p.setEffectManager(em);
        p.setTrailType(trailType);
        p.setTrailSpacing(trailSpacing);
        p.setLifetime(lifetime);
        p.setCritChance(critChance);
        p.setCritDamageMult(critDamageMult);
        p.setImpactKnockback(impactKnockback);
        p.setAlive(true);
        p.setStateTime(0);
        p.setDamageType(com.tikisadventure.combat.DamageType.KINETIC);
        p.setPenetration(0);

        p.calculateCritStats();

        return p;
    }

    public void dispose() {
    }

    public void freeProjectile(Projectile p) {
        if (p != null) {
            projectilePool.free(p);
        }
    }
}
