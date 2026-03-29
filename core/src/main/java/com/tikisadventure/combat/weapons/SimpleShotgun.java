package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.components.StandardPhysicsComponent;
import com.tikisadventure.combat.Weapon;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.projectile.Projectile;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;

public class SimpleShotgun extends Weapon {

    private int pellets = 6;
    private float spreadAngle = 35f;
    private float speedVariation = 3f;

    public SimpleShotgun(Entity owner, Weapon.ProjectileCreator factory, EffectManager effectManager) {
        super(owner, factory, new TextureRegion(new Texture("redbullet.png")), effectManager);

        this.sprite = new TextureRegion(new Texture("shotgun.png"));

        this.cd = 0.8f;
        this.bulletSpeed = 12f;
        this.damage = 10f;
        this.bulletSize = 0.3f;
        this.shootRange = 8f;
    }

    @Override
    protected void shoot() {
        if (objetive == null) return;

        Vector2 baseDir = new Vector2(objetive.getPosicion()).sub(worldPosition).nor();
        float baseAngle = baseDir.angleDeg();

        applyRecoil(1.8f, 4f);

        if (effectManager != null) {
            effectManager.spawnEffect(EffectType.CASQUILLO_ESCOPETA, worldPosition, baseDir);
        }

        for (int i = 0; i < pellets; i++) {
            float randomAngle = baseAngle + MathUtils.random(-spreadAngle / 2f, spreadAngle / 2f);
            Vector2 pelletDir = new Vector2(1, 0).setAngleDeg(randomAngle);
            float randomSpeed = bulletSpeed + MathUtils.random(-speedVariation, speedVariation);

            Projectile p = projectileFactory.create(
                new Vector2(worldPosition),
                pelletDir,
                randomSpeed,
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
        }
    }
}
