package com.tikisadventure.combat.abilities.effects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

public class SpawnProjectilesEffect implements AbilityEffect {
    private DamageType damageType;
    private int count;
    private float damage;
    private EffectManager effectManager;

    public SpawnProjectilesEffect(EffectManager effectManager, DamageType damageType, int count, float damage) {
        this.effectManager = effectManager;
        this.damageType = damageType;
        this.count = count;
        this.damage = damage;
    }

    @Override
    public void execute(Player owner, Array<Entity> enemies, Vector2 targetPosition) {
        Vector2 origin = owner.getPosition();
        Vector2 dir = targetPosition.cpy().sub(origin).nor();
        
        for (int i = 0; i < count; i++) {
            float angle = (i - count / 2f) * 10f;
            Vector2 bulletDir = dir.cpy().rotateDeg(angle);
            
            Projectile p = new Projectile(owner, origin, bulletDir, 10f, damage, 0f, 1f, 0.5f, null, effectManager, null, 0f);
            p.setDamageType(damageType);
            owner.addProjectile(p);
        }
    }
}
