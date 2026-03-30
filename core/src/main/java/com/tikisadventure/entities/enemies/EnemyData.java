package com.tikisadventure.entities.enemies;

import com.badlogic.gdx.utils.JsonValue;

public class EnemyData {
    public String type;
    public String spritePath;
    public float health, speed, damage, experience;
    public float width, height;
    public String behaviorType;
    public float attackRange, attackCooldown;

    public static EnemyData fromJson(JsonValue json) {
        EnemyData d = new EnemyData();
        d.type = json.name;
        d.health = json.getFloat("health", 3);
        d.speed = json.getFloat("speed", 2.5f);
        d.damage = json.getFloat("damage", 2);
        d.experience = json.getFloat("experience", 5);
        d.width = json.getFloat("width", 1);
        d.height = json.getFloat("height", 1);
        d.spritePath = json.getString("sprite", "slime.png");
        d.behaviorType = json.getString("type", "chaser");
        d.attackRange = json.getFloat("attack_range", 1.0f);
        d.attackCooldown = json.getFloat("attack_cooldown", 1.0f);
        return d;
    }
}
