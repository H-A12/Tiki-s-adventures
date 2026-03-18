package com.tikisadventure.abilities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;

public class DashAbility extends Ability {
    private float dashDuration = 0.2f;
    private float dashTimer = 0;
    private float dashForce = 28f;
    private Vector2 dashDir = new Vector2();

    // --- CLASE INTERNA CORREGIDA ---
    public static class DashGhost {
        public Vector2 pos;
        public float lifetime;
        public float maxLifetime; // Para calcular la transparencia (alpha)
        public boolean mirarDerecha;
        public TextureRegion frame; // <--- Añadido para saber qué dibujar

        public DashGhost(Vector2 pos, float lifetime, boolean mirarDerecha, TextureRegion frame) {
            this.pos = new Vector2(pos);
            this.lifetime = lifetime;
            this.maxLifetime = lifetime;
            this.mirarDerecha = mirarDerecha;
            this.frame = frame;
        }
    }

    private Array<DashGhost> ghosts = new Array<>();
    private float ghostSpawnTimer = 0;

    public DashAbility(Entity owner) {
        super(owner, 1.2f);
    }

    @Override
    public void activate() {
        if (!canUse()) return;

        float moveX = 0, moveY = 0;
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
        owner.isInvulnerable = true;
    }

    // Necesitamos pasarle el frame actual de Tiki para crear el fantasma
    public void update(float delta, TextureRegion currentFrame) {
        super.update(delta);

        if (dashTimer > 0) {
            dashTimer -= delta;
            owner.getPosicion().add(dashDir.x * dashForce * delta, dashDir.y * dashForce * delta);

            ghostSpawnTimer += delta;
            if (ghostSpawnTimer >= 0.04f) {
                ghostSpawnTimer = 0;
                // Guardamos el frame que Tiki tiene en este momento
                ghosts.add(new DashGhost(owner.getPosicion(), 0.35f, owner.mirarDerecha, currentFrame));
            }

            if (dashTimer <= 0) owner.isInvulnerable = false;
        }

        for (int i = ghosts.size - 1; i >= 0; i--) {
            DashGhost g = ghosts.get(i);
            g.lifetime -= delta;
            if (g.lifetime <= 0) ghosts.removeIndex(i);
        }
    }

    public void render(Batch batch) {
        for (DashGhost g : ghosts) {
            // Calculamos un alpha que se desvanece (fade out)
            float alpha = g.lifetime / g.maxLifetime;

            // Color azulado semi-transparente
            batch.setColor(0.5f, 0.7f, 1f, alpha * 0.6f);

            if (g.mirarDerecha) {
                batch.draw(g.frame, g.pos.x, g.pos.y, owner.getANCHO(), owner.getALTO());
            } else {
                batch.draw(g.frame, g.pos.x + owner.getANCHO(), g.pos.y, -owner.getANCHO(), owner.getALTO());
            }
        }
        // Resetear siempre el color a blanco
        batch.setColor(1f, 1f, 1f, 1f);
    }

    public boolean isDashing() { return dashTimer > 0; }
    public Array<DashGhost> getGhosts() { return ghosts; }
}
