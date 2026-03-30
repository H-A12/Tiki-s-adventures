package com.tikisadventure.combat.projectiles;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.base.components.*;
import com.tikisadventure.entities.base.components.Component;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.effects.EffectManager;
import com.tikisadventure.effects.EffectType;

public class Projectile extends Entity implements HasPosition, HasDirection, HasSpeed, HasDamage,
    HasRadius, HasOwner, Timed, Sensorable {

    private final Vector2 direction = new Vector2();
    private final Vector2 lastTrailPos = new Vector2();
    private final Vector2 tempPos = new Vector2();
    private final Vector2 trailDir = new Vector2();
    private final Array<Component> behaviors = new Array<>();

    private float currentRadius;
    private boolean sensorMode = false;
    private Entity projectileOwner;

    private EffectManager effectManager;
    private EffectType trailType;
    private float trailSpacing;

    // Animación interna para cumplir con el contrato de Entity
    protected Animation<TextureRegion> defaultAnimation;

    public Projectile(Entity owner, Vector2 pos, Vector2 dir, float speed, float dmg, float radius,
                      TextureRegion sprite, EffectManager em, EffectType trailType, float trailSpacing) {
        super(pos.x, pos.y);
        this.projectileOwner = owner;
        this.lastTrailPos.set(pos);
        this.direction.set(dir).nor();

        this.speed = speed;
        this.danyo = dmg;

        this.currentRadius = radius;
        this.ANCHO = radius * 2;
        this.ALTO = radius * 2;

        this.sprite = sprite;
        this.effectManager = em;
        this.trailType = trailType;
        this.trailSpacing = trailSpacing;

        // Creamos una animación de un solo frame para que el AnimationSystem no falle
        if (sprite != null) {
            this.defaultAnimation = new Animation<>(0.1f, sprite);
        }
    }

    /**
     * IMPLEMENTACIÓN OBLIGATORIA: Para proyectiles, devolvemos siempre
     * la animación por defecto independientemente del estado.
     */
    @Override
    public Animation<TextureRegion> getAnimationForState(Estado estado) {
        return defaultAnimation;
    }

    public void addBehavior(Component behavior) {
        behaviors.add(behavior);
        behavior.onAttach(this);
    }

    @Override
    public void update(float delta) {
        if (!alive) return;
        stateTime += delta;

        // 1. Ejecutar comportamientos (Tick de componentes como Shrapnel, Homing, etc.)
        for (Component c : behaviors) {
            c.tick(this, delta, null);
        }

        // 2. Gestión de estelas (Particles/Trails)
        handleTrail();

        // 3. Sincronizar hitboxes de la clase base Entity para colisiones
        actualizarHitboxes();
    }

    private void handleTrail() {
        if (trailType != null && effectManager != null && trailSpacing > 0) {
            float distMoved = posicion.dst(lastTrailPos);
            if (distMoved >= trailSpacing) {
                int count = (int) (distMoved / trailSpacing);
                trailDir.set(direction).scl(-0.5f);

                for (int i = 0; i < count; i++) {
                    float t = (float) i / count;
                    tempPos.set(lastTrailPos).lerp(posicion, t);
                    effectManager.spawnEffect(trailType, tempPos, trailDir);
                }
                lastTrailPos.set(posicion);
            }
        }
    }

    @Override
    public void render(Batch batch, float delta) {
        // Usamos el sprite inyectado por AnimationSystem o el original
        TextureRegion regionToDraw = (sprite != null) ? sprite : (defaultAnimation != null ? defaultAnimation.getKeyFrame(stateTime) : null);

        if (!alive || regionToDraw == null) return;

        float angle = direction.angleDeg();
        float width = currentRadius * 2;
        float aspectRatio = (float) regionToDraw.getRegionHeight() / regionToDraw.getRegionWidth();
        float height = width * aspectRatio;

        batch.draw(regionToDraw,
            posicion.x - width / 2f, posicion.y - height / 2f,
            width / 2f, height / 2f, width, height, 1f, 1f, angle);
    }

    // --- IMPLEMENTACIÓN DE INTERFACES ---

    @Override public Vector2 getPosition() { return posicion; }
    @Override public void setPosition(Vector2 pos) { this.posicion.set(pos); }

    @Override public Vector2 getDirection() { return direction; }
    @Override public void setDirection(Vector2 dir) { this.direction.set(dir).nor(); }

    @Override public float getSpeed() { return speed; }
    @Override public void setSpeed(float speed) { this.speed = speed; }

    @Override public float getDamage() { return danyo; }
    @Override public void setDamage(float damage) { this.danyo = damage; }

    @Override public float getRadius() { return currentRadius; }
    @Override public void setRadius(float radius) { this.currentRadius = radius; }

    @Override public boolean isSensorMode() { return sensorMode; }
    @Override public void setSensorMode(boolean sensorMode) { this.sensorMode = sensorMode; }

    @Override public Entity getOwner() { return projectileOwner; }
    @Override public void setOwner(Object owner) { this.projectileOwner = (Entity) owner; }

    @Override public float getStateTime() { return stateTime; }
    @Override public void setStateTime(float time) { this.stateTime = time; }
}
