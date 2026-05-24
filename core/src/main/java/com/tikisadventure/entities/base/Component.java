package com.tikisadventure.entities.base;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;

//Componente conectable a una Entity. Cada tick ejecuta su lógica.
//Usa el patrón Component para separar comportamientos (movimiento, IA, etc.).
public interface Component {
    //Ejecutar lógica del componente cada frame
    void tick(Object owner, float delta, Array<Entity> entities);
    
    default void onAttach(Object owner) {}
    default void onDetach(Object owner) {}
    default void onHit(Entity target) {}
    default void onDeath(Object owner, Array<Entity> entities) {}
    default void dispose() {}
}
