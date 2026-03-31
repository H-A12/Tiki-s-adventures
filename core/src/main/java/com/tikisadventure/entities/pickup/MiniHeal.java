package com.tikisadventure.entities.pickup;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.core.Assets;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

public class MiniHeal extends Pickup {

    private static TextureRegion texture;
    private float healAmount = 15f;

    public MiniHeal(Vector2 position){
        super(position);

        if (texture == null) {
            texture = Assets.getRegion("shared", "miniheal");
        }

        // Definimos el tamaño para las hitboxes heredadas
        this.ANCHO = 0.8f;
        this.ALTO = 0.8f;
    }

    @Override
    protected void onPickup(Entity entity){
        if(entity instanceof Player){
            Player player = (Player) entity;

            float currentHp = player.getVida();

            // Usamos vida_max que ya está en Entity/Player,
            // así es más directo que pedir el perfil
            float maxHp = player.getVida_max();

            // Aplicamos la curación
            player.setVida(Math.min(currentHp + healAmount, maxHp));

            System.out.println("¡Tiki curado! Vida: " + player.getVida());
        }
    }

    @Override
    public void render(Batch batch, float delta){
        if (texture == null) return;

        // Dibujamos usando las dimensiones de la entidad
        batch.draw(
            texture,
            posicion.x - ANCHO / 2,
            posicion.y - ALTO / 2,
            ANCHO,
            ALTO
        );
    }
}
