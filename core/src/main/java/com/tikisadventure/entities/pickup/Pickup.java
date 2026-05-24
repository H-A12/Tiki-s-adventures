package com.tikisadventure.entities.pickup;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.tikisadventure.components.HealthComponent;
import com.tikisadventure.components.RenderComponent;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

//Objeto recogible del suelo. Se atrae al jugador si está cerca.
//Implementa Poolable para reciclarse con Pool.
public abstract class Pickup extends Entity implements Poolable {

    protected float pickupRadius = 0.8f;

    protected float bobTimer = 0f;
    protected float bobOffset = 0f;

    private boolean isBeingAttracted = false;
    private float attractSpeed = 5.0f;

    public Pickup() {
        super();
        this.healthComponent = new HealthComponent(1);
        this.renderComponent = new RenderComponent(null, 0.5f, 0.5f);
        setANCHO(0.5f);
        setALTO(0.5f);
    }

    //Inicializar posición y resetear flags al reaparecer
    public void init(Vector2 position) {
        this.positionComponent.posicion.set(position);
        setAlive(true); // Lo revivimos
        this.healthComponent.currentHealth = 1;
        this.isBeingAttracted = false;
        this.attractSpeed = 5.0f;
        this.bobTimer = 0f;
        this.bobOffset = 0f;
    }

    @Override
    public void die() {
        setAlive(false);
    }

    @Override
    //Aplicar flotación, atracción al jugador y detectar recogida
    public void update(float delta, Entity player) {
        if (!isAlive() || player == null) return;

        bobTimer += delta;
        bobOffset = (float)Math.sin(bobTimer * 4f) * 0.04f;

        if (player instanceof Player) {
            Player p = (Player) player;
            float distanceToPlayer = positionComponent.posicion.dst(p.getPosition());
            if (distanceToPlayer <= p.getAttractionRange()) {
                isBeingAttracted = true;
            }
            if (isBeingAttracted) {
                Vector2 direction = new Vector2(p.getPosition()).sub(positionComponent.posicion).nor();
                positionComponent.posicion.mulAdd(direction, attractSpeed * delta);
                attractSpeed += 15.0f * delta;
            }
        }

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
    //Resetear flags al devolver al Pool
    public void reset() {
        setAlive(false);
        this.positionComponent.posicion.setZero();
        this.isBeingAttracted = false;
        this.attractSpeed = 5.0f;
        this.bobTimer = 0f;
        this.bobOffset = 0f;
    }
}
