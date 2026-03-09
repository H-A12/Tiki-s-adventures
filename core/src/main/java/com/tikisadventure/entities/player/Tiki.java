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

    private static Texture tikiTextura = new Texture("tiki.png");

    private TextureRegion[] regiones = TextureRegion.split(tikiTextura, 18, 26)[0];

    private Animation<TextureRegion> quieto;
    private Animation<TextureRegion> andar;
    private WeaponManager weaponManager;

    private Array<Entity> enemies;

    public Tiki() {

        quieto = new Animation<>(0, regiones[0]);

        andar = new Animation<>(0.15f, regiones[2], regiones[3], regiones[4]);
        andar.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

        ANCHO = 1 / 16f * regiones[0].getRegionWidth();
        ALTO = 1 / 16f * regiones[0].getRegionHeight();

        velocidad_max = 5;

        vida_max = 10;
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

        weaponManager.update(deltaTime, enemies);

        // Movimiento horizontal
        if (Gdx.input.isKeyPressed(Keys.A)) {
            velocidad.x = -velocidad_max;
            mirarDerecha = false;
            estado = Estado.Andando;
        }

        if (Gdx.input.isKeyPressed(Keys.D)) {
            velocidad.x = velocidad_max;
            mirarDerecha = true;
            estado = Estado.Andando;
        }

        // Movimiento vertical
        if (Gdx.input.isKeyPressed(Keys.W)) {
            velocidad.y = velocidad_max;
            estado = Estado.Andando;
        }

        if (Gdx.input.isKeyPressed(Keys.S)) {
            velocidad.y = -velocidad_max;
            estado = Estado.Andando;
        }

        posicion.x += velocidad.x * deltaTime;
        posicion.y += velocidad.y * deltaTime;

        weaponManager.update(deltaTime, enemies);

        actualizarHitboxes();

        if (velocidad.x == 0 && velocidad.y == 0) {
            estado = Estado.Quieto;
        }
    }

    @Override
    public void render(Batch batch, float delta) {

        TextureRegion frame;

        switch (estado) {
            case Quieto:
                frame = quieto.getKeyFrame(stateTime);
                break;

            case Andando:
                frame = andar.getKeyFrame(stateTime);
                break;

            default:
                frame = quieto.getKeyFrame(stateTime);
        }

        if (mirarDerecha) {
            batch.draw(frame, posicion.x, posicion.y, ANCHO, ALTO);
        } else {
            batch.draw(frame, posicion.x + ANCHO, posicion.y, -ANCHO, ALTO);
        }

        weaponManager.render(batch);

    }

    public void setEnemies(Array<Entity> enemies){
        this.enemies = enemies;
    }

    public WeaponManager getWeaponManager(){
        return weaponManager;
    }
}
