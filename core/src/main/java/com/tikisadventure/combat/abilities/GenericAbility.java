package com.tikisadventure.combat.abilities;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.abilities.effects.AbilityEffect;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

//Habilidad genérica con una lista de efectos que se ejecutan al activarse
public class GenericAbility implements Ability {
    private String name;
    private float cooldown;
    private float maxRange;
    private Array<AbilityEffect> effects;
    private com.tikisadventure.combat.DamageType damageType;
    private String spritePath;
    private float baseDamage;

    public GenericAbility(String name, float cooldown, float maxRange, com.tikisadventure.combat.DamageType damageType, Array<AbilityEffect> effects, String spritePath, float baseDamage) {
        this.name = name;
        this.cooldown = cooldown;
        this.maxRange = maxRange;
        this.damageType = damageType;
        this.effects = effects;
        this.spritePath = spritePath;
        this.baseDamage = baseDamage;
    }

    @Override
    public boolean activate(Player owner, Array<Entity> enemies, Vector2 targetPosition) {
        for (AbilityEffect effect : effects) {
            boolean executed = effect.execute(owner, enemies, targetPosition);
            if (executed) {
                return true;
            }
        }
        return false;
    }

    @Override
    public float getCooldown() { return cooldown; }
    @Override
    public float getMaxRange() { return maxRange; }
    @Override
    public String getName() { return name; }

    @Override
    public void dispose() {
        effects.clear();
    }

    @Override
    public com.tikisadventure.combat.DamageType getDamageType() {
        return damageType;
    }

    @Override
    public String getSpritePath() { return spritePath; }

    @Override
    public float getBaseDamage() { return baseDamage; }

}
