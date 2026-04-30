package com.tikisadventure.combat.abilities.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.GenericParticle;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.floors.FloorManager;

public class TeleportEffect implements AbilityEffect {
    private final EffectManager effectManager;
    private final String profile;

    public TeleportEffect(EffectManager em, String profile) {
        this.effectManager = em;
        this.profile = profile;
    }

    @Override
    public boolean execute(Player owner, Array<Entity> enemies, Vector2 targetPosition) {
        Vector2 safePos = findSafePosition(targetPosition);

        // 1. Creamos el fantasma en la posición de ORIGEN (sin explosión)
        spawnGhost(owner);

        // 2. Teletransportar al jugador
        owner.getPosition().set(safePos);

        // 3. Efecto visual (la magia) SOLO en la posición de DESTINO
        spawnVFX(safePos);

        return true;
    }

    /**
     * Crea un "fantasma" que se queda fijo desvaneciéndose en 2 segundos.
     */
    private void spawnGhost(Player owner) {
        float width = owner.getANCHO();

        EffectManager.EffectConfig ghostConfig = new EffectManager.EffectConfig();
        ghostConfig.size = width;
        ghostConfig.life = 2.0f;
        ghostConfig.fade = true;
        ghostConfig.physics = false;
        ghostConfig.randomRotation = false;
        ghostConfig.angle = 0;

        ghostConfig.startColor = new Color(0.2f, 1.0f, 1.0f, 0.6f);
        ghostConfig.endColor = new Color(0.2f, 1.0f, 1.0f, 0.0f);

        TextureRegion currentFrame;
        Vector2 input = owner.getInputDirection();

        if (input.isZero()) {
            currentFrame = owner.getProfile().idle.getKeyFrame(owner.getStateTime(), true);
        } else if (Math.abs(input.y) > Math.abs(input.x)) {
            currentFrame = (input.y > 0) ? owner.getProfile().up.getKeyFrame(owner.getStateTime(), true) : owner.getProfile().down.getKeyFrame(owner.getStateTime(), true);
        } else {
            currentFrame = (input.x > 0) ? owner.getProfile().right.getKeyFrame(owner.getStateTime(), true) : owner.getProfile().left.getKeyFrame(owner.getStateTime(), true);
        }

        effectManager.spawnEffectCustom(ghostConfig, currentFrame, owner.getPosition(), Vector2.Zero);
    }

    private void spawnVFX(Vector2 pos) {
        if (profile != null && !profile.isEmpty()) {
            EffectManager.ExplosionProfile expProfile = effectManager.getExplosionProfile(profile);
            if (expProfile != null) {
                if (expProfile.spritesheet != null) {
                    effectManager.spawnEffect(expProfile.spritesheet, pos, new Vector2(0, 0));
                }
                if (expProfile.smoke != null) {
                    effectManager.spawnEffect(expProfile.smoke, pos, new Vector2(0, 0));
                }
                if (expProfile.sparks != null) {
                    effectManager.spawnEffect(expProfile.sparks, pos, new Vector2(0, 0));
                }
            }
        }
    }

    private Vector2 findSafePosition(Vector2 startPos) {
        FloorManager fm = FloorManager.getInstance();
        if (fm == null) return startPos;

        float mapWidth = fm.getCollisionLayer() != null ? fm.getCollisionLayer().getWidth() : 100;
        float mapHeight = fm.getCollisionLayer() != null ? fm.getCollisionLayer().getHeight() : 100;
        float margin = 0.8f;

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
        float offset = 0.4f;
        return fm.isWall(cx, cy) || fm.isWall(cx - offset, cy) || fm.isWall(cx + offset, cy) ||
            fm.isWall(cx, cy - offset) || fm.isWall(cx, cy + offset);
    }
}
