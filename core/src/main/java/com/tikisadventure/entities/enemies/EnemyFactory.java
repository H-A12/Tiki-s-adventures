package com.tikisadventure.entities.enemies;

import com.tikisadventure.entities.base.Entity;

public interface EnemyFactory {
    // Añadimos coordenadas para que el Spawner sepa dónde ponerlos
    Entity create(float x, float y);
}
