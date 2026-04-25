package com.tikisadventure.combat.abilities.effects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.statuses.FreezeStatus;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

public class FreezeEffect implements AbilityEffect {
    private float radius;
    private float duration;
    private EffectManager effectManager;
    private String explosionProfile;

    public FreezeEffect(EffectManager effectManager, float radius, float duration, String explosionProfile) {
        this.effectManager = effectManager;
        this.radius = radius;
        this.duration = duration;
        this.explosionProfile = explosionProfile;
    }

    @Override
    public boolean execute(Player owner, Array<Entity> enemies, Vector2 targetPosition) {
        // Ejecutamos la animación de la explosión helada
        com.tikisadventure.combat.ExplosionUtility.spawnVisuals(effectManager, targetPosition, explosionProfile);

        // Congelamos a los que estén dentro del radio
        for (Entity e : enemies) {
            if (e.getPosition().dst(targetPosition) < radius) {
                e.getStatusManager().addStatus(new FreezeStatus(duration), e);
            }
        }
        return true;
    }
}
