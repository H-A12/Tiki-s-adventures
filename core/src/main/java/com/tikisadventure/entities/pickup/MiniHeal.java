package com.tikisadventure.entities.pickup;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.player.Tiki;

public class MiniHeal extends Pickup {

    private static Texture texture = new Texture("miniheal.png");

    private float healAmount = 5f;

    public MiniHeal(Vector2 position){
        super(position);
    }

    @Override
    protected void onPickup(Entity player){
        if(player instanceof Tiki){
            Tiki tiki = (Tiki) player;
            float newHp = tiki.getVida() + healAmount;
            float maxHp = tiki.getVida_max();
            tiki.setVida(Math.min(newHp, maxHp));
        }
    }

    @Override
    public void render(Batch batch, float delta){
        batch.draw(
            texture,
            posicion.x - 0.2f,
            posicion.y - 0.2f,
            0.4f,
            0.4f
        );
    }
}
