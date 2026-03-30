package com.tikisadventure.entities.player;

import com.badlogic.gdx.Input.Keys;
import com.tikisadventure.abilities.Ability;
import com.tikisadventure.abilities.DashAbility;
import com.tikisadventure.abilities.DashAbility2;

public enum CharacterType {
    TIKI("Tiki", 100f, 5f, "tiki.png", DashAbility.class, Keys.SPACE, DashAbility2.class, Keys.Q),
    MOKO("Moko", 200f, 3.5f, "tiki.png", DashAbility.class, Keys.SPACE, DashAbility2.class, Keys.Q),
    ZUKI("Zuki", 70f, 7.5f, "tiki.png", DashAbility.class, Keys.SPACE, DashAbility2.class, Keys.Q);

    public final String name;
    public final float maxHealth;
    public final float speed;
    public final String texturePath;
    public final Class<? extends Ability> ability1Class;
    public final int ability1Key;
    public final Class<? extends Ability> ability2Class;
    public final int ability2Key;

    CharacterType(String name, float maxHealth, float speed, String texturePath,
                  Class<? extends Ability> ability1Class, int ability1Key,
                  Class<? extends Ability> ability2Class, int ability2Key) {
        this.name = name;
        this.maxHealth = maxHealth;
        this.speed = speed;
        this.texturePath = texturePath;
        this.ability1Class = ability1Class;
        this.ability1Key = ability1Key;
        this.ability2Class = ability2Class;
        this.ability2Key = ability2Key;
    }
}
