package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.projectile.Projectile;
import com.tikisadventure.projectile.behaviors.*;
import com.tikisadventure.combat.Weapon;
import com.tikisadventure.effects.EffectManager; // IMPORTANTE
import com.tikisadventure.effects.EffectType;    // IMPORTANTE

public class SimplePistol extends Weapon {

    private float spreadAngle = 10f;

    // 1. Constructor actualizado con EffectManager
    public SimplePistol(Entity owner, Weapon.ProjectileCreator factory, EffectManager effectManager) {
        // 2. Pasamos el manager al super
        super(owner, factory, new TextureRegion(new Texture("yellowbullet.png")), effectManager);

        this.sprite = new TextureRegion(new Texture("handgun.png"));

        this.cd = 1f;
        this.bulletSpeed = 5f;
        this.damage = 8f;
        this.bulletSize = 0.15f;
        this.shootRange = 12f;
    }

    @Override
    public void update(float delta, Array<Entity> enemies) {
        super.update(delta, enemies);
    }

    @Override
    protected void shoot() {
        if (objetive == null) return;

        // --- EFECTOS VISUALES Y FÍSICOS ---
        applyRecoil(0.6f, 18f);

        // Dirección base hacia el enemigo (antes del spread para los efectos)
        Vector2 baseDir = new Vector2(objetive.getPosicion()).sub(worldPosition).nor();

        if (effectManager != null) {
            // Destello de disparo
           // effectManager.spawnEffect(EffectType.MUZZLE_FLASH, worldPosition, baseDir);
            // Casquillo de pistola (dorado)
            effectManager.spawnEffect(EffectType.CASQUILLO_PISTOLA, worldPosition, baseDir);
        }

        // --- LÓGICA DEL PROYECTIL ---
        Vector2 shotDir = new Vector2(baseDir);
        shotDir.rotateDeg(MathUtils.random(-spreadAngle / 2f, spreadAngle / 2f));

        Projectile p = projectileFactory.create(
            new Vector2(worldPosition),
            shotDir,
            bulletSpeed,
            damage,
            bulletSize,
            projectileTexture
        );

        p.addBehavior(new StandardPhysicsBehavior());
        if (owner instanceof Player) {
            ((Player) owner).addProjectile(p);
        }
    }

    @Override
    public void render(Batch batch) {
        super.render(batch);
    }
}
