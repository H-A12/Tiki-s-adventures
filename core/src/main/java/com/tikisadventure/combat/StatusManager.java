package com.tikisadventure.combat;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.statuses.StatusEffect;
import com.tikisadventure.entities.base.Entity;

public class StatusManager {
    private Array<StatusEffect> activeStatuses = new Array<>();

    public void update(Entity target, float delta) {
        for (int i = activeStatuses.size - 1; i >= 0; i--) {
            StatusEffect effect = activeStatuses.get(i);
            effect.tick(target, delta);
            if (effect.isExpired()) {
                effect.onRemove(target);
                activeStatuses.removeIndex(i);
            }
        }
    }

    public void addStatus(StatusEffect effect, Entity target) {
        // Simple check to prevent duplicate status types
        for (StatusEffect existing : activeStatuses) {
            if (existing.getType() == effect.getType()) {
                // If existing, maybe reset duration? Let's just keep existing for now.
                return;
            }
        }
        effect.onApply(target);
        activeStatuses.add(effect);
    }
}
