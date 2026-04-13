package com.tikisadventure.combat.abilities.effects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

public interface AbilityEffect {
    void execute(Player owner, Array<Entity> enemies, Vector2 targetPosition);
}
