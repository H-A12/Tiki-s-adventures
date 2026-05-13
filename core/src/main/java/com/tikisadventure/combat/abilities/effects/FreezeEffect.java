package com.tikisadventure.combat.abilities.effects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.combat.statuses.FreezeStatus;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

public class FreezeEffect implements AbilityEffect {
    private float radius;
    private float duration;
    private float baseDamage; // <-- NUEVA VARIABLE
    private EffectManager effectManager;
    private String explosionProfile;

    // <-- CONSTRUCTOR ACTUALIZADO
    public FreezeEffect(EffectManager effectManager, float radius, float duration, float baseDamage, String explosionProfile) {
        this.effectManager = effectManager;
        this.radius = radius;
        this.duration = duration;
        this.baseDamage = baseDamage;
        this.explosionProfile = explosionProfile;
    }

    @Override
    public boolean execute(Player owner, Array<Entity> enemies, Vector2 targetPosition) {
        // Ejecutamos la animación de la explosión helada
        com.tikisadventure.combat.ExplosionUtility.spawnVisuals(effectManager, targetPosition, explosionProfile);

        // <-- CALCULAMOS EL DAÑO ESCALADO CON HIELO
        float finalDamage = this.baseDamage;
        if (owner != null) {
            float bonus = owner.getDamageBonusByType(DamageType.ICE);
            finalDamage *= (1f + bonus);
        }

        // Aplicamos efectos a los que estén dentro del radio
        for (Entity e : enemies) {
            if (e.getPosition().dst(targetPosition) < radius) {
                // Aplicar congelación
                e.getStatusManager().addStatus(new FreezeStatus(duration), e);
                // Aplicar daño
                e.receiveDamage(finalDamage, false, DamageType.ICE);
            }
        }
        return true;
    }
}
