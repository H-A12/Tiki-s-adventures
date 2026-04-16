package com.tikisadventure.combat.projectiles;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.components.traits.DamageDealer;
import com.tikisadventure.components.traits.Orientable;
import com.tikisadventure.components.traits.Ownable;
import com.tikisadventure.components.traits.PositionProvider;
import com.tikisadventure.components.traits.RadiusProvider;
import com.tikisadventure.components.traits.SpeedProvider;
import com.tikisadventure.components.traits.Killable;
import com.tikisadventure.components.traits.Sensorable;
import com.tikisadventure.components.traits.Timed;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

import java.util.HashMap;
import java.util.Map;

public class Projectile implements PositionProvider, Orientable, SpeedProvider, DamageDealer,
    RadiusProvider, Ownable, Timed, Killable, Sensorable, Poolable {

    private Vector2 position = new Vector2();
    private Vector2 direction = new Vector2();
    private float speed;
    private float damage;
    private float critChance;
    private float critDamageMult;
    private DamageType damageType = DamageType.KINETIC;
    private float baseRadius;
    private float currentRadius;
    private boolean sensorMode = false;
    private float stateTime = 0;
    private boolean alive = true;
    private Entity owner;
    private TextureRegion sprite;
    private EffectManager effectManager;
    private String trailType;
    private float trailSpacing;
    private Vector2 lastTrailPos = new Vector2();
    private float trailAccumulator = 0f;
    
    private int penetrationCount = 0;
    private float impactKnockback = 0f;
    
    private boolean lastCritResult = false;
    private float lastDamageResult = 0f;

    private float maxLifetime = 5f;
    private float growthRate = 0f;
    private float maxRadius = Float.MAX_VALUE;
    private float rotationSpeed = 0f;
    private float baseDirectionAngle = 0f;

    private Map<Entity, Float> lastHitTimes = new HashMap<>();
    
    private Array<Component> components = new Array<>();

    public Projectile(Entity owner, Vector2 pos, Vector2 dir, float speed, float dmg, float critChance, float critDamageMult, float radius,
                      TextureRegion sprite, EffectManager em, String trailType, float trailSpacing) {
        this.owner = owner;
        this.position.set(pos);
        this.lastTrailPos.set(pos);
        this.trailAccumulator = 0f;
        this.direction.set(dir).nor();
        this.speed = speed;
        this.damage = dmg;
        this.critChance = critChance;
        this.critDamageMult = critDamageMult;
        this.baseRadius = radius;
        this.currentRadius = radius;
        this.sprite = sprite;
        this.effectManager = em;
        this.trailType = trailType;
        this.trailSpacing = trailSpacing;
        
        this.lastCritResult = MathUtils.random() < critChance;
        this.lastDamageResult = lastCritResult ? damage * critDamageMult : damage;
    }

    public void setLifetime(float seconds) { this.maxLifetime = seconds; }
    public void setPenetration(int penetration) { this.penetrationCount = penetration; }
    public boolean canPenetrate() { return penetrationCount > 0; }
    public void reducePenetration() { penetrationCount--; }

    public boolean isExpired() { return maxLifetime > 0 && stateTime >= maxLifetime; }
    public void setDamageType(DamageType type) { this.damageType = type; }
    public DamageType getDamageType() { return damageType; }
    
    public float getDamageValue() {
        return lastDamageResult;
    }
    
    public boolean isCrit() {
        return lastCritResult;
    }

    public boolean canHit(Entity entity) {
        if (entity instanceof Player) {
            Float lastHit = lastHitTimes.get(entity);
            return lastHit == null || (stateTime - lastHit) > 0.2f;
        }
        return true;
    }

    public void registerHit(Entity entity) {
        lastHitTimes.put(entity, stateTime);
    }

    public void update(float delta, Array<Entity> enemies) {
        if (!alive) return;
        stateTime += delta;
        position.mulAdd(direction, speed * delta);

        if (growthRate > 0) {
            float targetRadius = baseRadius + (growthRate * stateTime);
            currentRadius = Math.min(targetRadius, maxRadius);
        }

        if (isExpired()) {
            die(enemies);
        }

        if (trailType != null && effectManager != null && trailSpacing > 0) {
            float distMoved = position.dst(lastTrailPos);
            trailAccumulator += distMoved;

            if (trailAccumulator >= trailSpacing) {
                int count = (int) (trailAccumulator / trailSpacing);
                Vector2 tempPos = new Vector2();
                for (int i = 1; i <= count; i++) {
                    float t = (i * trailSpacing) / trailAccumulator;
                    tempPos.set(lastTrailPos).lerp(position, t);
                    effectManager.spawnEffect(trailType, tempPos, new Vector2(direction).scl(-1f));
                }
                trailAccumulator %= trailSpacing;
                lastTrailPos.set(position);
            }
        }
    }

    public void render(Batch batch) {
        if (!alive || sprite == null) return;
        float angle = direction.angleDeg();
        if (rotationSpeed != 0) {
            angle += rotationSpeed * stateTime;
        }
        float width = currentRadius * 2;
        float aspectRatio = (float) sprite.getRegionHeight() / sprite.getRegionWidth();
        float height = width * aspectRatio;
        batch.draw(sprite, position.x - width / 2f, position.y - height / 2f, width / 2f, height / 2f, width, height, 1f, 1f, angle);
    }

    public void die(Array<Entity> enemies) {
        this.alive = false;
        for (Component c : components) {
            c.onDeath(this, enemies);
        }
        lastHitTimes.clear();
    }

    @Override public void die() { die(new Array<>()); }

    @Override public Vector2 getPosition() { return position; }
    @Override public void setPosition(Vector2 pos) { this.position.set(pos); }
    public void setLastTrailPos(Vector2 pos) { this.lastTrailPos.set(pos); }
    @Override public Vector2 getDirection() { return direction; }
    @Override public void setDirection(Vector2 dir) { this.direction.set(dir).nor(); }
    @Override public float getSpeed() { return speed; }
    @Override public void setSpeed(float speed) { this.speed = speed; }
    @Override public float getDamage() { return damage; }
    @Override public void setDamage(float damage) { this.damage = damage; }
    @Override public float getRadius() { return currentRadius; }
    @Override public void setRadius(float radius) { this.currentRadius = Math.max(0.01f, radius); }
    @Override public boolean isSensorMode() { return sensorMode; }
    @Override public void setSensorMode(boolean sensorMode) { this.sensorMode = sensorMode; }
    @Override public Entity getOwner() { return owner; }
    @Override public void setOwner(Object owner) { this.owner = (Entity) owner; }
    @Override public float getStateTime() { return stateTime; }
    @Override public void setStateTime(float time) { this.stateTime = time; }
    @Override public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }
    public float getImpactKnockback() { return impactKnockback; }
    public void setImpactKnockback(float knockback) { this.impactKnockback = knockback; }

    public void setEffectManager(EffectManager em) { this.effectManager = em; }
    public void setTrailType(String type) { this.trailType = type; }
    public void setTrailSpacing(float spacing) { this.trailSpacing = spacing; }
    public void setCritChance(float chance) { this.critChance = chance; }
    public void setCritDamageMult(float mult) { this.critDamageMult = mult; }
    public void setSprite(TextureRegion sprite) { this.sprite = sprite; }
    public void setGrowthRate(float rate) { this.growthRate = rate; }
    public void setMaxRadius(float max) { this.maxRadius = max; }
    public void setRotationSpeed(float speed) { this.rotationSpeed = speed; }

    public void calculateCritStats() {
        this.lastCritResult = MathUtils.random() < critChance;
        this.lastDamageResult = lastCritResult ? damage * critDamageMult : damage;
    }

    public void addComponent(Component c) { components.add(c); }
    public Array<Component> getComponents() { return components; }
    public boolean hasExplosive() {
        for (Component c : components) {
            if (c instanceof com.tikisadventure.components.ExplosiveComponent) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void reset() {
        alive = false;
        position.setZero();
        direction.setZero();
        speed = 0;
        damage = 0;
        critChance = 0;
        critDamageMult = 0;
        damageType = DamageType.KINETIC;
        baseRadius = 0;
        currentRadius = 0;
        sensorMode = false;
        stateTime = 0;
        owner = null;
        sprite = null;
        effectManager = null;
        trailType = null;
        trailSpacing = 0;
        lastTrailPos.setZero();
        trailAccumulator = 0;
        penetrationCount = 0;
        impactKnockback = 0;
        lastCritResult = false;
        lastDamageResult = 0;
        maxLifetime = 5f;
        growthRate = 0f;
        maxRadius = Float.MAX_VALUE;
        rotationSpeed = 0f;
        lastHitTimes.clear();
        components.clear();
    }
}
