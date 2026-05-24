package com.tikisadventure.components.traits;

//Dar un dueño a una entidad
public interface Ownable {
    Object getOwner();
    void setOwner(Object owner);
}
