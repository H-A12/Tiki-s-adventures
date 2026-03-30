package com.tikisadventure.abilities;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;
// ESTA ES LA LÍNEA QUE TE FALTA:
import com.tikisadventure.entities.player.Player;

public interface Ability {
    /**
     * Activa la lógica especial del personaje.
     * @param owner El jugador que lanza la habilidad.
     * @param enemies Lista de enemigos por si la habilidad hace daño o los afecta.
     */
    void activate(Player owner, Array<Entity> enemies);

    float getCooldown();

    String getName();
}
