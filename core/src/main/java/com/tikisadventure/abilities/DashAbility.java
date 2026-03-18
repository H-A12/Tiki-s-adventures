package com.tikisadventure.abilities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;

public class DashAbility extends Ability {
    private float dashDuration = 0.2f;
    private float dashTimer = 0;
    private float dashForce = 28f;
    private Vector2 dashDir = new Vector2();

    // --- CLASE INTERNA PARA LA ESTELA ---
    public static class DashGhost {
        public Vector2 pos;
        public float lifetime;
        public boolean mirarDerecha;

        public DashGhost(Vector2 pos, float lifetime, boolean mirarDerecha) {
            this.pos = new Vector2(pos); // Copia la posición actual
            this.lifetime = lifetime;
            this.mirarDerecha = mirarDerecha;
        }
    }

    private Array<DashGhost> ghosts = new Array<>();
    private float ghostSpawnTimer = 0;

    public DashAbility(Entity owner) {
        super(owner, 1.2f); // Cooldown de 1.2 segundos
    }

    @Override
    public void activate() {
        if (!canUse()) return;

        float moveX = 0;
        float moveY = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.A)) moveX = -1;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) moveX = 1;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) moveY = 1;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) moveY = -1;

        dashDir.set(moveX, moveY).nor();

        if (dashDir.isZero()) {
            dashDir.set(owner.mirarDerecha ? 1 : -1, 0);
        }

        dashTimer = dashDuration;
        cooldownTimer = cooldown;

        // ACTIVAR INVULNERABILIDAD EN ENTITY
        owner.isInvulnerable = true;
    }

    @Override
    public void update(float delta) {
        super.update(delta); // Actualiza cooldownTimer de Ability

        if (dashTimer > 0) {
            dashTimer -= delta;

            // Mover al dueño
            owner.getPosicion().add(
                dashDir.x * dashForce * delta,
                dashDir.y * dashForce * delta
            );

            // GENERAR FANTASMAS MIENTRAS DURA EL DASH
            ghostSpawnTimer += delta;
            if (ghostSpawnTimer >= 0.04f) { // Crea uno cada 0.04 segundos
                ghostSpawnTimer = 0;
                ghosts.add(new DashGhost(owner.getPosicion(), 0.35f, owner.mirarDerecha));
            }

            // Desactivar invulnerabilidad al terminar el impulso
            if (dashTimer <= 0) {
                owner.isInvulnerable = false;
            }
        }

        // ACTUALIZAR TIEMPO DE VIDA DE LOS FANTASMAS
        for (int i = ghosts.size - 1; i >= 0; i--) {
            DashGhost g = ghosts.get(i);
            g.lifetime -= delta;
            if (g.lifetime <= 0) {
                ghosts.removeIndex(i);
            }
        }
    }

    public Array<DashGhost> getGhosts() {
        return ghosts;
    }

    public boolean isDashing() {
        return dashTimer > 0;
    }
}
