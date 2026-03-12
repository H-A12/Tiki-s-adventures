package com.tikisadventure.entities.enemies;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.tikisadventure.entities.Entity;

public class Slime extends Entity {
    static private Texture SlimeTextura = new Texture("slime.png");
    TextureRegion[] regiones = TextureRegion.split(SlimeTextura, 16, 16)[0];
    private Animation<TextureRegion> quieto;
    private Animation<TextureRegion> andar;
    private float rotacion;



    public void crearSlime() {

        quieto = new Animation<TextureRegion>(0, regiones[0]);
        andar = new Animation<TextureRegion>(0.15f, regiones[0], regiones[1]);
        andar.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

        ANCHO = 1 / 16f * regiones[0].getRegionWidth();
        ALTO = 1 / 16f * regiones[0].getRegionHeight();
        velocidad_max = 2.5f;
        danyo = 2;
        vida_max = 3;
        vida = vida_max;
        setAlive();
    }


    public void update(float deltaTime, Entity jugador) {
        if (deltaTime == 0) return;
        if (deltaTime > 0.1f) deltaTime = 0.1f;

        stateTime += deltaTime;

        // vector hacia el jugador
        float dx = jugador.getPosicion().x - posicion.x;
        float dy = jugador.getPosicion().y - posicion.y;
        float distancia = (float)Math.sqrt(dx*dx + dy*dy);

        if (distancia > 0) {
            // normalizar
            dx /= distancia;
            dy /= distancia;

            // velocidad
            velocidad.x = dx * velocidad_max;
            velocidad.y = dy * velocidad_max;

            actualizarHitboxes();

            // calcular ángulo en grados (0° = derecha)
            rotacion = (float)Math.toDegrees(Math.atan2(dy, dx));
        } else {
            velocidad.x = 0;
            velocidad.y = 0;
            actualizarHitboxes();
        }

        // actualizar posición
        posicion.x += velocidad.x * deltaTime;
        posicion.y += velocidad.y * deltaTime;

        // animación
        if (velocidad.x != 0 || velocidad.y != 0) {
            estado = Estado.Andando;
        } else {
            estado = Estado.Quieto;
        }
    }

    public void render(Batch batch, float deltaTime) {
        TextureRegion frame;
        switch (estado) {
            case Quieto: frame = quieto.getKeyFrame(stateTime); break;
            case Andando: frame = andar.getKeyFrame(stateTime); break;
            default: frame = quieto.getKeyFrame(stateTime); break;
        }

        // invertir sprite según velocidad x
        boolean mirarDerecha = velocidad.x >= 0;

        if (mirarDerecha) {
            batch.draw(frame, posicion.x, posicion.y, ANCHO, ALTO);
        } else {
            batch.draw(frame, posicion.x + ANCHO, posicion.y, -ANCHO, ALTO);
        }
    }



}

