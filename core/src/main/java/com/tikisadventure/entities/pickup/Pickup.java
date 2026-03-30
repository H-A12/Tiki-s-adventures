package com.tikisadventure.entities.pickup;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.entities.base.Entity;

public abstract class Pickup extends Entity {

    protected float pickupRadius = 0.8f;
    // Animación común para el objeto (se asignará en las clases hijas como MiniHeal)
    protected Animation<TextureRegion> animation;

    public Pickup(Vector2 position) {
        super(position.x, position.y);
        this.vida = 1;
        this.alive = true;
        this.ANCHO = 0.5f;
        this.ALTO = 0.5f;
    }

    /**
     * IMPLEMENTACIÓN OBLIGATORIA: Para Pickups, devolvemos la animación principal.
     * Esto hace que MiniHeal, Coin, etc., compilen automáticamente.
     */
    @Override
    public Animation<TextureRegion> getAnimationForState(Estado estado) {
        return animation;
    }

    public void update(float delta, Entity player) {
        if (!alive || player == null) return;

        float dist = posicion.dst(player.getHitboxActionTrigger().x, player.getHitboxActionTrigger().y);

        if (dist < pickupRadius + player.getHitboxActionTrigger().radius) {
            onPickup(player);
            this.alive = false;
        }

        actualizarHitboxes();
    }

    @Override
    public void update(float delta) {
        actualizarHitboxes();
        stateTime += delta;
    }

    @Override
    public abstract void render(Batch batch, float delta);

    protected abstract void onPickup(Entity player);

    public float getPickupRadius() {
        return pickupRadius;
    }
}
