package com.tikisadventure.entities.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.badlogic.gdx.utils.Array;
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

    private Array<Entity> enemies;

    public Tiki() {

        idle = new Animation<>(0.15f, regions_idle[0], regions_idle[1], regions_idle[2], regions_idle[3], regions_idle[4], regions_idle[5], regions_idle[6], regions_idle[7], regions_idle[8], regions_idle[9], regions_idle[10], regions_idle[11]);
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

        weaponManager = new WeaponManager(this);

        setAlive();
    }

    @Override
    public void update(float deltaTime, Entity player) {

        if (deltaTime == 0) return;
        if (deltaTime > 0.1f) deltaTime = 0.1f;

        stateTime += deltaTime;

        velocidad.x = 0;
        velocidad.y = 0;

        // Movimiento horizontal
        if (Gdx.input.isKeyPressed(Keys.A)) {
            velocidad.x = -velocidad_max;
            mirarDerecha = false;
            estado = Estado.walking_left;
        }

        if (Gdx.input.isKeyPressed(Keys.D)) {
            velocidad.x = velocidad_max;
            mirarDerecha = true;
            estado = Estado.walking_right;
        }

        // Movimiento vertical
        if (Gdx.input.isKeyPressed(Keys.W)) {
            velocidad.y = velocidad_max;
            estado = Estado.walking_up;
        }

        if (Gdx.input.isKeyPressed(Keys.S)) {
            velocidad.y = -velocidad_max;
            estado = Estado.walking_down;
        }
        if (velocidad.x == 0 && velocidad.y == 0) {
            estado = Estado.idle;
        }

        posicion.x += velocidad.x * deltaTime;
        posicion.y += velocidad.y * deltaTime;

        weaponManager.update(deltaTime, enemies);

        actualizarHitboxes();

        if (velocidad.x == 0 && velocidad.y == 0) {
            estado = Estado.idle;
        }
    }

    @Override
    public void render(Batch batch, float delta) {

        TextureRegion frame;

        switch (estado) {
            case idle:
                frame = idle.getKeyFrame(stateTime);
                break;

            case walking_down:
                frame = down.getKeyFrame(stateTime);
                break;
            case walking_up:
                frame = up.getKeyFrame(stateTime);
                break;
            case walking_left:
                frame = left.getKeyFrame(stateTime);
                break;
            case walking_right:
                frame = right.getKeyFrame(stateTime);
                break;


            default:
                frame = idle.getKeyFrame(stateTime);
        }
        batch.draw(frame, posicion.x, posicion.y, ANCHO, ALTO);
        weaponManager.render(batch);

    }

    public void setEnemies(Array<Entity> enemies){
        this.enemies = enemies;
    }

    public WeaponManager getWeaponManager(){
        return weaponManager;
    }
}
