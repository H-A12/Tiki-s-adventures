package com.tikisadventure.combat.abilities.effects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.effects.EffectManager;

public class ThrowEffect implements AbilityEffect {
    private EffectManager em;
    private String sprite;
    private float arc;
    private float speed;
    private float lifetime;
    private Array<AbilityEffect> onHitEffects;

    public ThrowEffect(EffectManager em, String sprite, float speed, float lifetime, Array<AbilityEffect> onHitEffects) {
        this.em = em;
        this.sprite = sprite;
        this.speed = speed;
        this.lifetime = lifetime;
        this.onHitEffects = onHitEffects;
    }

    @Override
    public void execute(Player owner, Array<Entity> enemies, Vector2 targetPosition) {
        float distance = targetPosition.dst(owner.getPosition());
        float dynamicLifetime = distance / speed; 
        GrenadeProjectile grenade = new GrenadeProjectile(owner, enemies, owner.getPosition(), targetPosition.cpy().sub(owner.getPosition()).nor(), speed, dynamicLifetime, sprite, onHitEffects, em);
        owner.addProjectile(grenade);
    }
}
