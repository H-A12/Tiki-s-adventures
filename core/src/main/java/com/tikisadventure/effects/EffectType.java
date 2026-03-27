package com.tikisadventure.effects;

public enum EffectType {
    CASQUILLO_PISTOLA("bulletcasing.png", 0.5f, 1.5f, true, true, 90f),
    CASQUILLO_ESCOPETA("shotguncasing.png", 0.8f, 2.0f, true, true, 90f),
    //MUZZLE_FLASH("gun.png", 0.8f, 0.05f, false, false, 0f),
    HUELLA_PISADA("gun.png", 0.3f, 3.0f, false, true, 0f),
    CHISPA_IMPACTO("gun.png", 0.1f, 0.4f, true, true, 0f),
    EXPLOSION_HUMO("gun.png", 1.0f, 0.8f, false, true, 0f);

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
