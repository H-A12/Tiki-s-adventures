package com.tikisadventure.combat.abilities.effects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.combat.statuses.BurningStatus;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

//Crear una zona de fuego que aplica estado de quemadura a los enemigos
public class BurningEffect implements AbilityEffect {
    private float radius;
    private float damagePerTick;
    private float duration;
    private EffectManager effectManager;
    private String explosionProfile;
    private DamageType damageType;

    public BurningEffect(EffectManager effectManager, float radius, float damagePerTick, float duration, String explosionProfile, DamageType damageType) {
        this.effectManager = effectManager;
        this.radius = radius;
        this.damagePerTick = damagePerTick;
        this.duration = duration;
        this.explosionProfile = explosionProfile;
        this.damageType = damageType;
    }

    @Override
    public boolean execute(Player owner, Array<Entity> enemies, Vector2 targetPosition) {
        com.tikisadventure.combat.ExplosionUtility.spawnVisuals(effectManager, targetPosition, explosionProfile);

        float bonus = owner.getDamageBonusByType(damageType);
        float finalDamagePerTick = damagePerTick * (1f + bonus);

        for (Entity e : enemies) {
            if (e.getPosition().dst(targetPosition) < radius) {
                e.getStatusManager().addStatus(new BurningStatus(effectManager, duration, finalDamagePerTick, 0.5f, damageType, owner), e);
            }
        }
        return true;
    }
}
