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

public class MachineGunBehavior implements AttackBehavior {

    private ProjectileCreator factory;
    private TextureRegion projectileTexture;
    private float speed;
    private float damage;
    private float size;
    private int bulletsPerBurst = 3;
    private float timeBetweenBullets = 0.07f;
    private float spreadAngle = 8f;

    private int bulletsShotInCurrentBurst = 0;
    private float burstTimer = 0;
    private boolean isBursting = false;
    private Vector2 burstDirection = new Vector2();
    private Entity owner;
    private Entity target;
    private Vector2 worldPosition;
    private EffectManager em;

    public MachineGunBehavior(ProjectileCreator factory, TextureRegion texture, float speed, float damage, float size) {
        this.factory = factory;
        this.projectileTexture = texture;
        this.speed = speed;
        this.damage = damage;
        this.size = size;
    }

    @Override
    public void execute(Entity owner, Entity target, Vector2 worldPosition, EffectManager em) {
        this.owner = owner;
        this.target = target;
        this.worldPosition = worldPosition;
        this.em = em;

        if (!isBursting && target != null) {
            isBursting = true;
            bulletsShotInCurrentBurst = 0;
            burstTimer = timeBetweenBullets;
            burstDirection.set(target.getPosicion()).sub(worldPosition).nor();
        }
    }

    @Override
    public void update(float delta) {
        if (isBursting) {
            burstTimer += delta;
            if (burstTimer >= timeBetweenBullets) {
                fireSingleBullet();
                burstTimer = 0;
            }
        }
    }

    private void fireSingleBullet() {
        if (target != null && target.isAlive()) {
            burstDirection.set(target.getPosicion()).sub(worldPosition).nor();
        }

        if (em != null) {
            em.spawnEffect(EffectType.CASQUILLO_PISTOLA, worldPosition, burstDirection);
        }

        Vector2 finalDir = new Vector2(burstDirection);
        float randomOffset = MathUtils.random(-spreadAngle / 2f, spreadAngle / 2f);
        finalDir.rotateDeg(randomOffset);

        Projectile p = factory.create(
            new Vector2(worldPosition),
            finalDir,
            speed,
            damage,
            size,
            projectileTexture,
            em,
            null,
            0f
        );
        p.addComponent(new StandardPhysicsComponent());
        if (owner instanceof Player) {
            ((Player) owner).addProjectile(p);
        }

        bulletsShotInCurrentBurst++;
        if (bulletsShotInCurrentBurst >= bulletsPerBurst) {
            isBursting = false;
        }
    }
}
