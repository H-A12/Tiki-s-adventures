package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.player.Player;

public class BasicGun extends Weapon {

    private static Texture texture;

    public BasicGun(Entity owner, Weapon.BulletCreator bulletFactory) {
        super(owner, bulletFactory);

        // 1. Configuramos los stats heredados de Weapon
        this.cd = 0.6f;           // Antes fireRate
        this.bulletSpeed = 14f;
        this.damage = 10f;
        this.bulletSize = 0.2f;
        this.shootRange = 8f;     // Rango de detección

        // 2. Carga de textura
        if (texture == null) {
            texture = new Texture("gun.png");
        }
        this.sprite = new TextureRegion(texture);
    }

    @Override
    public void update(float delta, Array<Entity> enemies) {
        // Llamamos al update de la clase padre (Weapon)
        // El padre ya se encarga de buscar enemigos y llamar a shoot() cuando toque
        super.update(delta, enemies);
    }

    @Override
    public void shoot() {
        // Si llegamos aquí es porque Weapon ya comprobó el cooldown y encontró un objetivo
        if (objetive == null) return;

        // Calculamos dirección usando worldPosition (nombre correcto en la clase padre)
        Vector2 dir = new Vector2(objetive.getPosicion()).sub(worldPosition).nor();

        // Creamos la bala usando la fábrica
        Bullet b = bulletFactory.create(
            new Vector2(worldPosition),
            dir,
            bulletSpeed,
            damage,
            bulletSize
        );

        // Registramos la bala en el Player
        if (owner instanceof Player) {
            ((Player) owner).addBullet(b);
        }
    }

    @Override
    public void render(Batch batch) {
        // Usamos el render del padre que ya tiene la lógica de rotación y flip
        super.render(batch);
    }
}
