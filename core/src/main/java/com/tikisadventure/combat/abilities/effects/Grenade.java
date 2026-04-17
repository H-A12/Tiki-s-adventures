package com.tikisadventure.combat.abilities.effects;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.core.Assets;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;

public class Grenade extends Entity {
    private Vector2 velocity;
    private float arcY;
    private float maxArc;
    private float lifetime;
    private float totalLifetime;
    private TextureRegion sprite;
    private Array<AbilityEffect> onHitEffects;
    private Player owner;
    private Array<Entity> enemies;

    public Grenade(Player owner, Array<Entity> enemies, Vector2 pos, Vector2 dir, float speed, float lifetime, float maxArc, String spriteName, Array<AbilityEffect> onHitEffects) {
        this.owner = owner;
        this.enemies = enemies;
        this.positionComponent.posicion.set(pos);
        this.velocity = dir.nor().scl(speed);
        this.totalLifetime = lifetime;
        this.lifetime = lifetime;
        this.maxArc = maxArc;
        this.sprite = Assets.getRegion("shared", spriteName);
        this.onHitEffects = onHitEffects;
        setANCHO(1.0f);
        setALTO(1.0f);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        lifetime -= delta;
        positionComponent.posicion.mulAdd(velocity, delta);
        
        float t = 1 - (lifetime / totalLifetime);
        arcY = 4 * maxArc * t * (1 - t);
        
        if (lifetime <= 0) {
            die();
            for (AbilityEffect effect : onHitEffects) {
                effect.execute(owner, enemies, positionComponent.posicion);
            }
        }
    }

    @Override
    public void draw(Batch batch, float delta) {
        if (sprite == null) return;
        float width = 1.0f;
        float height = width * ((float)sprite.getRegionHeight() / sprite.getRegionWidth());
        batch.draw(sprite, positionComponent.posicion.x - width / 2f, positionComponent.posicion.y - height / 2f + arcY, width, height);
    }

    @Override public void update(float delta, Entity target) { update(delta); }
}
