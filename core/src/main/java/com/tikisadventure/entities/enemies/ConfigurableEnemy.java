package com.tikisadventure.entities.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.tikisadventure.entities.base.Entity;

public class ConfigurableEnemy extends Entity {

    private Animation<TextureRegion> idleAnimation;
    private Animation<TextureRegion> walkAnimation;
    private Animation<TextureRegion> deathAnimation; // Añadida para el estado dead
    private String aiType = "chaser";

    public ConfigurableEnemy(float x, float y, Animation<TextureRegion> idle, Animation<TextureRegion> walk) {
        super(x, y);
        this.idleAnimation = idle;
        this.walkAnimation = walk;
        this.estado = Estado.idle;

        // Dimensiones base por defecto
        this.ANCHO = 1f;
        this.ALTO = 1f;
    }

    /**
     * IMPLEMENTACIÓN OBLIGATORIA: Conecta el estado del AnimationSystem con las animaciones.
     */
    @Override
    public Animation<TextureRegion> getAnimationForState(Estado estado) {
        switch (estado) {
            case dead:
                return deathAnimation != null ? deathAnimation : idleAnimation;
            case walking:
            case walking_side:
            case walking_up:
            case walking_down:
                return walkAnimation != null ? walkAnimation : idleAnimation;
            case idle:
            default:
                return idleAnimation;
        }
    }

    public String getAiType() { return aiType; }
    public void setAiType(String aiType) { this.aiType = aiType; }
    public void setDeathAnimation(Animation<TextureRegion> anim) { this.deathAnimation = anim; }

    @Override
    public void update(float delta) {
        if (!alive && estado != Estado.dead) return;

        // 1. Actualizar timers e invulnerabilidad heredados de Entity
        updateTimers(delta);

        // 2. Movimiento: Aplicamos velocidad si está vivo
        if (alive) {
            posicion.mulAdd(velocidad, speed * delta);
        }

        // 3. Físicas: Retroceso por golpes
        applyKnockback(delta);

        // 4. Sincronizar hitboxes
        actualizarHitboxes();

        stateTime += delta;

        // 5. El AnimationSystem se encargará de cambiar el 'estado' y el 'sprite',
        // pero mantenemos esta lógica interna como respaldo o para la IA:
        if (alive && velocidad.len2() > 0.01f) {
            if (Math.abs(velocidad.x) > 0.1f) {
                mirarDerecha = velocidad.x > 0;
            }
        }
    }

    @Override
    public void render(Batch batch, float delta) {
        // Usamos el 'sprite' inyectado por AnimationSystem.
        // Si es nulo, intentamos obtener un frame manualmente como respaldo.
        TextureRegion frame = sprite;
        if (frame == null && idleAnimation != null) {
            frame = idleAnimation.getKeyFrame(stateTime, true);
        }

        if (frame == null) return;

        // Feedback visual de daño (Tinte rojizo)
        if (isInvulnerable) {
            batch.setColor(1, 0.5f, 0.5f, 0.8f);
        }

        // Control de orientación (Flip horizontal)
        boolean flip = !mirarDerecha;

        batch.draw(frame,
            posicion.x - ANCHO / 2f + (flip ? ANCHO : 0),
            posicion.y - ALTO / 2f,
            flip ? -ANCHO : ANCHO,
            ALTO);

        // Resetear color
        if (isInvulnerable) batch.setColor(1, 1, 1, 1);
    }
}
