package com.tikisadventure.entities;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.weapons.Weapon;
import com.badlogic.gdx.graphics.g2d.Batch;

public abstract class Entity {

    protected float ANCHO;

    public Circle getHitboxEventTrigger() {
        return hitboxEventTrigger;
    }

    public Circle getHitboxActionTrigger() {
        return hitboxActionTrigger;
    }

    protected Circle hitboxEventTrigger;
    protected Circle hitboxActionTrigger;
    protected float ALTO;
    protected float velocidad_max;
    protected float danyo;
    protected float vida_max;
    protected Array<Weapon> weapons;
    protected int maxWeapons;
    protected boolean alive;


    public float getVida() {
        return vida;
    }

    public void setVida(float vida) {
        this.vida = vida;
    }

    protected float vida;
    protected enum Estado {
        Quieto, Andando
    }

    protected final Vector2 posicion = new Vector2();
    protected final Vector2 velocidad= new Vector2();

    protected Estado estado = Estado.Andando;
    protected float stateTime = 0;
    protected boolean mirarDerecha = true;

    public Entity() {
        hitboxEventTrigger = new Circle();
        hitboxActionTrigger
            = new Circle();
    }

    public void actualizarHitboxes() {
        // Hitbox generosa: un poco más grande que el sprite
        hitboxEventTrigger.set(posicion.x + ANCHO / 2, posicion.y + ALTO / 2, Math.max(ANCHO, ALTO) * 0.7f);

        // Hitbox precisa: un poco más pequeña que el sprite
        hitboxActionTrigger.set(posicion.x + ANCHO / 2, posicion.y + ALTO / 2, Math.max(ANCHO, ALTO) * 0.4f);
    }

    public void receiveDamage(float quantity){
        if(vida <= 0) return; //ya muerto

        vida -= quantity;

        if(vida <= 0) die();
    }

    public void die(){
        alive = false;
    }


    public float getANCHO() {
        return ANCHO;
    }

    public Vector2 getPosicion(){
        return this.posicion;
    }

    public void setANCHO(float ANCHO) {
        this.ANCHO = ANCHO;
    }

    public float getALTO() {
        return ALTO;
    }

    public void setALTO(float ALTO) {
        this.ALTO = ALTO;
    }


    public float getVida_max() {
        return vida_max;
    }

    public float getDanyo() {
        return danyo;
    }

    public void setVida_max(float vida_max) {
        this.vida_max = vida_max;
    }

    public void setDanyo(float danyo) {
        this.danyo = danyo;
    }

    public void setAlive(){this.alive = true;}

    public boolean isAlive(){return alive;}

    public abstract void update(float delta, Entity player);

    public abstract void render(Batch batch, float delta);

}
