package com.tikisadventure.systems;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.components.traits.Knockbackable;
import com.tikisadventure.systems.events.EventBus;
import com.tikisadventure.systems.events.HitEvent;

public class CombatSystem {
    private final EffectManager effectManager;

    public CombatSystem(EffectManager effectManager) {
        this.effectManager = effectManager;
    }

    public void update(Array<Projectile> projectiles, Array<Entity> enemies, float delta) {
        for (Projectile p : projectiles) {
            if (!p.isAlive()) continue;

            Vector2 pos = p.getPosition();
            float hitRadius = p.getRadius();

            for (Entity e : enemies) {
                if (!e.isAlive()) continue;

                float enemyRadius = e.getHitboxActionTrigger().radius;
                float totalRadius = hitRadius + enemyRadius;

                if (pos.dst2(e.getPosicion()) <= totalRadius * totalRadius) {
                    if (!p.canHit(e)) continue;
                    p.registerHit(e);

                    e.receiveDamage(p.getDamageValue(), p.isCrit(), p.getDamageType());
                    
                    for (Component c : p.getComponents()) {
                        c.onHit(e);
                    }
                    
                    EventBus.publish(new HitEvent(e.getPosicion()));

                    if (p.canPenetrate()) {
                        p.reducePenetration();
                    } else {
                        p.die();
                        return;
                    }
                }
            }
        }
    }
}
