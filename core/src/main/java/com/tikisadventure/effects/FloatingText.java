package com.tikisadventure.effects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Pool;
import com.tikisadventure.core.Assets;
import com.tikisadventure.ui.FontManager;

//Texto flotante o imagen que aparece al hacer daño (números, iconos).
//Implementa Poolable para reciclarse con Pool.
public class FloatingText implements Pool.Poolable {
    public float x, y;
    public float vx, vy;
    public float lifeTime;
    public float alpha;
    public String text;
    public boolean active;
    public boolean isCritical;
    public Color color = Color.WHITE;

    public float scaleMult = 1.0f;
    public boolean useFont = false;
    public boolean useGravity = true;
    public boolean isBlinkEnabled = false;
    private int numBlinks = 0;
    public TextureRegion imageRegion = null; 
    private Color originalColor;
    private static BitmapFont defaultFont;

    private static final float GRAVITY = -15.0f;
    private static final float INITIAL_VY = 5.0f;
    private static final float DIGIT_WIDTH = 0.3f;
    private static final float DIGIT_HEIGHT = 0.5f;

    //Cargar fuente por defecto si no está inicializada
    public FloatingText() {
        if (defaultFont == null) {
            defaultFont = FontManager.getFont(15);
            defaultFont.setUseIntegerPositions(false);
        }
    }

    //Inicializar texto flotante con imagen en vez de texto
    public void initImage(float x, float y, TextureRegion image, Color baseColor, float scaleMult, boolean isBlinkEnabled, int numBlinks, float speedMultiplier) {
        this.x = x;
        this.y = y;
        this.vx = (float) (Math.random() * 2.0 - 1.0);
        this.vy = INITIAL_VY * speedMultiplier;
        this.lifeTime = 0.8f;
        this.alpha = 1.0f;
        this.text = null;
        this.imageRegion = image;
        this.active = true;
        this.isCritical = false;
        this.originalColor = new Color(baseColor);
        this.color = new Color(originalColor);
        this.scaleMult = scaleMult;
        this.useFont = false;
        this.useGravity = false;
        this.isBlinkEnabled = isBlinkEnabled;
        this.numBlinks = numBlinks;
    }

    //Inicializar texto flotante con string, color y opciones visuales
    public void init(float x, float y, String text, boolean isCritical, Color baseColor, float scaleMult, boolean useFont, boolean useGravity, boolean isBlinkEnabled, int numBlinks, float speedMultiplier) {
        this.x = x;
        this.y = y;
        this.vx = (float) (Math.random() * 2.0 - 1.0);
        this.vy = INITIAL_VY * speedMultiplier;
        this.lifeTime = 0.8f;
        this.alpha = 1.0f;
        this.text = text;
        this.imageRegion = null;
        this.active = true;
        this.isCritical = isCritical;
        this.originalColor = new Color(isCritical ? Color.YELLOW : baseColor);
        this.color = new Color(originalColor);
        this.scaleMult = scaleMult;
        this.useFont = useFont;
        this.useGravity = useGravity;
        this.isBlinkEnabled = isBlinkEnabled;
        this.numBlinks = numBlinks;
    }

    //Mover el texto, reducir vida y aplicar parpadeo
    public void update(float delta) {
        if (useGravity) vy += GRAVITY * delta;
        x += vx * delta;
        y += vy * delta;
        lifeTime -= delta;
        alpha = Math.max(0, lifeTime / 0.8f);

        if (isBlinkEnabled && numBlinks > 0) {
            float progress = 1.0f - (lifeTime / 0.8f);
            float factor = (float) (Math.sin(progress * 2.0 * Math.PI * numBlinks) + 1.0) / 2.0f;
            this.color.set(originalColor).lerp(Color.WHITE, factor);
            this.color.a = originalColor.a;
        } else {
            this.color.set(originalColor);
        }

        if (lifeTime <= 0) active = false;
    }

    //Dibujar el texto o imagen con el color y escala actuales
    public void render(Batch batch) {
        batch.setColor(color.r, color.g, color.b, alpha);
        float scale = (isCritical ? 1.5f : 1.0f) * scaleMult;

        if (useFont) {
            float fontScale = scale * 0.04f;
            defaultFont.getData().setScale(fontScale);
            float offset = 0.02f;
            defaultFont.setColor(0, 0, 0, alpha);
            defaultFont.draw(batch, text, x - offset, y);
            defaultFont.draw(batch, text, x + offset, y);
            defaultFont.draw(batch, text, x, y - offset);
            defaultFont.draw(batch, text, x, y + offset);
            defaultFont.setColor(color.r, color.g, color.b, alpha);
            defaultFont.draw(batch, text, x, y);
            defaultFont.getData().setScale(1.0f);
        } else if (imageRegion != null) {
             float scaledWidth = imageRegion.getRegionWidth() * 0.01f * scale; 
             float scaledHeight = imageRegion.getRegionHeight() * 0.01f * scale;
             batch.draw(imageRegion, x, y, scaledWidth, scaledHeight);
        } else if (text != null) {
            float currentX = x;
            float scaledWidth = DIGIT_WIDTH * scale;
            float scaledHeight = DIGIT_HEIGHT * scale;

            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                int regionIndex = -1;
                if (c == '+') {
                    regionIndex = 0;
                } else {
                    int digit = Character.getNumericValue(c);
                    if (digit >= 0 && digit <= 9) {
                        regionIndex = digit + 1;
                    }
                }
                
                if (regionIndex != -1) {
                    batch.draw(Assets.numberRegions[regionIndex], currentX, y, scaledWidth, scaledHeight);
                    currentX += scaledWidth;
                }
            }
        }
        batch.setColor(1, 1, 1, 1);
    }

    @Override
    public void reset() {
        active = false;
        imageRegion = null;
        text = null;
    }
}
