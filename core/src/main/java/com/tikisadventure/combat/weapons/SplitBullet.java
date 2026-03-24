package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.player.Player;

public class SplitBullet extends Bullet {

    private static Texture texture;

    // 1. Añadimos Entity owner al constructor
    public SplitBullet(Entity owner, Vector2 startPos, Vector2 dir, float speed, float damage, float radius) {
        // 2. Pasamos owner al super (ahora son 7 parámetros)
        super(owner, startPos, dir, speed, damage, radius, false);

        if (texture == null) {
            texture = new Texture("bullet.png");
        }
        this.sprite = new TextureRegion(texture);
    }

    @Override
    protected void onHit(Entity enemy) {
        // Aplicamos daño al enemigo principal
        super.onHit(enemy);

        // 3. Lógica de fragmentación
        int numFragments = 4;
        for (int i = 0; i < numFragments; i++) {
            // Creamos una dirección rotada para cada fragmento
            Vector2 fragmentDir = new Vector2(1, 0).rotateDeg(i * 90);

            // 4. Creamos los fragmentos (NormalBullet)
            // Usamos 'owner' y 'position' que heredamos de Bullet
            NormalBullet fragment = new NormalBullet(
                owner,
                new Vector2(position),
                fragmentDir,
                speed * 0.7f,
                damage * 0.5f,
                radius * 0.6f
            );

            // 5. Los añadimos al mundo si el dueño es el Jugador
            if (owner instanceof Player) {
                ((Player) owner).addBullet(fragment);
            }
        }
    }
}
