package com.tikisadventure.combat;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.statuses.StatusEffect;
import com.tikisadventure.entities.base.Entity;

//Gestionar los estados activos de una entidad (quemadura, veneno, etc.)
public class StatusManager {
    private Array<StatusEffect> activeStatuses = new Array<>();

    public void update(Entity target, float delta) {
        for (int i = activeStatuses.size - 1; i >= 0; i--) {
            if (i >= activeStatuses.size) continue;

            StatusEffect effect = activeStatuses.get(i);
            effect.tick(target, delta);

            if (!target.isAlive()) {
                break;
            }

            if (effect.isExpired()) {
                effect.onRemove(target);
                if (i < activeStatuses.size) {
                    activeStatuses.removeIndex(i);
                }
            }
        }
    }

    public void addStatus(StatusEffect effect, Entity target) {
        for (StatusEffect existing : activeStatuses) {
            if (existing.getType() == effect.getType()) {
                existing.refreshDuration();
                return;
            }
        }

        effect.onApply(target);
        activeStatuses.add(effect);
    }

    public boolean hasStatus(StatusType type) {
        for (StatusEffect effect : activeStatuses) {
            if (effect.getType() == type) return true;
        }
        return false;
    }

    public StatusEffect getStatus(StatusType type) {
        for (StatusEffect effect : activeStatuses) {
            if (effect.getType() == type) return effect;
        }
        return null;
    }

    public void dispose() {
        for (StatusEffect se : activeStatuses) {
            if (se != null) {
                se.dispose();
            }
        }
        activeStatuses.clear();
    }
}
