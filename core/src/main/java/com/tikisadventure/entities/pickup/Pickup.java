package com.tikisadventure.entities.pickup;

import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.entities.Entity;

public abstract class Pickup extends Entity {

    protected float pickupRadius = 1.5f;

    public Pickup(Vector2 position){
        this.posicion.set(position);
        this.vida = 1;
        setAlive();
    }

    @Override
    public void update(float delta, Entity player){

        float pickupCenterX = posicion.x;
        float pickupCenterY = posicion.y;

        float playerCenterX = player.getHitboxActionTrigger().x;
        float playerCenterY = player.getHitboxActionTrigger().y;

        float dx = pickupCenterX - playerCenterX;
        float dy = pickupCenterY - playerCenterY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if(dist < pickupRadius + player.getHitboxActionTrigger().radius){

            onPickup(player);
            alive = false;
        }
    }

    protected abstract void onPickup(Entity player);

    public float getPickupRadius(){
        return pickupRadius;
    }
}
