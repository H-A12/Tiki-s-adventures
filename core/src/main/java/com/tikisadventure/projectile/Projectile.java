package com.tikisadventure.projectile;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;

public class Projectile {
    public Vector2 position = new Vector2();
    public Vector2 direction = new Vector2();
    public float speed;
    public float damage;
    public float radius;
    public float stateTime = 0;
    public boolean alive = true;

    public Entity owner;
    public TextureRegion sprite;

    // La lista de "bloques de Lego"
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

    public void addBehavior(ProjectileBehavior b) { behaviors.add(b); }

    public void update(float delta, Array<Entity> enemies) {
        stateTime += delta;
        // Ejecutamos todos los comportamientos inyectados
        for (ProjectileBehavior b : behaviors) {
            b.update(this, delta, enemies);
        }
    }

    public void render(Batch batch) {
        if (!alive || sprite == null) return;
        batch.draw(sprite, position.x - radius, position.y - radius, radius * 2, radius * 2);
    }
}
