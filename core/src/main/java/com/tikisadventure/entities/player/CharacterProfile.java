package com.tikisadventure.entities.player;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.tikisadventure.entities.abilities.Ability;

public class CharacterProfile {
    public String name;
    public TextureRegion sprite;

    // Stats base: Esto es lo que define "físicamente" al personaje
    public float maxHealth;
    public float speed;

    // Su habilidad única (Dash, Escudo, etc.)
    public Ability specialAbility;

    // HEMOS ELIMINADO: Weapon.BulletCreator bulletType;
    // Porque ahora la bala le pertenece al Arma, no al ADN del personaje.

    public CharacterProfile(String name, float health, float speed, Ability ability) {
        this.name = name;
        this.maxHealth = health;
        this.speed = speed;
        this.specialAbility = ability;
    }
}
