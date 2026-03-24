package com.tikisadventure.entities.player;

public enum CharacterType {
    TIKI("Tiki", 100f, 5f, "tiki.png"),
    MOKO("Moko", 200f, 3.5f, "tiki.png"),
    ZUKI("Zuki", 70f, 7.5f, "tiki.png");

    public final String name;
    public final float maxHealth;
    public final float speed;
    public final String texturePath;

    CharacterType(String name, float maxHealth, float speed, String texturePath) {
        this.name = name;
        this.maxHealth = maxHealth;
        this.speed = speed;
        this.texturePath = texturePath;
    }
}
