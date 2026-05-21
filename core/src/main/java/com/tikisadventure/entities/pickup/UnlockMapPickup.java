package com.tikisadventure.entities.pickup;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.audio.AudioManager;
import com.tikisadventure.audio.AudioType;
import com.tikisadventure.core.Assets;
import com.tikisadventure.core.GameSession;
import com.tikisadventure.core.SaveManager;
import com.tikisadventure.entities.base.Entity;

public class UnlockMapPickup extends Pickup {
    private static TextureRegion desertIcon;
    private static TextureRegion castilloIcon;
    private String mapId;
    private TextureRegion icon;

    public UnlockMapPickup() {
        super();
        if (desertIcon == null) desertIcon = Assets.getRegion("shared", "UI_assets/DesertMatchIcon");
        if (castilloIcon == null) castilloIcon = Assets.getRegion("shared", "UI_assets/CastilloMatchIcon");
        setANCHO(0.8f);
        setALTO(0.8f);
    }

    public void init(Vector2 position, String mapId) {
        super.init(position);
        this.mapId = mapId;
        this.icon = "castillo".equals(mapId) ? castilloIcon : desertIcon;
    }

    @Override
    protected void onPickup(Entity entity) {
        if (SaveManager.isMapUnlocked(mapId)) {
            GameSession.coinsCollectedThisRun += 100;
            AudioManager.playSFX(AudioType.COIN);
        } else {
            SaveManager.unlockMap(mapId);
            AudioManager.playSFX(AudioType.STAT_PICKUP);
        }
    }

    @Override
    public void draw(Batch batch, float delta) {
        if (icon == null || !isAlive()) return;
        batch.draw(icon,
            positionComponent.posicion.x - getANCHO() / 2,
            positionComponent.posicion.y - getALTO() / 2 + bobOffset,
            getANCHO(), getALTO());
    }

    @Override
    public void reset() {
        super.reset();
        mapId = null;
        icon = null;
    }

    public String getMapId() { return mapId; }
}
