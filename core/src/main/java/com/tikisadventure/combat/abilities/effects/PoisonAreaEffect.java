package com.tikisadventure.combat.abilities.effects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.statuses.PoisonStatus;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

public class PoisonAreaEffect implements AbilityEffect {
    private final EffectManager effectManager;
    private final float radius;
    private final float damagePerTick;
    private final float duration;
    private final float interval;
    private final String profile;

    public PoisonAreaEffect(EffectManager effectManager, float radius, float damagePerTick, float duration, float interval, String profile) {
        this.effectManager = effectManager;
        this.radius = radius;
        this.damagePerTick = damagePerTick;
        this.duration = duration;
        this.interval = interval;
        this.profile = profile;
    }

    @Override
    public boolean execute(Player owner, Array<Entity> enemies, Vector2 targetPosition) {

        // 1. Mostrar la explosión visual en el punto de impacto
        if (profile != null && !profile.isEmpty()) {
            EffectManager.ExplosionProfile expProfile = effectManager.getExplosionProfile(profile);
            if (expProfile != null) {
                effectManager.spawnEffect(expProfile.spritesheet, targetPosition, new Vector2(0, 0));
                effectManager.spawnEffect(expProfile.smoke, targetPosition, new Vector2(0, 0));
                effectManager.spawnEffect(expProfile.sparks, targetPosition, new Vector2(0, 0));
            }
        }

        // 2. Aplicar el estado de Veneno a todos los enemigos en el área
        for (Entity e : enemies) {
            if (e.isAlive() && e.getPosition().dst(targetPosition) <= radius) {
                e.getStatusManager().addStatus(new PoisonStatus(effectManager, damagePerTick, interval, duration), e);
            }
        }

        return true; // Indicamos que el efecto se ejecutó correctamente
    }
}
