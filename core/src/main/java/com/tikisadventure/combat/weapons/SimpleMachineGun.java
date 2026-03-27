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
import com.tikisadventure.effects.EffectManager; // IMPORTANTE
import com.tikisadventure.effects.EffectType;    // IMPORTANTE

public class SimpleMachineGun extends Weapon {

    private int bulletsPerBurst = 3;
    private float timeBetweenBullets = 0.07f;
    private float spreadAngle = 8f;

    private int bulletsShotInCurrentBurst = 0;
    private float burstTimer = 0;
    private boolean isBursting = false;

    // 1. El constructor ahora recibe EffectManager
    public SimpleMachineGun(Entity owner, Weapon.ProjectileCreator factory, EffectManager effectManager) {
        // 2. Pasamos el manager al super (la clase Weapon)
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
        if (!isBursting) {
            isBursting = true;
            bulletsShotInCurrentBurst = 0;
            burstTimer = timeBetweenBullets;
        }
    }

    private void fireSingleBullet() {
        if (objetive == null || !objetive.isAlive()) {
            isBursting = false;
            return;
        }

        // 3. Efectos visuales por cada bala
        applyRecoil(0.2f, 20f);

        // Calculamos dirección para los efectos
        Vector2 dir = new Vector2(objetive.getPosicion()).sub(worldPosition).nor();

        if (effectManager != null) {
            // Expulsa casquillo dorado y pequeño (tipo pistola para ametralladora)
            effectManager.spawnEffect(EffectType.CASQUILLO_PISTOLA, worldPosition, dir);
            // Destello en el cañón
            //effectManager.spawnEffect(EffectType.MUZZLE_FLASH, worldPosition, dir);
        }

        float randomOffset = MathUtils.random(-spreadAngle / 2f, spreadAngle / 2f);
        dir.rotateDeg(randomOffset);

        Projectile p = projectileFactory.create(
            new Vector2(worldPosition),
            dir,
            bulletSpeed,
            damage,
            bulletSize,
            projectileTexture
        );

        if (owner instanceof Player) {
            ((Player) owner).addProjectile(p);
        }

        bulletsShotInCurrentBurst++;
        if (bulletsShotInCurrentBurst >= bulletsPerBurst) {
            isBursting = false;
        }
    }
}
