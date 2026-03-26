package com.tikisadventure.projectile;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;

public class Projectile {
    private Vector2 position = new Vector2();
    private Vector2 direction = new Vector2();
    private float speed;
    private float damage;
    private float radius; // Ahora mutable vía Setter
    private float stateTime = 0;
    private boolean alive = true;

    private Entity owner;
    private TextureRegion sprite;

    private Array<ProjectileBehavior> behaviors = new Array<>();

    public Projectile(Entity owner, Vector2 pos, Vector2 dir, float speed, float dmg, float radius, TextureRegion sprite) {
        this.owner = owner;
        this.position.set(pos);
        this.direction.set(dir).nor();
        this.speed = speed;
        this.damage = dmg;
        this.radius = radius;
        this.sprite = sprite;
    }

    // --- LÓGICA ---

    public void addBehavior(ProjectileBehavior b) {
        behaviors.add(b);
    }

    public void update(float delta, Array<Entity> enemies) {
        if (!alive) return;
        stateTime += delta;

        for (ProjectileBehavior b : behaviors) {
            b.update(this, delta, enemies);
        }
    }

    public void render(Batch batch) {
        if (!alive || sprite == null) return;

        // Renderizado dinámico: usa el radio actual (que puede haber cambiado por un Behavior)
        batch.draw(
            sprite,
            position.x - radius,
            position.y - radius,
            radius * 2,
            radius * 2
        );
    }

    public void die() {
        this.alive = false;
    }

    // --- GETTERS & SETTERS ---

    public Vector2 getPosition() { return position; }
    public Vector2 getDirection() { return direction; }
    public float getSpeed() { return speed; }
    public float getDamage() { return damage; }
    public float getRadius() { return radius; }

    // CAMBIO: Nuevo Setter para permitir que los Behaviors cambien el tamaño
    public void setRadius(float radius) {
        this.radius = Math.max(0.01f, radius);
    }

    public Entity getOwner() { return owner; }
    public boolean isAlive() { return alive; }
    public float getStateTime() { return stateTime; }

    public <T extends ProjectileBehavior> T getBehavior(Class<T> type) {
        for (ProjectileBehavior b : behaviors) {
            if (type.isInstance(b)) return type.cast(b);
        }
        return null;
    }
}
