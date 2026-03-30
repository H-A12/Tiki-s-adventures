package com.tikisadventure.entities.base.components;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;

public class TimedComponent implements Component {
    private final float lifeTime;
    private float elapsed = 0;

    public TimedComponent(float seconds) {
        this.lifeTime = seconds;
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {
        // Solo necesitamos que el dueño pueda morir para que el componente funcione
        if (owner instanceof Killable) {
            elapsed += delta;

            if (elapsed >= lifeTime) {
                ((Killable) owner).die();
            }
        }
    }

    @Override
    public void onAttach(Object owner) {
        // Reiniciamos el cronómetro al añadirlo a una nueva entidad
        elapsed = 0;
    }
}
