package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;

public abstract class Weapon {
    // Stats base de combate
    protected float cooldown = 1f;
    protected float timer = 0;
    protected float damage = 10f;
    protected float bulletSpeed = 10f;
    protected float bulletSize = 0.2f;
    protected float shootRange = 8f; // Rango de detección

    // Feedback visual (Recoil/Retroceso)
    protected final Vector2 recoilOffset = new Vector2();
    protected float recoilForce = 0.4f;
    protected float recoilRecovery = 8f;

    // Referencias y Posicionamiento
    protected Entity owner;
    protected Entity target;
    protected final Vector2 worldPosition = new Vector2(); // Calculada por WeaponManager

    // Recursos visuales
    protected TextureRegion sprite;
    protected TextureRegion projectileTexture;
    protected ProjectileCreator projectileFactory;
    protected EffectManager effectManager;
    protected float visualAngle;

    /**
     * Interfaz para delegar la creación de proyectiles al sistema global
     */
    public interface ProjectileCreator {
        Projectile create(Vector2 pos, Vector2 dir, float speed, float dmg, float size,
                          TextureRegion tex, EffectManager em, EffectType trailType, float trailInterval);
    }

    public Weapon(Entity owner, ProjectileCreator factory, TextureRegion bulletTex, EffectManager effectManager) {
        this.owner = owner;
        this.projectileFactory = factory;
        this.projectileTexture = bulletTex;
        this.effectManager = effectManager;
    }

    public void update(float delta, Array<Entity> enemies) {
        timer += delta;

        // 1. Buscar objetivo si no hay uno o si el actual murió/se alejó
        if (enemies != null) {
            searchTarget(enemies);
        }

        // 2. Lógica de disparo
        if (target != null && target.isAlive() && timer >= cooldown) {
            shoot();
            timer = 0;
            applyRecoil(recoilForce);
        }

        // 3. Suavizado del retroceso (Vuelve al centro)
        recoilOffset.lerp(Vector2.Zero, recoilRecovery * delta);

        // 4. Actualizar ángulo visual (mirar al enemigo)
        if (target != null) {
            // Calculamos el ángulo desde el arma hacia el enemigo
            Vector2 targetPos = target.getPosicion();
            visualAngle = new Vector2(targetPos.x - worldPosition.x, targetPos.y - worldPosition.y).angleDeg();
        }
    }

    private void searchTarget(Array<Entity> enemies) {
        // Validar si el objetivo actual sigue siendo válido
        if (target != null) {
            if (!target.isAlive() || worldPosition.dst2(target.getPosicion()) > (shootRange * shootRange)) {
                target = null;
            }
        }

        if (target != null) return;

        // Buscar el enemigo más cercano dentro del rango
        Entity closest = null;
        float minDistanceSq = shootRange * shootRange;

        for (Entity e : enemies) {
            if (!e.isAlive()) continue;
            float distSq = worldPosition.dst2(e.getPosicion());
            if (distSq < minDistanceSq) {
                minDistanceSq = distSq;
                closest = e;
            }
        }
        target = closest;
    }

    protected abstract void shoot();

    protected void applyRecoil(float force) {
        if (target == null) return;
        // El retroceso empuja el arma en dirección opuesta al objetivo
        Vector2 dir = new Vector2(target.getPosicion()).sub(worldPosition).nor();
        recoilOffset.set(dir).scl(-force);
    }

    public void render(Batch batch) {
        if (sprite == null) return;

        float w = 1.2f;
        float h = 1.2f;
        float originX = w / 2f;
        float originY = h / 2f;

        // Corrección visual: Evitar que el arma quede boca abajo al apuntar a la izquierda
        float scaleY = (visualAngle > 90 && visualAngle < 270) ? -1f : 1f;

        batch.draw(sprite,
            (worldPosition.x + recoilOffset.x) - originX,
            (worldPosition.y + recoilOffset.y) - originY,
            originX, originY, w, h, 1f, scaleY, visualAngle);
    }

    // --- Getters y Setters ---
    public void setPosition(float x, float y) { worldPosition.set(x, y); }
    public void setOwner(Entity owner) { this.owner = owner; }
    public Vector2 getPosicion() { return worldPosition; }
}
