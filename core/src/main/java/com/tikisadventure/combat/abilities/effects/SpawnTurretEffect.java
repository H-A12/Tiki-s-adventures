package com.tikisadventure.combat.abilities.effects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.DamageType;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.combat.weapons.ProjectileCreator;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.gadgets.Turret;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.screens.GameScreen;
import com.tikisadventure.floors.FloorManager;

//Colocar una torreta automática que dispara a los enemigos
public class SpawnTurretEffect implements AbilityEffect {
    private final EffectManager effectManager;
    private final float duration;
    private final float fireRate;
    private final float baseDamage;
    private final float range;
    private final DamageType damageType;
    private final String profile;
    private final ProjectileCreator projectileCreator;

    public SpawnTurretEffect(ProjectileCreator pc, EffectManager effectManager, float duration, float fireRate, float baseDamage, float range, DamageType damageType, String profile) {
        this.effectManager = effectManager;
        this.duration = duration;
        this.fireRate = fireRate;
        this.baseDamage = baseDamage;
        this.range = range;
        this.damageType = damageType;
        this.profile = profile;
        this.projectileCreator = pc;
    }

    @Override
    public boolean execute(Player owner, Array<Entity> enemies, Vector2 targetPosition) {
        Vector2 safePosition = findSafePosition(targetPosition);

        if (GameScreen.activeTurrets == null) {
            GameScreen.activeTurrets = new Array<>();
        }

        float finalDamage = this.baseDamage;
        if (owner != null) {
            float bonus = owner.getDamageBonusByType(this.damageType);
            finalDamage *= (1f + bonus);
        }

        Turret turret = new Turret(effectManager, safePosition, duration, projectileCreator, fireRate, finalDamage, range, damageType, owner, profile);
        GameScreen.activeTurrets.add(turret);

        return true;
    }

    private Vector2 findSafePosition(Vector2 startPos) {
        FloorManager fm = FloorManager.getInstance();
        if (fm == null) return startPos;

        float mapWidth = fm.getCollisionLayer() != null ? fm.getCollisionLayer().getWidth() : 100;
        float mapHeight = fm.getCollisionLayer() != null ? fm.getCollisionLayer().getHeight() : 100;
        float margin = 1.2f;

        Vector2 checkPos = new Vector2(startPos);
        checkPos.x = Math.max(margin, Math.min(mapWidth - margin, checkPos.x));
        checkPos.y = Math.max(margin, Math.min(mapHeight - margin, checkPos.y));

        if (!isOverlappingWall(fm, checkPos.x, checkPos.y)) return checkPos;

        float step = 0.5f;
        for (float r = step; r <= 5.0f; r += step) {
            for (float angle = 0; angle < 360; angle += 45) {
                float rad = (float) Math.toRadians(angle);
                float nx = checkPos.x + (float) Math.cos(rad) * r;
                float ny = checkPos.y + (float) Math.sin(rad) * r;
                nx = Math.max(margin, Math.min(mapWidth - margin, nx));
                ny = Math.max(margin, Math.min(mapHeight - margin, ny));

                if (!isOverlappingWall(fm, nx, ny)) return new Vector2(nx, ny);
            }
        }
        return checkPos;
    }

    private boolean isOverlappingWall(FloorManager fm, float cx, float cy) {
        float offset = 0.55f;
        return fm.isWall(cx, cy) || fm.isWall(cx - offset, cy) || fm.isWall(cx + offset, cy) ||
            fm.isWall(cx, cy - offset) || fm.isWall(cx, cy + offset);
    }
}
