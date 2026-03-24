package com.tikisadventure.entities.pickup;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.entities.Entity;

public abstract class Pickup extends Entity {

    protected float pickupRadius = 0.8f; // Radio de recolección

    public Pickup(Vector2 position) {
        super(); // Inicializa hitboxes de Entity
        this.posicion.set(position);
        this.vida = 1;
        this.alive = true;

        // Dimensiones por defecto para los drops
        this.ANCHO = 0.5f;
        this.ALTO = 0.5f;
    }

    @Override
    public void update(float delta, Entity player) {
        if (!alive || player == null) return;

        // Usamos dst() de LibGDX que es más limpio que Math.sqrt
        // Comparamos la distancia entre el centro del pickup y el centro de la hitbox del jugador
        float dist = posicion.dst(player.getHitboxActionTrigger().x, player.getHitboxActionTrigger().y);

        if (dist < pickupRadius + player.getHitboxActionTrigger().radius) {
            onPickup(player);
            this.alive = false;
        }

        actualizarHitboxes();
    }

    /**
     * MÉTODO OBLIGATORIO: Como Entity tiene render(Batch, float) abstracto,
     * Pickup (que es hijo de Entity) debe declararlo o implementarlo.
     * Aquí lo declaramos como abstracto para que XPOrb o MiniHeal lo definan.
     */
    @Override
    public abstract void render(Batch batch, float delta);

    protected abstract void onPickup(Entity player);

    public float getPickupRadius() {
        return pickupRadius;
    }
}
