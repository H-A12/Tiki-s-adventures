package com.tikisadventure.entities.player;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.projectiles.ProjectileManager;
import com.tikisadventure.systems.ExperienceSystem;

public class Player extends Entity {

    private final ExperienceSystem experienceSystem;
    private final CharacterProfile profile;
    private ProjectileManager projectileManager;

    private float dashTimer = 0;
    private final Vector2 dashDirection = new Vector2();

    public Player(CharacterProfile profile) {
        super(0, 0);
        this.profile = profile;
        this.experienceSystem = new ExperienceSystem();

        // Inicializamos estadísticas desde el perfil
        this.setStats(profile.maxHealth, profile.speed, 0f, 0);

        this.ANCHO = 1.2f;
        this.ALTO = 1.2f;
    }

    /**
     * Sincronizado con los nombres de CharacterProfile: idle, up, down, left, right.
     * Si una animación es nula, devolvemos 'idle' como salvaguarda.
     */
    @Override
    public Animation<TextureRegion> getAnimationForState(Estado estado) {
        switch (estado) {
            case walking_up:
                return profile.up != null ? profile.up : profile.idle;
            case walking_down:
                return profile.down != null ? profile.down : profile.idle;
            case walking_side:
                // El AnimationSystem decide 'walking_side', nosotros elegimos 'right'
                // y el render se encarga de voltearlo si miraDerecha es falso.
                return profile.right != null ? profile.right : profile.idle;
            case dead:
                // Usamos la animación de muerte si existe
                return profile.dead != null ? profile.dead : profile.idle;
            case idle:
            default:
                return profile.idle;
        }
    }

    public void setProjectileManager(ProjectileManager pm) {
        this.projectileManager = pm;
    }

    public void addProjectile(Projectile p) {
        if (projectileManager != null) {
            projectileManager.add(p);
        }
    }

    public void applyDashImpulse(Vector2 impulse, float duration) {
        this.dashDirection.set(impulse);
        this.dashTimer = duration;
        setInvulnerable(duration);
    }

    @Override
    public void update(float delta) {
        if (!alive) return;

        // Movimiento: Dash vs Velocidad de Input
        if (dashTimer > 0) {
            posicion.mulAdd(dashDirection, delta);
            dashTimer -= delta;
        } else {
            posicion.mulAdd(velocidad, speed * delta);
        }

        applyKnockback(delta);
        updateTimers(delta);
        actualizarHitboxes();

        // El stateTime se acumula para que el AnimationSystem sepa qué frame pedir
        stateTime += delta;
    }

    @Override
    public void render(Batch batch, float delta) {
        if (!alive) return;

        // Efecto visual de parpadeo cuando eres invulnerable (post-hit o dash)
        if (isInvulnerable && ((int)(stateTime * 15) % 2 == 0)) return;

        // 'sprite' es la variable de la clase base Entity, actualizada por AnimationSystem
        if (sprite != null) {
            boolean flip = !mirarDerecha;

            batch.draw(sprite,
                posicion.x - ANCHO / 2f + (flip ? ANCHO : 0),
                posicion.y - ALTO / 2f,
                flip ? -ANCHO : ANCHO,
                ALTO);
        } else if (profile.sprite != null) {
            // Fallback: si la animación aún no se procesó, dibujamos el sprite estático
            batch.draw(profile.sprite, posicion.x - ANCHO / 2f, posicion.y - ALTO / 2f, ANCHO, ALTO);
        }
    }

    public ExperienceSystem getExperienceSystem() { return experienceSystem; }
    public CharacterProfile getProfile() { return profile; }
    public boolean isDashing() { return dashTimer > 0; }
}
