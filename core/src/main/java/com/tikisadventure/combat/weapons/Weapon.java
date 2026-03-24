package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;

public abstract class Weapon {

    // --- Stats del arma ---
    protected float cd; // Cooldown
    protected float lastShootTime = 0;

    protected float damage;
    protected float bulletSpeed;
    protected float bulletSize;
    protected float shootRange = 10f; // Rango por defecto

    // --- Target ---
    protected Entity objetive;

    // --- Posición y Referencias ---
    protected Vector2 worldPosition = new Vector2();
    protected Entity owner;
    protected TextureRegion sprite;
    protected BulletCreator bulletFactory;

    // --- Rotación visual ---
    protected float visualAngle;

    // Interfaz para crear balas dinámicamente
    public interface BulletCreator {
        Bullet create(Vector2 pos, Vector2 dir, float speed, float dmg, float size);
    }

    // ÚNICO CONSTRUCTOR: Unificado para evitar errores de compilación
    public Weapon(Entity owner, BulletCreator factory) {
        this.owner = owner;
        this.bulletFactory = factory;
    }

    public void update(float delta, Array<Entity> enemies) {
        searchEnemy(enemies);
        tryShoot(delta);
        updateVisual();
    }

    private void updateVisual() {
        if (objetive != null && objetive.isAlive()) {
            // Calculamos el ángulo hacia el objetivo
            Vector2 dir = new Vector2(
                objetive.getPosicion().x - worldPosition.x,
                objetive.getPosicion().y - worldPosition.y
            );
            visualAngle = dir.angleDeg();
        }
    }

    // El WeaponManager usa este método para mover el arma
    public void setPosition(float x, float y) {
        worldPosition.set(x, y);
    }

    // MÉTODO CLAVE: Este es el que buscaba BasicGun
    public Entity findNearestEnemy(Array<Entity> enemies) {
        searchEnemy(enemies);
        return objetive;
    }

    private void searchEnemy(Array<Entity> enemies) {
        // Si el objetivo ya no es válido, buscamos uno nuevo
        if (objetive != null && (!objetive.isAlive() || worldPosition.dst2(objetive.getPosicion()) > shootRange * shootRange)) {
            objetive = null;
        }

        if (objetive != null) return;

        Entity closest = null;
        float minDistance = Float.MAX_VALUE;

        for (Entity e : enemies) {
            if (!e.isAlive()) continue;

            float distanceSq = worldPosition.dst2(e.getPosicion());

            if (distanceSq < minDistance && distanceSq <= shootRange * shootRange) {
                minDistance = distanceSq;
                closest = e;
            }
        }
        objetive = closest;
    }

    private void tryShoot(float delta) {
        lastShootTime += delta;

        if (objetive == null || !objetive.isAlive()) return;

        if (lastShootTime >= cd) {
            shoot();
            lastShootTime = 0;
        }
    }

    // Las clases hijas (BasicGun, etc.) deben implementar esto
    protected abstract void shoot();

    public void render(Batch batch) {
        if (sprite == null) return;

        // Ajuste de tamaño basado en 16 pixeles por metro
        float width = sprite.getRegionWidth() / 16f;
        float height = sprite.getRegionHeight() / 16f;

        float originX = width / 2f;
        float originY = height / 2f;

        // Lógica para que el arma no se vea de cabeza cuando apunta a la izquierda
        float scaleY = 1f;
        if (visualAngle > 90 && visualAngle < 270) {
            scaleY = -1f;
        }

        batch.draw(
            sprite,
            worldPosition.x - originX,
            worldPosition.y - originY,
            originX,
            originY,
            width,
            height,
            1f,     // scaleX
            scaleY, // scaleY (flip vertical si mira a la izquierda)
            visualAngle
        );
    }

    // Getters necesarios
    public Vector2 getWorldPosition() { return worldPosition; }
    public Entity getObjetive() { return objetive; }
    public float getDamage() { return damage; }
    public float getBulletSpeed() { return bulletSpeed; }
    public float getBulletSize() { return bulletSize; }
}
