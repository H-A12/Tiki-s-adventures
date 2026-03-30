package com.tikisadventure.combat.abilities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

public class Dash implements Ability {

    private final float DASH_FORCE = 22f;
    private final float DASH_DURATION = 0.25f;

    @Override
    public void activate(Player owner, Array<Entity> enemies) {
        Vector2 dir = new Vector2(owner.getVelocidad()).nor();
        if (dir.isZero()) dir.set(1, 0);

        owner.applyDashImpulse(dir.scl(DASH_FORCE), DASH_DURATION);
        owner.setInvulnerable(DASH_DURATION + 0.1f);
    }

    @Override
    public float getCooldown() {
        return 1.5f;
    }

    @Override
    public String getName() {
        return "Dash";
    }
}
