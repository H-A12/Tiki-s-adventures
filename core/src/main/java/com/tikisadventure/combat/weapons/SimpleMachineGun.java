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

public class SimpleMachineGun extends Weapon {

    private int bulletsPerBurst = 3;
    private float timeBetweenBullets = 0.07f;
    private float spreadAngle = 8f;

    private int bulletsShotInCurrentBurst = 0;
    private float burstTimer = 0;
    private boolean isBursting = false;

    public SimpleMachineGun(Entity owner, Weapon.ProjectileCreator factory) {
        super(owner, factory, new TextureRegion(new Texture("greenbullet.png")));
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
                fireSingleBullet(); // <--- Aquí es donde se procesa cada bala
                burstTimer = 0;
            }
        }
    }

    @Override
    protected void shoot() {
        // Quitamos el applyRecoil de aquí, porque shoot() solo inicia la ráfaga
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

        // --- NUEVO: APLICAR RECOIL POR CADA BALA ---
        // Usamos valores pequeños porque se van a acumular 3 veces seguidas rápidamente.
        // Fuerza: 0.2f (pequeño) | Recuperación: 20f (rápido para no quedar bloqueado)
        applyRecoil(0.2f, 20f);

        Vector2 dir = new Vector2(objetive.getPosicion()).sub(worldPosition).nor();
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
