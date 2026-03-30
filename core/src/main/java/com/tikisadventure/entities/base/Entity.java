package com.tikisadventure.entities.base;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.entities.base.components.Killable;
import com.tikisadventure.entities.base.components.Knockbackable;

/**
 * Clase base para todas las entidades del juego (Player, Enemigos, NPCs).
 * Ubicada en: core/src/main/java/com/tikisadventure/entities/base/Entity.java
 */
public abstract class Entity implements Knockbackable, Killable {

    // Posicionamiento y Física
    public final Vector2 posicion = new Vector2();
    protected final Vector2 velocidad = new Vector2();
    protected final Vector2 knockbackVelocity = new Vector2();

    // Stats Base
    protected float speed;
    protected float vida;
    protected float vida_max;
    protected float danyo;
    protected boolean alive = true;

    // Estado de Invulnerabilidad (Dash y Daño)
    protected boolean isInvulnerable = false;
    protected float invulnerableTimer = 0;

    // Renderizado y Animación
    protected TextureRegion sprite; // Frame actual inyectado por AnimationSystem
    public float ANCHO = 1f;
    public float ALTO = 1f;
    protected float stateTime = 0;
    protected boolean mirarDerecha = true;

    public enum Estado {
        idle,
        walking,
        walking_side,
        walking_up,
        walking_down,
        dead
    }
    protected Estado estado = Estado.idle;

    // Colisiones y Triggers
    protected Circle hitboxEventTrigger; // Rango de detección
    protected Circle hitboxActionTrigger; // Hitbox física/daño
    protected int experience;

    public Entity(float x, float y) {
        this.posicion.set(x, y);
        this.hitboxEventTrigger = new Circle();
        this.hitboxActionTrigger = new Circle();
    }

    // --- LÓGICA DE ACTUALIZACIÓN ---

    /**
     * Gestiona los contadores de tiempo (Invulnerabilidad, etc).
     * Debe ser llamado en el update() de las clases hijas.
     */
    protected void updateTimers(float delta) {
        if (invulnerableTimer > 0) {
            invulnerableTimer -= delta;
            if (invulnerableTimer <= 0) {
                isInvulnerable = false;
                invulnerableTimer = 0;
            }
        }
    }

    /**
     * Aplica el movimiento de retroceso suave.
     */
    protected void applyKnockback(float delta) {
        if (knockbackVelocity.len2() > 0.01f) {
            posicion.mulAdd(knockbackVelocity, delta);
            knockbackVelocity.scl(1f - 8f * delta); // Fricción
            if (knockbackVelocity.len2() < 0.01f) knockbackVelocity.setZero();
        }
    }

    public void actualizarHitboxes() {
        // Centramos las hitboxes en la posición de la entidad
        hitboxEventTrigger.set(posicion.x, posicion.y, Math.max(ANCHO, ALTO) * 0.7f);
        hitboxActionTrigger.set(posicion.x, posicion.y, Math.max(ANCHO, ALTO) * 0.4f);
    }

    // --- SISTEMA DE COMBATE (Killable & Knockbackable) ---

    @Override
    public boolean receiveDamage(float quantity) {
        if (!alive || isInvulnerable || quantity <= 0) return false;

        vida -= quantity;
        if (vida <= 0) {
            vida = 0;
            die();
        }
        return true;
    }

    @Override
    public void applyKnockback(Vector2 direction, float force) {
        // Multiplicamos por un factor para que el impacto se note
        this.knockbackVelocity.add(direction.x * force * 25f, direction.y * force * 25f);
    }

    @Override
    public void die() {
        this.alive = false;
        this.estado = Estado.dead;
    }

    // --- MÉTODOS PARA SISTEMAS GLOBALES (Animation/Render) ---

    public abstract Animation<TextureRegion> getAnimationForState(Estado estado);

    public void setEstado(Estado nuevoEstado) { this.estado = nuevoEstado; }
    public Estado getEstado() { return estado; }

    public void setMirarDerecha(boolean mirarDerecha) { this.mirarDerecha = mirarDerecha; }
    public boolean isMirarDerecha() { return mirarDerecha; }

    public void setInvulnerable(float duration) {
        this.isInvulnerable = true;
        this.invulnerableTimer = duration;
    }

    public void setStats(float hp, float spd, float dmg, int exp) {
        this.vida_max = hp;
        this.vida = hp;
        this.speed = spd;
        this.danyo = dmg;
        this.experience = exp;
    }

    public void addStateTime(float delta) { this.stateTime += delta; }
    public float getStateTime() { return stateTime; }

    public void setSprite(TextureRegion region) { this.sprite = region; }
    public TextureRegion getSprite() { return sprite; }

    // --- MÉTODOS ABSTRACTOS OBLIGATORIOS ---
    public abstract void update(float delta);
    public abstract void render(Batch batch, float delta);

    // --- GETTERS Y SETTERS BÁSICOS ---
    public Vector2 getPosicion() { return posicion; }
    public Vector2 getVelocidad() { return velocidad; }
    public float getVida() { return vida; }
    public float getVida_max() { return vida_max; }
    public float getDanyo() { return danyo; }
    public boolean isAlive() { return alive; }
    public boolean isInvulnerable() { return isInvulnerable; }
    public Circle getHitboxActionTrigger() { return hitboxActionTrigger; }
    // Añade esto al final de la clase Entity
    public float getANCHO() {
        return ANCHO;
    }

    public float getALTO() {
        return ALTO;
    }
    @Override public Vector2 getKnockbackVelocity() { return knockbackVelocity; }
    @Override public void setKnockbackVelocity(Vector2 v) { knockbackVelocity.set(v); }
}
