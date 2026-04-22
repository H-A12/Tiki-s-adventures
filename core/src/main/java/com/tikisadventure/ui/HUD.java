package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.input.TouchpadInput;
import com.tikisadventure.screens.GameScreen;
import com.tikisadventure.systems.ExperienceSystem;
import com.tikisadventure.systems.powerUps.PowerUp;

public class HUD {

    private LevelUpUI levelUpUI;
    private Stage stage;

    private Label fpsLabel;
    private Label hpLabel;
    private Label levelLabel;
    private Label scoreLabel;

    private Label ability1NameLabel;
    private Label ability2NameLabel;

    private ProgressBar xpBar;
    private ProgressBar ability1Bar;
    private ProgressBar ability2Bar;

    private com.tikisadventure.entities.player.Player player;
    private Touchpad moveTouchpad;
    private Touchpad aimTouchpad;
    private Button interactButton;
    private Button dashButton;
    private Button ability2Button;
    private TouchpadInput touchpadInput;

    public HUD(Batch batch, com.tikisadventure.entities.player.Player player, boolean showTouchpads) {

        stage = new Stage(new ScreenViewport(), batch);
        this.player = player;

        Skin skin = new Skin();

        com.badlogic.gdx.graphics.g2d.TextureAtlas atlas = new com.badlogic.gdx.graphics.g2d.TextureAtlas(Gdx.files.internal("SkinsMenu/flat/skin/skin.atlas"));
        skin.addRegions(atlas);

        com.badlogic.gdx.graphics.g2d.BitmapFont font = new com.badlogic.gdx.graphics.g2d.BitmapFont();
        skin.add("default", font);

        Window.WindowStyle windowStyle = new Window.WindowStyle();
        windowStyle.background = skin.newDrawable("rect", new Color(0.2f, 0.2f, 0.2f, 1f));
        windowStyle.titleFont = font;
        windowStyle.titleFontColor = Color.WHITE;
        skin.add("default", windowStyle);

        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.font = font;
        textButtonStyle.up = skin.newDrawable("rect", new Color(0.3f, 0.3f, 0.3f, 1f));
        textButtonStyle.over = skin.newDrawable("rect", new Color(0.4f, 0.4f, 0.4f, 1f));
        textButtonStyle.down = skin.newDrawable("rect", new Color(0.5f, 0.3f, 0.3f, 1f));
        skin.add("default", textButtonStyle);

        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        skin.add("default", labelStyle);

        if (showTouchpads) {
            float hudHeight = Gdx.graphics.getHeight();

            Touchpad.TouchpadStyle touchpadStyle = new Touchpad.TouchpadStyle();
            touchpadStyle.background = skin.newDrawable("rect", new Color(1f, 1f, 1f, 0.3f));
            touchpadStyle.knob = skin.newDrawable("check-on", new Color(1f, 1f, 1f, 0.5f));

            moveTouchpad = new Touchpad(10, touchpadStyle);
            moveTouchpad.setBounds(15, 15, 120, 120);
            stage.addActor(moveTouchpad);

            aimTouchpad = new Touchpad(10, touchpadStyle);
            aimTouchpad.setBounds(hudHeight - 135, 15, 120, 120);
            stage.addActor(aimTouchpad);

            ImageButton.ImageButtonStyle buttonStyle = new ImageButton.ImageButtonStyle();
            buttonStyle.imageUp = skin.newDrawable("rect", new Color(0.3f, 0.8f, 0.3f, 0.8f));
            buttonStyle.imageOver = skin.newDrawable("rect", new Color(0.4f, 0.9f, 0.4f, 0.9f));
            buttonStyle.imageDown = skin.newDrawable("rect", new Color(0.2f, 0.7f, 0.2f, 0.9f));

            interactButton = new ImageButton(buttonStyle);
            interactButton.setSize(50, 50);
            interactButton.setPosition(hudHeight - 270, 15);
            stage.addActor(interactButton);

            ImageButton.ImageButtonStyle dashButtonStyle = new ImageButton.ImageButtonStyle();
            dashButtonStyle.imageUp = skin.newDrawable("rect", new Color(0.3f, 0.3f, 0.8f, 0.8f));
            dashButtonStyle.imageOver = skin.newDrawable("rect", new Color(0.4f, 0.4f, 0.9f, 0.9f));
            dashButtonStyle.imageDown = skin.newDrawable("rect", new Color(0.2f, 0.2f, 0.7f, 0.9f));

            dashButton = new ImageButton(dashButtonStyle);
            dashButton.setSize(50, 50);
            dashButton.setPosition(hudHeight - 220, 15);
            stage.addActor(dashButton);

            ImageButton.ImageButtonStyle ability2ButtonStyle = new ImageButton.ImageButtonStyle();
            ability2ButtonStyle.imageUp = skin.newDrawable("rect", new Color(0.8f, 0.3f, 0.3f, 0.8f));
            ability2ButtonStyle.imageOver = skin.newDrawable("rect", new Color(0.9f, 0.4f, 0.4f, 0.9f));
            ability2ButtonStyle.imageDown = skin.newDrawable("rect", new Color(0.7f, 0.2f, 0.2f, 0.9f));

            ability2Button = new ImageButton(ability2ButtonStyle);
            ability2Button.setSize(50, 50);
            ability2Button.setPosition(hudHeight - 270, 75);
            stage.addActor(ability2Button);

            touchpadInput = new TouchpadInput(moveTouchpad, aimTouchpad, interactButton, dashButton, ability2Button);
        }

        levelUpUI = new LevelUpUI(skin, new Runnable() {
            @Override
            public void run() {
                cerrarVentanaNivel();
            }
        });
        levelUpUI.setVisible(false);
        stage.addActor(levelUpUI);

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.top();

        hpLabel = new Label("HP: 0", skin);
        fpsLabel = new Label("FPS: 0", skin);
        levelLabel = new Label("LVL 1", skin);
        scoreLabel = new Label("Puntos: 0", skin);

        ProgressBar.ProgressBarStyle xpBarStyle = new ProgressBar.ProgressBarStyle();
        xpBarStyle.background = skin.newDrawable("rect", Color.DARK_GRAY);
        xpBarStyle.background.setMinHeight(4);
        xpBarStyle.knobBefore = skin.newDrawable("rect", Color.CYAN);
        xpBarStyle.knobBefore.setMinHeight(4);
        xpBar = new ProgressBar(0f, 1f, 0.01f, false, xpBarStyle);

        ProgressBar.ProgressBarStyle cdStyle1 = new ProgressBar.ProgressBarStyle();
        cdStyle1.background = skin.newDrawable("rect", Color.DARK_GRAY);
        cdStyle1.background.setMinHeight(4);
        cdStyle1.knobBefore = skin.newDrawable("rect", Color.YELLOW);
        cdStyle1.knobBefore.setMinHeight(4);
        ability1Bar = new ProgressBar(0f, 1f, 0.01f, false, cdStyle1);

        ProgressBar.ProgressBarStyle cdStyle2 = new ProgressBar.ProgressBarStyle();
        cdStyle2.background = skin.newDrawable("rect", Color.DARK_GRAY);
        cdStyle2.background.setMinHeight(4);
        cdStyle2.knobBefore = skin.newDrawable("rect", Color.ORANGE);
        cdStyle2.knobBefore.setMinHeight(4);
        ability2Bar = new ProgressBar(0f, 1f, 0.01f, false, cdStyle2);

        mainTable.add(hpLabel).left().pad(10);
        mainTable.add(levelLabel).center().expandX();
        mainTable.add(fpsLabel).right().pad(10);
        mainTable.add(scoreLabel).center().pad(10);
        mainTable.row();
        mainTable.add(xpBar).colspan(4).expandX().fillX().padLeft(10).padRight(10).padBottom(5);
        mainTable.row();

        mainTable.add().expandY();
        mainTable.row();

        ability1NameLabel = new Label("---", skin);
        ability2NameLabel = new Label("---", skin);

        Table cdTable = new Table();

        cdTable.add(ability1Bar).width(150).padRight(40);
        cdTable.add(ability2Bar).width(150);
        cdTable.row().padTop(5);

        cdTable.add(ability1NameLabel).padRight(40).center();
        cdTable.add(ability2NameLabel).center();

        mainTable.add(cdTable).colspan(4).center().bottom().padBottom(20);

        stage.addActor(mainTable);
    }

