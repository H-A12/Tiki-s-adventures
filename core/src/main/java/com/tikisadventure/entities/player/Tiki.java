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
import com.tikisadventure.systems.ExperienceSystem;

public class Tiki extends Entity {

    private static Texture tikiTextura = new Texture("tiki.png");

    private TextureRegion[] regiones = TextureRegion.split(tikiTextura, 16, 16)[0];

    private Animation<TextureRegion> quieto;
    private Animation<TextureRegion> andar;
    private WeaponManager weaponManager;

    private Array<Entity> enemies;

    private ExperienceSystem experienceSystem = new ExperienceSystem();

    public Tiki() {

        quieto = new Animation<>(0, regiones[0]);

        andar = new Animation<>(0.15f,
            regiones[1],
            regiones[2]);

        andar.setPlayMode(Animation.PlayMode.LOOP);

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

        boolean movingLeft = Gdx.input.isKeyPressed(Keys.A);
        boolean movingRight = Gdx.input.isKeyPressed(Keys.D);
        boolean movingUp = Gdx.input.isKeyPressed(Keys.W);
        boolean movingDown = Gdx.input.isKeyPressed(Keys.S);

        // Movimiento horizontal - si ambas presionadas, se anulan
        if (movingLeft && movingRight) {
            velocidad.x = 0;
        } else if (movingLeft) {
            velocidad.x = -velocidad_max;
            mirarDerecha = false;
            estado = Estado.Andando;
        } else if (movingRight) {
            velocidad.x = velocidad_max;
            mirarDerecha = true;
            estado = Estado.Andando;
        }

        // Movimiento vertical - si ambas presionadas, se anulan
        if (movingUp && movingDown) {
            velocidad.y = 0;
        } else if (movingUp) {
            velocidad.y = velocidad_max;
            estado = Estado.Andando;
        } else if (movingDown) {
            velocidad.y = -velocidad_max;
            estado = Estado.Andando;
        }

        // Normalizar velocidad en diagonal
        if (velocidad.x != 0 && velocidad.y != 0) {
            float len = velocidad.len();
            velocidad.x = (velocidad.x / len) * velocidad_max;
            velocidad.y = (velocidad.y / len) * velocidad_max;
        }

        posicion.x += velocidad.x * deltaTime;
        posicion.y += velocidad.y * deltaTime;

        actualizarHitboxes();

        if (velocidad.x == 0 && velocidad.y == 0) {
            estado = Estado.Quieto;
        }
    }

    public void updateWeapons(float deltaTime) {
        weaponManager.update(deltaTime, enemies);
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

    public ExperienceSystem getExperienceSystem(){
        return experienceSystem;
    }
}
