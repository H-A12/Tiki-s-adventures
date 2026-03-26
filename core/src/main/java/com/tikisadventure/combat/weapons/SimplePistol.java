package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.projectile.Projectile;
import com.tikisadventure.projectile.behaviors.StandardPhysics;
import com.tikisadventure.combat.Weapon;

public class SimplePistol extends Weapon {

    public SimplePistol(Entity owner, Weapon.ProjectileCreator factory) {
        // CAMBIO: Ahora pasamos la textura del proyectil al constructor super
        super(owner, factory, new TextureRegion(new Texture("gun.png")));

        // Textura del arma en sí
        this.sprite = new TextureRegion(new Texture("gun.png"));

        // Stats del arma
        this.cd = 0.4f;
        this.bulletSpeed = 16f;
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

        // 1. Calculamos dirección
        Vector2 dir = new Vector2(objetive.getPosicion()).sub(worldPosition).nor();

        // 2. Creamos el proyectil usando la fábrica
        // CAMBIO: Ahora pasamos 'projectileTexture' (la que definimos en el super)
        Projectile p = projectileFactory.create(
            new Vector2(worldPosition),
            dir,
            bulletSpeed,
            damage,
            bulletSize,
            projectileTexture
        );

        // 3. Inyectamos el comportamiento
        p.addBehavior(new StandardPhysics());

        // 4. Lo añadimos al jugador
        if (owner instanceof Player) {
            ((Player) owner).addProjectile(p);
        }
    }

    @Override
    public void render(Batch batch) {
        super.render(batch);
    }
}
