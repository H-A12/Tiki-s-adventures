package com.tikisadventure.entities.base.components;

public interface Killable {
    // El CollisionSystem necesita que devuelva boolean para el feedback visual
    boolean receiveDamage(float quantity);

    // El AnimationSystem y los managers necesitan saber si sigue viva
    boolean isAlive();

    // El estado final de la entidad
    void die();
}
