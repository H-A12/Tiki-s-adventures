package com.tikisadventure.combat.abilities;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

public interface Ability {
    void activate(Player owner, Array<Entity> enemies);
    float getCooldown();
    String getName();
}
