package com.tikisadventure.combat.abilities.effects;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.projectiles.Projectile;
import com.tikisadventure.core.Assets;
import com.tikisadventure.entities.base.Entity;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.effects.EffectManager;

public class GrenadeProjectile extends Projectile {
    private TextureRegion sprite;
    private Array<AbilityEffect> onHitEffects;
    private Player owner;
    private Array<Entity> enemies;

    private float rotationOffset;

    public GrenadeProjectile(Player owner, Array<Entity> enemies, Vector2 pos, Vector2 dir, float speed, float lifetime, String spriteName, Array<AbilityEffect> onHitEffects, EffectManager em, String trailType, float trailSpacing) {
        super(owner, pos, dir, speed, 0, 0, 0, 0.5f, Assets.getRegion("shared", spriteName), em, trailType, trailSpacing);
        this.owner = owner;
        this.enemies = enemies;
        this.onHitEffects = onHitEffects;
        this.sprite = Assets.getRegion("shared", spriteName);
        this.rotationOffset = com.badlogic.gdx.math.MathUtils.random(0f, 360f);
        setLifetime(lifetime);
    }

    @Override
    public void die(Array<Entity> enemies) {
        if (isAlive()) {
            System.out.println("GrenadeProjectile dying! onHitEffects size: " + onHitEffects.size);
            for (AbilityEffect effect : onHitEffects) {
                effect.execute(owner, enemies, getPosition());
            }
        }
        super.die(enemies);
    }

    @Override
    public void render(Batch batch) {
        if (sprite == null) return;
        float width = 1.0f;
        float height = width * ((float)sprite.getRegionHeight() / sprite.getRegionWidth());
        float rotation = rotationOffset + (getStateTime() * 360f);
        batch.draw(sprite, getPosition().x - width / 2f, getPosition().y - height / 2f, width / 2f, height / 2f, width, height, 1f, 1f, rotation);
    }
}
