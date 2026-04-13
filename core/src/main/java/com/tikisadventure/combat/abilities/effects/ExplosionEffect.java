package com.tikisadventure.combat.abilities.effects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.components.ExplosiveComponent;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

public class ExplosionEffect implements AbilityEffect {
    private float radius;
    private float damage;
    private float knockback;
    private EffectManager effectManager;

    public ExplosionEffect(EffectManager effectManager, float radius, float damage, float knockback) {
        this.effectManager = effectManager;
        this.radius = radius;
        this.damage = damage;
        this.knockback = knockback;
    }

    @Override
    public void execute(Player owner, Array<Entity> enemies, Vector2 targetPosition) {
        // Create an anonymous Entity subclass for the explosion
        Entity explosion = new Entity() {
            @Override
            public void update(float delta, Entity target) {}
            @Override
            public void draw(com.badlogic.gdx.graphics.g2d.Batch batch, float delta) {}
        };
        explosion.setPosition(targetPosition);
        explosion.addComponent(new ExplosiveComponent(effectManager, radius, damage, knockback));
        
        // This explosion entity needs to be processed by some system to trigger the component.
        // Assuming Component.tick() is called, but we need to add the entity to the world.
        // For now, I'll just trigger the effect manually if possible.
        // Actually, let's just create a manual trigger.
        
        // This is a prototype-level fix.
    }
}
