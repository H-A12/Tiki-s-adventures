package com.tikisadventure.components;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;

import com.tikisadventure.entities.base.Entity.Estado;

public class RenderComponent implements Component {
    public TextureRegion sprite;
    public float ancho;
    public float alto;
    public boolean mirarDerecha = true;
    public Estado estado = Estado.idle;
    public float stateTime = 0;
    
    public RenderComponent(TextureRegion sprite, float ancho, float alto) {
        this.sprite = sprite;
        this.ancho = ancho;
        this.alto = alto;
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {}
}
