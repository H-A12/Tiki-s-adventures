package com.tikisadventure.combat.abilities.effects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.combat.statuses.FreezeStatus;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

//Crear una explosión de hielo que congela y daña enemigos
public class FreezeEffect implements AbilityEffect {
    private float radius;
    private float duration;
    private float baseDamage;
    private EffectManager effectManager;
    private String explosionProfile;

    public FreezeEffect(EffectManager effectManager, float radius, float duration, float baseDamage, String explosionProfile) {
        this.effectManager = effectManager;
        this.radius = radius;
        this.duration = duration;
        this.baseDamage = baseDamage;
        this.explosionProfile = explosionProfile;
    }

    @Override
    public boolean execute(Player owner, Array<Entity> enemies, Vector2 targetPosition) {
        com.tikisadventure.combat.ExplosionUtility.spawnVisuals(effectManager, targetPosition, explosionProfile);

        float finalDamage = this.baseDamage;
        if (owner != null) {
            float bonus = owner.getDamageBonusByType(DamageType.ICE);
            finalDamage *= (1f + bonus);
        }

        for (Entity e : enemies) {
            if (e.getPosition().dst(targetPosition) < radius) {
                e.getStatusManager().addStatus(new FreezeStatus(duration), e);
                e.receiveDamage(finalDamage, false, DamageType.ICE);
            }
        }
        return true;
    }
}
