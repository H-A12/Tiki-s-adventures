package com.tikisadventure.combat.abilities.actives;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.abilities.Ability;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

public class DashAbility implements Ability {
    protected float dashForce = 25f;      // La "potencia" del empujón
    protected float dashDuration = 0.15f; // Cuánto dura el impulso (segundos)
    protected float cooldown = 1.5f;

    @Override
    public void activate(Player owner, Array<Entity> enemies, Vector2 targetPosition) {
        com.tikisadventure.components.VelocityComponent velComp = owner.getComponent(com.tikisadventure.components.VelocityComponent.class);
        
        if (velComp != null) {
            System.out.println("DashAbility activated. Velocity: " + velComp.velocidad);
            if (!velComp.velocidad.isZero()) {
                execute(owner, velComp.velocidad.cpy().nor());
            } else {
                System.out.println("DashAbility failed: velocity is zero");
            }
        } else {
            System.out.println("DashAbility failed: velComp is null");
        }
    }

    public void execute(Entity owner, Vector2 direction) {
        if (owner instanceof Player) {
            Player p = (Player) owner;
            p.applyDashImpulse(direction.scl(dashForce), dashDuration);
        }
    }

    @Override public float getCooldown() { return cooldown; }
    @Override public float getMaxRange() { return 0f; }
    @Override public String getName() { return "Dash"; }
}
