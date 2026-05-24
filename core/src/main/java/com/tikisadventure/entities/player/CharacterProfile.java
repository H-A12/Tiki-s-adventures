package com.tikisadventure.entities.player;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.tikisadventure.combat.abilities.Ability;

//Perfil de personaje jugable: datos, animaciones y habilidades
public class CharacterProfile {
    public String name;
    public TextureRegion sprite;
    public float maxHealth;
    public float speed;
    public String startingWeapon;
    public Ability specialAbility1;
    public Ability specialAbility2;
    public int ability1Key;
    public int ability2Key;
    public String ability1Name;
    public String ability2Name;

    public Animation<TextureRegion> idle, up, down, left, right;

    public CharacterProfile(String name, float health, float speed, String startingWeapon,
                          Ability ability1, int key1,
                          Ability ability2, int key2,
                          TextureRegion sprite) {
        this.name = name;
        this.maxHealth = health;
        this.speed = speed;
        this.startingWeapon = startingWeapon;
        this.specialAbility1 = ability1;
        this.ability1Key = key1;
        this.specialAbility2 = ability2;
        this.ability2Key = key2;
        this.sprite = sprite;
    }

    public void dispose() {
        if (specialAbility1 != null) {
            specialAbility1.dispose();
            specialAbility1 = null;
        }
        if (specialAbility2 != null) {
            specialAbility2.dispose();
            specialAbility2 = null;
        }
    }
}
