package com.tikisadventure.entities.player;

import com.tikisadventure.abilities.DashAbility;
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

    private static Texture tikiTextura = new Texture("Blanco.png");
    private TextureRegion[] regiones = TextureRegion.split(tikiTextura, 16, 16)[0];

    private Animation<TextureRegion> quieto;
    private Animation<TextureRegion> andar;

    private WeaponManager weaponManager;
    private DashAbility dash;
    private Array<Entity> enemies;

    public Tiki() {
        quieto = new Animation<>(0, regiones[0]);
        andar = new Animation<>(0.15f, regiones[1], regiones[2], regiones[3]);
        andar.setPlayMode(Animation.PlayMode.LOOP);

        ANCHO = 2;
        ALTO = 2;

        velocidad_max = 5;
        vida_max = 100000;
        vida = vida_max;

        weaponManager = new WeaponManager(this);
        this.dash = new DashAbility(this);
        setAlive();
    }

    @Override
    public void update(float deltaTime, Entity player) {
        if (deltaTime == 0) return;
        if (deltaTime > 0.1f) deltaTime = 0.1f;

        // --- 1. ACTUALIZAR CRONÓMETROS DE DAÑO ---
        if (this.damageFlashTimer > 0) {
            this.damageFlashTimer -= deltaTime;
        }

        // ¡NUEVO!: Restar el tiempo de invulnerabilidad post-daño
        if (this.invulnerableTimer > 0) {
            this.invulnerableTimer -= deltaTime;
        }

        stateTime += deltaTime;

        // --- 2. MOVIMIENTO ---
        velocidad.set(0, 0);

        if (Gdx.input.isKeyPressed(Keys.A)) {
            velocidad.x = -velocidad_max;
            mirarDerecha = false;
        }
        if (Gdx.input.isKeyPressed(Keys.D)) {
            velocidad.x = velocidad_max;
            mirarDerecha = true;
        }
        if (Gdx.input.isKeyPressed(Keys.W)) velocidad.y = velocidad_max;
        if (Gdx.input.isKeyPressed(Keys.S)) velocidad.y = -velocidad_max;

        estado = (velocidad.x != 0 || velocidad.y != 0) ? Estado.Andando : Estado.Quieto;

        // --- 3. DASH Y POSICIÓN ---
        dash.update(deltaTime);
        if (Gdx.input.isKeyJustPressed(Keys.SPACE)) {
            dash.activate();
        }

        posicion.x += velocidad.x * deltaTime;
        posicion.y += velocidad.y * deltaTime;

        // --- 4. ACTUALIZACIONES SECUNDARIAS ---
        weaponManager.update(deltaTime, enemies);
        actualizarHitboxes();
    }

    @Override
    public void render(Batch batch, float delta) {
        TextureRegion frame;

        switch (estado) {
            case Quieto:  frame = quieto.getKeyFrame(stateTime); break;
            case Andando: frame = andar.getKeyFrame(stateTime); break;
            default:      frame = quieto.getKeyFrame(stateTime);
        }

        com.badlogic.gdx.graphics.Color originalColor = batch.getColor().cpy();

        // 1. DIBUJAR ESTELA DEL DASH
        for (com.tikisadventure.abilities.DashAbility.DashGhost ghost : dash.getGhosts()) {
            float alpha = (ghost.lifetime / 0.35f) * 0.6f;
            batch.setColor(0.4f, 0.7f, 1f, alpha);
            if (ghost.mirarDerecha) {
                batch.draw(frame, ghost.pos.x, ghost.pos.y, ANCHO, ALTO);
            } else {
                batch.draw(frame, ghost.pos.x + ANCHO, ghost.pos.y, -ANCHO, ALTO);
            }
        }

        // 2. SELECCIÓN DE COLOR (Daño > Dash > Normal)
        batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);

        if (this.damageFlashTimer > 0) {
            batch.setColor(1f, 0f, 0f, 1f); // ROJO
        } else if (isInvulnerable) {
            batch.setColor(0.7f, 0.8f, 1f, 0.9f); // AZUL (Dash)
        }

        // 3. EFECTO DE PARPADEO SI ES INVULNERABLE TRAS GOLPE
        if (this.invulnerableTimer > 0 && this.damageFlashTimer <= 0) {
            // Parpadeo rápido usando el seno del tiempo
            float alpha = (Math.sin(stateTime * 25f) > 0) ? 1f : 0.4f;
            com.badlogic.gdx.graphics.Color actual = batch.getColor();
            batch.setColor(actual.r, actual.g, actual.b, alpha);
        }

        // 4. DIBUJAR PERSONAJE
        if (mirarDerecha) {
            batch.draw(frame, posicion.x, posicion.y, ANCHO, ALTO);
        } else {
            batch.draw(frame, posicion.x + ANCHO, posicion.y, -ANCHO, ALTO);
        }

        // 5. LIMPIEZA
        batch.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        weaponManager.render(batch);
        batch.setColor(originalColor);
    }

    public void setEnemies(Array<Entity> enemies) { this.enemies = enemies; }
    public WeaponManager getWeaponManager() { return weaponManager; }
    public DashAbility getDashAbility() {
        return dash;
    }
}
