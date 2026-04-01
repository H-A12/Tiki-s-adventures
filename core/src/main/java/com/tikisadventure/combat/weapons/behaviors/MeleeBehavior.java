package com.tikisadventure.combat.weapons.behaviors;

import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.entities.base.Entity;
import java.util.ArrayList;
import java.util.List;

public class MeleeBehavior implements AttackBehavior {

    private float range;
    private float arcAngle;
    private float speed;
    private List<HitModifier> hitModifiers = new ArrayList<>();
    private com.tikisadventure.combat.weapons.Weapon weapon;
    private boolean isSwinging = false;
    private float swingTimer = 0f;
    private float swingRadius;
    private float pivotX;
    private float pivotY;
    private float baseAttackAngle;

    public MeleeBehavior(float range, float arcAngle, float speed, float swingRadius, float pivotX, float pivotY) {
        this.range = range;
        this.arcAngle = arcAngle;
        this.speed = speed;
        this.swingRadius = swingRadius;
        this.pivotX = pivotX;
        this.pivotY = pivotY;
    }

    public void addModifier(HitModifier modifier) {
        hitModifiers.add(modifier);
    }

    @Override
    public void execute(Entity owner, Entity target, Vector2 worldPosition, EffectManager em) {
        if (target != null && owner.getPosicion().dst(target.getPosicion()) <= range) {
            Vector2 dir = new Vector2(target.getPosicion()).sub(owner.getPosicion());
            baseAttackAngle = dir.angleDeg();
            
            float currentAngle = baseAttackAngle; // Use base angle for initial impact
            for (HitModifier modifier : hitModifiers) {
                modifier.apply(owner, target, em, currentAngle);
            }
            if (weapon != null) {
                isSwinging = true;
                swingTimer = 0f;
                weapon.setPivot(pivotX, pivotY);
            }
        }
    }

    @Override
    public void update(float delta) {
        if (isSwinging && weapon != null) {
            swingTimer += delta;
            float progress = swingTimer / speed;
            if (progress >= 1f) {
                isSwinging = false;
                weapon.setSwingOffset(0, 0);
                weapon.setSwingRotation(0);
            } else {
                // Sinusoidal easing for distance (swing out and in)
                float distance = (float) Math.sin(progress * Math.PI) * swingRadius;
                
                // Angle within arc (-arc/2 to +arc/2)
                float angleOffset = (-arcAngle / 2f) + (progress * arcAngle);
                
                // Position relative to player
                float totalAngle = baseAttackAngle + angleOffset;
                float x = (float) Math.cos(Math.toRadians(totalAngle)) * distance;
                float y = (float) Math.sin(Math.toRadians(totalAngle)) * distance;
                
                weapon.setSwingOffset(x, y);
                weapon.setSwingRotation(angleOffset);
            }
        }
    }

    @Override
    public void setWeapon(com.tikisadventure.combat.weapons.Weapon weapon) {
        this.weapon = weapon;
    }
}
