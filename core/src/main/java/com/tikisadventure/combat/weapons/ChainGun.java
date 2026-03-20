package com.tikisadventure.combat.weapons;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.entities.Entity;

public class ChainGun extends Weapon {

    private static Texture texture = new Texture("gun.png");

    private Array<ChainLightning> lightnings = new Array<>();

    private int bounces;
    private float range;
    private float bounceDelay;

    public ChainGun(Entity owner, int bounces, float damage, float range, float cd, float bounceDelay) {
        super(owner);

        this.sprite = new TextureRegion(texture);

        this.bounces = bounces;
        this.damage = damage;
        this.range = range;
        this.shootRange = range;
        this.cd = cd;
        this.bounceDelay = bounceDelay;
    }

    @Override
    protected void shoot() {
        if (objetive == null) return;

        Vector2 startPos = worldPosition.cpy();

        ChainLightning lightning = new ChainLightning(
            startPos,
            objetive,
            damage,
            bounces,
            bounceDelay
        );

        lightnings.add(lightning);
    }

    @Override
    public void update(float delta, Array<Entity> enemies) {
        super.update(delta, enemies);

        for (int i = lightnings.size - 1; i >= 0; i--) {
            ChainLightning cl = lightnings.get(i);

            cl.update(delta, enemies);

            if (!cl.isActive()) {
                lightnings.removeIndex(i);
            }
        }
    }

    @Override
    public void render(Batch batch) {
        super.render(batch);

        for (ChainLightning cl : lightnings) {
            drawLightning(batch, cl);
        }
    }

    private void drawLightning(Batch batch, ChainLightning lightning) {
        Array<Vector2> positions = lightning.getHitPositions();
        if (positions.size < 1) return;

        Texture tex = getLightningTexture();
        Sprite sprite = new Sprite(tex);

        Vector2 startPos = lightning.getStartPosition();

        // Dibujar desde el arma hasta el primer enemigo
        Vector2 firstHit = positions.get(0);
        drawLineWithRotation(batch, sprite, startPos, firstHit);

        // Dibujar líneas entre los enemigos golpeados
        for (int i = 0; i < positions.size - 1; i++) {
            drawLineWithRotation(batch, sprite, positions.get(i), positions.get(i + 1));
        }
    }

    private void drawLineWithRotation(Batch batch, Sprite sprite, Vector2 from, Vector2 to) {
        float dx = to.x - from.x;
        float dy = to.y - from.y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        if (length < 0.01f) return;

        // Calcular ángulo en radianes
        float angle = (float) Math.atan2(dy, dx);

        // Convertir a grados y ajustar para libGDX
        float angleDeg = (float) Math.toDegrees(angle);

        float width = 0.25f;

        sprite.setSize(length, width);
        sprite.setPosition(from.x, from.y - width / 2);
        sprite.setOrigin(0, width / 2);
        sprite.setRotation(angleDeg);
        sprite.draw(batch);
    }

    private Texture lightningTexture;

    private Texture getLightningTexture() {
        if (lightningTexture == null) {
            com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pixmap.setColor(0.3f, 0.6f, 1f, 1f);
            pixmap.fill();
            lightningTexture = new Texture(pixmap);
            pixmap.dispose();
        }
        return lightningTexture;
    }

    public Array<ChainLightning> getLightnings() {
        return lightnings;
    }
}
