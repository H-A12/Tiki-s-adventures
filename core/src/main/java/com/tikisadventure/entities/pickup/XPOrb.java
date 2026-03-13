package com.tikisadventure.entities.pickup;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.entities.Entity;

public class XPOrb extends Entity {

    private int value;

    private float speed = 6f;

    private float magnetRadius = 3f;
    private float pickupRadius = 0.4f;

    private Vector2 tmp = new Vector2();

    public XPOrb(Vector2 pos, int value){
        this.posicion.set(pos);
        this.value = value;
    }

    @Override
    public void update(float delta, Entity player){

        float dist = posicion.dst(player.getPosicion());

        if(dist < magnetRadius){

            tmp.set(
                player.getPosicion().x - posicion.x,
                player.getPosicion().y - posicion.y
            ).nor();

            posicion.mulAdd(tmp, speed * delta);
        }

        if(dist < pickupRadius){
            alive = false;
        }
    }

    @Override
    public void render(Batch batch, float delta) {

    }

    public int getValue() {
        return this.value;
    }
}
