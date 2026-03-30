package com.tikisadventure.entities.player;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.tikisadventure.combat.abilities.Ability;

public class CharacterProfile {
    public String name;
    public TextureRegion sprite; // Sprite estático (para la UI/Menú)
    public float maxHealth;
    public float speed;

    public Ability specialAbility1;
    public Ability specialAbility2;
    public int ability1Key;
    public int ability2Key;

    // Animaciones para el AnimationSystem
    public Animation<TextureRegion> idle, up, down, left, right, dead;

    public CharacterProfile(String name, float health, float speed,
                            Ability ability1, int key1,
                            Ability ability2, int key2,
                            TextureRegion sprite) {
        this.name = name;
        this.maxHealth = health;
        this.speed = speed;
        this.specialAbility1 = ability1;
        this.ability1Key = key1;
        this.specialAbility2 = ability2;
        this.ability2Key = key2;
        this.sprite = sprite;
    }

    /**
     * Método de seguridad para asegurar que 'idle' nunca sea nulo.
     * Si el AnimationSystem pide una animación que no existe, devolverá el idle.
     */
    public Animation<TextureRegion> getAnimationSafe(Animation<TextureRegion> target) {
        return (target != null) ? target : idle;
    }
}
