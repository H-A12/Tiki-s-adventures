package io.github.some_example_name.personajes.jugables;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import io.github.some_example_name.personajes.Personaje;

public class Tiki extends Personaje {

    static private Texture tikiTextura = new Texture("tiki.png");
    TextureRegion[] regiones = TextureRegion.split(tikiTextura, 18, 26)[0];
    private Animation<TextureRegion> quieto;
    private Animation<TextureRegion> andar;


    public void crearTiki() {
        quieto = new Animation<TextureRegion>(0, regiones[0]);
        andar = new Animation<TextureRegion>(0.15f, regiones[2], regiones[3], regiones[4]);
        andar.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);
        ANCHO = 1 / 16f * regiones[0].getRegionWidth();
        ALTO = 1 / 16f * regiones[0].getRegionHeight();
        velocidad_max = 5;
        vida_max = 10;
        vida = vida_max;
        setAlive();

    }

    public void update(float deltaTime) {
        if (deltaTime == 0)
            return;

        if (deltaTime > 0.1f)
            deltaTime = 0.1f;

        stateTime += deltaTime;

        // Velocidad inicial 0
        velocidad.x = 0;
        velocidad.y = 0;
        actualizarHitboxes();

        // Movimiento horizontal
        if (Gdx.input.isKeyPressed(Keys.A)) {
            velocidad.x = -velocidad_max; // izquierda
            mirarDerecha = false;
            estado = Estado.Andando;
        }
        if (Gdx.input.isKeyPressed(Keys.D)) {
            velocidad.x = velocidad_max; // derecha
            mirarDerecha = true;
            estado = Estado.Andando;
        }

        // Movimiento vertical
        if (Gdx.input.isKeyPressed(Keys.W)) {
            velocidad.y = velocidad_max; // subir
            estado = Estado.Andando;
        }
        if (Gdx.input.isKeyPressed(Keys.S)) {
            velocidad.y = -velocidad_max; // bajar
            estado = Estado.Andando;
        }

        // Actualizar posición usando deltaTime
        posicion.x += velocidad.x * deltaTime;
        posicion.y += velocidad.y * deltaTime;
        actualizarHitboxes();

        if (velocidad.x == 0 && velocidad.y == 0) {
            estado = Estado.Quieto;
        }
    }

    public void render(Batch batch, float deltaTime) {
        stateTime += deltaTime;

        Animation<TextureRegion> animActual = (estado == Estado.Andando) ? andar : quieto;
        TextureRegion frame = animActual.getKeyFrame(stateTime, true);

        if (mirarDerecha) {
            batch.draw(frame, posicion.x, posicion.y, ANCHO, ALTO);
        } else {
            batch.draw(frame, posicion.x + ANCHO, posicion.y, -ANCHO, ALTO);
        }
    }

}
