package com.tikisadventure.combat.weapons.behaviors;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.weapons.ProjectileCreator;
import com.tikisadventure.components.StandardPhysicsComponent;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ShotgunBehavior implements AttackBehavior {

    private ProjectileCreator factory;
    private TextureRegion projectileTexture;
    private int pellets = 6;
    private float spreadAngle = 35f;
    private float speedVariation = 3f;
    private float bulletSpeed;
    private float damage;
    private float bulletSize;

    public ShotgunBehavior(ProjectileCreator factory, TextureRegion texture, float speed, float damage, float size) {
        this.factory = factory;
        this.projectileTexture = texture;
        this.bulletSpeed = speed;
        this.damage = damage;
        this.bulletSize = size;
    }

    @Override
    public void execute(Entity owner, Entity target, Vector2 worldPosition, EffectManager em) {
        if (target == null) return;

        Vector2 baseDir = new Vector2(target.getPosicion()).sub(worldPosition).nor();
        float baseAngle = baseDir.angleDeg();

        if (em != null) {
            em.spawnEffect(EffectType.CASQUILLO_ESCOPETA, worldPosition, baseDir);
        }

        for (int i = 0; i < pellets; i++) {
            float randomAngle = baseAngle + MathUtils.random(-spreadAngle / 2f, spreadAngle / 2f);
            Vector2 pelletDir = new Vector2(1, 0).setAngleDeg(randomAngle);
            float randomSpeed = bulletSpeed + MathUtils.random(-speedVariation, speedVariation);

            Projectile p = factory.create(
                new Vector2(worldPosition),
                pelletDir,
                randomSpeed,
                damage,
                bulletSize,
                projectileTexture,
                em,
                null,
                0f
            );

            p.addComponent(new StandardPhysicsComponent());

            if (owner instanceof Player) {
                ((Player) owner).addProjectile(p);
            }
        }
    }

    @Override
    public void update(float delta) {}
}
