package com.tikisadventure.combat.weapons.behaviors;

import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.effects.EffectManager;

public class ThrustBehavior implements AttackBehavior {

    private float damage;
    private float range;
    private float speed;

    public ThrustBehavior(float damage, float range, float speed) {
        this.damage = damage;
        this.range = range;
        this.speed = speed;
    }

    @Override
    public void execute(Entity owner, Entity target, Vector2 worldPosition, EffectManager em) {
        // Lógica de detección de colisión en línea (Thrust)
    }

    @Override
    public void update(float delta) {
        // Lógica para animar la estocada
    }
}
