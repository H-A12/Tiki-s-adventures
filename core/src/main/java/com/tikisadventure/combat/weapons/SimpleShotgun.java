package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.Weapon; // Importamos la clase base que está un nivel arriba
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.projectile.Projectile;
import com.tikisadventure.projectile.behaviors.StandardPhysicsBehavior;
import com.tikisadventure.projectile.behaviors.ZigZagBehavior;

public class SimpleShotgun extends Weapon {

    private int pellets = 5;          // Perdigones por disparo
    private float spreadAngle = 30f;  // Apertura del abanico en grados

    public SimpleShotgun(Entity owner, Weapon.ProjectileCreator factory) {
        // CAMBIO: Pasamos la textura del perdigón al constructor de la clase base
        super(owner, factory, new TextureRegion(new Texture("redbullet.png")));

        // Textura del arma (escopeta)
        this.sprite = new TextureRegion(new Texture("gun.png"));

        // Stats de escopeta: lenta pero devastadora de cerca
        this.cd = 0.8f;
        this.bulletSpeed = 12f;
        this.damage = 10f;
        this.bulletSize = 0.12f;
        this.shootRange = 8f;
    }

    @Override
    public void update(float delta, Array<Entity> enemies) {
        super.update(delta, enemies);
    }

    @Override
    protected void shoot() {
        if (objetive == null) return;

        // 1. Calculamos la dirección base hacia el objetivo
        Vector2 baseDir = new Vector2(objetive.getPosicion()).sub(worldPosition).nor();
        float baseAngle = baseDir.angleDeg();

        // 2. Calculamos los ángulos para el abanico (distribución simétrica)
        float startAngle = baseAngle - (spreadAngle / 2f);
        float angleStep = (pellets > 1) ? spreadAngle / (pellets - 1) : 0;

        // 3. Bucle para crear los perdigones
        for (int i = 0; i < pellets; i++) {
            float currentAngle = startAngle + (angleStep * i);
            Vector2 pelletDir = new Vector2(1, 0).setAngleDeg(currentAngle);

            // CAMBIO: Ahora pasamos 'projectileTexture' (definida en el super) al factory
            Projectile p = projectileFactory.create(
                new Vector2(worldPosition),
                pelletDir,
                bulletSpeed,
                damage,
                bulletSize,
                projectileTexture
            );

            // Inyectamos el comportamiento físico estándar
            p.addBehavior(new StandardPhysicsBehavior());


            // Añadimos cada perdigón al sistema del jugador
            if (owner instanceof Player) {
                ((Player) owner).addProjectile(p);
            }
        }
    }

    @Override
    public void render(Batch batch) {
        super.render(batch);
    }
}
