package com.tikisadventure.entities.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.tikisadventure.core.Assets;
import com.tikisadventure.components.HealthComponent;
import com.tikisadventure.components.RenderComponent;
import com.tikisadventure.entities.base.Entity;

public class Slime extends Entity {
    // Usamos static para no cargar la textura mil veces si hay mil slimes
    static private TextureRegion slimeTextura;
    private TextureRegion[] regiones;
    private Animation<TextureRegion> quieto;
    private Animation<TextureRegion> andar;

    public void crearSlime() {
        if (slimeTextura == null) {
            slimeTextura = Assets.getRegion("shared", "slime");
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

        // --- Ajuste de Stats (Usando Componentes) ---
        renderComponent = new RenderComponent(null, 1f, 1f);
        this.velocityComponent.speed = 2.5f;
        setDamage(2);
        this.healthComponent = new HealthComponent(3);
        setExperience(5);
    }

    @Override
    public void update(float deltaTime, Entity jugador) {
        super.update(deltaTime);
        if (!isAlive() || jugador == null) return;

        applyKnockback(deltaTime);

        // Vector hacia el jugador usando los métodos de Vector2 (más limpio)
        // destino.sub(origen)
        velocityComponent.velocidad.set(jugador.getPosition()).sub(positionComponent.posicion);

        float distancia = velocityComponent.velocidad.len();

        if (distancia > 0.1f) { // Evita que el slime "tiemble" encima del jugador
            velocityComponent.velocidad.nor().scl(velocityComponent.speed); // Normalizar y escalar por la velocidad

            setEstado(Estado.walking);
            setMirarDerecha(velocityComponent.velocidad.x >= 0);
        } else {
            velocityComponent.velocidad.setZero();
            setEstado(Estado.idle);
        }

        actualizarHitboxes();
    }

    @Override
    public void draw(Batch batch, float deltaTime) {
        TextureRegion frame;
        if (getEstado() == Estado.walking) {
            frame = andar.getKeyFrame(getStateTime());
        } else {
            frame = quieto.getKeyFrame(getStateTime());
        }

        // Dibujar centrado en la posición
        float x = positionComponent.posicion.x - getANCHO() / 2;
        float y = positionComponent.posicion.y - getALTO() / 2;

        if (isMirarDerecha()) {
            batch.draw(frame, x, y, getANCHO(), getALTO());
        } else {
            // Flip horizontal si mira a la izquierda
            batch.draw(frame, x + getANCHO(), y, -getANCHO(), getALTO());
        }
    }
}
