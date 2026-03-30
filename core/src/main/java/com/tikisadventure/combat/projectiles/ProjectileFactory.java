package com.tikisadventure.combat.projectiles;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.entities.base.components.StandardPhysicsComponent;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;
import com.tikisadventure.entities.base.Entity;

public class ProjectileFactory implements Weapon.ProjectileCreator {

    private final EffectManager effectManager;
    private final TextureRegion bulletTexture;
    private final Array<Entity> allEntities; // Referencia necesaria para el registro global

    public ProjectileFactory(Array<Entity> allEntities, EffectManager em, TextureRegion bulletTex) {
        this.allEntities = allEntities;
        this.effectManager = em;
        this.bulletTexture = (bulletTex != null) ? bulletTex : null;
    }

    @Override
    public Projectile create(Vector2 pos, Vector2 dir, float speed, float dmg, float size,
                             TextureRegion tex, EffectManager em, EffectType trailType, float trailInterval) {

        // 1. Priorizamos la textura pasada por el arma, si no, usamos la por defecto
        TextureRegion finalTex = (tex != null) ? tex : bulletTexture;

        // 2. Instanciamos el proyectil (Hereda de Entity)
        Projectile p = new Projectile(null, pos, dir, speed, dmg, size, finalTex, em, trailType, trailInterval);

        // 3. Inyectamos la f�sica est�ndar (Movimiento lineal)
        p.addBehavior(new StandardPhysicsComponent());

        // 4. LO REGISTRAMOS EN EL MUNDO
        // Esto es vital para que RenderSystem y CollisionSystem lo vean
        allEntities.add(p);

        return p;
    }
}
