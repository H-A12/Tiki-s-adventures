package com.tikisadventure.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.floors.FloorManager;

public class BounceComponent implements Component {
    private int remainingBounces;
    private Projectile projectile;

    public BounceComponent(int maxBounces) {
        this.remainingBounces = maxBounces;
    }

    @Override
    public void onAttach(Object owner) {
        if (owner instanceof Projectile) {
            this.projectile = (Projectile) owner;
        }
    }

    @Override
    public void onHit(Entity target) {
        // 1. REBOTE CONTRA ENEMIGOS
        if (projectile != null && remainingBounces > 0 && projectile.canPenetrate()) {
            Vector2 projPos = projectile.getPosition();
            Vector2 targetPos = target.getPosition();

            // Calculamos vector normal y reflejamos dirección
            Vector2 normal = new Vector2(projPos).sub(targetPos).nor();
            Vector2 dir = projectile.getDirection();
            float dot = dir.dot(normal);
            dir.sub(normal.scl(2 * dot));
            projectile.setDirection(dir);

            projectile.clearHitTimes();
            remainingBounces--;

            // Empujamos la bala para sacarla de la hitbox
            projectile.getPosition().mulAdd(dir, 0.6f);
        }
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {
        if (projectile == null || remainingBounces <= 0 || !projectile.canPenetrate()) return;

        // 2. REBOTE SÓLO CONTRA LOS BORDES DEL MAPA
        FloorManager fm = FloorManager.getInstance();
        if (fm != null) {
            Vector2 pos = projectile.getPosition();

            // Comprobamos si la bala ha tocado un muro (ya sea borde, roca o árbol)
            if (fm.isWall(pos.x, pos.y)) {

                // Obtenemos el ancho y alto del nivel (por defecto suele ser 32)
                int mapWidth = fm.getCollisionLayer() != null ? fm.getCollisionLayer().getWidth() : 32;
                int mapHeight = fm.getCollisionLayer() != null ? fm.getCollisionLayer().getHeight() : 32;

                // ¿Es este muro un borde del mapa? (Comprobamos si está en el margen exterior de 2 casillas)
                boolean isBorder = pos.x <= 2 || pos.x >= mapWidth - 2 || pos.y <= 2 || pos.y >= mapHeight - 2;

                // Si es el borde del mapa, calculamos el rebote
                if (isBorder) {
                    Vector2 dir = projectile.getDirection();

                    // Retrocedemos virtualmente la bala para saber con qué cara del bloque hemos chocado
                    float prevX = pos.x - dir.x * projectile.getSpeed() * delta;
                    float prevY = pos.y - dir.y * projectile.getSpeed() * delta;

                    boolean hitX = fm.isWall(pos.x, prevY);
                    boolean hitY = fm.isWall(prevX, pos.y);

                    // Reflejamos el eje correspondiente
                    if (hitX) dir.x = -dir.x;
                    if (hitY) dir.y = -dir.y;
                    if (!hitX && !hitY) {
                        // Ha dado justo en la esquina exacta
                        dir.x = -dir.x;
                        dir.y = -dir.y;
                    }

                    projectile.setDirection(dir);
                    projectile.clearHitTimes();
                    remainingBounces--;

                    // Teletransportamos la bala un poco hacia afuera del muro para que no se atasque
                    projectile.getPosition().set(prevX, prevY).mulAdd(dir, 0.2f);
                }
                // NOTA: Si NO es un borde (isBorder == false), no hacemos nada.
                // La bala se quedará dentro de la roca/árbol y el sistema de colisiones normal del juego la destruirá.
            }
        }
    }
}
