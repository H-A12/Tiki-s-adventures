package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.entities.Entity; // Importante añadir esto

public class NormalBullet extends Bullet {

    // Usamos una textura estática para no cargarla miles de veces en memoria
    private static Texture texture;

    public NormalBullet(Entity owner, Vector2 startPos, Vector2 dir, float speed, float damage, float radius) {
        // CORRECCIÓN: Ahora pasamos 'owner' al constructor padre (Bullet)
        super(owner, startPos, dir, speed, damage, radius, false);

        // Cargamos la textura solo si es la primera vez
        if (texture == null) {
            texture = new Texture("bullet.png");
        }

        this.sprite = new TextureRegion(texture);
    }
}
