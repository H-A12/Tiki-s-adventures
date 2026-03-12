package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;

public abstract class Weapon {

    // --- Stats del arma ---
    protected float cd;
    private float lastShootTime = 0;

    protected float damage;
    protected float bulletSpeed;
    protected float bulletSize;
    protected float shootRange;

    // --- Target ---
    protected Entity objetive;

    // --- Posición ---
    protected Vector2 worldPosition = new Vector2();

    protected Entity owner;
    protected TextureRegion sprite;

    // --- Rotación visual ---
    protected float visualAngle;

    public Weapon(Entity owner){
        this.owner = owner;
    }

    public void update(float delta, Array<Entity> enemies){

        searchEnemy(enemies);

        tryShoot(delta);

        updateVisual(delta);
    }

    private void updateVisual(float delta){
        if(objetive != null){

            Vector2 dir = new Vector2(
                objetive.getPosicion().x - worldPosition.x,
                objetive.getPosicion().y - worldPosition.y
            );

            visualAngle = dir.angleDeg();
        }
    }

    // WeaponManager controla la posición
    public void setPosition(float x, float y){
        worldPosition.set(x, y);
    }

    private void searchEnemy(Array<Entity> enemies){

        // Si el objetivo sigue vivo no buscamos otro
        if(objetive != null && objetive.isAlive()) return;

        Entity closest = null;
        float minDistance = Float.MAX_VALUE;

        for(Entity e : enemies){

            if(!e.isAlive()) continue;

            float dx = e.getPosicion().x - worldPosition.x;
            float dy = e.getPosicion().y - worldPosition.y;

            float distance = dx*dx + dy*dy;

            if(distance < minDistance && distance <= shootRange * shootRange){
                minDistance = distance;
                closest = e;
            }
        }

        objetive = closest;
    }

    private void tryShoot(float delta){

        lastShootTime += delta;

        if(objetive == null || !objetive.isAlive()) return;

        if(lastShootTime >= cd){
            shoot();
            lastShootTime = 0;
        }
    }

    protected abstract void shoot();

    public void render(Batch batch){

        if(sprite == null) return;

        float width = sprite.getRegionWidth() / 16f;
        float height = sprite.getRegionHeight() / 16f;

        float originX = width / 2f;
        float originY = height / 2f;

        float scaleX = 1f;

        if(visualAngle > 90 && visualAngle < 270){
            scaleX = -1f;
        }

        batch.draw(
            sprite,
            worldPosition.x - originX,
            worldPosition.y - originY,
            originX,
            originY,
            width,
            height,
            scaleX,
            1f,
            visualAngle
        );
    }

    public Vector2 getWorldPosition(){
        return worldPosition;
    }

    public Entity getObjetive(){
        return objetive;
    }

    public float getDamage(){
        return damage;
    }

    public float getBulletSpeed(){
        return bulletSpeed;
    }

    public float getBulletSize(){
        return bulletSize;
    }

    public Entity getOwner(){
        return owner;
    }
}
