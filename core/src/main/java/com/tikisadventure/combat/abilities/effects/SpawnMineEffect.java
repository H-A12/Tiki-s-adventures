package com.tikisadventure.combat.abilities.effects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.abilities.effects.AbilityEffect;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.entities.gadgets.SewerMine; // Import corregido aquí

public class SpawnMineEffect implements AbilityEffect {
    private final EffectManager effectManager;
    private final float duration;
    private final float radius;
    private final float damage;
    private final String profile;
    private final Array<SewerMine> globalMinesList;

    public SpawnMineEffect(EffectManager em, Array<SewerMine> minesList, float duration, float radius, float damage, String profile) {
        this.effectManager = em;
        this.globalMinesList = minesList;
        this.duration = duration;
        this.radius = radius;
        this.damage = damage;
        this.profile = profile;
    }

    @Override
    public boolean execute(Player owner, Array<Entity> enemies, Vector2 targetPosition) {
        // Creamos la mina en la posición de destino
        SewerMine mine = new SewerMine(effectManager, targetPosition, duration, radius, damage, profile);
        globalMinesList.add(mine);
        return true;
    }
}
