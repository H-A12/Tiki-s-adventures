package com.tikisadventure.ui;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.tikisadventure.localization.LanguageManager;

import java.util.Random;

public class ScreenTipsUI {

    private static final String[] TIP_KEYS = {
        "loading.tip.1",  "loading.tip.2",  "loading.tip.3",
        "loading.tip.4",  "loading.tip.5",  "loading.tip.6",
        "loading.tip.7",  "loading.tip.8",  "loading.tip.9",
        "loading.tip.10", "loading.tip.11", "loading.tip.12",
        "loading.tip.13", "loading.tip.14", "loading.tip.15",
        "loading.tip.16", "loading.tip.17", "loading.tip.18",
        "loading.tip.19", "loading.tip.20"
    };

    private final Random random;
    private String currentKey;
    private String currentText;
    private GlyphLayout layout;

    public ScreenTipsUI() {
        this.random = new Random();
        pickRandomTip();
    }

    public void pickRandomTip() {
        currentKey = TIP_KEYS[random.nextInt(TIP_KEYS.length)];
        currentText = null;
        layout = null;
    }

    public void render(SpriteBatch batch, BitmapFont font, float screenWidth, float screenHeight) {
        if (currentText == null) {
            currentText = LanguageManager.t(currentKey);
        }
        if (layout == null) {
            layout = new GlyphLayout(font, currentText);
        }

        float marginBottom = 16f;
        float x = (screenWidth - layout.width) / 2f;
        float y = marginBottom + layout.height;
        font.draw(batch, currentText, x, y);
    }
}
