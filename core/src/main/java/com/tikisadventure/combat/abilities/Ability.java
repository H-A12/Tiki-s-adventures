package com.tikisadventure.combat.abilities;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;
// ESTA ES LA LÍNEA QUE TE FALTA:
import com.tikisadventure.entities.player.Player;

public interface Ability {
    void activate(Player owner, Array<Entity> enemies, com.badlogic.gdx.math.Vector2 targetPosition);

    float getCooldown();

    float getMaxRange();

    String getName();

    default void dispose() {}
}
