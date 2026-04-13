package com.tikisadventure.entities.base;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;

public interface Component {
    void tick(Object owner, float delta, Array<Entity> entities);
    
    default void onAttach(Object owner) {}
    default void onDetach(Object owner) {}
    default void onHit(Entity target) {}
    default void onDeath(Object owner, Array<Entity> entities) {}
}
