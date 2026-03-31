package com.tikisadventure.combat.weapons.behaviors;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.weapons.ProjectileCreator;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

public class ProjectileBehavior implements AttackBehavior {

    private ProjectileCreator factory;
    private TextureRegion projectileTexture;
    private float speed;
    private float damage;
    private float size;
    private EffectType trailType;
    private float trailInterval;

    public ProjectileBehavior(ProjectileCreator factory, TextureRegion texture, float speed, 
                             float damage, float size, EffectType trailType, float trailInterval) {
        this.factory = factory;
        this.projectileTexture = texture;
        this.speed = speed;
        this.damage = damage;
        this.size = size;
        this.trailType = trailType;
        this.trailInterval = trailInterval;
    }

    @Override
    public void execute(Entity owner, Entity target, Vector2 worldPosition, EffectManager em) {
        if (target == null) return;

        Vector2 dir = new Vector2(target.getPosicion()).sub(worldPosition).nor();

        Projectile p = factory.create(
            new Vector2(worldPosition),
            dir,
            speed,
            damage,
            size,
            projectileTexture,
            em,
            trailType,
            trailInterval
        );

        if (owner instanceof Player) {
            ((Player) owner).addProjectile(p);
        }
    }

    @Override
    public void update(float delta) {
        // Projectiles themselves don't need behavior updates here
    }
}
