package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;

public abstract class Weapon {
    protected float cd = 1f;
    protected float lastShootTime = 0;
    protected float damage = 10f;
    protected float bulletSpeed = 10f;
    protected float bulletSize = 0.2f;
    protected float shootRange = 10f;

    protected Vector2 recoilOffset = new Vector2(0, 0);
    protected float recoilForce = 0.4f;
    protected float recoilRecovery = 8f;

    protected Entity objetive;
    protected Vector2 worldPosition = new Vector2();
    protected Entity owner;
    protected TextureRegion sprite;
    protected TextureRegion projectileTexture;
    protected ProjectileCreator projectileFactory;
    protected EffectManager effectManager;
    protected float visualAngle;

    public interface ProjectileCreator {
        Projectile create(Vector2 pos, Vector2 dir, float speed, float dmg, float size,
                          TextureRegion tex, EffectManager em, EffectType trailType, float trailInterval);
    }

    public Weapon(Entity owner, ProjectileCreator factory, TextureRegion bulletTex, EffectManager effectManager) {
        this.owner = owner;
        this.projectileFactory = factory;
        this.projectileTexture = bulletTex;
        this.effectManager = effectManager;
    }

    // --- SETTERS MANTENIDOS PARA FLEXIBILIDAD ---

    public void setDamage(float damage) { this.damage = damage; }
    public void setCooldown(float cd) { this.cd = cd; }
    public void setOwner(Entity owner) { this.owner = owner; }
    public void setBulletSpeed(float speed) { this.bulletSpeed = speed; }

    // ------------------------------------------

    public void update(float delta, Array<Entity> enemies) {
        // Se ha eliminado la línea que forzaba worldPosition a owner.getPosicion()
        // Ahora la posición la gestiona el WeaponManager externamente con setPosition()

        searchEnemy(enemies);
        tryShoot(delta);
        recoilOffset.lerp(Vector2.Zero, recoilRecovery * delta);
        updateVisual();
    }

    private void updateVisual() {
        if (objetive != null && objetive.isAlive()) {
            Vector2 dir = new Vector2(objetive.getPosicion().x - worldPosition.x, objetive.getPosicion().y - worldPosition.y);
            visualAngle = dir.angleDeg();
        }
    }

    public void setPosition(float x, float y) { worldPosition.set(x, y); }

    private void searchEnemy(Array<Entity> enemies) {
        if (objetive != null && (!objetive.isAlive() || worldPosition.dst2(objetive.getPosicion()) > shootRange * shootRange)) {
            objetive = null;
        }
        if (objetive != null) return;

        Entity closest = null;
        float minDistanceSq = Float.MAX_VALUE;
        for (Entity e : enemies) {
            if (!e.isAlive()) continue;
            float distanceSq = worldPosition.dst2(e.getPosicion());
            if (distanceSq < minDistanceSq && distanceSq <= shootRange * shootRange) {
                minDistanceSq = distanceSq;
                closest = e;
            }
        }
        objetive = closest;
    }

    public void tryShoot() {
        if (lastShootTime >= cd) {
            shoot();
            lastShootTime = 0;
        }
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

    protected void applyRecoil(float customForce, float customRecovery) {
        if (objetive == null) return;
        this.recoilRecovery = customRecovery;
        Vector2 dir = new Vector2(objetive.getPosicion()).sub(worldPosition).nor();
        recoilOffset.set(dir).scl(-customForce);
    }

    public void render(Batch batch) {
        if (sprite == null) return;
        float width = 1.2f; float height = 1.2f;
        float originX = width / 2f; float originY = height / 2f;
        float scaleY = (visualAngle > 90 && visualAngle < 270) ? -1f : 1f;
        batch.draw(sprite, (worldPosition.x + recoilOffset.x) - originX, (worldPosition.y + recoilOffset.y) - originY, originX, originY, width, height, 1f, scaleY, visualAngle);
    }

    public Vector2 getWorldPosition() { return worldPosition; }
    public Entity getObjetive() { return objetive; }
    public Entity getOwner() { return owner; }
}
