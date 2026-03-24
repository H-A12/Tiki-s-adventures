package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;

public abstract class Bullet {

    protected Entity owner;         // Quién disparó la bala (Jugador o Enemigo)
    protected Vector2 position;    // Posición actual en el mundo
    protected Vector2 direction;   // Vector normalizado de dirección

    protected float speed;         // Velocidad de desplazamiento
    protected float damage;        // Daño que inflige
    protected float radius;        // Radio de la hitbox circular

    protected boolean penetrate;   // Si atraviesa enemigos o muere al primer choque
    protected boolean alive = true;

    protected TextureRegion sprite; // Imagen de la bala

    public Bullet(Entity owner, Vector2 startPos, Vector2 dir, float speed, float damage, float radius, boolean penetrate) {
        this.owner = owner;
        this.position = new Vector2(startPos);
        this.direction = new Vector2(dir).nor(); // Nos aseguramos de que esté normalizado

        this.speed = speed;
        this.damage = damage;
        this.radius = radius;
        this.penetrate = penetrate;
    }

    public void update(float delta, Array<Entity> enemies) {
        if (!alive) return;

        // Movimiento: Nueva Posicion = Posicion + (Direccion * Velocidad * Tiempo)
        position.mulAdd(direction, speed * delta);

        // Detección de colisiones
        for (Entity e : enemies) {
            if (!e.isAlive()) continue;

            // Optimizamos usando dst2 (distancia al cuadrado) para evitar raíces cuadradas costosas
            float dist2 = position.dst2(e.getPosicion());
            float hitRadius = radius + e.getHitboxActionTrigger().radius;

            if (dist2 <= hitRadius * hitRadius) {
                onHit(e); // Ejecutamos la lógica de impacto

                if (!penetrate) {
                    alive = false;
                    break;
                }
            }
        }
    }

    /**
     * Lógica que ocurre al impactar.
     * Las balas especiales (como SplitBullet) sobrescriben este método.
     */
    protected void onHit(Entity enemy) {
        enemy.receiveDamage(damage);
    }

    public void render(Batch batch) {
        if (sprite != null && alive) {
            // Dibujamos la textura centrada en la posición de la bala
            batch.draw(sprite,
                position.x - radius,
                position.y - radius,
                radius * 2,
                radius * 2
            );
        }
    }

    // --- Getters y Setters ---

    public boolean isAlive() { return alive; }

    public Vector2 getPosicion() { return position; } // Nombre consistente con tus otras clases

    public Vector2 getDirection() { return direction; }

    public float getRadius() { return radius; }

    public Entity getOwner() { return owner; }

    /**
     * Calcula la velocidad vectorial actual (Dirección * Rapidez).
     * Útil para que balas hijas (como la fragmentaria) sepan con qué fuerza salir.
     */
    public Vector2 getVelocity() {
        return new Vector2(direction).scl(speed);
    }
}
