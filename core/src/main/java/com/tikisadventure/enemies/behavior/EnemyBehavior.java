package com.tikisadventure.enemies.behavior;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;

public interface EnemyBehavior {
    
    void update(Entity enemy, Entity target, float delta, Array<Entity> allEnemies);
    
    float getAttackRange();
    
    float getAttackDamage();
    
    float getAttackCooldown();
    
    String getBehaviorType();
}
