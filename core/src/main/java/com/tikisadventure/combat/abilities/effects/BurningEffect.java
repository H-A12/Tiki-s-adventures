package com.tikisadventure.combat.abilities.effects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.statuses.BurningStatus;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

public class BurningEffect implements AbilityEffect {
    private float radius;
    private float damagePerTick;
    private float duration;
    private EffectManager effectManager;
    private String explosionProfile;

    public BurningEffect(EffectManager effectManager, float radius, float damagePerTick, float duration, String explosionProfile) {
        this.effectManager = effectManager;
        this.radius = radius;
        this.damagePerTick = damagePerTick;
        this.duration = duration;
        this.explosionProfile = explosionProfile;
    }

    @Override
    public void execute(Player owner, Array<Entity> enemies, Vector2 targetPosition) {
        // Only spawn visuals, do not apply combat logic
        com.tikisadventure.combat.ExplosionUtility.spawnVisuals(effectManager, targetPosition, explosionProfile);
        
        // Find enemies in radius and apply BurningStatus
        for (Entity e : enemies) {
            if (e.getPosition().dst(targetPosition) < radius) {
                e.getStatusManager().addStatus(new BurningStatus(effectManager, duration, damagePerTick, 0.5f), e);
            }
        }
    }
}
