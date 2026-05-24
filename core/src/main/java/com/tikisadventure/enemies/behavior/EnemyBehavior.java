package com.tikisadventure.enemies.behavior;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;

//Interfaz que deben implementar todos los comportamientos de enemigos.
//Cada behavior define cómo se mueve, ataca y en qué rango.
public interface EnemyBehavior {
    
    //Ejecutar lógica del enemigo cada frame: moverse, atacar, cambiar estado
    void update(Entity enemy, Entity target, float delta, Array<Entity> allEnemies);
    
    float getAttackRange();
    
    float getAttackDamage();
    
    float getAttackCooldown();
    
    String getBehaviorType();

    default boolean isInWindup() { return false; }
}
