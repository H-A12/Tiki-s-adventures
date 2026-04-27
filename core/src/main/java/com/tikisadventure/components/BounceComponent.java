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

            // SOLUCIÓN BUG "DOBLE DAÑO": Empujamos la bala para sacarla de la hitbox
            projectile.getPosition().mulAdd(dir, 0.6f);
        }
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {
        if (projectile == null || remainingBounces <= 0 || !projectile.canPenetrate()) return;

        // 2. REBOTE CONTRA EL JUGADOR
        Entity shooter = projectile.getOwner();
        if (shooter != null && shooter.isAlive() && projectile.getStateTime() > 0.15f) {
            float projRadius = projectile.getRadius();
            float shooterRadius = shooter.getHitboxActionTrigger().radius;
            float totalRadius = projRadius + shooterRadius;

            if (projectile.getPosition().dst2(shooter.getPosition()) <= totalRadius * totalRadius) {
                Vector2 projPos = projectile.getPosition();
                Vector2 targetPos = shooter.getPosition();

                Vector2 normal = new Vector2(projPos).sub(targetPos).nor();
                Vector2 dir = projectile.getDirection();
                float dot = dir.dot(normal);
                dir.sub(normal.scl(2 * dot));
                projectile.setDirection(dir);

                projectile.clearHitTimes();
                remainingBounces--;
                projectile.getPosition().mulAdd(dir, 0.6f); // Empujón anti-atrapamiento
                return; // Si rebota en el jugador, no comprobamos paredes en este frame
            }
        }

        // 3. REBOTE CONTRA PAREDES Y OBSTÁCULOS
        FloorManager fm = FloorManager.getInstance();
        if (fm != null) {
            Vector2 pos = projectile.getPosition();

            // Comprobamos si la posición actual ha entrado en un muro
            if (fm.isWall(pos.x, pos.y)) {
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
                    // Ha dado justo en la esquina de un bloque
                    dir.x = -dir.x;
                    dir.y = -dir.y;
                }

                projectile.setDirection(dir);
                projectile.clearHitTimes();
                remainingBounces--;

                // Teletransportamos la bala fuera de la pared para que el sistema no la mate por chocar
                projectile.getPosition().set(prevX, prevY).mulAdd(dir, 0.2f);
            }
        }
    }
}
