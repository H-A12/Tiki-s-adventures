package com.tikisadventure.entities.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.abilities.DashAbility;
import com.tikisadventure.combat.weapons.WeaponManager;
import com.tikisadventure.entities.Entity;

public class Tiki extends Entity {

    private static Texture movement_idle = new Texture("idle.png");
    private static Texture movement_down = new Texture("down.png");
    private static Texture movement_up = new Texture("up.png");
    private static Texture movement_left = new Texture("left.png");
    private static Texture movement_right= new Texture("right.png");


    private TextureRegion[] regions_idle = TextureRegion.split(movement_idle, 16, 16)[0];
    private TextureRegion[] regions_down = TextureRegion.split(movement_down, 16, 16)[0];
    private TextureRegion[] regions_up = TextureRegion.split(movement_up, 16, 16)[0];
    private TextureRegion[] regions_left = TextureRegion.split(movement_left, 16, 16)[0];
    private TextureRegion[] regions_right = TextureRegion.split(movement_right, 16, 16)[0];

    private Animation<TextureRegion> idle;
    private Animation<TextureRegion> down;
    private Animation<TextureRegion> up;
    private Animation<TextureRegion> left;
    private Animation<TextureRegion> right;

    private WeaponManager weaponManager;
    private DashAbility dash;
    private Array<Entity> enemies;

    public Tiki() {

        idle = new Animation<>(0.15f, regions_idle[0],regions_idle[1],regions_idle[2],regions_idle[3],regions_idle[4],regions_idle[5],regions_idle[6],regions_idle[7],regions_idle[8],regions_idle[9],regions_idle[10],regions_idle[11]);
        idle.setPlayMode(Animation.PlayMode.LOOP);

        down = new Animation<>(0.15f, regions_down[0],regions_down[1],regions_down[2],regions_down[3]);
        down.setPlayMode(Animation.PlayMode.LOOP);

        up = new Animation<>(0.15f, regions_up[0],regions_up[1],regions_up[2],regions_up[3]);
        up.setPlayMode(Animation.PlayMode.LOOP);

        left = new Animation<>(0.15f, regions_left[0],regions_left[1],regions_left[2],regions_left[3]);
        left.setPlayMode(Animation.PlayMode.LOOP);

        right = new Animation<>(0.15f, regions_right[0],regions_right[1],regions_right[2],regions_right[3]);
        right.setPlayMode(Animation.PlayMode.LOOP);





        ANCHO = 2;
        ALTO = 2;

        velocidad_max = 5;

        vida_max = 100000;
        vida = vida_max;

        this.POST_DAMAGE_INVULNERABILITY = 0.5f; //invulnerabilidad

        weaponManager = new WeaponManager(this);
        this.dash = new DashAbility(this); //habilidad de dash
        setAlive();
    }

    @Override
    public void update(float deltaTime, Entity player) {
        if (deltaTime == 0) return;
        if (deltaTime > 0.1f) deltaTime = 0.1f;

        // Timers de daño e invulnerabilidad
        if (this.invulnerableTimer > 0) this.invulnerableTimer -= deltaTime;
        if (this.invulnerableTimer <= 0) this.invulnerableTimer = 0;
        if (this.damageFlashTimer > 0) this.damageFlashTimer -= deltaTime;

        stateTime += deltaTime;

        // --- 1. Obtener frame actual (Usamos 'right' para ambos lados horizontales) ---
        TextureRegion currentFrame;
        switch (estado) {
            case walking_down:  currentFrame = down.getKeyFrame(stateTime); break;
            case walking_up:    currentFrame = up.getKeyFrame(stateTime); break;
            case walking_left:
            case walking_right: currentFrame = right.getKeyFrame(stateTime); break;
            default:            currentFrame = idle.getKeyFrame(stateTime); break;
        }

        // --- 2. Lógica de activación y actualización del Dash ---
        if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            dash.activate();
        }

        dash.update(deltaTime, currentFrame); // UNICA LLAMADA AL UPDATE DEL DASH

        // --- 3. Movimiento normal (Solo si no está haciendo dash) ---
        if (!dash.isDashing()) {
            velocidad.x = 0;
            velocidad.y = 0;

            if (Gdx.input.isKeyPressed(Keys.A)) {
                velocidad.x = -velocidad_max;
                mirarDerecha = false;
                estado = Estado.walking_left;
            } else if (Gdx.input.isKeyPressed(Keys.D)) {
                velocidad.x = velocidad_max;
                mirarDerecha = true;
                estado = Estado.walking_right;
            }

            if (Gdx.input.isKeyPressed(Keys.W)) {
                velocidad.y = velocidad_max;
                estado = Estado.walking_up;
            } else if (Gdx.input.isKeyPressed(Keys.S)) {
                velocidad.y = -velocidad_max;
                estado = Estado.walking_down;
            }

            if (velocidad.x == 0 && velocidad.y == 0) {
                estado = Estado.idle;
            }
        }

        posicion.x += velocidad.x * deltaTime;
        posicion.y += velocidad.y * deltaTime;

        weaponManager.update(deltaTime, enemies);
        actualizarHitboxes();
    }

    @Override
    public void render(Batch batch, float delta) {
        TextureRegion frame;

        // Aquí también usamos 'right' para ambos para que el flip de abajo funcione
        switch (estado) {
            case walking_down:  frame = down.getKeyFrame(stateTime); break;
            case walking_up:    frame = up.getKeyFrame(stateTime); break;
            case walking_left:
            case walking_right: frame = right.getKeyFrame(stateTime); break;
            default:            frame = idle.getKeyFrame(stateTime); break;
        }

        dash.render(batch);

        batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);

        if (this.damageFlashTimer > 0) {
            batch.setColor(1f, 0f, 0f, 1f); // Rojo si recibe daño
        }
        else if (this.invulnerableTimer > 0) {
            float alpha = (Math.sin(stateTime * 25f) > 0) ? 1f : 0.4f;
            batch.setColor(1f, 1f, 1f, alpha); // Parpadeo
        }

        if (mirarDerecha) {
            batch.draw(frame, posicion.x, posicion.y, ANCHO, ALTO);
        } else {
            batch.draw(frame, posicion.x + ANCHO, posicion.y, -ANCHO, ALTO);
        }

        batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        weaponManager.render(batch);
    }

    public void setEnemies(Array<Entity> enemies){
        this.enemies = enemies;
    }

    public WeaponManager getWeaponManager(){
        return weaponManager;
    }
    // En Tiki.java
    public DashAbility getDashAbility() {
        return dash;
    }
}
