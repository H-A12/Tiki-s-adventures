package com.tikisadventure.effects;

import com.badlogic.gdx.graphics.Color;

public enum EffectType {
    // --- EFECTOS DE CASQUILLOS Y HUELLAS ---
    CASQUILLO_PISTOLA("BulletCasing", 1f, 1.5f, true, true, 90f, Color.WHITE, Color.WHITE, 0.95f),
    CASQUILLO_BOLTER("BulletCasing", 2f, 1.5f, true, true, 80f, Color.WHITE, Color.WHITE, 0.95f),
    CASQUILLO_ESCOPETA("ShotgunCasing", 1.3f, 2.0f, true, true, 90f, Color.WHITE, Color.WHITE, 0.95f),
    HUELLA_PISADA("gun", 0.3f, 3.0f, false, true, 0f, Color.WHITE, Color.WHITE, 1f),

    // --- EFECTOS DE IMPACTO Y TRAILS ---
    CHISPA_IMPACTO("BulletCasing", 0.1f, 0.4f, true, true, 0f, Color.YELLOW, Color.RED, 0.9f),
    TRAIL_LASER("BlueBullet", 0.5f, 0.4f, false, true, 0f, Color.CYAN, Color.BLUE, 1f),
    TRAIL_SMOKE("GrayBullet", 0.8f, 0.4f, false, true, 0f, Color.LIGHT_GRAY, Color.DARK_GRAY, 0.95f),

    // --- COMPONENTES DE LA NUEVA EXPLOSIÓN ---
    EXPLOSION_FLASH("RedBullet", 1.0f, 0.1f, false, true, 0f, Color.YELLOW, Color.ORANGE, 1f),
    EXPLOSION_HUMO("GrayBullet", 1.5f, 0.8f, false, true, 0f, Color.LIGHT_GRAY, Color.BLACK, 0.98f),
    EXPLOSION_CHISPA("BulletCasing", 0.5f, 0.5f, true, true, 0f, Color.YELLOW, Color.RED, 0.9f),
    EXPLOSION_SLIME("GreenBullet", 0.6f, 1.2f, true, true, 0f, Color.GREEN, Color.LIME, 0.9f);

    public final String textureName;
    public final float baseSize;
    public final float lifeTime;
    public final boolean hasPhysics;
    public final boolean fadeOut;
    public final float ejectionAngle;
    public final Color startColor;
    public final Color endColor;
    public final float friction;

    EffectType(String tex, float size, float life, boolean physics, boolean fade, float angle, Color startColor, Color endColor, float friction) {
        this.textureName = tex;
        this.baseSize = size;
        this.lifeTime = life;
        this.hasPhysics = physics;
        this.fadeOut = fade;
        this.ejectionAngle = angle;
        this.startColor = startColor;
        this.endColor = endColor;
        this.friction = friction;
    }
}
