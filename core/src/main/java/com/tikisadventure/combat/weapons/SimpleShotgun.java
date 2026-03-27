package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils; // Importante para el random
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.Weapon;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.projectile.Projectile;
import com.tikisadventure.projectile.behaviors.StandardPhysicsBehavior;

public class SimpleShotgun extends Weapon {

    private int pellets = 6;          // Un número par suele quedar bien con random
    private float spreadAngle = 35f;  // Un poco más de apertura para el caos
    private float speedVariation = 3f; // Variación de velocidad para que no lleguen todos a la vez

    public SimpleShotgun(Entity owner, Weapon.ProjectileCreator factory) {
        super(owner, factory, new TextureRegion(new Texture("redbullet.png")));
        this.sprite = new TextureRegion(new Texture("shotgun.png"));

        this.cd = 0.8f;
        this.bulletSpeed = 12f;
        this.damage = 10f;
        this.bulletSize = 0.12f;
        this.shootRange = 8f;
    }

    @Override
    protected void shoot() {
        if (objetive == null) return;

        // En el shoot() de la Escopeta
        applyRecoil(1.8f, 4f);

        // 1. Dirección base hacia el enemigo
        Vector2 baseDir = new Vector2(objetive.getPosicion()).sub(worldPosition).nor();
        float baseAngle = baseDir.angleDeg();

        // 2. Bucle de perdigones
        for (int i = 0; i < pellets; i++) {

            // --- EL TRUCO DEL CAOS ---
            // En lugar de usar un step fijo (i * angleStep),
            // calculamos un ángulo aleatorio dentro del rango del abanico
            float randomAngle = baseAngle + MathUtils.random(-spreadAngle / 2f, spreadAngle / 2f);

            Vector2 pelletDir = new Vector2(1, 0).setAngleDeg(randomAngle);

            // EXTRA: Variamos un poco la velocidad de cada perdigón
            // para que la nube de balas tenga profundidad
            float randomSpeed = bulletSpeed + MathUtils.random(-speedVariation, speedVariation);

            Projectile p = projectileFactory.create(
                new Vector2(worldPosition),
                pelletDir,
                randomSpeed,
                damage,
                bulletSize,
                projectileTexture
            );

            p.addBehavior(new StandardPhysicsBehavior());

            if (owner instanceof Player) {
                ((Player) owner).addProjectile(p);
            }
        }
    }
}
