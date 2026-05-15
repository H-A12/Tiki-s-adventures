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
        if (texture == null) texture = Assets.getRegion("shared", "pickup_assets/orbXP");
    }

    public void init(Vector2 position, int value) {
        super.init(position);
        this.value = value;
    }

    @Override
    protected void onPickup(Entity entity) {
        if (entity instanceof Player) {
            Player player = (Player) entity;
            int xpFinal = Math.round(this.value * player.getXpMultiplier());

            EventBus.publish(new OrbCollectedEvent(xpFinal));
        }
    }

    @Override
    public void draw(Batch batch, float delta) {
        if (texture == null || !isAlive()) return;
        // Calculamos el tamaño visual con un aumento del 50% (factor 1.5)
        float drawW = getANCHO() * 1.3f;
        float drawH = getALTO() * 1.3f;

        batch.draw(texture,
            positionComponent.posicion.x - drawW / 2,
            positionComponent.posicion.y - drawH / 2 + bobOffset,
            drawW,
            drawH
        );
    }

    @Override
    public void reset() {
        super.reset();
        this.value = 0;
    }
}
