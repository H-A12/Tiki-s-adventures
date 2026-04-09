package com.tikisadventure.entities.base;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.tikisadventure.core.Assets;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.components.traits.Killable;
import com.tikisadventure.components.traits.Knockbackable;
import com.tikisadventure.systems.events.EventBus;
import com.tikisadventure.systems.events.EntityDiedEvent;
import com.tikisadventure.systems.events.DamageEvent;
import com.tikisadventure.systems.events.EventListener;

public abstract class Entity implements Knockbackable, Killable {

    protected final Vector2 posicion = new Vector2();
    protected final Vector2 velocidad = new Vector2();
    protected final Vector2 knockbackVelocity = new Vector2();
    protected float speed;
    protected float vida;
    protected float vida_max;
    protected float danyo;
    protected boolean alive = true;
    protected int scoreValue;

    protected TextureRegion sprite;
    protected float ANCHO;
    protected float ALTO;
    protected float stateTime = 0;
    protected boolean mirarDerecha = true;

    protected float damageFlashTimer = 0f;

    public enum Estado {
        idle, walking, walking_down, walking_up, walking_left, walking_right;
    }
    protected Estado estado = Estado.walking;

    protected Circle hitboxEventTrigger;
    protected Circle hitboxActionTrigger;

    protected int experience;

    private final EventListener<DamageEvent> damageListener = event -> {
        if (event.entity == this) {
            damageFlashTimer = 0.15f;
        }
    };

    public Entity() {
        hitboxEventTrigger = new Circle();
        hitboxActionTrigger = new Circle();
        alive = true;
        EventBus.subscribe(DamageEvent.class, damageListener);
    }

    public void actualizarHitboxes() {
        hitboxEventTrigger.set(posicion.x, posicion.y, Math.max(ANCHO, ALTO) * 0.7f);
        hitboxActionTrigger.set(posicion.x, posicion.y, Math.max(ANCHO, ALTO) * 0.4f);
    }

    public void receiveDamage(float quantity) {
        if (!alive) return;
        vida -= quantity;
        EventBus.publish(new DamageEvent(this));

        if (vida <= 0) {
            vida = 0;
            die();
        }
    }

    @Override
    public void die() {
        EventBus.unsubscribe(DamageEvent.class, damageListener);
        alive = false;
        EventBus.publish(new EntityDiedEvent(this));
    }

    public abstract void update(float delta, Entity target);
    
    public void update(float delta) {
        if (damageFlashTimer > 0) {
            damageFlashTimer -= delta;
        }
    }

    public final void render(Batch batch, float delta) {
        if (!alive) return;
        
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
        if (knockbackVelocity.len() > 0.1f) {
            posicion.mulAdd(knockbackVelocity, delta);
            knockbackVelocity.scl(1f - 8f * delta);
            if (knockbackVelocity.len() < 0.1f) {
                knockbackVelocity.setZero();
            }
        }
    }

    public Vector2 getPosicion() { return posicion; }
    public float getVida() { return vida; }
    public float getVida_max() { return vida_max; }
    public void setVida_max(float vida_max) { this.vida_max = vida_max; }
    public boolean isAlive() { return alive; }
    public float getDanyo() { return danyo; }
    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }
    public Circle getHitboxActionTrigger() { return hitboxActionTrigger; }
    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }
    public void setVida(float vida) {
        this.vida = vida;
        if(this.vida <= 0) die();
    }
    public float getANCHO() { return ANCHO; }
    public float getALTO() { return ALTO; }
    public void setANCHO(float ANCHO) { this.ANCHO = ANCHO; }
    public void setALTO(float ALTO) { this.ALTO = ALTO; }

    @Override
    public Vector2 getKnockbackVelocity() {
        return knockbackVelocity;
    }

    @Override
    public void setKnockbackVelocity(Vector2 velocity) {
        knockbackVelocity.set(velocity);
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }
    public Estado getEstado() {
        return estado;
    }
    public void setMirarDerecha(boolean mirar) {
        this.mirarDerecha = mirar;
    }
    public int getScoreValue() { return scoreValue; }
    public void setScoreValue(int scoreValue) { this.scoreValue = scoreValue; }
}