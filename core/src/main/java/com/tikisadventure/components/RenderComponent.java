package com.tikisadventure.components;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;

public class RenderComponent implements Component {
    public TextureRegion sprite;
    public float ancho;
    public float alto;
    public boolean mirarDerecha = true;
    public String estado = "idle"; // Can be mapped to Entity.Estado

    public RenderComponent(TextureRegion sprite, float ancho, float alto) {
        this.sprite = sprite;
        this.ancho = ancho;
        this.alto = alto;
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {}
}
