package com.tikisadventure.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Pool;
import com.tikisadventure.core.Assets;

public class FloatingText implements Pool.Poolable {
    public float x, y;
    public float vx, vy;
    public float lifeTime;
    public float alpha;
    public String text;
    public boolean active;
    public boolean isCritical;
    public Color color = Color.WHITE;

    // Nuevas variables
    public float scaleMult = 1.0f;
    public boolean useFont = false;
    private static BitmapFont defaultFont; // Fuente genérica cargada 1 sola vez

    private static final float GRAVITY = -15.0f;
    private static final float INITIAL_VY = 5.0f;
    private static final float DIGIT_WIDTH = 0.3f;
    private static final float DIGIT_HEIGHT = 0.5f;

    public FloatingText() {
        if (defaultFont == null) {
            defaultFont = new BitmapFont();
            // IMPORTANTE para que las fuentes no vibren en mundos pequeños (como el tuyo de 20x20)
            defaultFont.setUseIntegerPositions(false);
        }
    }

    public void init(float x, float y, String text, boolean isCritical, Color baseColor, float scaleMult, boolean useFont) {
        this.x = x;
        this.y = y;
        this.vx = (float) (Math.random() * 2.0 - 1.0);
        this.vy = INITIAL_VY;
        this.lifeTime = 0.8f;
        this.alpha = 1.0f;
        this.text = text;
        this.active = true;
        this.isCritical = isCritical;
        this.color = isCritical ? Color.YELLOW : baseColor;
        this.scaleMult = scaleMult;
        this.useFont = useFont;
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
        float scale = (isCritical ? 1.5f : 1.0f) * scaleMult;

        if (useFont) {
            float fontScale = scale * 0.04f;
            defaultFont.getData().setScale(fontScale);

            // Grosor del contorno negro (ajustado a tu cámara de 20x20)
            float offset = 0.02f;

            // 1. Dibujamos el contorno negro (4 direcciones)
            defaultFont.setColor(0, 0, 0, alpha); // Negro transparente según la vida del texto
            defaultFont.draw(batch, text, x - offset, y);
            defaultFont.draw(batch, text, x + offset, y);
            defaultFont.draw(batch, text, x, y - offset);
            defaultFont.draw(batch, text, x, y + offset);

            // 2. Dibujamos el texto original de color encima
            defaultFont.setColor(color.r, color.g, color.b, alpha);
            defaultFont.draw(batch, text, x, y);

            defaultFont.getData().setScale(1.0f); // Reset
        } else {
            float currentX = x;
            float scaledWidth = DIGIT_WIDTH * scale;
            float scaledHeight = DIGIT_HEIGHT * scale;

            for (int i = 0; i < text.length(); i++) {
                int digit = Character.getNumericValue(text.charAt(i));
                if (digit >= 0 && digit <= 9) {
                    batch.draw(Assets.numberRegions[digit], currentX, y, scaledWidth, scaledHeight);
                    currentX += scaledWidth;
                }
            }
        }
        batch.setColor(1, 1, 1, 1);
    }

    @Override
    public void reset() {
        active = false;
    }
}
