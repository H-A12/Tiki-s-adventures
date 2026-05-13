package com.tikisadventure.combat;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.statuses.StatusEffect;
import com.tikisadventure.entities.base.Entity;

public class StatusManager {
    private Array<StatusEffect> activeStatuses = new Array<>();

    public void update(Entity target, float delta) {
        for (int i = activeStatuses.size - 1; i >= 0; i--) {
            // Por seguridad, si algo vació la lista de golpe, saltamos
            if (i >= activeStatuses.size) continue;

            StatusEffect effect = activeStatuses.get(i);
            effect.tick(target, delta);

            // --- PROTECCIÓN CONTRA CRASHEOS ---
            // Si el enemigo ha muerto por culpa del daño que le acaba de hacer el tick(),
            // rompemos el bucle inmediatamente porque la entidad ya no es válida.
            if (!target.isAlive()) {
                break;
            }

            if (effect.isExpired()) {
                effect.onRemove(target);
                // Segunda capa de seguridad al borrar
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
