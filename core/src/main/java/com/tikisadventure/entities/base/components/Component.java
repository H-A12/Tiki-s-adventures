package com.tikisadventure.entities.base.components;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;

public interface Component {
    void tick(Object owner, float delta, Array<Entity> entities);

    default void onAttach(Object owner) {}
    default void onDetach(Object owner) {}
}
