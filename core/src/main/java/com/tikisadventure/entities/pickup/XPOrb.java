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

    public XPOrb() {
        super();
        if (texture == null) texture = Assets.getRegion("shared", "xp_orb");
    }

    public void init(Vector2 position, int value) {
        super.init(position);
        this.value = value;
    }

    @Override
    protected void onPickup(Entity entity) {
        if (entity instanceof Player) EventBus.publish(new OrbCollectedEvent(value));
    }

    @Override
    public void draw(Batch batch, float delta) {
        if (texture == null || !isAlive()) return;
        batch.draw(texture, positionComponent.posicion.x - getANCHO() / 2, positionComponent.posicion.y - getALTO() / 2, getANCHO(), getALTO());
    }

    @Override
    public void reset() {
        super.reset();
        this.value = 0;
    }
}
