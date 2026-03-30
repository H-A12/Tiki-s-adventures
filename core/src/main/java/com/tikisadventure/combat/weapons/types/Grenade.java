package com.tikisadventure.combat.weapons.types;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.components.ExplosiveComponent;
import com.tikisadventure.components.GrenadeComponent;
import com.tikisadventure.components.ShrapnelComponent;
import com.tikisadventure.components.StandardPhysicsComponent;
import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.combat.projectiles.Projectile;

public class Grenade extends Weapon {

    private float spreadAngle = 10f;

    public Grenade(Entity owner, Weapon.ProjectileCreator factory, EffectManager effectManager) {
        super(owner, factory, new TextureRegion(new Texture("bomb.png")), effectManager);

        this.sprite = new TextureRegion(new Texture("bomb.png"));
        this.cd = 1f;
        this.bulletSpeed = 5f;
        this.damage = 0f;
        this.bulletSize = 0.4f;
        this.shootRange = 12f;
    }

    @Override
    protected void shoot() {
        if (objetive == null) return;
        applyRecoil(0.6f, 18f);

        Vector2 baseDir = new Vector2(objetive.getPosicion()).sub(worldPosition).nor();
        Vector2 shotDir = new Vector2(baseDir).rotateDeg(MathUtils.random(-spreadAngle / 2f, spreadAngle / 2f));

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
        p.addComponent(new GrenadeComponent(1.4f, 0.7f, 2, 0.75f, 4f));
        p.addComponent(new ExplosiveComponent(effectManager, 0f, 5f, 60f, 15, 20));
        p.addComponent(new ShrapnelComponent(projectileFactory, "yellowbullet.png", 20, 2f, 0.2f));

        ((Player) owner).addProjectile(p);
    }
}
