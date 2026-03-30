package com.tikisadventure.entities.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.tikisadventure.core.Assets;
import com.tikisadventure.entities.base.Entity;

public class Slime extends Entity {
    // Usamos static para no cargar la textura mil veces si hay mil slimes
    static private TextureRegion slimeTextura;
    private TextureRegion[] regiones;
    private Animation<TextureRegion> quieto;
    private Animation<TextureRegion> andar;

    public void crearSlime() {
        if (slimeTextura == null) {
            slimeTextura = Assets.getRegion("slime");
        }

        // Split the region itself
        int frameSize = 16;
        int frameCount = slimeTextura.getRegionWidth() / frameSize;
        regiones = new TextureRegion[frameCount];
        for (int i = 0; i < frameCount; i++) {
            regiones[i] = new TextureRegion(slimeTextura, i * frameSize, 0, frameSize, frameSize);
        }
        quieto = new Animation<>(0.1f, regiones[0]);
        andar = new Animation<>(0.15f, regiones[0], regiones[1]);
        andar.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

        // --- Ajuste de Stats (Usando los nombres de Entity) ---
        this.ANCHO = 1f; // Tamaño estándar en metros/unidades
        this.ALTO = 1f;
        this.speed = 2.5f;     // Antes velocidad_max
        this.danyo = 2;
        this.vida_max = 3;
        this.vida = vida_max;
        this.experience = 5;
        this.alive = true;
    }

    @Override
    public void update(float deltaTime, Entity jugador) {
        if (!alive || jugador == null) return;

        applyKnockback(deltaTime);
        stateTime += deltaTime;

        // Vector hacia el jugador usando los métodos de Vector2 (más limpio)
        // destino.sub(origen)
        velocidad.set(jugador.getPosicion()).sub(posicion);

        float distancia = velocidad.len();

        if (distancia > 0.1f) { // Evita que el slime "tiemble" encima del jugador
            velocidad.nor().scl(speed); // Normalizar y escalar por la velocidad

            // Actualizar posición: posicion.mulAdd(velocidad, delta)
            posicion.mulAdd(velocidad, deltaTime);

            estado = Estado.walking;
            mirarDerecha = velocidad.x >= 0;
        } else {
            velocidad.setZero();
            estado = Estado.idle;
        }

        actualizarHitboxes();
    }

    @Override
    public void render(Batch batch, float deltaTime) {
        if (!alive) return;

        TextureRegion frame;
        if (estado == Estado.walking) {
            frame = andar.getKeyFrame(stateTime);
        } else {
            frame = quieto.getKeyFrame(stateTime);
        }

        // Dibujar centrado en la posición
        float x = posicion.x - ANCHO / 2;
        float y = posicion.y - ALTO / 2;

        if (mirarDerecha) {
            batch.draw(frame, x, y, ANCHO, ALTO);
        } else {
            // Flip horizontal si mira a la izquierda
            batch.draw(frame, x + ANCHO, y, -ANCHO, ALTO);
        }
    }
}
