package com.tikisadventure.combat.weapons;

import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.effects.EffectManager;

//Interfaz para modificar un proyectil antes de dispararlo
public interface ProjectileModifier {
    void apply(Projectile p, EffectManager em);
}