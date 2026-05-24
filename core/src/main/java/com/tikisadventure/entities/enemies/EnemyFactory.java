package com.tikisadventure.entities.enemies;

import com.tikisadventure.entities.base.Entity;

//Fábrica que crea entidades enemigas
public interface EnemyFactory {
    Entity create();
}
