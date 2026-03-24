package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.player.Player;

public class SimpleShotgun extends Weapon {

    private int pellets = 5;          // Perdigones por disparo
    private float spreadAngle = 30f;  // Apertura del abanico

    public SimpleShotgun(Entity owner, Weapon.BulletCreator bulletFactory) {
        super(owner, bulletFactory);

        // Cargamos la textura igual que en la pistola
        this.sprite = new TextureRegion(new Texture("gun.png"));

        // Configuramos los stats heredados de Weapon
        this.cd = 0.8f;           // Más lenta que la pistola
        this.bulletSpeed = 12f;
        this.damage = 10f;
        this.bulletSize = 0.12f;
        this.shootRange = 8f;     // Menos alcance, más potencia de cerca
    }

    @Override
    public void update(float delta, Array<Entity> enemies) {
        // Llamamos al padre para que maneje el cooldown y busque objetivos
        super.update(delta, enemies);
    }

    @Override
    public void shoot() {
        if (objetive == null) return;

        // Calculamos la dirección base hacia el objetivo
        Vector2 baseDir = new Vector2(objetive.getPosicion()).sub(worldPosition).nor();
        float baseAngle = baseDir.angleDeg();

        // Calculamos los ángulos para crear el abanico
        float startAngle = baseAngle - (spreadAngle / 2f);
        float angleStep = spreadAngle / (pellets - 1);

        // Bucle para crear los 5 perdigones
        for (int i = 0; i < pellets; i++) {
            float currentAngle = startAngle + (angleStep * i);
            Vector2 pelletDir = new Vector2(1, 0).setAngleDeg(currentAngle);

            // Usamos la fábrica igual que en la pistola
            Bullet b = bulletFactory.create(
                new Vector2(worldPosition),
                pelletDir,
                bulletSpeed,
                damage,
                bulletSize
            );

            // Pasamos la bala al Player
            if (owner instanceof Player) {
                ((Player) owner).addBullet(b);
            }
        }
    }

    @Override
    public void render(Batch batch) {
        // Usamos el render del padre que ya sabe cómo dibujar el sprite
        super.render(batch);
    }
}
