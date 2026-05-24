package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.math.Vector2;

//Punto de emisión con tipo de partícula y desplazamiento
public class Emitter {
    public String type;
    public Vector2 offset;

    public Emitter(String type, Vector2 offset) {
        this.type = type;
        this.offset = offset;
    }
}