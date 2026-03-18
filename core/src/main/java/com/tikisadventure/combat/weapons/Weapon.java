package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;

public abstract class Weapon {

    // --- Stats del arma ---
    protected float cd;
    protected float lastShootTime = 0; // Cambiado a protected para resetearlo en ráfagas

    protected float damage;
    protected float bulletSpeed;
    protected float bulletSize;
    protected float shootRange;
    protected float accuracy = 0; // <--- NUEVO: 0 es precisión perfecta

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

    protected void updateVisual(float delta){
        if(objetive != null){
            Vector2 dir = new Vector2(
                objetive.getPosicion().x - worldPosition.x,
                objetive.getPosicion().y - worldPosition.y
            );
            visualAngle = dir.angleDeg();
        }
    }

    // Método útil para que cualquier arma calcule la dirección con el error de puntería
    protected Vector2 getDirectionWithSpread() {
        Vector2 dir = new Vector2(
            objetive.getPosicion().x - worldPosition.x,
            objetive.getPosicion().y - worldPosition.y
        ).nor();

        if (accuracy > 0) {
            float randomOffset = (float)(Math.random() * accuracy * 2) - accuracy;
            dir.setAngleDeg(dir.angleDeg() + randomOffset);
        }
        return dir;
    }

    public void setPosition(float x, float y){
        worldPosition.set(x, y);
    }

    // Cambiado a protected para que BurstGun pueda reutilizarlo en su update
    protected void searchEnemy(Array<Entity> enemies){
        if(objetive != null && objetive.isAlive()) return;

        Entity closest = null;
        float minDistance = Float.MAX_VALUE;

        for(Entity e : enemies){
            if(!e.isAlive()) continue;

            float distance = worldPosition.dst2(e.getPosicion()); // dst2 es más rápido que dx*dx+dy*dy

            if(distance < minDistance && distance <= shootRange * shootRange){
                minDistance = distance;
                closest = e;
            }
        }
        objetive = closest;
    }

    // Cambiado a protected para dar control a las subclases
    protected void tryShoot(float delta){
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
        // Corregido: Si el ángulo está entre 90 y 270, el arma está "boca abajo", la giramos
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
            1f,     // ScaleX siempre 1
            scaleX, // Usamos scaleY para el flip vertical si scaleX diera problemas
            visualAngle
        );
    }

    // Getters
    public Vector2 getWorldPosition() { return worldPosition; }
    public Entity getObjetive() { return objetive; }
    public float getDamage() { return damage; }
    public float getBulletSpeed() { return bulletSpeed; }
    public float getBulletSize() { return bulletSize; }
    public Entity getOwner() { return owner; }
}
