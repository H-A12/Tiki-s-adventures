package com.tikisadventure.combat.abilities.actives;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.abilities.Ability;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

public class DashAbility2 implements Ability {
    protected float dashForce = 25f;      // La "potencia" del empujón
    protected float dashDuration = 0.15f; // Cuánto dura el impulso (segundos)
    protected float cooldown = 1.5f;

    @Override
    public void activate(Player owner, Array<Entity> enemies, Vector2 targetPosition) {
        Vector2 dir = new Vector2(0, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.W)) dir.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) dir.y -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) dir.x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) dir.x += 1;

        if (dir.isZero()) dir.set(1, 0);

        execute(owner, dir.nor());
    }

    public void execute(Entity owner, Vector2 direction) {
        if (owner instanceof Player) {
            Player p = (Player) owner;
            // 25f es la fuerza, 0.15f es la duración.
            // ¡Prueba a cambiar estos valores hasta que te guste el "feeling"!
            p.applyDashImpulse(direction.scl(25f), 0.15f);
        }
    }

    @Override public float getCooldown() { return cooldown; }
    @Override public float getMaxRange() { return 0f; }
    @Override public String getName() { return "Dash"; }
}
