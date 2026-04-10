package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.weapons.Emitter;
import com.tikisadventure.combat.weapons.ProjectileModifier;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.systems.events.EventBus;
import com.tikisadventure.systems.events.FiredEvent;

public class Weapon {
    // Stats
    protected int price = 0;
    protected int tier = 1;
    protected Array<String> categories = new Array<>();
    protected float critChance = 0.05f;
    protected float critDamageMult = 1.5f;

    protected float cd = 1f;
    protected float lastShootTime = 0;
    protected float damage = 10f;
    protected float bulletSpeed = 10f;
    protected float bulletSize = 0.2f;
    protected float shootRange = 10f;
    protected float recoilForce = 0.0f;
    protected float recoilRecovery = 0.0f;
    protected float projectileLifetime = 5.0f;
    protected float spread = 0f;
    protected float imprecision = 0f;
    protected int burstCount = 1;
    protected float burstInterval = 0f;
    protected int projectileCount = 1;
    protected Vector2 spawnOffset = new Vector2(0, 0);
    protected Vector2 muzzleFlashOffset = new Vector2(0, 0);
    protected TextureRegion projectileTexture;
    protected ProjectileCreator projectileCreator;
    protected EffectType trailType;
    protected float trailInterval = 0f;
    protected EffectType muzzleFlashType;
    protected Array<Emitter> emitters = new Array<>();
    protected Array<ProjectileModifier> modifiers = new Array<>();

    // State
    protected int bulletsShotInCurrentBurst = 0;
    protected float burstTimer = 0;
    protected boolean isBursting = false;
    protected Vector2 burstDirection = new Vector2();
    protected Vector2 recoilOffset = new Vector2(0, 0);
    protected Entity objetive;
    protected Vector2 worldPosition = new Vector2();
    protected Entity owner;
    protected TextureRegion sprite;
    protected EffectManager effectManager;
    protected float visualAngle;
    protected float pivotX = 0.5f;
    protected float pivotY = 0.5f;
    protected Vector2 swingOffset = new Vector2();
    protected float swingRotation = 0f;

    public Weapon(Entity owner, ProjectileCreator pc, EffectManager effectManager) {
        this.owner = owner;
        this.projectileCreator = pc;
        this.effectManager = effectManager;
    }

    // Setters
    public void setPrice(int price) { this.price = price; }
    public void setTier(int tier) { this.tier = tier; }
    public void addCategory(String category) { this.categories.add(category); }
    public void setCritChance(float critChance) { this.critChance = critChance; }
    public void setCritDamageMult(float critDamageMult) { this.critDamageMult = critDamageMult; }
    public void setDamage(float damage) { this.damage = damage; }
    public void setCooldown(float cd) { this.cd = cd; }
    public void setShootRange(float range) { this.shootRange = range; }
    public void setProjectileTexture(TextureRegion texture) { this.projectileTexture = texture; }
    public void setBulletSpeed(float speed) { this.bulletSpeed = speed; }
    public void setBulletSize(float size) { this.bulletSize = size; }
    public void setRecoil(float force, float recovery) { this.recoilForce = force; this.recoilRecovery = recovery; }
    public void setBurst(int count, float interval) { this.burstCount = count; this.burstInterval = interval; }
    public void setProjectileCount(int count) { this.projectileCount = count; }
    public void setSpread(float spread) { this.spread = spread; }
    public void setImprecision(float imprecision) { this.imprecision = imprecision; }
    public void setSpawnOffset(Vector2 offset) { this.spawnOffset.set(offset); }
    public void setMuzzleFlashOffset(Vector2 offset) { this.muzzleFlashOffset.set(offset); }
    public void setMuzzleFlashType(EffectType type) { this.muzzleFlashType = type; }
    public void setTrail(EffectType type, float interval) { this.trailType = type; this.trailInterval = interval; }
    public void setProjectileLifetime(float lifetime) { this.projectileLifetime = lifetime; }
    public void addEmitter(Emitter e) { this.emitters.add(e); }
    public void addModifier(ProjectileModifier m) { this.modifiers.add(m); }
    public void setSprite(TextureRegion sprite) { this.sprite = sprite; }

