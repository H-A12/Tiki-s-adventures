package com.tikisadventure.combat.weapons.modifiers;

import com.badlogic.gdx.utils.JsonValue;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.weapons.ProjectileModifier;
import com.tikisadventure.components.RandomSpriteComponent;
import com.tikisadventure.effects.EffectManager;
import java.util.ArrayList;

//Elegir sprite aleatorio para el proyectil
public class RandomSpriteModifier implements ProjectileModifier {
    private final ArrayList<String> spriteNames;

    public RandomSpriteModifier(JsonValue spritesJson) {
        this.spriteNames = new ArrayList<>();
        if (spritesJson != null && spritesJson.isArray()) {
            for (JsonValue spriteJson : spritesJson) {
                spriteNames.add(spriteJson.asString());
            }
        }
    }

    @Override
    public void apply(Projectile p, EffectManager em) {
        RandomSpriteComponent component = new RandomSpriteComponent(spriteNames);
        p.addComponent(component);
        component.onAttach(p);
    }
}