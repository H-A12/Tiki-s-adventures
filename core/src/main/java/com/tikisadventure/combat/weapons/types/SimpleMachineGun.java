package com.tikisadventure.combat.weapons.types;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.core.Assets;
import com.tikisadventure.components.StandardPhysicsComponent;
import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;

public class SimpleMachineGun extends Weapon {

    private int bulletsPerBurst = 3;
    private float timeBetweenBullets = 0.07f;
    private float spreadAngle = 8f;

    private int bulletsShotInCurrentBurst = 0;
    private float burstTimer = 0;
    private boolean isBursting = false;
    private Vector2 burstDirection = new Vector2();

    public SimpleMachineGun(Entity owner, Weapon.ProjectileCreator factory, EffectManager effectManager) {
        super(owner, factory, Assets.getRegion("GreenBullet"), effectManager);
        this.sprite = Assets.getRegion("Machinegun");

        this.cd = 0.9f;
        this.bulletSpeed = 20f;
        this.damage = 4f;
        this.bulletSize = 0.19f;
        this.shootRange = 14f;
    }

    @Override
    public void update(float delta, Array<Entity> enemies) {
        super.update(delta, enemies);

        if (isBursting) {
            burstTimer += delta;
            if (burstTimer >= timeBetweenBullets) {
                fireSingleBullet();
                burstTimer = 0;
            }
        }
    }

    @Override
    protected void shoot() {
        if (!isBursting && objetive != null) {
            isBursting = true;
            bulletsShotInCurrentBurst = 0;
            burstTimer = timeBetweenBullets;

            burstDirection.set(objetive.getPosicion()).sub(worldPosition).nor();
        }
    }

    private void fireSingleBullet() {
        if (objetive != null && objetive.isAlive()) {
            burstDirection.set(objetive.getPosicion()).sub(worldPosition).nor();
        }

        applyRecoil(0.2f, 20f);

        if (effectManager != null) {
            effectManager.spawnEffect(EffectType.CASQUILLO_PISTOLA, worldPosition, burstDirection);
        }

        Vector2 finalDir = new Vector2(burstDirection);
        float randomOffset = MathUtils.random(-spreadAngle / 2f, spreadAngle / 2f);
        finalDir.rotateDeg(randomOffset);

        Projectile p = projectileFactory.create(
            new Vector2(worldPosition),
            finalDir,
            bulletSpeed,
            damage,
            bulletSize,
            projectileTexture,
            effectManager,
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
