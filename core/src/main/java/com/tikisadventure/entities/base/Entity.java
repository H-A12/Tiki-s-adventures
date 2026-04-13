package com.tikisadventure.entities.base;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.tikisadventure.core.Assets;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.components.traits.Killable;
import com.tikisadventure.components.traits.Knockbackable;
import com.tikisadventure.components.traits.PositionProvider;
import com.tikisadventure.components.traits.SpeedProvider;
import com.tikisadventure.components.traits.DamageDealer;
import com.tikisadventure.components.traits.RadiusProvider;
import com.tikisadventure.components.traits.Orientable;
import com.tikisadventure.systems.events.EventBus;
import com.tikisadventure.systems.events.EntityDiedEvent;
import com.tikisadventure.systems.events.DamageEvent;
import com.tikisadventure.systems.events.EventListener;
import com.tikisadventure.components.HealthComponent;
import com.tikisadventure.components.PositionComponent;
import com.tikisadventure.components.VelocityComponent;
import com.tikisadventure.components.RenderComponent;
import com.tikisadventure.components.StatsComponent;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.combat.StatusManager;
import com.tikisadventure.entities.base.Component;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Array;

public abstract class Entity implements Knockbackable, Killable, PositionProvider, SpeedProvider, DamageDealer, RadiusProvider, Orientable, Disposable {

    protected PositionComponent positionComponent = new PositionComponent(0,0);
    protected VelocityComponent velocityComponent = new VelocityComponent(0);
    protected HealthComponent healthComponent;
    protected RenderComponent renderComponent;
    protected StatsComponent statsComponent = new StatsComponent(0, 0, 0);

    private boolean alive = true;
    protected StatusManager statusManager = new StatusManager();
    protected Array<Component> components = new Array<>();
    protected float damageFlashTimer = 0f;
    protected Circle hitboxEventTrigger;
    protected Circle hitboxActionTrigger;
    
    public enum Estado {
        idle, walking, walking_down, walking_up, walking_left, walking_right;
    }

    private final EventListener<DamageEvent> damageListener = event -> {
        if (event.entity == this) {
            damageFlashTimer = 0.15f;
        }
    };

    public Entity() {
        hitboxEventTrigger = new Circle();
        hitboxActionTrigger = new Circle();
        EventBus.subscribe(DamageEvent.class, damageListener);
    }

    public void actualizarHitboxes() {
        hitboxEventTrigger.set(positionComponent.posicion.x, positionComponent.posicion.y, Math.max(getANCHO(), getALTO()) * 0.7f);
        hitboxActionTrigger.set(positionComponent.posicion.x, positionComponent.posicion.y, Math.max(getANCHO(), getALTO()) * 0.4f);
    }

    public void receiveDamage(float quantity, boolean isCritical, DamageType damageType) {
        if (!isAlive() || healthComponent == null) return;
        healthComponent.currentHealth -= quantity;
        EventBus.publish(new DamageEvent(this, quantity, isCritical, damageType));

        if (healthComponent.currentHealth <= 0) {
            healthComponent.currentHealth = 0;
            die();
        }
    }

    @Override
    public void dispose() {
        EventBus.unsubscribe(DamageEvent.class, damageListener);
    }

    @Override
    public void die() {
        dispose();
        EventBus.publish(new EntityDiedEvent(this));
    }

    public abstract void update(float delta, Entity target);
    
    public void update(float delta) {
        if (renderComponent != null) renderComponent.stateTime += delta;
        if (damageFlashTimer > 0) {
            damageFlashTimer -= delta;
        }
        statusManager.update(this, delta);
        for (Component c : components) {
            c.tick(this, delta, null);
        }
    }
    
    public void addComponent(Component c) { components.add(c); c.onAttach(this); }
    public void removeComponent(Component c) { components.removeValue(c, true); c.onDetach(this); }
    public <T extends Component> T getComponent(Class<T> type) {
        for (Component c : components) {
            if (type.isInstance(c)) return type.cast(c);
        }
        if (type.isInstance(positionComponent)) return type.cast(positionComponent);
        if (type.isInstance(velocityComponent)) return type.cast(velocityComponent);
        if (type.isInstance(healthComponent)) return type.cast(healthComponent);
        if (type.isInstance(renderComponent)) return type.cast(renderComponent);
        if (type.isInstance(statsComponent)) return type.cast(statsComponent);
        
        return null;
    }
    public StatusManager getStatusManager() { return statusManager; }

