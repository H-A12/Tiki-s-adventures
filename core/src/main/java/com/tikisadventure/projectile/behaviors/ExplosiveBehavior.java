package com.tikisadventure.projectile.behaviors;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.projectile.Projectile;
import com.tikisadventure.projectile.ProjectileBehavior;

public class ExplosiveBehavior implements ProjectileBehavior {

    private final EffectManager effectManager;
    private final float explosionDamage; // <--- NUEVO: Daño propio de la explosión
    private final float explosionRadius;
    private final float knockbackForce;
    private final int smokeCount;
    private final int sparkCount;

    private boolean hasExploded = false;

    // CONSTRUCTOR ACTUALIZADO: Ahora acepta el daño como segundo parámetro
    public ExplosiveBehavior(EffectManager em, float damage, float radius, float force, int smokes, int sparks) {
        this.effectManager = em;
        this.explosionDamage = damage; // Guardamos el daño específico
        this.explosionRadius = radius;
        this.knockbackForce = force;
        this.smokeCount = smokes;
        this.sparkCount = sparks;
    }

    @Override
    public void update(Projectile p, float delta, Array<Entity> enemies) {
        if (!p.isAlive() && !hasExploded) {
            explode(p, enemies);
            hasExploded = true;
        }
    }

    private void explode(Projectile p, Array<Entity> enemies) {
        if (effectManager == null) return;

        // --- 1. EFECTOS VISUALES ---
        effectManager.spawnEffect(EffectType.EXPLOSION_FLASH, p.getPosition(), new Vector2(0, 0));

        for (int i = 0; i < smokeCount; i++) {
            Vector2 offset = new Vector2(p.getPosition()).add(MathUtils.random(-0.3f, 0.3f), MathUtils.random(-0.3f, 0.3f));
            Vector2 smokeDir = new Vector2(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).scl(0.5f);
            effectManager.spawnEffect(EffectType.EXPLOSION_HUMO, offset, smokeDir);
        }

        for (int i = 0; i < sparkCount; i++) {
            Vector2 sparkDir = new Vector2(MathUtils.random(-1f, 1f), MathUtils.random(-1f, 1f)).nor().scl(MathUtils.random(4f, 8f));
            effectManager.spawnEffect(EffectType.EXPLOSION_CHISPA, p.getPosition(), sparkDir);
        }

        // --- 2. LÓGICA DE DAÑO Y EMPUJE ---
        for (Entity enemy : enemies) {
            if (enemy.isAlive()) {
                float distance = p.getPosition().dst(enemy.getPosicion());

                if (distance <= explosionRadius) {
                    // CAMBIO CLAVE: Usamos explosionDamage en lugar de p.getDamage()
                    enemy.receiveDamage(this.explosionDamage);

                    Vector2 pushDir = new Vector2(enemy.getPosicion()).sub(p.getPosition()).nor();
                    if (pushDir.len() == 0) pushDir.set(1, 0);

                    float intensity = 1.0f - (distance / explosionRadius);
                    float finalForce = knockbackForce * intensity;

                    enemy.getPosicion().mulAdd(pushDir, finalForce);
                }
            }
        }
    }
}
