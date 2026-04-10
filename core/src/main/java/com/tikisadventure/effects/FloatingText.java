package com.tikisadventure.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Pool;
import com.tikisadventure.core.Assets;

public class FloatingText implements Pool.Poolable {
    public float x, y;
    public float vx, vy;
    public float lifeTime;
    public float alpha;
    public String text;
    public boolean active;
    public Color color = Color.WHITE;

    private static final float GRAVITY = -15.0f;
    private static final float INITIAL_VY = 5.0f;
    private static final float DIGIT_WIDTH = 0.3f;
    private static final float DIGIT_HEIGHT = 0.5f;

    public void init(float x, float y, float damage, boolean isCritical, Color baseColor) {
        this.x = x;
        this.y = y;
        this.vx = (float) (Math.random() * 2.0 - 1.0);
        this.vy = INITIAL_VY;
        this.lifeTime = 0.8f;
        this.alpha = 1.0f;
        this.text = String.valueOf((int) damage);
        this.active = true;
        this.color = isCritical ? Color.YELLOW : baseColor;
    }

    public void update(float delta) {
        vy += GRAVITY * delta;
        x += vx * delta;
        y += vy * delta;
        lifeTime -= delta;
        alpha = Math.max(0, lifeTime / 0.8f);
        if (lifeTime <= 0) active = false;
    }

    public void render(Batch batch) {
        batch.setColor(color.r, color.g, color.b, alpha);
        float currentX = x;
        for (int i = 0; i < text.length(); i++) {
            int digit = Character.getNumericValue(text.charAt(i));
            if (digit >= 0 && digit <= 9) {
                batch.draw(Assets.numberRegions[digit], currentX, y, DIGIT_WIDTH, DIGIT_HEIGHT);
                currentX += DIGIT_WIDTH;
            }
        }
        batch.setColor(1, 1, 1, 1);
    }

    @Override
    public void reset() {
        active = false;
    }
}
