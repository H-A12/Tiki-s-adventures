package com.tikisadventure.systems;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.enemies.ConfigurableEnemy;
import com.tikisadventure.entities.player.Player;

public class AISystem {

    private final Vector2 tempVec = new Vector2();

    public void update(Array<Entity> entities, Player player, float delta) {
        for (Entity entity : entities) {
            // Solo procesamos entidades que hereden de ConfigurableEnemy y estén vivas
            if (entity instanceof ConfigurableEnemy && entity.isAlive()) {
                ConfigurableEnemy enemy = (ConfigurableEnemy) entity;

                // 1. Obtenemos el tipo de IA desde su configuración (cargada del JSON)
                String aiType = enemy.getAiType(); // "chaser", "ranged", etc.

                if ("chaser".equalsIgnoreCase(aiType)) {
                    applyChaserAI(enemy, player);
                } else if ("ranged".equalsIgnoreCase(aiType)) {
                    applyRangedAI(enemy, player);
                }
            }
        }
    }

    /**
     * IA Básica: Persigue al jugador directamente.
     */
    private void applyChaserAI(ConfigurableEnemy enemy, Player player) {
        // Vector desde el enemigo hacia el jugador
        tempVec.set(player.getPosicion()).sub(enemy.getPosicion());

        if (tempVec.len2() > 0.1f) {
            enemy.getVelocidad().set(tempVec.nor());
        } else {
            enemy.getVelocidad().setZero();
        }
    }

    /**
     * IA de Rango: Mantiene una distancia de seguridad.
     */
    private void applyRangedAI(ConfigurableEnemy enemy, Player player) {
        tempVec.set(player.getPosicion()).sub(enemy.getPosicion());
        float distSq = tempVec.len2();
        float idealDist = 5f; // Distancia preferida para disparar

        if (distSq < (idealDist - 1) * (idealDist - 1)) {
            // Demasiado cerca: Huir del jugador
            enemy.getVelocidad().set(tempVec.nor().scl(-1));
        } else if (distSq > (idealDist + 1) * (idealDist + 1)) {
            // Demasiado lejos: Acercarse
            enemy.getVelocidad().set(tempVec.nor());
        } else {
            // En rango: Quedarse quieto y apuntar (el sistema de combate disparará)
            enemy.getVelocidad().setZero();
        }
    }
}
