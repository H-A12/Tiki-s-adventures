package com.tikisadventure.entities.gadgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.ExplosionUtility;
import com.tikisadventure.core.Assets;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;

//Espantapájaros: revive al jugador si muere mientras esté activo.
//Al acabarse el temporizador explota con ExplosionUtility.
public class Scarecrow extends Entity {
    private final EffectManager effectManager;
    private float timer;
    private final float duration;
    private final String profile;
    private TextureRegion region;

    public Scarecrow(EffectManager effectManager, Vector2 position, float duration, String profile) {
        this.effectManager = effectManager;
        this.getPosition().set(position);
        this.timer = duration;
        this.duration = duration;
        this.profile = profile;

        this.region = Assets.getRegion("shared", "weapons_assets/Scarecrow");
        if (this.region == null) {
            this.region = Assets.getRegion("shared", "Scarecrow");
        }

        setANCHO(1.2f);
        setALTO(1.2f);
        actualizarHitboxes();
    }

    @Override
    //Contar hacia abajo, parpadear al final y explotar al llegar a 0
    public void update(float delta, Array<Entity> enemies) {
        if (!isAlive()) return;

        timer -= delta;

        if (timer <= 3f) {
            float blink = (float) Math.abs(Math.sin(timer * 15f));
            getTintColor().set(1f, 1f, 1f, 1f).lerp(Color.BLACK, blink * 0.7f);
        }

        if (timer <= 0) {
            if (profile != null && !profile.isEmpty()) {
                ExplosionUtility.spawnVisuals(effectManager, getPosition(), profile);
            }
            setAlive(false);
        }
    }

    @Override
    //Dibujar espantapájaros con tintado de parpadeo y barra de duración
    public void draw(Batch batch, float delta) {
        if (region == null) return;

        Color prevColor = batch.getColor();
        batch.setColor(getTintColor());

        float size = 1.2f;
        batch.draw(region, getPosition().x - size/2f, getPosition().y - size/2f, size, size);

        batch.setColor(prevColor);

        drawProgressBar(batch, timer, duration);
    }

    @Override public void update(float delta, Entity target) {}
}
