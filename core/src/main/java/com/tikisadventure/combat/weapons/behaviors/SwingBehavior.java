package com.tikisadventure.combat.weapons.behaviors;

import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.weapons.ProjectileCreator;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.effects.EffectManager;

public class SwingBehavior implements AttackBehavior {

    private float damage;
    private float range;
    private float arcAngle;
    private float speed;

    public SwingBehavior(float damage, float range, float arcAngle, float speed) {
        this.damage = damage;
        this.range = range;
        this.arcAngle = arcAngle;
        this.speed = speed;
    }

    @Override
    public void execute(Entity owner, Entity target, Vector2 worldPosition, EffectManager em) {
        // Lógica de detección de colisión en arco (Swing)
        // ... por ahora simplificado: detecta en un radio frente al dueño
    }

    @Override
    public void update(float delta) {
        // Lógica para animar el arco
    }
}
