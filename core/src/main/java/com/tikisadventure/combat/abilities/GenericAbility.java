package com.tikisadventure.combat.abilities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.abilities.effects.AbilityEffect;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

public class GenericAbility implements Ability {
    private String name;
    private float cooldown;
    private float maxRange;
    private Array<AbilityEffect> effects;

    public GenericAbility(String name, float cooldown, float maxRange, Array<AbilityEffect> effects) {
        this.name = name;
        this.cooldown = cooldown;
        this.maxRange = maxRange;
        this.effects = effects;
    }

    @Override
    public void activate(Player owner, Array<Entity> enemies, Vector2 targetPosition) {
        for (AbilityEffect effect : effects) {
            effect.execute(owner, enemies, targetPosition);
        }
    }

    @Override
    public float getCooldown() { return cooldown; }
    @Override
    public float getMaxRange() { return maxRange; }
    @Override
    public String getName() { return name; }
}
