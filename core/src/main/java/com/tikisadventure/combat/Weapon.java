package com.tikisadventure.combat;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.projectile.Projectile;
import com.tikisadventure.effects.EffectManager; // <--- AÑADIDO

public abstract class Weapon {

    // --- Stats del arma ---
    protected float cd;
    protected float lastShootTime = 0;
    protected float damage;
    protected float bulletSpeed;
    protected float bulletSize;
    protected float shootRange = 10f;

    // --- Sistema de Recoil (Retroceso) ---
    protected Vector2 recoilOffset = new Vector2(0, 0);
    protected float recoilForce = 0.4f;
    protected float recoilRecovery = 8f;

    // --- Target ---
    protected Entity objetive;

    // --- Posición y Referencias ---
    protected Vector2 worldPosition = new Vector2();
    protected Entity owner;
    protected TextureRegion sprite;
    protected TextureRegion projectileTexture;
    protected ProjectileCreator projectileFactory;

    // --- NUEVO: Referencia para los efectos ---
    protected EffectManager effectManager;

    protected float visualAngle;

    public interface ProjectileCreator {
        Projectile create(Vector2 pos, Vector2 dir, float speed, float dmg, float size, TextureRegion tex);
    }

    // Constructor actualizado para recibir el manager
    public Weapon(Entity owner, ProjectileCreator factory, TextureRegion bulletTex, EffectManager effectManager) {
        this.owner = owner;
        this.projectileFactory = factory;
        this.projectileTexture = bulletTex;
        this.effectManager = effectManager; // <--- ASIGNADO
    }

    public void update(float delta, Array<Entity> enemies) {
        searchEnemy(enemies);
        tryShoot(delta);

        // Tu lógica original de recoil con .lerp
        recoilOffset.lerp(Vector2.Zero, recoilRecovery * delta);

        updateVisual();
    }

    private void updateVisual() {
        if (objetive != null && objetive.isAlive()) {
            Vector2 dir = new Vector2(
                objetive.getPosicion().x - worldPosition.x,
                objetive.getPosicion().y - worldPosition.y
            );
            visualAngle = dir.angleDeg();
        }
    }

    public void setPosition(float x, float y) {
        worldPosition.set(x, y);
    }

    private void searchEnemy(Array<Entity> enemies) {
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

    protected abstract void shoot();

    // Mantenemos tu applyRecoil original con .set()
    protected void applyRecoil(float customForce, float customRecovery) {
        if (objetive == null) return;
        this.recoilRecovery = customRecovery;
        Vector2 dir = new Vector2(objetive.getPosicion()).sub(worldPosition).nor();
        recoilOffset.set(dir).scl(-customForce);
    }

    public void render(Batch batch) {
        if (sprite == null) return;

        float width = 1.2f;
        float height = 1.2f;
        float originX = width / 2f;
        float originY = height / 2f;

        float scaleY = 1f;
        if (visualAngle > 90 && visualAngle < 270) {
            scaleY = -1f;
        }

        batch.draw(
            sprite,
            (worldPosition.x + recoilOffset.x) - originX,
            (worldPosition.y + recoilOffset.y) - originY,
            originX,
            originY,
            width,
            height,
            1f,
            scaleY,
            visualAngle
        );
    }

    public Vector2 getWorldPosition() { return worldPosition; }
    public Entity getObjetive() { return objetive; }
}
