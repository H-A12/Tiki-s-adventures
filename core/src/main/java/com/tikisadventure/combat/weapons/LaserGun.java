package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.components.StandardPhysicsComponent;
import com.tikisadventure.combat.Weapon;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.projectile.Projectile;

public class LaserGun extends Weapon {

    private float spreadAngle = 10f;

    public LaserGun(Entity owner, ProjectileCreator factory, EffectManager effectManager) {
        super(owner, factory, new TextureRegion(new Texture("bluelaser.png")), effectManager);

        this.sprite = new TextureRegion(new Texture("lasergun.png"));

        this.cd = 0.8f;
        this.bulletSpeed = 10f;
        this.damage = 20f;
        this.bulletSize = 0.7f;
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
            EffectType.TRAIL_LASER,
            0.01f
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
