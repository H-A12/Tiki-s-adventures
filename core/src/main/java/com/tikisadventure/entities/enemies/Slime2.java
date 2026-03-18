package com.tikisadventure.entities.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.tikisadventure.entities.Entity;

public class Slime2 extends Entity {
    static private Texture SlimeTextura = new Texture("SlimeSprite.png");
    TextureRegion[] regiones = TextureRegion.split(SlimeTextura, 16, 16)[0];
    private Animation<TextureRegion> quieto;
    private Animation<TextureRegion> andar;
    // La rotación se puede usar para efectos visuales más adelante
    private float rotacion;

    public void crearSlime() {
        quieto = new Animation<TextureRegion>(0, regiones[0]);
        andar = new Animation<TextureRegion>(0.15f, regiones[0], regiones[1], regiones[2], regiones[3]);
        andar.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

        ANCHO = 1 / 16f * regiones[0].getRegionWidth();
        ALTO = 1 / 16f * regiones[0].getRegionHeight();
        velocidad_max = 2.5f;
        danyo = 20;
        vida_max = 20;
        vida = vida_max;

        this.POST_DAMAGE_INVULNERABILITY = 0.5f;

        setExperience(5);
        setAlive();
    }

    @Override
    public void update(float deltaTime, Entity jugador) {
        if (deltaTime == 0) return;
        if (deltaTime > 0.1f) deltaTime = 0.1f;

        // --- 1. ACTUALIZAR TIMERS DE LA ENTIDAD (Heredados de Entity) ---
        if (this.damageFlashTimer > 0) {
            this.damageFlashTimer -= deltaTime;
        }
        // IMPORTANTE: Sin esto, el Slime será inmune para siempre tras el primer golpe
        if (this.invulnerableTimer > 0) {
            this.invulnerableTimer -= deltaTime;
        }

        stateTime += deltaTime;

        // Vector hacia el jugador
        float dx = jugador.getPosicion().x - posicion.x;
        float dy = jugador.getPosicion().y - posicion.y;
        float distancia = (float)Math.sqrt(dx*dx + dy*dy);

        // --- 2. LÓGICA DE ATAQUE ---
        // Nota: El daño ahora lo gestionan los I-Frames de Tiki en su receiveDamage
        if (distancia < 0.8f && jugador.isAlive()) {
            jugador.receiveDamage(this.danyo);
        }

        // Movimiento IA simple
        if (distancia > 0.1f) {
            dx /= distancia;
            dy /= distancia;
            velocidad.x = dx * velocidad_max;
            velocidad.y = dy * velocidad_max;

            rotacion = (float)Math.toDegrees(Math.atan2(dy, dx));
        } else {
            velocidad.set(0, 0);
        }

        posicion.x += velocidad.x * deltaTime;
        posicion.y += velocidad.y * deltaTime;

        actualizarHitboxes();

        estado = (velocidad.x != 0 || velocidad.y != 0) ? Estado.walking : Estado.standing;
    }

    @Override
    public void render(Batch batch, float deltaTime) {
        TextureRegion frame;
        switch (estado) {
            case standing: frame = quieto.getKeyFrame(stateTime); break;
            case walking: frame = andar.getKeyFrame(stateTime); break;
            default: frame = quieto.getKeyFrame(stateTime); break;
        }

        com.badlogic.gdx.graphics.Color originalColor = batch.getColor().cpy();

        // --- 3. FEEDBACK VISUAL ---
        if (this.damageFlashTimer > 0) {
            batch.setColor(1f, 0.3f, 0.3f, 1f); // Rojo si recibe daño

        }

        boolean mirarDerecha = velocidad.x >= 0;
        if (mirarDerecha) {
            batch.draw(frame, posicion.x, posicion.y, ANCHO, ALTO);
        } else {
            batch.draw(frame, posicion.x + ANCHO, posicion.y, -ANCHO, ALTO);
        }

        batch.setColor(originalColor);
    }

}
