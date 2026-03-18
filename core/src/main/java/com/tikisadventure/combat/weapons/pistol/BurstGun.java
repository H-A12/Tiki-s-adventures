package com.tikisadventure.combat.weapons.pistol;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.weapons.Bullet;
import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.entities.Entity;
import effects.Casing;

public class BurstGun extends Weapon {

    // Texturas estáticas
    private static Texture texture;
    private static Texture bulletTexture;
    private static Texture casingTexture;

    private TextureRegion casingRegion;
    private Array<Bullet> bullets = new Array<>();
    private Array<Casing> casings = new Array<>();

    // Variables de ráfaga
    private int burstCount = 5;
    private int currentBurst = 0;
    private float burstDelay = 0.08f;
    private float burstCooldown = 0.4f;
    private float burstTimer = 0;

    private boolean isBursting = false;
    private boolean canBurst = true;

    // Sistema de retroceso (Recoil)
    private float recoilAmount = 0.5f;
    private float recoilRecovery = 6f;
    private float currentRecoil = 0;

    public BurstGun(Entity owner) {
        super(owner);

        if (texture == null) texture = new Texture("uzi.png");
        if (bulletTexture == null) bulletTexture = new Texture("bullet.png");
        if (casingTexture == null) casingTexture = new Texture("casing.png");

        sprite = new TextureRegion(texture);
        casingRegion = new TextureRegion(casingTexture);

        // --- Configuración (Heredada de Weapon) ---
        this.accuracy = 12f;      // Ángulo de dispersión de las balas
        this.cd = 0.8f;             // Cooldown entre ráfagas
        this.damage = 10f;
        this.bulletSpeed = 10f;
        this.bulletSize = 0.15f;
        this.shootRange = 7f;

        // Ángulo inicial: 0 es la posición natural de tu sprite (Izquierda)
        this.visualAngle = 0f;
    }

    private void fireSingleBullet() {
        if (objetive == null) return;

        // 1. Obtener dirección con dispersión (desde la clase Weapon)
        Vector2 dir = getDirectionWithSpread();

        // 2. Crear bala
        Bullet bullet = new Bullet(worldPosition, dir, bulletSpeed, damage, bulletSize, false);
        bullets.add(bullet);

        // 3. Efectos de retroceso y casquillos
        currentRecoil += recoilAmount;
        if (currentRecoil > 0.5f) currentRecoil = 0.5f;

        float sideDir = (objetive.getPosicion().x > owner.getPosicion().x) ? -1f : 1f;
        casings.add(new Casing(worldPosition, new Vector2(sideDir * 2f, 4f)));
    }

    @Override
    protected void shoot() {
        if (!canBurst || isBursting) return;
        isBursting = true;
        currentBurst = 0;
        burstTimer = 0;
        canBurst = false;
    }

    @Override
    public void update(float delta, Array<Entity> enemies) {
        // --- 1. Lógica de Apuntado Independiente ---
        searchEnemy(enemies); // Busca objetivos en el rango

        if (objetive != null && objetive.isAlive()) {
            updateVisual(delta);     // Calcula el ángulo matemático al enemigo
            this.visualAngle += 180; // Parche para sprite orientado a la izquierda
        } else {
            // Posición de descanso: Mirar a la izquierda (0 grados para tu sprite)
            this.visualAngle = 0f;
        }

        // --- 2. Lógica de Ráfaga ---
        if (isBursting) {
            burstTimer += delta;
            if (burstTimer >= burstDelay && currentBurst < burstCount) {
                fireSingleBullet();
                currentBurst++;
                burstTimer = 0;
            }
            if (currentBurst >= burstCount) {
                isBursting = false;
                burstTimer = -burstCooldown;
            }
        } else if (!canBurst) {
            burstTimer += delta;
            if (burstTimer >= 0) canBurst = true;
        }

        // --- 3. Cooldown Global ---
        lastShootTime += delta;
        if (canBurst && !isBursting && objetive != null && lastShootTime >= cd) {
            shoot();
            lastShootTime = 0;
        }

        // --- 4. Recuperación de Retroceso ---
        if (currentRecoil > 0) {
            currentRecoil -= recoilRecovery * delta;
            if (currentRecoil < 0) currentRecoil = 0;
        }

        // --- 5. Actualizar Proyectiles ---
        for (int i = bullets.size - 1; i >= 0; i--) {
            bullets.get(i).update(delta, enemies);
            if (!bullets.get(i).isAlive()) bullets.removeIndex(i);
        }
        for (int i = casings.size - 1; i >= 0; i--) {
            casings.get(i).update(delta);
            if (casings.get(i).lifeTime <= 0) casings.removeIndex(i);
        }
    }

    @Override
    public void render(Batch batch) {
        if (sprite == null) return;

        float width = sprite.getRegionWidth() / 16f;
        float height = sprite.getRegionHeight() / 16f;
        float originX = width / 2f;
        float originY = height / 2f;

        // --- Lógica de Flip Dinámica ---
        // Decidimos si el arma está apuntando a la derecha (entre 90 y 270 grados)
        // para aplicar el espejo vertical y que el mango no quede arriba.
        float scaleY = 1f;
        if (visualAngle > 90 && visualAngle < 270) {
            scaleY = -1f;
        }

        float renderX = worldPosition.x - originX;
        float renderY = worldPosition.y - originY;

        // Aplicar retroceso visual según el ángulo actual
        if (currentRecoil > 0) {
            float angleRad = (float) Math.toRadians(visualAngle);
            renderX -= (float) Math.cos(angleRad) * currentRecoil;
            renderY -= (float) Math.sin(angleRad) * currentRecoil;
        }

        // 1. Render Casquillos
        for (Casing c : casings) {
            batch.draw(casingRegion, c.pos.x - 0.25f, c.pos.y - 0.25f, 0.25f, 0.25f, 0.5f, 0.5f, 1f, 1f, c.rotation);
        }

        // 2. Render Arma
        batch.draw(sprite, renderX, renderY, originX, originY, width, height, 1f, scaleY, visualAngle);

        // 3. Render Balas
        for (Bullet b : bullets) {
            float s = b.getRadius() * 6f;
            batch.draw(bulletTexture, b.getPosition().x - s/2f, b.getPosition().y - s/2f, s, s);
        }
    }
}