    public final void render(Batch batch, float delta) {
        if (!isAlive()) return;
        
        if (damageFlashTimer > 0 && Assets.whiteFlashShader != null) {
            batch.setShader(Assets.whiteFlashShader);
            Assets.whiteFlashShader.setUniformf("u_flashIntensity", 1.0f);
        } else {
            batch.setShader(null);
        }
        
        draw(batch, delta);
        
        batch.setShader(null);
    }
    
    public abstract void draw(Batch batch, float delta);

    protected void applyKnockback(float delta) {
        if (velocityComponent.knockbackVelocity.len() > 0.1f) {
            positionComponent.posicion.mulAdd(velocityComponent.knockbackVelocity, delta);
            velocityComponent.knockbackVelocity.scl(1f - 8f * delta);
            if (velocityComponent.knockbackVelocity.len() < 0.1f) {
                velocityComponent.knockbackVelocity.setZero();
            }
        }
    }

    public Vector2 getPosition() { return positionComponent.posicion; }
    public void setPosition(Vector2 pos) { positionComponent.posicion.set(pos); }
    public float getVida() { return healthComponent != null ? healthComponent.currentHealth : 0; }
    public float getVida_max() { return healthComponent != null ? healthComponent.maxHealth : 0; }
    public void setVida_max(float vida_max) { if (healthComponent != null) healthComponent.maxHealth = vida_max; }
    public boolean isAlive() { return alive; }
    public float getDamage() { return statsComponent != null ? statsComponent.damage : 0; }
    public void setDamage(float damage) { if (statsComponent != null) statsComponent.damage = damage; }
    public int getExperience() { return statsComponent != null ? statsComponent.experience : 0; }
    public void setExperience(int experience) { if (statsComponent != null) statsComponent.experience = experience; }
    public float getStateTime() { return renderComponent != null ? renderComponent.stateTime : 0; }
    public void setStateTime(float stateTime) { if (renderComponent != null) renderComponent.stateTime = stateTime; }
    public Circle getHitboxActionTrigger() { return hitboxActionTrigger; }
    public float getSpeed() { return velocityComponent.speed; }
    public void setSpeed(float speed) { velocityComponent.speed = speed; }
    public void setVida(float vida) {
        if (healthComponent != null) {
            healthComponent.currentHealth = vida;
            if(healthComponent.currentHealth <= 0) die();
        }
    }
    public float getRadius() { return Math.max(getANCHO(), getALTO()) * 0.5f; }
    public void setRadius(float radius) { 
        // In this current implementation, radius is derived from dimensions.
        // If we want a custom radius, we should add a field to RenderComponent or Entity.
    }
    public float getANCHO() { return renderComponent != null ? renderComponent.ancho : 0; }
    public float getALTO() { return renderComponent != null ? renderComponent.alto : 0; }
    public void setANCHO(float ANCHO) { if (renderComponent != null) renderComponent.ancho = ANCHO; }
    public void setALTO(float ALTO) { if (renderComponent != null) renderComponent.alto = ALTO; }

    @Override
    public Vector2 getKnockbackVelocity() {
        return velocityComponent.knockbackVelocity;
    }

    @Override
    public void setKnockbackVelocity(Vector2 velocity) {
        velocityComponent.knockbackVelocity.set(velocity);
    }

    public void setEstado(Estado estado) {
        if (renderComponent != null) renderComponent.estado = estado;
    }
    public Estado getEstado() {
        return renderComponent != null ? renderComponent.estado : Estado.idle;
    }
    public Vector2 getDirection() {
        return isMirarDerecha() ? new Vector2(1, 0) : new Vector2(-1, 0);
    }
    public void setDirection(Vector2 dir) {
        setMirarDerecha(dir.x > 0);
    }
    public void setMirarDerecha(boolean mirar) {
        if (renderComponent != null) renderComponent.mirarDerecha = mirar;
    }
    public boolean isMirarDerecha() { return renderComponent != null ? renderComponent.mirarDerecha : true; }
    public int getScoreValue() { return statsComponent != null ? statsComponent.scoreValue : 0; }
    public void setScoreValue(int scoreValue) { if (statsComponent != null) statsComponent.scoreValue = scoreValue; }
}
