package com.tikisadventure.entities.player;

import com.badlogic.gdx.graphics.g2d.Animation; // <--- ESTE IMPORT ES VITAL
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.tikisadventure.entities.abilities.Ability;

public class CharacterProfile {
    public String name;
    public TextureRegion sprite;
    public float maxHealth;
    public float speed;
    public Ability specialAbility;

    // Animaciones (Ahora con el import correcto)
    public Animation<TextureRegion> idle, up, down, left, right;

    public CharacterProfile(String name, float health, float speed, Ability ability, TextureRegion sprite) {
        this.name = name;
        this.maxHealth = health;
        this.speed = speed;
        this.specialAbility = ability;
        this.sprite = sprite;
    }
}
