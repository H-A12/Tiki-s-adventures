package com.tikisadventure.components.traits;

//Dar la capacidad de morir a una entidad
public interface Killable {
    boolean isAlive();
    void die();
}
