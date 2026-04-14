package com.tikisadventure.entities.pickup;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.components.HealthComponent;
import com.tikisadventure.components.RenderComponent;
import com.tikisadventure.entities.base.Entity;

public abstract class Pickup extends Entity {

    protected float pickupRadius = 0.8f; // Radio de recolección

    public Pickup(Vector2 position) {
        super();
        this.positionComponent.posicion.set(position);
        this.healthComponent = new HealthComponent(1);
        this.renderComponent = new RenderComponent(null, 0.5f, 0.5f);

        setANCHO(0.5f);
        setALTO(0.5f);
    }


    @Override
    public void update(float delta, Entity player) {
        if (!isAlive() || player == null) return;

        // Usamos dst() de LibGDX que es más limpio que Math.sqrt
        // Comparamos la distancia entre el centro del pickup y el centro de la hitbox del jugador
        float dist = positionComponent.posicion.dst(player.getHitboxActionTrigger().x, player.getHitboxActionTrigger().y);

        if (dist < pickupRadius + player.getHitboxActionTrigger().radius) {
            onPickup(player);
            die();
        }

        actualizarHitboxes();
    }

    @Override
    public abstract void draw(Batch batch, float delta);

    protected abstract void onPickup(Entity player);

    public float getPickupRadius() {
        return pickupRadius;
    }
}
