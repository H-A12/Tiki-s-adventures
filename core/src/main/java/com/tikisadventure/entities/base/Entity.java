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
import com.tikisadventure.entities.player.Player;
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

    protected boolean frozen = false;

    protected Color tintColor = new Color(Color.WHITE); // Blanco por defecto (sin tinte)

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

        if (this instanceof Player && ((Player) this).isDashing()) {
            return;
        }

        healthComponent.currentHealth -= quantity;
        EventBus.publish(new DamageEvent(this, quantity, isCritical, damageType));

        if (healthComponent.currentHealth <= 0) {
            healthComponent.currentHealth = 0;
            die();
        }
    }

    @Override
    public void dispose() {
        statusManager.dispose();
        EventBus.unsubscribe(DamageEvent.class, damageListener);
    }

    @Override
    public void die() {
        this.alive = false;
        for (Component c : components) {
            c.dispose();
        }
        components.clear();
        dispose();
        EventBus.publish(new EntityDiedEvent(this));
    }

    public void update(float delta, Array<Entity> entities) {
        if (renderComponent != null && !frozen) renderComponent.stateTime += delta;

        if (damageFlashTimer > 0) {
            damageFlashTimer -= delta;
        }
        statusManager.update(this, delta);
        for (Component c : components) {
            c.tick(this, delta, entities);
        }
    }

    public void update(float delta) {
        update(delta, (Array<Entity>) null);
    }

    public abstract void update(float delta, Entity target);

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

        // Aunque no lo usemos en el hielo, dejamos el tintColor por si en el
        // futuro quieres usarlo para veneno o fuego.
        Color prevColor = batch.getColor();
        batch.setColor(tintColor.r * prevColor.r, tintColor.g * prevColor.g, tintColor.b * prevColor.b, tintColor.a * prevColor.a);

        draw(batch, delta); // Dibuja el sprite normal de la entidad hija

        batch.setColor(prevColor); // Restauramos el color original del Batch

        // --- NUEVO: DIBUJAR BLOQUE DE HIELO SI ESTÁ CONGELADO ---
        if (frozen) {
            TextureRegion iceRegion = Assets.getRegion("shared", "particle_assets/IceBlock");
            if (iceRegion != null) {
                // Hacemos que sea un cuadrado perfecto basándonos en su lado más grande.
                // El 1.2f es para que el hielo sea un 20% más grande que el enemigo y lo cubra bien.
                float size = Math.max(getANCHO(), getALTO()) * 1.2f;

                // Centramos el cuadrado en la posición actual de la entidad
                float x = positionComponent.posicion.x - size / 2f;
                float y = positionComponent.posicion.y - size / 2f;

                // Le damos un poco de transparencia (0.85f) para que se vea al monstruo dentro
                batch.setColor(1f, 1f, 1f, 0.85f);
                batch.draw(iceRegion, x, y, size, size);

                batch.setColor(prevColor); // Restauramos de nuevo
            }
        }

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
    public void setAlive(boolean alive) {this.alive = alive;}
    public float getDamage() { return statsComponent != null ? statsComponent.damage : 0; }
    public void setDamage(float damage) { if (statsComponent != null) statsComponent.damage = damage; }
    public int getExperience() { return statsComponent != null ? statsComponent.experience : 0; }
    public void setExperience(int experience) { if (statsComponent != null) statsComponent.experience = experience; }
    public float getStateTime() { return renderComponent != null ? renderComponent.stateTime : 0; }
    public void setStateTime(float stateTime) { if (renderComponent != null) renderComponent.stateTime = stateTime; }
    public Circle getHitboxActionTrigger() { return hitboxActionTrigger; }
    public float getSpeed() { return velocityComponent.speed; }
    public void setSpeed(float speed) { velocityComponent.speed = speed; }
    public Vector2 getVelocity() { return velocityComponent.velocidad; }
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

    public Color getTintColor() { return tintColor; }
    public void setTintColor(Color color) { this.tintColor.set(color); }

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

    public boolean isFrozen() { return frozen; }
    public void setFrozen(boolean frozen) { this.frozen = frozen; }

    public HealthComponent getHealthComponent() {
        return healthComponent;
    }

}
