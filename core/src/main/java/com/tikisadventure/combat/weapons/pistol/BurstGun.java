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

    // Texturas estáticas para no sobrecargar la memoria
    private static Texture texture;
    private static Texture bulletTexture;
    private static Texture casingTexture;

    // Región necesaria para rotar y escalar los casquillos sin errores de tipo
    private TextureRegion casingRegion;

    private Array<Bullet> bullets = new Array<>();
    private Array<Casing> casings = new Array<>();

    // Variables de ráfaga
    private int burstCount = 3;
    private int currentBurst = 0;
    private float burstDelay = 0.08f;
    private float burstCooldown = 0.4f;
    private float burstTimer = 0;

    private boolean isBursting = false;
    private boolean canBurst = true;
    private float shootTimer = 0;

    // Variables de retroceso (Ajustadas para acumulación por bala)
    private float recoilAmount = 0.5f;   // Fuerza por cada bala individual
    private float recoilRecovery = 6f;    // Velocidad de regreso a la mano
    private float currentRecoil = 0;

    public BurstGun(Entity owner) {
        super(owner);

        // Carga segura: solo inicializa si las texturas son nulas
        if (texture == null) texture = new Texture("uzi.png");
        if (bulletTexture == null) bulletTexture = new Texture("bullet.png");
        if (casingTexture == null) casingTexture = new Texture("casing.png");

        sprite = new TextureRegion(texture);
        casingRegion = new TextureRegion(casingTexture);

        this.visualAngle = 180f;
        cd = 0.8f;
        damage = 8f;
        bulletSpeed = 10f;
        bulletSize = 0.15f;
        shootRange = 7f;
    }

    private void fireSingleBullet() {
        if (objetive == null) return;

        // 1. Lógica de Proyectil
        Vector2 dir = new Vector2(
            objetive.getPosicion().x - worldPosition.x,
            objetive.getPosicion().y - worldPosition.y
        ).nor();

        Bullet bullet = new Bullet(worldPosition, dir, bulletSpeed, damage, bulletSize, false);
        bullets.add(bullet);

        // 2. APLICAR RETROCESO (Ahora por cada bala individual)
        currentRecoil += recoilAmount;
        if (currentRecoil > 0.5f) currentRecoil = 0.5f; // Límite para no perder el arma

        // 3. Lógica de Casquillos (Salto hacia arriba)
        float sideDir = (objetive.getPosicion().x > owner.getPosicion().x) ? -1f : 1f;
        float vx = sideDir * (1.5f + (float)Math.random());
        float vy = 4.0f + (float)Math.random() * 2.0f; // Impulso vertical positivo

        casings.add(new Casing(worldPosition, new Vector2(vx, vy)));
    }

    @Override
    protected void shoot() {
        if (!canBurst || isBursting) return;

        // Iniciamos el estado de ráfaga
        isBursting = true;
        currentBurst = 0;
        burstTimer = 0;
        canBurst = false;

        // Ya no aplicamos recoil aquí, se encarga fireSingleBullet
    }

    @Override
    public void update(float delta, Array<Entity> enemies) {
        // --- 1. Buscar objetivo cercano ---
        if (objetive == null || !objetive.isAlive()) {
            Entity closest = null;
            float minDistance = Float.MAX_VALUE;
            for (Entity e : enemies) {
                if (!e.isAlive()) continue;
                float distance = worldPosition.dst2(e.getPosicion());
                if (distance < minDistance && distance <= shootRange * shootRange) {
                    minDistance = distance;
                    closest = e;
                }
            }
            objetive = closest;
        }

        // --- 2. Apuntar visualmente ---
        if (objetive != null) {
            Vector2 dir = new Vector2(objetive.getPosicion().x - worldPosition.x, objetive.getPosicion().y - worldPosition.y);
            visualAngle = dir.angleDeg();
        }

        // --- 3. Manejar ráfaga y cooldown ---
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

        // --- 4. Intentar disparar ---
        shootTimer += delta;
        if (canBurst && !isBursting && objetive != null && objetive.isAlive() && shootTimer >= cd) {
            shoot();
            shootTimer = 0;
        }

        // --- 5. Recuperación de retroceso ---
        if (currentRecoil > 0) {
            currentRecoil -= recoilRecovery * delta;
            if (currentRecoil < 0) currentRecoil = 0;
        }

        // --- 6. Actualizar Balas ---
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet b = bullets.get(i);
            b.update(delta, enemies);
            if (!b.isAlive()) bullets.removeIndex(i);
        }

        // --- 7. Actualizar Casquillos ---
        for (int i = casings.size - 1; i >= 0; i--) {
            Casing c = casings.get(i);
            c.update(delta);
            if (c.lifeTime <= 0) casings.removeIndex(i);
        }
    }

    @Override
    public void render(Batch batch) {
        if (sprite == null) return;

        // Dimensiones y origen
        float width = sprite.getRegionWidth() / 16f;
        float height = sprite.getRegionHeight() / 16f;
        float originX = width / 2f;
        float originY = height / 2f;

        // Espejo (Flip) según la dirección del objetivo
        boolean targetIsRight = (objetive != null && objetive.getPosicion().x > owner.getPosicion().x);
        float scaleY = targetIsRight ? -1f : 1f;
        float renderAngle = visualAngle + 180;

        // Aplicar desplazamiento de retroceso a la posición de renderizado
        float renderX = worldPosition.x - originX;
        float renderY = worldPosition.y - originY;

        if (currentRecoil > 0) {
            float angleRad = (float) Math.toRadians(visualAngle);
            renderX -= (float) Math.cos(angleRad) * currentRecoil;
            renderY -= (float) Math.sin(angleRad) * currentRecoil;
        }

        // 1. Dibujar Casquillos (Detrás del arma)
        for (Casing c : casings) {
            float cSize = 0.5f; // Tamaño del casquillo
            batch.draw(casingRegion, c.pos.x - cSize/2f, c.pos.y - cSize/2f, cSize/2f, cSize/2f, cSize, cSize, 1f, 1f, c.rotation);
        }

        // 2. Dibujar Arma
        batch.draw(sprite, renderX, renderY, originX, originY, width, height, 1f, scaleY, renderAngle);

        // 3. Dibujar Balas
        for (Bullet b : bullets) {
            float displayRadios = b.getRadius() * 6.0f;
            batch.draw(bulletTexture, b.getPosition().x - (displayRadios / 2f), b.getPosition().y - (displayRadios / 2f), displayRadios, displayRadios);
        }
    }
}