    // Logic
    public void update(float delta, Array<Entity> enemies) {
        searchEnemy(enemies);
        tryAttack(delta);
        recoilOffset.lerp(Vector2.Zero, recoilRecovery * delta);
        updateVisual();
        
        if (isBursting) {
            burstTimer += delta;
            if (burstTimer >= burstInterval) {
                fireShot(objetive != null ? new Vector2(objetive.getPosicion()).sub(worldPosition).nor() : burstDirection);
                burstTimer = 0;
                bulletsShotInCurrentBurst++;
                if (bulletsShotInCurrentBurst >= burstCount) {
                    isBursting = false;
                }
            }
        }
    }

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
            if (burstCount > 1) {
                if (!isBursting) {
                    isBursting = true;
                    bulletsShotInCurrentBurst = 0;
                    burstTimer = burstInterval;
                    burstDirection.set(objetive.getPosicion()).sub(worldPosition).nor();
                }
            } else {
                fireShot(new Vector2(objetive.getPosicion()).sub(worldPosition).nor());
            }
            lastShootTime = 0;
        }
    }

    private void fireShot(Vector2 baseDir) {
        if (recoilForce > 0) {
            applyRecoil(recoilForce, recoilRecovery);
        }
        
        float baseAngle = baseDir.angleDeg();
        float weaponAngle = visualAngle;
        
        Vector2 muzzleFlashPos = new Vector2(worldPosition);
        if (muzzleFlashType != null) {
            boolean isFlipped = weaponAngle > 90 && weaponAngle < 270;
            float offsetAngle = isFlipped ? weaponAngle - 180 : weaponAngle;
            
            Vector2 muzzleOffset = new Vector2(muzzleFlashOffset);
            if (isFlipped) {
                muzzleOffset.x = -muzzleOffset.x;
            }
            Vector2 rotatedMuzzleOffset = muzzleOffset.rotateDeg(offsetAngle);
            muzzleFlashPos = new Vector2(worldPosition).add(rotatedMuzzleOffset);
        }
        
        for (Emitter emitter : emitters) {
            boolean isFlipped = weaponAngle > 90 && weaponAngle < 270;
            float offsetAngle = isFlipped ? weaponAngle - 180 : weaponAngle;
            Vector2 emitterOffset = new Vector2(emitter.offset);
            if (isFlipped) {
                emitterOffset.x = -emitterOffset.x;
            }
            Vector2 rotatedOffset = emitterOffset.rotateDeg(offsetAngle);
            Vector2 emitterPos = new Vector2(worldPosition).add(rotatedOffset);
            Vector2 ejectionDir = new Vector2(1, 0).setAngleDeg(offsetAngle);
            EventBus.publish(new FiredEvent(emitterPos, ejectionDir, emitter.type, null));
        }
        
        if (muzzleFlashType != null) {
            EventBus.publish(new FiredEvent(muzzleFlashPos, baseDir, null, muzzleFlashType));
        }
        
        boolean isFlipped = weaponAngle > 90 && weaponAngle < 270;
        float spawnOffsetAngle = isFlipped ? weaponAngle - 180 : weaponAngle;
        Vector2 spawnOffsetVec = new Vector2(spawnOffset);
        if (isFlipped) {
            spawnOffsetVec.x = -spawnOffsetVec.x;
        }
        Vector2 rotatedSpawnOffset = spawnOffsetVec.rotateDeg(spawnOffsetAngle);

        for (int i = 0; i < projectileCount; i++) {
            float angle = baseAngle;
            if (projectileCount > 1) {
                angle += MathUtils.random(-spread / 2f, spread / 2f);
            }
            if (imprecision > 0) {
                angle += MathUtils.random(-imprecision, imprecision);
            }
            Vector2 dir = new Vector2(1, 0).setAngleDeg(angle);

            Projectile p = projectileCreator.create(
                new Vector2(worldPosition).add(rotatedSpawnOffset),
                dir, bulletSpeed, damage, bulletSize,
                projectileTexture, effectManager, trailType, trailInterval,
                projectileLifetime
            );
            
            for (ProjectileModifier modifier : modifiers) {
                modifier.apply(p, effectManager);
            }

            if (owner instanceof Player) {
                ((Player) owner).addProjectile(p);
            }
        }
    }

    private void updateVisual() {
        if (objetive != null && objetive.isAlive()) {
            Vector2 dir = new Vector2(objetive.getPosicion().x - worldPosition.x, objetive.getPosicion().y - worldPosition.y);
            visualAngle = dir.angleDeg();
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

    public void setPosition(float x, float y) { worldPosition.set(x, y); }
    public float getVisualAngle() { return visualAngle; }
}