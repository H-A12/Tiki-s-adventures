package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.projectile.Projectile;
import com.tikisadventure.projectile.behaviors.LifetimeBehavior;
import com.tikisadventure.projectile.behaviors.PulseSizeBehavior;
import com.tikisadventure.projectile.behaviors.ZigZagBehavior; // El nuevo
import com.tikisadventure.projectile.behaviors.StandardPhysicsBehavior;
import com.tikisadventure.combat.Weapon;

public class SimplePistol extends Weapon {

    public SimplePistol(Entity owner, Weapon.ProjectileCreator factory) {
        // Configuramos la textura del proyectil (bala amarilla)
        super(owner, factory, new TextureRegion(new Texture("yellowbullet.png")));

        // Textura del arma que sostiene el personaje
        this.sprite = new TextureRegion(new Texture("gun.png"));

        // Estadísticas balanceadas
        this.cd = 0.1f;            // Cadencia de disparo
        this.bulletSpeed = 5f;    // Velocidad frontal
        this.damage = 8f;          // Daño por impacto
        this.bulletSize = 0.15f;   // Radio de la bala
        this.shootRange = 12f;     // Distancia máxima teórica
    }

    @Override
    public void update(float delta, Array<Entity> enemies) {
        super.update(delta, enemies);
    }

    @Override
    protected void shoot() {
        // Si no hay un objetivo fijado por el WeaponManager, no disparamos
        if (objetive == null) return;

        // 1. Calculamos la dirección hacia el enemigo
        Vector2 dir = new Vector2(objetive.getPosicion()).sub(worldPosition).nor();

        // 2. Creamos el objeto Proyectil (vacío de lógica todavía)
        Projectile p = projectileFactory.create(
            new Vector2(worldPosition),
            dir,
            bulletSpeed,
            damage,
            bulletSize,
            projectileTexture
        );

        // 3. INYECCIÓN DE COMPORTAMIENTOS (El "Secreto")
        p.addBehavior(new StandardPhysicsBehavior());
        p.addBehavior(new ZigZagBehavior());
        p.addBehavior(new PulseSizeBehavior(10,1));
        p.addBehavior(new LifetimeBehavior(0.5f));

        // 4. Entregamos la bala al mundo a través del Player
        if (owner instanceof Player) {
            ((Player) owner).addProjectile(p);
        }
    }

    @Override
    public void render(Batch batch) {
        super.render(batch);
    }
}
