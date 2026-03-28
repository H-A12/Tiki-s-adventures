package com.tikisadventure.effects;

public enum EffectType {
    // --- EFECTOS DE CASQUILLOS Y HUELLAS ---
    CASQUILLO_PISTOLA("bulletcasing.png", 0.5f, 1.5f, true, true, 90f),
    CASQUILLO_ESCOPETA("shotguncasing.png", 0.8f, 2.0f, true, true, 90f),
    HUELLA_PISADA("gun.png", 0.3f, 3.0f, false, true, 0f),

    // --- EFECTOS DE IMPACTO Y TRAILS ---
    CHISPA_IMPACTO("bulletcasing.png", 0.1f, 0.4f, true, true, 0f),
    TRAIL_LASER("bluebullet.png", 0.5f, 0.4f, false, true, 0f),
    TRAIL_SMOKE("graybullet.png", 0.8f, 0.4f, false, true, 0f),

    // --- COMPONENTES DE LA NUEVA EXPLOSIÓN ---
    // El Flash es el destello inicial brillante
    EXPLOSION_FLASH("yellowbullet.png", 3.0f, 0.1f, false, true, 0f),
    // El Humo es la nube que se expande
    EXPLOSION_HUMO("graybullet.png", 1.5f, 0.8f, false, true, 0f),
    // Las Chispas son los fragmentos que salen volando
    EXPLOSION_CHISPA("bulletcasing.png", 0.5f, 0.5f, true, true, 0f);

    public final String textureName;
    public final float baseSize;
    public final float lifeTime;
    public final boolean hasPhysics;
    public final boolean fadeOut;
    public final float ejectionAngle;

    EffectType(String tex, float size, float life, boolean physics, boolean fade, float angle) {
        this.textureName = tex;
        this.baseSize = size;
        this.lifeTime = life;
        this.hasPhysics = physics;
        this.fadeOut = fade;
        this.ejectionAngle = angle;
    }
}
