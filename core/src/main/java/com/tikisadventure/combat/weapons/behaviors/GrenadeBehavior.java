package com.tikisadventure.combat.weapons.behaviors;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.weapons.ProjectileCreator;
import com.tikisadventure.components.ExplosiveComponent;
import com.tikisadventure.components.GrenadeComponent;
import com.tikisadventure.components.ShrapnelComponent;
import com.tikisadventure.components.StandardPhysicsComponent;
import com.tikisadventure.core.Assets;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

public class GrenadeBehavior implements AttackBehavior {
    private ProjectileCreator factory;

    public GrenadeBehavior(ProjectileCreator factory) {
        this.factory = factory;
    }

    @Override
    public void execute(Entity owner, Entity target, Vector2 worldPosition, EffectManager em) {
        if (target == null) return;
        Vector2 baseDir = new Vector2(target.getPosicion()).sub(worldPosition).nor();
        Vector2 shotDir = new Vector2(baseDir).rotateDeg(MathUtils.random(-5f, 5f));

        Projectile p = factory.create(
            new Vector2(worldPosition),
            shotDir, 5f, 0f, 0.4f,
            Assets.getRegion("Bomb"),
            em,
            null, 0f
        );

        p.addComponent(new StandardPhysicsComponent());
        p.addComponent(new GrenadeComponent(1.4f, 0.7f, 2, 0.75f, 4f));
        p.addComponent(new ExplosiveComponent(em, 0f, 5f, 60f, 15, 20));
        p.addComponent(new ShrapnelComponent(factory, "YellowBullet", 20, 2f, 0.2f));

        if (owner instanceof Player) {
            ((Player) owner).addProjectile(p);
        }
    }

    @Override
    public void update(float delta) {}
}
