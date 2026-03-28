package com.tikisadventure.projectile;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.combat.Weapon;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;
import com.tikisadventure.projectile.behaviors.StandardPhysicsBehavior;

// AHORA IMPLEMENTA LA INTERFAZ DE WEAPON
public class ProjectileFactory implements Weapon.ProjectileCreator {

    private final EffectManager effectManager;
    private final TextureRegion bulletTexture;
    private final TextureRegion shrapnelTexture;

    public ProjectileFactory(EffectManager em, TextureRegion bulletTex, TextureRegion shrapnelTex) {
        this.effectManager = em;
        this.bulletTexture = bulletTex;
        this.shrapnelTexture = shrapnelTex;
    }

    /**
     * Implementación del método estándar de la interfaz
     */
    @Override
    public Projectile create(Vector2 pos, Vector2 dir, float speed, float dmg, float size,
                             TextureRegion tex, EffectManager em, EffectType trailType, float trailInterval) {

        // Usamos los parámetros que vienen de la llamada (el owner suele ser null aquí o manejado por el arma)
        Projectile p = new Projectile(null, pos, dir, speed, dmg, size, tex, em, trailType, trailInterval);
        p.addBehavior(new StandardPhysicsBehavior());
        return p;
    }

    /**
     * Método especializado para balas (el que usas en el código actual)
     */
    public Projectile createBullet(Entity owner, Vector2 pos, Vector2 dir, float speed, float dmg, float size,
                                   EffectType trailType, float trailInterval) {

        Projectile p = new Projectile(owner, pos, dir, speed, dmg, size, bulletTexture, effectManager, trailType, trailInterval);
        p.addBehavior(new StandardPhysicsBehavior());
        return p;
    }


}
