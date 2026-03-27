package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils; // Necesario para el random
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.projectile.Projectile;
import com.tikisadventure.projectile.behaviors.*;
import com.tikisadventure.combat.Weapon;

public class SimplePistol extends Weapon {

    // --- Nueva variable de dispersión ---
    private float spreadAngle = 10f; // 5 grados hacia cada lado del centro

    public SimplePistol(Entity owner, Weapon.ProjectileCreator factory) {
        super(owner, factory, new TextureRegion(new Texture("yellowbullet.png")));

        this.sprite = new TextureRegion(new Texture("handgun.png"));

        // Estadísticas balanceadas
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

        // En el shoot() de la Pistola
        applyRecoil(0.6f, 18f);

        // 1. Calculamos la dirección base hacia el enemigo
        Vector2 dir = new Vector2(objetive.getPosicion()).sub(worldPosition).nor();

        // --- APLICAR SPREAD ---
        // Rotamos el vector de dirección un valor aleatorio entre -5 y 5
        dir.rotateDeg(MathUtils.random(-spreadAngle / 2f, spreadAngle / 2f));

        // 2. Creamos el objeto Proyectil con la dirección ya desviada
        Projectile p = projectileFactory.create(
            new Vector2(worldPosition),
            dir,
            bulletSpeed,
            damage,
            bulletSize,
            projectileTexture
        );

        // 3. INYECCIÓN DE COMPORTAMIENTOS
        p.addBehavior(new StandardPhysicsBehavior());

        // 4. Entregamos la bala al mundo
        if (owner instanceof Player) {
            ((Player) owner).addProjectile(p);
        }
    }

    @Override
    public void render(Batch batch) {
        super.render(batch);
    }
}
