package com.tikisadventure.components;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.entities.base.Component;
import com.tikisadventure.entities.base.Entity;

//Hacer que un proyectil vuele hacia adelante y vuelva al dueño como un boomerang
public class BoomerangComponent implements Component {
    private final float maxDistance;
    private final Weapon weapon;

    private boolean returning = false;
    private boolean caughtByPlayer = false;
    private float distanceTraveled = 0f;

    private boolean inGracePeriod = false;
    private float graceDistanceTraveled = 0f;
    private final float OVERSHOOT_LIMIT = 2.5f;

    public BoomerangComponent(float maxDistance, Weapon weapon) {
        this.maxDistance = maxDistance;
        this.weapon = weapon;
    }

    public void onAttach(Projectile p) {
        if (weapon != null) weapon.activeBoomerangs++;
        p.setPenetration(0);
        p.setLifetime(10f);
        p.setSensorMode(false);
    }

    @Override
    public void tick(Object owner, float delta, Array<Entity> enemies) {
        if (!(owner instanceof Projectile)) return;
        Projectile p = (Projectile) owner;

        //Atravesar al enemigo antes de volver
        if (inGracePeriod) {
            float frameDist = p.getSpeed() * delta;
            graceDistanceTraveled += frameDist;

            if (graceDistanceTraveled >= OVERSHOOT_LIMIT) {
                inGracePeriod = false;
                startReturn(p);
            }
        }
        //Ir hacia el enemigo
        else if (!returning) {
            distanceTraveled += p.getSpeed() * delta;
            if (distanceTraveled >= maxDistance) {
                startReturn(p);
            }
        }
        //Volver al dueño
        else {
            Entity pOwner = p.getOwner();
            if (pOwner != null && pOwner.isAlive()) {
                Vector2 ownerPos = pOwner.getPosition();
                Vector2 pPos = p.getPosition();

                if (pPos.dst(ownerPos) < 1.0f) {
                    caughtByPlayer = true;
                    p.die(enemies);
                } else {
                    p.setDirection(new Vector2(ownerPos).sub(pPos).nor());
                }
            } else {
                caughtByPlayer = true;
                p.die(enemies);
            }
        }
    }

    private void startReturn(Projectile p) {
        if (returning) return;
        returning = true;
        p.clearHitTimes();
        p.setSensorMode(true);
        p.setPenetration(999);
    }

    @Override
    public void onDeath(Object owner, Array<Entity> enemies) {
        if (!(owner instanceof Projectile)) return;
        Projectile p = (Projectile) owner;

        if (caughtByPlayer || p.isExpired()) {
            if (weapon != null) {
                weapon.activeBoomerangs--;
                if (weapon.activeBoomerangs < 0) weapon.activeBoomerangs = 0;
            }
        }
        else {
            p.setAlive(true);

            if (!returning && !inGracePeriod) {
                inGracePeriod = true;
                graceDistanceTraveled = 0f;
                p.setPenetration(999);
            }
        }
    }

    @Override
    public void dispose() {}
}
