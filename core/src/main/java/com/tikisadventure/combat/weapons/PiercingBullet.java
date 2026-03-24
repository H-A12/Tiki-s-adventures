package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.entities.Entity;

public class PiercingBullet extends Bullet {

    private static Texture texture;
    private int pierceCount = 3; // Puede atravesar 3 enemigos

    public PiercingBullet(Entity owner, Vector2 startPos, Vector2 dir, float speed, float damage, float radius) {
        // CORRECCIÓN: Ahora pasamos 'owner' y mantenemos 'true' para penetración inicial
        super(owner, startPos, dir, speed, damage, radius, true);

        if (texture == null) {
            // Puedes usar una textura distinta para que el jugador sepa que es perforante
            texture = new Texture("bullet.png");
        }
        this.sprite = new TextureRegion(texture);
    }

    @Override
    protected void onHit(Entity enemy) {
        // Aplicamos el daño al enemigo usando la lógica del padre
        super.onHit(enemy);

        // Lógica de desgaste de la bala
        pierceCount--;

        if (pierceCount <= 0) {
            this.alive = false; // Se "rompe" tras el tercer impacto
            this.penetrate = false;
        }
    }
}