    public TouchpadInput getTouchpadInput() { return touchpadInput; }
    public Touchpad getMoveTouchpad() { return moveTouchpad; }
    public Touchpad getAimTouchpad() { return aimTouchpad; }

    public void update(float hp, ExperienceSystem xpSystem, int score, float ab1Cd, float ab2Cd){

        hpLabel.setText("HP: " + (int)hp);
        levelLabel.setText("LVL " + xpSystem.getLevel());
        xpBar.setValue(xpSystem.getXPPercent());
        fpsLabel.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
        scoreLabel.setText("Puntos: " + score);
        ability1Bar.setValue(ab1Cd);
        ability2Bar.setValue(ab2Cd);
    }

    public void render(){
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

        if (levelUpUI.isVisible() && Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
            cerrarVentanaNivel();
        }
    }

    public void resize(int width, int height){
        stage.getViewport().update(width, height, true);
    }

    public void showLevelUpWindow(com.badlogic.gdx.utils.Array<PowerUp> opciones) {
        levelUpUI.show(stage.getWidth(), stage.getHeight(), opciones, this.player);
    }

    private void cerrarVentanaNivel() {
        player.getExperienceSystem().consumeLevel();
        if (player.getExperienceSystem().getLevelsPending() <= 0) {
            GameScreen.isGamePaused = false;
            levelUpUI.setVisible(false);
            Gdx.input.setInputProcessor(null);
        } else {
        }
    }

    public void setAbilityNames(String name1, String name2) {
        ability1NameLabel.setText(name1 != null ? name1 : "---");
        ability2NameLabel.setText(name2 != null ? name2 : "---");
    }

    public Stage getStage() {
        return stage;
    }
}