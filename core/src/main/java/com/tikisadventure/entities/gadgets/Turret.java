package com.tikisadventure.entities.gadgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.combat.weapons.ProjectileCreator;
import com.tikisadventure.core.Assets;
import com.tikisadventure.entities.base.Entity;

public class Turret extends Entity {
    private float timer;
    private final float duration;
    private final ProjectileCreator projectileCreator;
    private final TextureRegion footRegion;
    private final TextureRegion headRegion;
    private final float fireRate;
    private final float damage;
    private final float range;
    private Entity target;
    private float fireCooldown;
    private float visualAngle = 0f;
    private float recoilRotation = 0f;
    private final Array<Projectile> projectiles = new Array<>();

    public Turret(Vector2 position, float duration, ProjectileCreator projectileCreator, float fireRate, float damage, float range) {
        this.getPosition().set(position);
        this.timer = duration;
        this.duration = duration;
        this.projectileCreator = projectileCreator;
        this.fireRate = fireRate;
        this.damage = damage;
        this.range = range;
        this.fireCooldown = 0f;

        this.footRegion = Assets.getRegion("shared", "weapons_assets/TurretFoot");
        this.headRegion = Assets.getRegion("shared", "weapons_assets/TurretHead");

        setANCHO(1.2f);
        setALTO(1.2f);
        actualizarHitboxes();
    }

    @Override
    public void update(float delta, Array<Entity> enemies) {
        if (!isAlive()) return;

        timer -= delta;

        if (timer <= 3f) {
            float blink = (float) Math.abs(Math.sin(timer * 15f));
            getTintColor().set(1f, 1f, 1f, 1f).lerp(Color.BLACK, blink * 0.7f);
        }

        if (timer <= 0) {
            setAlive(false);
            return;
        }

        if (fireCooldown > 0) {
            fireCooldown -= delta;
        }

        if (recoilRotation > 0) {
            recoilRotation -= delta * 1440f;
            if (recoilRotation < 0) recoilRotation = 0;
        }

        searchTarget(enemies);
        if (target != null && target.isAlive()) {
            Vector2 toTarget = new Vector2(target.getPosition()).sub(getPosition());
            float targetAngle = toTarget.angleDeg();
            visualAngle = MathUtils.lerp(visualAngle, targetAngle, 10f * delta);

            if (fireCooldown <= 0) {
                fire(target);
                fireCooldown = fireRate;
            }
        }
    }

    private void searchTarget(Array<Entity> enemies) {
        if (target != null && target.isAlive()) {
            float dist = getPosition().dst(target.getPosition());
            if (dist <= range) return;
        }

        target = null;
        float minDist = range * range;

        for (Entity e : enemies) {
            if (!e.isAlive()) continue;
            float distSq = getPosition().dst2(e.getPosition());
            if (distSq < minDist) {
                minDist = distSq;
                target = e;
            }
        }
    }

    private void fire(Entity target) {
        Vector2 dir = new Vector2(target.getPosition()).sub(getPosition()).nor();
        Vector2 spawnPos = new Vector2(getPosition()).add(0, 0.5f).add(dir.cpy().scl(0.6f));

        Projectile p = projectileCreator.create(
            spawnPos, dir, 12f, damage, 0.25f,
            Assets.getRegion("shared", "particle_assets/TurretBullet"),
            null, null, 0f, 1.5f,
            0.1f, 1.5f, 5f, this
        );

        if (p != null) {
            projectiles.add(p);
            recoilRotation = 360f;
        }
    }

    public Array<Projectile> getProjectiles() {
        return projectiles;
    }

    @Override
    public void draw(Batch batch, float delta) {
        Color prevColor = batch.getColor();
        batch.setColor(getTintColor());

        float size = 1.2f;

        if (footRegion != null) {
            batch.draw(footRegion, getPosition().x - size/2f, getPosition().y - size/2f, size, size);
        }

        if (headRegion != null) {
            float headX = getPosition().x;
            float headY = getPosition().y + 0.5f;
            float drawAngle = visualAngle + recoilRotation;

            boolean flipped = drawAngle > 90 && drawAngle < 270;

            batch.draw(headRegion, headX - size/2f, headY - size/2f, size/2f, size/2f, size, size, 1f, flipped ? -1f : 1f, drawAngle);
        }

        batch.setColor(prevColor);

        for (Projectile p : projectiles) {
            p.render(batch);
        }
    }

    @Override public void update(float delta, Entity target) {}

    @Override
    public void dispose() {
    }
}
