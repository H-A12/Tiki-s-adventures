package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.Weapon;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.projectile.Projectile;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;
import com.tikisadventure.projectile.behaviors.StandardPhysicsBehavior;

public class SimpleMachineGun extends Weapon {

    private int bulletsPerBurst = 3;
    private float timeBetweenBullets = 0.07f;
    private float spreadAngle = 8f;

    private int bulletsShotInCurrentBurst = 0;
    private float burstTimer = 0;
    private boolean isBursting = false;

    // NUEVO: Guardamos la dirección hacia donde empezó a disparar la ráfaga
    private Vector2 burstDirection = new Vector2();

    public SimpleMachineGun(Entity owner, Weapon.ProjectileCreator factory, EffectManager effectManager) {
        super(owner, factory, new TextureRegion(new Texture("greenbullet.png")), effectManager);
        this.sprite = new TextureRegion(new Texture("machinegun.png"));

        this.cd = 0.9f;
        this.bulletSpeed = 20f;
        this.damage = 4f;
        this.bulletSize = 0.12f;
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

            // CAPTURAMOS la dirección inicial de la ráfaga
            burstDirection.set(objetive.getPosicion()).sub(worldPosition).nor();
        }
    }

    private void fireSingleBullet() {
        // Si el objetivo sigue vivo, actualizamos para perseguirlo
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

        // --- CAMBIO: Se pasa 'null' y '0f' para que NO tenga trail ---
        Projectile p = projectileFactory.create(
            new Vector2(worldPosition),
            finalDir,
            bulletSpeed,
            damage,
            bulletSize,
            projectileTexture,
            effectManager,
            null,  // Sin tipo de efecto (null)
            0f     // Sin intervalo (0)
        );
        p.addBehavior(new StandardPhysicsBehavior());
        if (owner instanceof Player) {
            ((Player) owner).addProjectile(p);
        }

        bulletsShotInCurrentBurst++;
        if (bulletsShotInCurrentBurst >= bulletsPerBurst) {
            isBursting = false;
        }
    }
}
