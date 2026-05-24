package com.tikisadventure.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.floors.FloorManager;

//Hacer que un proyectil rebote contra enemigos y bordes del mapa
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
        //Rebotar contra enemigos
        if (projectile != null && remainingBounces > 0 && projectile.canPenetrate()) {
            Vector2 projPos = projectile.getPosition();
            Vector2 targetPos = target.getPosition();

            Vector2 normal = new Vector2(projPos).sub(targetPos).nor();
            Vector2 dir = projectile.getDirection();
            float dot = dir.dot(normal);
            dir.sub(normal.scl(2 * dot));
            projectile.setDirection(dir);

            projectile.clearHitTimes();
            remainingBounces--;

            projectile.getPosition().mulAdd(dir, 0.6f);
        }
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> entities) {
        if (projectile == null || remainingBounces <= 0 || !projectile.canPenetrate()) return;

        //Rebotar contra bordes del mapa
        FloorManager fm = FloorManager.getInstance();
        if (fm != null) {
            Vector2 pos = projectile.getPosition();

            if (fm.isWall(pos.x, pos.y)) {

                int mapWidth = fm.getCollisionLayer() != null ? fm.getCollisionLayer().getWidth() : 32;
                int mapHeight = fm.getCollisionLayer() != null ? fm.getCollisionLayer().getHeight() : 32;

                boolean isBorder = pos.x <= 2 || pos.x >= mapWidth - 2 || pos.y <= 2 || pos.y >= mapHeight - 2;

                if (isBorder) {
                    Vector2 dir = projectile.getDirection();

                    float prevX = pos.x - dir.x * projectile.getSpeed() * delta;
                    float prevY = pos.y - dir.y * projectile.getSpeed() * delta;

                    boolean hitX = fm.isWall(pos.x, prevY);
                    boolean hitY = fm.isWall(prevX, pos.y);

                    if (hitX) dir.x = -dir.x;
                    if (hitY) dir.y = -dir.y;
                    if (!hitX && !hitY) {
                        dir.x = -dir.x;
                        dir.y = -dir.y;
                    }

                    projectile.setDirection(dir);
                    projectile.clearHitTimes();
                    remainingBounces--;

                    projectile.getPosition().set(prevX, prevY).mulAdd(dir, 0.2f);
                }
            }
        }
    }
}
