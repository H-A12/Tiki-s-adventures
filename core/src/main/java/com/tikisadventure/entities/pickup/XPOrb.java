package com.tikisadventure.entities.pickup;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.player.Player;

public class XPOrb extends Pickup {

    private static Texture texture;
    private int value;

    public XPOrb(Vector2 position, int value) {
        super(position);
        this.value = value;

        // Carga perezosa de la textura (Singleton)
        if (texture == null) {
            texture = new Texture("xp_orb.png");
        }
    }

    @Override
    protected void onPickup(Entity entity) {
        if (entity instanceof Player) {
            Player player = (Player) entity;

            // CAMBIO: Usamos addXP en lugar de addExperience
            if (player.getExperienceSystem() != null) {
                player.getExperienceSystem().addXP(value);
            }

            this.alive = false;
            System.out.println("XP recogida: " + value);
        }
    }
    @Override
    public void render(Batch batch, float delta) {
        if (texture == null) return;

        // Usamos las variables ANCHO y ALTO que definimos en el constructor de Pickup
        // para que el dibujo coincida con la lógica de colisión
        batch.draw(
            texture,
            posicion.x - ANCHO / 2,
            posicion.y - ALTO / 2,
            ANCHO,
            ALTO
        );
    }

    public int getValue() {
        return value;
    }
}
