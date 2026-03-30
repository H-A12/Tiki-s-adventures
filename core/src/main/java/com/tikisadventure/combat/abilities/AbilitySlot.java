package com.tikisadventure.combat.abilities;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

public class AbilitySlot {
    private final Ability ability;
    private float cooldownTimer;

    public AbilitySlot(Ability ability) {
        this.ability = ability;
    }

    public void update(float delta) {
        if (cooldownTimer > 0) cooldownTimer -= delta;
    }

    public void tryActivate(Player owner, Array<Entity> enemies) {
        if (cooldownTimer <= 0) {
            ability.activate(owner, enemies);
            cooldownTimer = ability.getCooldown();
        }
    }

    public boolean isReady() { return cooldownTimer <= 0; }
    public float getCooldownPercent() { return Math.max(0, cooldownTimer / ability.getCooldown()); }
}
