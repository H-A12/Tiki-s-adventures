package com.tikisadventure.systems;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.base.Entity.Estado;

public class AnimationSystem {

    private final float MOVE_THRESHOLD = 0.15f;
    private final float MOVE_THRESHOLD_SQ = MOVE_THRESHOLD * MOVE_THRESHOLD;

    public void update(Array<Entity> entities, float delta) {
        for (Entity e : entities) {
            // 1. Si la entidad está muerta, forzamos estado dead
            if (!e.isAlive()) {
                e.setEstado(Estado.dead);
                actualizarFrame(e, delta);
                continue;
            }

            // 2. Determinar el estado visual basado en la velocidad
            determinarEstadoDireccional(e);

            // 3. Actualizar tiempo y frame
            actualizarFrame(e, delta);
        }
    }

    private void determinarEstadoDireccional(Entity e) {
        float vx = e.getVelocidad().x;
        float vy = e.getVelocidad().y;
        float speedSq = e.getVelocidad().len2();

        if (speedSq < MOVE_THRESHOLD_SQ) {
            e.setEstado(Estado.idle);
            return;
        }

        if (Math.abs(vx) > Math.abs(vy)) {
            e.setEstado(Estado.walking_side);
            if (vx > 0) e.setMirarDerecha(true);
            else if (vx < 0) e.setMirarDerecha(false);
        } else {
            if (vy > 0) {
                e.setEstado(Estado.walking_up);
            } else {
                e.setEstado(Estado.walking_down);
            }
        }
    }

    private void actualizarFrame(Entity e, float delta) {
        e.addStateTime(delta);
        Animation<TextureRegion> anim = e.getAnimationForState(e.getEstado());

        if (anim != null) {
            // No hacemos loop si está muerto para que se quede en el último frame (el suelo)
            boolean looping = (e.getEstado() != Estado.dead);
            e.setSprite(anim.getKeyFrame(e.getStateTime(), looping));
        }
    }
}
