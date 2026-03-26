package com.tikisadventure.projectile.behaviors;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.projectile.Projectile;
import com.tikisadventure.projectile.ProjectileBehavior;

public class ZigZagBehavior implements ProjectileBehavior {

    private float amplitude = 40f;
    private float frequency = 40f;
    private final Vector2 sideStep = new Vector2();

    @Override
    public void update(Projectile p, float delta, Array<Entity> enemies) {
        // 1. Calculamos el lado (perpendicular)
        sideStep.set(-p.getDirection().y, p.getDirection().x);

        // 2. Calculamos la fuerza lateral usando COSENO
        // Usamos Coseno para que la "velocidad lateral" oscile correctamente
        float lateralSpeed = MathUtils.cos(p.getStateTime() * frequency) * amplitude;

        // 3. SOLO aplicamos el movimiento lateral
        p.getPosition().mulAdd(sideStep, lateralSpeed * delta);
    }
}
