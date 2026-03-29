package com.tikisadventure.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.Weapon;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.player.Player;

public class ShrapnelComponent implements Component {

    private Weapon.ProjectileCreator creator;
    private com.badlogic.gdx.graphics.g2d.TextureRegion texture;
    private int count;
    private float shrapnelDamage;
    private float size;
    private boolean hasSpawned = false;

    public ShrapnelComponent(Weapon.ProjectileCreator creator, String internalPath, 
                            int count, float damage, float size) {
        this.creator = creator;
        this.count = count;
        this.shrapnelDamage = damage;
        this.size = size;

        try {
            com.badlogic.gdx.graphics.Texture temp = new com.badlogic.gdx.graphics.Texture(internalPath);
            this.texture = new com.badlogic.gdx.graphics.g2d.TextureRegion(temp);
        } catch (Exception e) {
            System.err.println("Error cargando textura de metralla: " + internalPath);
        }
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {
        if (!(owner instanceof Killable)) return;

        Killable killable = (Killable) owner;
        
        if (!killable.isAlive() && !hasSpawned) {
            hasSpawned = true;
            spawnShrapnel(owner);
        }
    }

    private void spawnShrapnel(Object owner) {
        if (texture == null) return;
        if (!(owner instanceof HasPosition)) return;

        HasPosition posInterface = (HasPosition) owner;
        Vector2 position = posInterface.getPosition();

        for (int i = 0; i < count; i++) {
            float angle = MathUtils.random(0f, 360f);
            Vector2 dir = new Vector2(1, 0).rotateDeg(angle);
            float speed = MathUtils.random(10f, 22f);

            com.tikisadventure.projectile.Projectile s = creator.create(
                new Vector2(position),
                dir,
                speed,
                this.shrapnelDamage,
                size * MathUtils.random(0.8f, 1.2f),
                this.texture,
                null, null, 0
            );

            s.addBehavior(new StandardPhysicsComponent());
            s.addBehavior(new TimedComponent(0.4f));

            if (owner instanceof HasOwner) {
                Object ownerObj = ((HasOwner) owner).getOwner();
                if (ownerObj instanceof Player) {
                    ((Player) ownerObj).addProjectile(s);
                }
            }
        }
    }

    @Override
    public void onAttach(Object owner) {
        hasSpawned = false;
    }
}
