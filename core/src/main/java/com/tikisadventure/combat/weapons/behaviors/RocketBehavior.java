package com.tikisadventure.combat.weapons.behaviors;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.weapons.ProjectileCreator;
import com.tikisadventure.components.ExplosiveComponent;
import com.tikisadventure.components.StandardPhysicsComponent;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class RocketBehavior implements AttackBehavior {

    private ProjectileCreator factory;
    private TextureRegion projectileTexture;
    private float speed = 5f;
    private float damage = 40f;
    private float size = 0.4f;
    private float spreadAngle = 10f;

    public RocketBehavior(ProjectileCreator factory, TextureRegion texture) {
        this.factory = factory;
        this.projectileTexture = texture;
    }

    @Override
    public void execute(Entity owner, Entity target, Vector2 worldPosition, EffectManager em) {
        if (target == null) return;

        Vector2 baseDir = new Vector2(target.getPosicion()).sub(worldPosition).nor();
        Vector2 shotDir = new Vector2(baseDir).rotateDeg(MathUtils.random(-spreadAngle / 2f, spreadAngle / 2f));

        Projectile p = factory.create(
            new Vector2(worldPosition),
            shotDir,
            speed,
            damage,
            size,
            projectileTexture,
            em,
            EffectType.TRAIL_SMOKE,
            0.2f
        );

        p.addComponent(new StandardPhysicsComponent());
        p.addComponent(new ExplosiveComponent(em, 15.0f, 3.0f, 2.0f, 10, 25));
        
        if (owner instanceof Player) {
            ((Player) owner).addProjectile(p);
        }
    }

    @Override
    public void update(float delta) {}
}
