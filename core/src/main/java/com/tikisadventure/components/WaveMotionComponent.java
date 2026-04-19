package com.tikisadventure.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;

public class WaveMotionComponent implements Component {
    private float amplitude;
    private float frequency;
    private float elapsedTime;
    private Projectile projectile;
    private Vector2 baseDirection;
    private Vector2 perpendicular;
    private float initialAngle;

    public WaveMotionComponent(float amplitude, float frequency) {
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.elapsedTime = 0f;
    }

    @Override
    public void onAttach(Object owner) {
        if (owner instanceof Projectile) {
            this.projectile = (Projectile) owner;
            this.baseDirection = new Vector2(projectile.getDirection());
            this.perpendicular = new Vector2(-baseDirection.y, baseDirection.x);
            this.initialAngle = projectile.getDirection().angleRad();
        }
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {
        if (projectile == null) return;

        elapsedTime += delta;

        float offset = (float) Math.sin(elapsedTime * frequency) * amplitude;

        Vector2 direction = new Vector2(baseDirection);
        float newAngle = initialAngle + (offset * frequency * 0.1f);
        direction.set((float) Math.cos(newAngle), (float) Math.sin(newAngle));
        direction.nor();

        projectile.setDirection(direction);

        Vector2 perpendicularOffset = new Vector2(perpendicular).scl(offset);
        Vector2 newPos = new Vector2(projectile.getPosition()).add(perpendicularOffset);
        projectile.setPosition(newPos);
    }
}