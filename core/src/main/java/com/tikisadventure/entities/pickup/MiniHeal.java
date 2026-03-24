package com.tikisadventure.entities.pickup;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.player.Player;

public class MiniHeal extends Pickup {

    private static Texture texture;
    private float healAmount = 15f;

    public MiniHeal(Vector2 position){
        super(position);

        // Carga segura de textura
        if (texture == null) {
            texture = new Texture("miniheal.png");
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
