package com.tikisadventure.entities.pickup;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.player.Tiki;

public class XPOrb extends Pickup {

    private static Texture texture = new Texture("xp_orb.png");

    private int value;

    public XPOrb(Vector2 position, int value){
        super(position);
        this.value = value;
    }

    @Override
    protected void onPickup(Entity player){

        if(player instanceof Tiki){
            ((Tiki) player).getExperienceSystem().addXP(value);
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

    public int getValue(){
        return value;
    }
}
