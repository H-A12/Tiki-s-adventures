package com.tikisadventure.combat.weapons.behaviors;

import com.badlogic.gdx.utils.ObjectMap;

public class BehaviorRegistry {
    private static final ObjectMap<String, BehaviorFactory> behaviors = new ObjectMap<>();

    public static void register(String type, BehaviorFactory factory) {
        behaviors.put(type, factory);
    }

    public static BehaviorFactory get(String type) {
        return behaviors.get(type);
    }
}
