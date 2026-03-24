package com.tikisadventure.entities.abilities;

import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.entities.Entity;
import com.tikisadventure.entities.player.Player;

public abstract class DashAbility {
    protected float dashDuration = 0.2f; // Cuánto dura el impulso
    protected float dashSpeedMultiplier = 3f; // Qué tan rápido va
    protected float cooldown = 1.5f;

    // Método que todos los Dash comparten
    public void execute(Entity owner, Vector2 direction) {
        if (direction.isZero()) return;

        // Lógica base: Impulsar a la entidad
        owner.getPosicion().mulAdd(direction.nor(), dashSpeedMultiplier);

        // Aquí podrías añadir un sonido general de "woosh"
        onDashEffect(owner);
    }

    // Método abstracto: Cada personaje hace algo distinto al dash (humo, fuego, etc.)
    public abstract void onDashEffect(Entity owner);
}
