package com.tikisadventure.entities.pickup;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.core.Assets;
import com.tikisadventure.core.GameSession;
import com.tikisadventure.entities.base.Entity;

public class CoinPickup extends Pickup {
    private static TextureRegion texture;
    private int coinAmount;

    public CoinPickup() {
        super();
        if (texture == null) texture = Assets.getRegion("shared", "UI_assets/coin");
        setANCHO(1.4f);
        setALTO(1.4f);
    }

    public void init(Vector2 position, int coinAmount) {
        super.init(position);
        this.coinAmount = coinAmount;
    }

    @Override
    protected void onPickup(Entity entity) {
        GameSession.coinsCollectedThisRun += coinAmount;
    }

    @Override
    public void draw(Batch batch, float delta) {
        if (texture == null || !isAlive()) return;
        batch.draw(texture,
            positionComponent.posicion.x - getANCHO() / 2,
            positionComponent.posicion.y - getALTO() / 2,
            getANCHO(), getALTO());
    }

    @Override
    public void reset() {
        super.reset();
        coinAmount = 0;
    }

    public int getCoinAmount() { return coinAmount; }
}
