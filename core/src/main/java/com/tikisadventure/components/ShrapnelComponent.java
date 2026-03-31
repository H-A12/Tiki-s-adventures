package com.tikisadventure.components;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.weapons.ProjectileCreator;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.core.Assets;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ShrapnelComponent implements Component {

    private ProjectileCreator creator;
    private TextureRegion texture;
    private int count;
    private float shrapnelDamage;
    private float size;
    private boolean hasSpawned = false;

    public ShrapnelComponent(ProjectileCreator creator, String atlasName, String regionName, 
                            int count, float damage, float size) {
        this.creator = creator;
        this.count = count;
        this.shrapnelDamage = damage;
        this.size = size;

        this.texture = Assets.getRegion(atlasName, regionName);
        if (this.texture == null) {
            System.err.println("Error cargando textura de metralla: " + atlasName + "/" + regionName);
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

            com.tikisadventure.combat.projectiles.Projectile s = creator.create(
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
