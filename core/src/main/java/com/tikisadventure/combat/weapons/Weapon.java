package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.combat.weapons.behaviors.AttackBehavior;
import com.tikisadventure.effects.EffectManager;

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
    protected AttackBehavior attackBehavior;
    protected EffectManager effectManager;
    protected float visualAngle;
    protected float pivotX = 0.5f;
    protected float pivotY = 0.5f;
    protected Vector2 swingOffset = new Vector2();
    protected float swingRotation = 0f;

    public Weapon(Entity owner, AttackBehavior behavior, EffectManager effectManager) {
        this.owner = owner;
        this.attackBehavior = behavior;
        this.effectManager = effectManager;
    }

    public void setPivot(float x, float y) { this.pivotX = x; this.pivotY = y; }
    public void setSwingOffset(float x, float y) { this.swingOffset.set(x, y); }
    public void setSwingRotation(float rotation) { this.swingRotation = rotation; }

    public void setDamage(float damage) { this.damage = damage; }
    public void setCooldown(float cd) { this.cd = cd; }
    public void setOwner(Entity owner) { this.owner = owner; }
    public void setBulletSpeed(float speed) { this.bulletSpeed = speed; }

    public void update(float delta, Array<Entity> enemies) {
        searchEnemy(enemies);
        tryAttack(delta);
        recoilOffset.lerp(Vector2.Zero, recoilRecovery * delta);
        updateVisual();
        if (attackBehavior != null) attackBehavior.update(delta);
    }

    private void updateVisual() {
        if (objetive != null && objetive.isAlive()) {
            Vector2 dir = new Vector2(objetive.getPosicion().x - worldPosition.x, objetive.getPosicion().y - worldPosition.y);
            visualAngle = dir.angleDeg();
        }
    }

    public void setPosition(float x, float y) { worldPosition.set(x, y); }
    public float getVisualAngle() { return visualAngle; }

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

    private void tryAttack(float delta) {
        lastShootTime += delta;
        if (objetive == null || !objetive.isAlive()) return;
        if (lastShootTime >= cd) {
            if (attackBehavior != null) {
                attackBehavior.execute(owner, objetive, worldPosition, effectManager);
            }
            lastShootTime = 0;
        }
    }

    public void applyRecoil(float customForce, float customRecovery) {
        if (objetive == null) return;
        this.recoilRecovery = customRecovery;
        Vector2 dir = new Vector2(objetive.getPosicion()).sub(worldPosition).nor();
        recoilOffset.set(dir).scl(-customForce);
    }

    public void render(Batch batch) {
        if (sprite == null) return;
        float width = 1.2f; float height = 1.2f;
        float originX = pivotX * width; float originY = pivotY * height;
        float scaleY = (visualAngle + swingRotation > 90 && visualAngle + swingRotation < 270) ? -1f : 1f;
        
        batch.draw(sprite, 
            (worldPosition.x + recoilOffset.x + swingOffset.x) - originX, 
            (worldPosition.y + recoilOffset.y + swingOffset.y) - originY, 
            originX, originY, width, height, 1f, scaleY, visualAngle + swingRotation);
    }

    public Vector2 getWorldPosition() { return worldPosition; }
    public Entity getObjetive() { return objetive; }
    public Entity getOwner() { return owner; }
}
