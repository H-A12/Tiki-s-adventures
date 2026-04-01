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

    public MeleeBehavior(float range, float arcAngle, float speed) {
        this.range = range;
        this.arcAngle = arcAngle;
        this.speed = speed;
    }

    public void addModifier(HitModifier modifier) {
        hitModifiers.add(modifier);
    }

    @Override
    public void execute(Entity owner, Entity target, Vector2 worldPosition, EffectManager em) {
        // Collision detection logic based on range and arcAngle would go here
        // For now, assuming hit detected if within range
        if (target != null && owner.getPosicion().dst(target.getPosicion()) <= range) {
            for (HitModifier modifier : hitModifiers) {
                modifier.apply(owner, target, em);
            }
        }
    }

    @Override
    public void update(float delta) {
        // Animation logic
    }
}
