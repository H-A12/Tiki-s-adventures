package com.tikisadventure.entities.pickup;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.tikisadventure.components.HealthComponent;
import com.tikisadventure.components.RenderComponent;
import com.tikisadventure.entities.base.Entity;

public abstract class Pickup extends Entity implements Poolable {

    protected float pickupRadius = 0.8f;

    //El constructor crea solo carcasa vacia 1 vez
    public Pickup() {
        super();
        this.healthComponent = new HealthComponent(1);
        this.renderComponent = new RenderComponent(null, 0.5f, 0.5f);
        setANCHO(0.5f);
        setALTO(0.5f);
    }

    //Dar valores al salir a la pool
    public void init(Vector2 position) {
        this.positionComponent.posicion.set(position);
        setAlive(true); // Lo revivimos
        this.healthComponent.currentHealth = 1;
    }

    //Sobrescribimos die, no borra componentes internos
    @Override
    public void die() {
        setAlive(false);
    }

    @Override
    public void update(float delta, Entity player) {
        if (!isAlive() || player == null) return;

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

    @Override
    public void reset() {
        setAlive(false);
        this.positionComponent.posicion.setZero();
    }
}
