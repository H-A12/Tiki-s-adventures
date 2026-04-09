package com.tikisadventure.entities.pickup;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.core.Assets;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.systems.events.EventBus;
import com.tikisadventure.systems.events.OrbCollectedEvent;

public class XPOrb extends Pickup {

    private static TextureRegion texture;
    private int value;

    public XPOrb(Vector2 position, int value) {
        super(position);
        this.value = value;

        // Carga perezosa de la textura (Singleton)
        if (texture == null) {
            texture = Assets.getRegion("shared", "xp_orb");
        }
    }

    @Override
    protected void onPickup(Entity entity) {
        if (entity instanceof Player) {
            EventBus.publish(new OrbCollectedEvent(value));
            this.alive = false;
        }
    }
    
    @Override
    public void draw(Batch batch, float delta) {
        if (texture == null) return;

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
