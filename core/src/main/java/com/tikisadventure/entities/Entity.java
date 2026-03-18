package com.tikisadventure.entities;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.weapons.Weapon;
import com.badlogic.gdx.graphics.g2d.Batch;

public abstract class Entity {

    // --- Atributos de Tamaño y Hitboxes ---
    protected float ANCHO;
    protected float ALTO;
    protected Circle hitboxEventTrigger;
    protected Circle hitboxActionTrigger;

    // --- Atributos de Combate y Estado ---
    protected float velocidad_max;
    protected float danyo;
    protected float vida_max;
    protected float vida;
    protected boolean alive;

    protected float damageFlashTimer = 0;
    protected float invulnerableTimer = 0;

    protected final float DAMAGE_FLASH_DURATION = 0.2f;

    // esto son los frames de invulnerabilidad
    protected float POST_DAMAGE_INVULNERABILITY = 0f;

    public boolean isInvulnerable = false;

    protected Array<Weapon> weapons;
    protected int maxWeapons;
    protected int experience;

    // --- MOVIMIENTO Y ANIMACIÓN ---
    public enum Estado {
        standing, walking, idle, walking_down, walking_up, walking_left, walking_right
    }

    protected final Vector2 posicion = new Vector2();
    protected final Vector2 velocidad = new Vector2();
    protected Estado estado = Estado.idle;
    protected float stateTime = 0;

    public boolean mirarDerecha = true;

    public Entity() {
        hitboxEventTrigger = new Circle();
        hitboxActionTrigger = new Circle();
    }

    public void actualizarHitboxes() {
        hitboxEventTrigger.set(posicion.x + ANCHO / 2, posicion.y + ALTO / 2, Math.max(ANCHO, ALTO) * 0.7f);
        hitboxActionTrigger.set(posicion.x + ANCHO / 2, posicion.y + ALTO / 2, Math.max(ANCHO, ALTO) * 0.4f);
    }

    public void receiveDamage(float quantity) {
        // Si ya es invulnerable (por dash, timer o flag), ignoramos el daño
        if (!alive || isInvulnerable || invulnerableTimer > 0) return;

        this.vida -= quantity;
        this.damageFlashTimer = DAMAGE_FLASH_DURATION;

        // Aplicamos el tiempo configurado para esta entidad específica
        this.invulnerableTimer = POST_DAMAGE_INVULNERABILITY;

        if (this.vida <= 0) {
            this.vida = 0;
            die();
        }
    }

    public void die() {
        alive = false;
    }

    // --- Getters y Setters ---
    public Circle getHitboxEventTrigger() { return hitboxEventTrigger; }
    public Circle getHitboxActionTrigger() { return hitboxActionTrigger; }
    public float getVida() { return vida; }
    public void setVida(float vida) {
        this.vida = vida;
        if (this.vida <= 0) die();
    }
    public Vector2 getPosicion() { return this.posicion; }
    public float getANCHO() { return ANCHO; }
    public void setANCHO(float ANCHO) { this.ANCHO = ANCHO; }
    public float getALTO() { return ALTO; }
    public void setALTO(float ALTO) { this.ALTO = ALTO; }
    public float getVida_max() { return vida_max; }
    public void setVida_max(float vida_max) { this.vida_max = vida_max; }
    public float getDanyo() { return danyo; }
    public void setDanyo(float danyo) { this.danyo = danyo; }
    public void setAlive() { this.alive = true; }
    public boolean isAlive() { return alive; }
    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    public abstract void update(float delta, Entity player);
    public abstract void render(Batch batch, float delta);
}
