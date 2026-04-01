package com.tikisadventure.combat.weapons.behaviors;

import com.badlogic.gdx.utils.ObjectMap;

public class BehaviorRegistry {
    private static final ObjectMap<String, BehaviorFactory> registry = new ObjectMap<>();

    public static void register(String type, BehaviorFactory factory) {
        registry.put(type, factory);
    }

    public static BehaviorFactory get(String type) {
        return registry.get(type);
    }
}
