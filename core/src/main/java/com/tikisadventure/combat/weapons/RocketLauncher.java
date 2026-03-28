package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
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
import com.tikisadventure.projectile.behaviors.ExplosiveBehavior;
import com.tikisadventure.projectile.behaviors.StandardPhysicsBehavior;

public class RocketLauncher extends Weapon {

    private float spreadAngle = 10f;

    public RocketLauncher(Entity owner, ProjectileCreator factory, EffectManager effectManager) {
        super(owner, factory, new TextureRegion(new Texture("rocketbullet.png")), effectManager);

        this.sprite = new TextureRegion(new Texture("rocketlauncher.png"));

        this.cd = 1f;
        this.bulletSpeed = 5f;
        this.damage = 40f;
        this.bulletSize = 0.4f;
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
            //effectManager.spawnEffect(EffectType.CASQUILLO_PISTOLA, worldPosition, baseDir);
        }

        Vector2 shotDir = new Vector2(baseDir);
        shotDir.rotateDeg(MathUtils.random(-spreadAngle / 2f, spreadAngle / 2f));

        // --- CAMBIO APLICADO: Ahora pasamos parámetros de trail y EffectManager ---
        Projectile p = projectileFactory.create(
            new Vector2(worldPosition),
            shotDir,
            bulletSpeed,
            damage,
            bulletSize,
            projectileTexture,
            effectManager,         // Pasamos el manager
            EffectType.TRAIL_SMOKE, // Elegimos el efecto de estela
            0.2f                  // Elegimos la frecuencia (cada 0.05s)
        );

        p.addBehavior(new StandardPhysicsBehavior());
        p.addBehavior(new ExplosiveBehavior(effectManager, 15.0f, 3.0f, 2.0f, 10, 25));        if (owner instanceof Player) {
            ((Player) owner).addProjectile(p);
        }
    }

    @Override
    public void render(Batch batch) {
        super.render(batch);
    }
}
