package com.tikisadventure.combat.weapons.types;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.core.Assets;
import com.tikisadventure.components.StandardPhysicsComponent;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;

public class SimplePistol extends Weapon {

    private float spreadAngle = 10f;

    public SimplePistol(Entity owner, Weapon.ProjectileCreator factory, EffectManager effectManager) {
        super(owner, factory, Assets.getRegion("YellowBullet"), effectManager);

        this.sprite = Assets.getRegion("Handgun");

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

        applyRecoil(0.6f, 18f);

        Vector2 baseDir = new Vector2(objetive.getPosicion()).sub(worldPosition).nor();

        if (effectManager != null) {
            effectManager.spawnEffect(EffectType.CASQUILLO_PISTOLA, worldPosition, baseDir);
        }

        Vector2 shotDir = new Vector2(baseDir);
        shotDir.rotateDeg(MathUtils.random(-spreadAngle / 2f, spreadAngle / 2f));

        Projectile p = projectileFactory.create(
            new Vector2(worldPosition),
            shotDir,
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
    }

    @Override
    public void render(Batch batch) {
        super.render(batch);
    }
}
