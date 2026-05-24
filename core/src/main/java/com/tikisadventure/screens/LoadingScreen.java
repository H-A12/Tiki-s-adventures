package com.tikisadventure.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.tikisadventure.core.Assets;
import com.tikisadventure.ui.FontManager;
import com.tikisadventure.ui.ScreenTipsUI;

//Transition screen showing character, loading dots, tips, then fade into game
public class LoadingScreen implements Screen {

    //Virtual resolution for the loading screen
    private static final float VIRTUAL_WIDTH = 800f;
    private static final float VIRTUAL_HEIGHT = 480f;

    //Game reference and target GameScreen
    private final Game game;
    private final GameScreen gameScreen;
    //Rendering: batch, camera, viewport
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private Viewport viewport;
    //Fonts for dot animation and tip text
    private BitmapFont fontDots;
    private BitmapFont fontTips;
    //Walk animation of the selected character
    private Animation<TextureRegion> characterAnim;
    //Random gameplay tip renderer
    private ScreenTipsUI tipsUI;
    private Texture fadeOverlay;
    //Animation state: timers, dot counter, fade alpha, phase
    private float stateTime;
    private float elapsed;
    private float dotTimer;
    private float fadeAlpha;
    private int dotCount;
    private int phase;

    //Timing constants
    private static final float MIN_DURATION = 4f;
    private static final float FADE_DURATION = 0.5f;
    private static final float DOT_INTERVAL = 0.5f;

    //Loading-screen phase constants
    private static final int PHASE_ANIMATING = 0;
    private static final int PHASE_FADING = 1;
    private static final int PHASE_DONE = 2;

    /** Stores references and loads the character walk animation. */
    public LoadingScreen(Game game, GameScreen gameScreen, String characterId) {
        this.game = game;
        this.gameScreen = gameScreen;
        loadCharacterAnimation(characterId);
    }

    /** Parses player_config.json to build the walk animation for the given character ID, falling back to Tiki. */
    private void loadCharacterAnimation(String characterId) {
        try {
            JsonValue characters = new JsonReader().parse(Gdx.files.internal("data/player_config.json"));
            JsonValue charEntry = null;
            for (JsonValue c : characters.get("characters")) {
                if (c.getString("id", "").equals(characterId)) {
                    charEntry = c;
                    break;
                }
            }
            if (charEntry == null) {
                charEntry = characters.get("characters").get(0);
            }

            int defaultFrameSize = 16;
            int frameSize = charEntry.getInt("frameSize", defaultFrameSize);
            float frameDuration = charEntry.getFloat("frameDuration", 0.15f);
            String atlasName = charEntry.getString("texturePath").replace(".png", "").toLowerCase();

            TextureRegion strip = Assets.getRegion(atlasName, "player_assets/" + atlasName + "/down");
            if (strip != null) {
                int frameCount = strip.getRegionWidth() / frameSize;
                TextureRegion[] frames = new TextureRegion[frameCount];
                for (int i = 0; i < frameCount; i++) {
                    frames[i] = new TextureRegion(strip, i * frameSize, 0, frameSize, frameSize);
                }
                characterAnim = new Animation<>(frameDuration, frames);
            }
        } catch (Exception e) {
            characterAnim = null;
        }

        if (characterAnim == null) {
            TextureRegion strip = Assets.getRegion("tiki", "player_assets/tiki/down");
            TextureRegion[] frames = new TextureRegion[4];
            for (int i = 0; i < 4; i++) {
                frames[i] = new TextureRegion(strip, i * 16, 0, 16, 16);
            }
            characterAnim = new Animation<>(0.15f, frames);
        }
    }

    /** Initializes camera, viewport, fonts, tips, fade overlay, and resets all animation state. */
    @Override
    public void show() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera();
        viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        batch.setProjectionMatrix(camera.combined);

        fontDots = FontManager.getFont(18);
        fontDots.setColor(Color.WHITE);
        fontTips = FontManager.getFont(8);
        fontTips.setColor(Color.WHITE);

        tipsUI = new ScreenTipsUI();

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        fadeOverlay = new Texture(pixmap);
        pixmap.dispose();

        stateTime = 0f;
        elapsed = 0f;
        dotTimer = 0f;
        dotCount = 0;
        fadeAlpha = 0f;
        phase = PHASE_ANIMATING;
    }

    /** Draws the character, dots, tips, and fade overlay; advances through phases until transitioning to GameScreen. */
    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        batch.setProjectionMatrix(camera.combined);

        stateTime += delta;
        dotTimer += delta;

        if (dotTimer >= DOT_INTERVAL) {
            dotTimer -= DOT_INTERVAL;
            dotCount = (dotCount + 1) % 4;
        }

        batch.begin();

        TextureRegion frame = characterAnim.getKeyFrame(stateTime, true);
        float charSize = 80f;
        float charX = (VIRTUAL_WIDTH - charSize) / 2f;
        float charY = (VIRTUAL_HEIGHT - charSize) / 2f;
        batch.draw(frame, charX, charY, charSize, charSize);

        StringBuilder dots = new StringBuilder();
        for (int i = 0; i < dotCount; i++) {
            dots.append(".");
        }
        String dotText = dots.toString();
        GlyphLayout layout = new GlyphLayout(fontDots, dotText);
        float textX = (VIRTUAL_WIDTH - layout.width) / 2f;
        fontDots.draw(batch, dotText, textX, charY - 20f);

        tipsUI.render(batch, fontTips, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

        if (phase == PHASE_FADING) {
            fadeAlpha += delta / FADE_DURATION;
            if (fadeAlpha > 1f) fadeAlpha = 1f;
            batch.setColor(0f, 0f, 0f, fadeAlpha);
            batch.draw(fadeOverlay, 0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
            batch.setColor(Color.WHITE);
        }

        batch.end();

        switch (phase) {
            case PHASE_ANIMATING:
                elapsed += delta;
                if (elapsed >= MIN_DURATION) {
                    phase = PHASE_FADING;
                }
                break;
            case PHASE_FADING:
                if (fadeAlpha >= 1f) {
                    phase = PHASE_DONE;
                    game.setScreen(gameScreen);
                }
                break;
        }
    }

    /** Updates the viewport on resize. */
    @Override
    public void resize(int width, int height) {
        if (viewport != null) viewport.update(width, height, true);
    }

    /** Calls dispose when the screen is hidden. */
    @Override
    public void hide() {
        dispose();
    }

    //No-op
    @Override
    public void pause() {
    }

    //No-op
    @Override
    public void resume() {
    }

    /** Disposes batch and fade overlay texture. */
    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (fadeOverlay != null) fadeOverlay.dispose();
    }
}
