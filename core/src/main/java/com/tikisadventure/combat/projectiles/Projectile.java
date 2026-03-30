package com.tikisadventure.combat.projectiles;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.components.Component;
import com.tikisadventure.components.HasDamage;
import com.tikisadventure.components.HasDirection;
import com.tikisadventure.components.HasOwner;
import com.tikisadventure.components.HasPosition;
import com.tikisadventure.components.HasRadius;
import com.tikisadventure.components.HasSpeed;
import com.tikisadventure.components.Killable;
import com.tikisadventure.components.Sensorable;
import com.tikisadventure.components.Timed;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;
import com.tikisadventure.entities.base.Entity;

public class Projectile implements HasPosition, HasDirection, HasSpeed, HasDamage, 
                             HasRadius, HasOwner, Timed, Killable, Sensorable {

    private Vector2 position = new Vector2();
    private Vector2 direction = new Vector2();
    private float speed;
    private float damage;

    private float baseRadius;
    private float currentRadius;
    private boolean sensorMode = false;

    private float stateTime = 0;
    private boolean alive = true;

    private Entity owner;
    private TextureRegion sprite;

    private EffectManager effectManager;
    private EffectType trailType;
    private float trailSpacing;
    private Vector2 lastTrailPos = new Vector2();

    private Array<Component> components = new Array<>();

    public Projectile(Entity owner, Vector2 pos, Vector2 dir, float speed, float dmg, float radius,
                      TextureRegion sprite, EffectManager em, EffectType trailType, float trailSpacing) {
        this.owner = owner;
        this.position.set(pos);
        this.lastTrailPos.set(pos);
        this.direction.set(dir).nor();
        this.speed = speed;
        this.damage = dmg;

        this.baseRadius = radius;
        this.currentRadius = radius;

        this.sprite = sprite;
        this.effectManager = em;
        this.trailType = trailType;
        this.trailSpacing = trailSpacing;
    }

    public void addComponent(Component component) {
        components.add(component);
        component.onAttach(this);
    }

    public void addBehavior(Component behavior) {
        addComponent(behavior);
    }

    public void update(float delta, Array<Entity> enemies) {
        if (!alive) return;
        stateTime += delta;

        for (Component c : components) {
            c.tick(this, delta, enemies);
        }

        if (trailType != null && effectManager != null && trailSpacing > 0) {
            float distMoved = position.dst(lastTrailPos);

            if (distMoved >= trailSpacing) {
                int count = (int) (distMoved / trailSpacing);
                Vector2 tempPos = new Vector2();

                for (int i = 0; i < count; i++) {
                    float t = (float) i / count;
                    tempPos.set(lastTrailPos).lerp(position, t);
                    effectManager.spawnEffect(trailType, tempPos, new Vector2(direction).scl(-1f));
                }
                lastTrailPos.set(position);
            }
        }
    }

    public void render(Batch batch) {
        if (!alive || sprite == null) return;

        float angle = direction.angleDeg();
        float width = currentRadius * 2;
        float aspectRatio = (float) sprite.getRegionHeight() / sprite.getRegionWidth();
        float height = width * aspectRatio;

        batch.draw(
            sprite,
            position.x - width / 2f,
            position.y - height / 2f,
            width / 2f,
            height / 2f,
            width,
            height,
            1f, 1f,
            angle
        );
    }

    @Override
    public void die() {
        this.alive = false;
    }

    @Override
    public Vector2 getPosition() { return position; }
    @Override
    public void setPosition(Vector2 pos) { this.position.set(pos); }

    @Override
    public Vector2 getDirection() { return direction; }
    @Override
    public void setDirection(Vector2 dir) { this.direction.set(dir).nor(); }

    @Override
    public float getSpeed() { return speed; }
    @Override
    public void setSpeed(float speed) { this.speed = speed; }

    @Override
    public float getDamage() { return damage; }
    @Override
    public void setDamage(float damage) { this.damage = damage; }

    public float getBaseRadius() { return baseRadius; }

    @Override
    public float getRadius() { return currentRadius; }

    @Override
    public void setRadius(float radius) { this.currentRadius = Math.max(0.01f, radius); }

    @Override
    public boolean isSensorMode() { return sensorMode; }
    @Override
    public void setSensorMode(boolean sensorMode) { this.sensorMode = sensorMode; }

    @Override
    public Entity getOwner() { return owner; }
    @Override
    public void setOwner(Object owner) { 
        this.owner = (Entity) owner; 
    }

    @Override
    public float getStateTime() { return stateTime; }
    @Override
    public void setStateTime(float time) { this.stateTime = time; }

    @Override
    public boolean isAlive() { return alive; }
    public void setAlive(boolean alive) { this.alive = alive; }

    public <T> T getComponent(Class<T> type) {
        for (Component c : components) {
            if (type.isInstance(c)) return type.cast(c);
        }
        return null;
    }
}
