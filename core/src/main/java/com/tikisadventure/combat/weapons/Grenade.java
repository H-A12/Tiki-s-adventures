package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.Weapon;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.projectile.Projectile;
import com.tikisadventure.projectile.ProjectileFactory;
import com.tikisadventure.projectile.behaviors.*;

public class Grenade extends Weapon {

    private float spreadAngle = 10f;

    // Usamos Weapon.ProjectileCreator para que coincida con tus otras armas
    public Grenade(Entity owner, Weapon.ProjectileCreator factory, EffectManager effectManager) {
        super(owner, factory, new TextureRegion(new Texture("bomb.png")), effectManager);

        this.sprite = new TextureRegion(new Texture("bomb.png"));
        this.cd = 1f;
        this.bulletSpeed = 5f;
        this.damage = 0f;
        this.bulletSize = 0.15f;
        this.shootRange = 12f;
    }

    @Override
    protected void shoot() {
        if (objetive == null) return;
        applyRecoil(0.6f, 18f);

        Vector2 baseDir = new Vector2(objetive.getPosicion()).sub(worldPosition).nor();
        Vector2 shotDir = new Vector2(baseDir).rotateDeg(MathUtils.random(-spreadAngle / 2f, spreadAngle / 2f));

        // Creamos el proyectil usando el método create de la factory heredada
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

        p.addBehavior(new StandardPhysicsBehavior());
        p.addBehavior(new GrenadeBehavior(2.5f, 1.2f, 4, 0.7f, 25f));
        //p.addBehavior(new ExplosiveBehavior(effectManager, 0f, 5f, 12f, 15, 20));
        p.addBehavior(new ShrapnelBehavior(projectileFactory, "yellowbullet.png", 20, 2f, 0.2f));

        ((Player) owner).addProjectile(p);
        }
    }

