package com.tikisadventure.entities.gadgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.ExplosionUtility;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.core.Assets;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;

//Mina que al armarte explota y daña enemigos cercanos.
//Usa ExplosionUtility para los efectos visuales.
public class SewerMine extends Entity {
    private final EffectManager effectManager;
    private float timer;
    private final float duration;
    private float armingTimer = 1.0f;
    private final float radius;
    private final float damage;
    private final String explosionProfile;
    private final TextureRegion region;
    private final DamageType damageType;

    private boolean exploding = false;

    public SewerMine(EffectManager em, Vector2 position, float duration, float radius, float damage, String profile, DamageType damageType) {
        this.effectManager = em;
        this.getPosition().set(position);
        this.timer = duration;
        this.duration = duration;
        this.radius = radius;
        this.damage = damage;
        this.explosionProfile = profile;
        this.damageType = damageType;
        this.region = Assets.getRegion("shared", "weapons_assets/Sewer");

        actualizarHitboxes();
    }

    @Override
    //Actualizar temporizador, parpadear al final y detectar enemigos cercanos
    public void update(float delta, Array<Entity> enemies) {
        if (!isAlive() || exploding) return;

        timer -= delta;

        if (armingTimer > 0) {
            armingTimer -= delta;
        }

        if (timer <= 5f) {
            float blink = (float) Math.abs(Math.sin(timer * 12f));
            getTintColor().set(1f, 1f, 1f, 1f).lerp(Color.BLACK, blink * 0.7f);
        }

        if (timer <= 0) {
            setAlive(false);
            return;
        }

        // 2. Detección de Enemigos (SOLO si ya se ha armado)
        if (armingTimer <= 0 && enemies != null) {
            for (Entity enemy : enemies) {
                if (enemy.isAlive() && enemy.getPosition().dst(this.getPosition()) <= radius) {
                    detonate(enemies);
                    break;
                }
            }
        }
    }

    //Explotar: efectos visuales y daño en área a enemigos
    private void detonate(Array<Entity> enemies) {
        exploding = true;

        ExplosionUtility.spawnVisuals(effectManager, getPosition(), explosionProfile);

        // Daño en área a enemigos
        for (Entity e : enemies) {
            if (e.isAlive() && e.getPosition().dst(this.getPosition()) <= radius * 1.5f) {
                e.receiveDamage(damage, false, this.damageType);
            }
        }

        setAlive(false);
    }

    @Override
    //Dibujar mina con tintado de parpadeo y barra de duración
    public void draw(Batch batch, float delta) {
        Color prevColor = batch.getColor();
        batch.setColor(getTintColor());

        float size = 1.2f;
        batch.draw(region, getPosition().x - size/2f, getPosition().y - size/2f, size, size);

        batch.setColor(prevColor);

        drawProgressBar(batch, timer, duration);
    }

    @Override public void update(float delta, Entity target) {}
}
