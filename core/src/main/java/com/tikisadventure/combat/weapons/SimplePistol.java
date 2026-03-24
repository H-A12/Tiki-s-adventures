package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.player.Player;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class SimplePistol extends Weapon {

    public SimplePistol(Entity owner, Weapon.BulletCreator bulletFactory) {
        super(owner, bulletFactory);

        this.sprite = new TextureRegion(new Texture("gun.png"));

        // Configuramos los stats heredados de la clase Weapon
        this.cd = 0.4f;           // Un poco más rápida que la BasicGun
        this.bulletSpeed = 16f;   // Balas más veloces
        this.damage = 8f;         // Menos daño por bala
        this.bulletSize = 0.15f;  // Balas más pequeñas
        this.shootRange = 12f;    // Mayor alcance de detección
    }

    @Override
    public void update(float delta, Array<Entity> enemies) {
        // MUY IMPORTANTE: Llamar al padre.
        // Weapon.update ya maneja el timer, la búsqueda de enemigos y llama a shoot()
        super.update(delta, enemies);
    }

    @Override
    public void shoot() {
        // Si Weapon decidió llamar a este método, es porque hay un objetivo válido
        if (objetive == null) return;

        // Calculamos la dirección usando worldPosition (el nombre en la clase padre)
        Vector2 dir = new Vector2(objetive.getPosicion()).sub(worldPosition).nor();

        // Usamos la fábrica para crear el proyectil
        Bullet b = bulletFactory.create(
            new Vector2(worldPosition),
            dir,
            bulletSpeed,
            damage,
            bulletSize
        );

        // Pasamos la bala al Player para que la gestione el motor del juego
        if (owner instanceof Player) {
            ((Player) owner).addBullet(b);
        }
    }

    @Override
    public void render(Batch batch) {
        // Si no le asignas un sprite, puedes dibujar un cuadrado temporal
        // o llamar a super.render(batch) si ya le pusiste textura en el constructor
        super.render(batch);
    }
}
