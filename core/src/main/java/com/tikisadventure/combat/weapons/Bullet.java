package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;

public class Bullet {

    private Vector2 position;
    private Vector2 direction;

    private float speed;
    private float damage;
    private float radius;

    private boolean penetrate;
    private boolean alive = true;

    public Bullet(Vector2 startPos, Vector2 dir, float speed, float damage, float radius, boolean penetrate) {

        this.position = new Vector2(startPos);
        this.direction = new Vector2(dir).nor();

        this.speed = speed;
        this.damage = damage;
        this.radius = radius;
        this.penetrate = penetrate;
    }

    public void update(float delta, Array<Entity> enemies){

        if(!alive) return;

        position.mulAdd(direction, speed * delta);

        for(Entity e : enemies){

            if(!e.isAlive()) continue;

            float dist2 = position.dst2(e.getPosicion());

            float hitRadius = radius + e.getHitboxActionTrigger().radius;

            if(dist2 <= hitRadius * hitRadius){

                e.receiveDamage(damage);

                if(!penetrate){
                    alive = false; // bala se destruye
                    break;
                }
            }
        }
    }

    public boolean isAlive(){
        return alive;
    }

    public Vector2 getPosition(){
        return position;
    }

    public float getRadius(){
        return radius;
    }
    public boolean getPenetration(){
        return penetrate;
    }

    public Vector2 getDirection() {return direction;}

}
