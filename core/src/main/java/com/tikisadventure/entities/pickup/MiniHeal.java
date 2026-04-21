package com.tikisadventure.entities.pickup;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.core.Assets;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

public class MiniHeal extends Pickup {
    private static TextureRegion texture;
    private float healAmount = 10f;

    public MiniHeal() {
        super();
        if (texture == null) texture = Assets.getRegion("shared", "miniheal");
        setANCHO(0.8f);
        setALTO(0.8f);
    }

    public void init(Vector2 position) {
        super.init(position);
    }

    @Override
    protected void onPickup(Entity entity){
        if(entity instanceof Player){
            Player player = (Player) entity;
            player.setVida(Math.min(player.getVida() + healAmount, player.getVida_max()));
        }
    }

    @Override
    public void draw(Batch batch, float delta){
        if (texture == null || !isAlive()) return;
        batch.draw(texture, positionComponent.posicion.x - getANCHO() / 2, positionComponent.posicion.y - getALTO() / 2, getANCHO(), getALTO());
    }
}
