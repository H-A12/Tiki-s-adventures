package com.tikisadventure.combat.weapons.types;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.core.Assets;
import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.combat.weapons.ProjectileCreator;
import com.tikisadventure.combat.weapons.behaviors.AttackBehavior;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.components.StandardPhysicsComponent;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

public class LaserGun extends Weapon {

    public LaserGun(Entity owner, ProjectileCreator factory, EffectManager effectManager) {
        super(owner, new LaserBehavior(factory), effectManager);
        this.sprite = Assets.getRegion("LaserGun");

        this.cd = 0.8f;
        this.shootRange = 12f;
    }

    private static class LaserBehavior implements AttackBehavior {
        private ProjectileCreator factory;

        public LaserBehavior(ProjectileCreator factory) {
            this.factory = factory;
        }

        @Override
        public void execute(Entity owner, Entity target, Vector2 worldPosition, EffectManager em) {
            if (target == null) return;
            Vector2 dir = new Vector2(target.getPosicion()).sub(worldPosition).nor();
            dir.rotateDeg(MathUtils.random(-5f, 5f));

            if (em != null) {
                em.spawnEffect(EffectType.CASQUILLO_PISTOLA, worldPosition, dir);
            }

            Projectile p = factory.create(
                new Vector2(worldPosition),
                dir, 10f, 20f, 0.7f,
                Assets.getRegion("BlueLaser"),
                em,
                EffectType.TRAIL_LASER,
                0.01f
            );

            p.addComponent(new StandardPhysicsComponent());
            if (owner instanceof Player) {
                ((Player) owner).addProjectile(p);
            }
        }

        @Override
        public void update(float delta) {}
    }
}
