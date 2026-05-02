package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
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

    private Table statsPanel;
    private Label toggleStatsButton;
    private boolean statsVisible = true;

    private Label kineticLabel, explosiveLabel, fireLabel, poisonLabel, iceLabel, energyLabel;
    private Label critLabel, luckLabel, xpBonusLabel, speedLabel;
    private Label healthBonusLabel;

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

        createStatsPanel(skin);
    }

    private void createStatsPanel(Skin skin) {
        statsPanel = new Table();
        statsPanel.setBackground(skin.newDrawable("rect", new Color(0.1f, 0.1f, 0.1f, 0.85f)));
        statsPanel.setSize(120, 300);
        statsPanel.setPosition(10, 50);
        statsPanel.pad(8);

        Label titleLabel = new Label("MEJORAS", skin);
        titleLabel.setFontScale(1.1f);

        healthBonusLabel = new Label("HP: +0", skin);
        kineticLabel = new Label("KIN: +0%", skin);
        explosiveLabel = new Label("EXP: +0%", skin);
        fireLabel = new Label("FUE: +0%", skin);
        poisonLabel = new Label("VEN: +0%", skin);
        iceLabel = new Label("HIE: +0%", skin);
        energyLabel = new Label("ENE: +0%", skin);
        critLabel = new Label("CRT: +0%", skin);
        luckLabel = new Label("SUE: +0", skin);
        xpBonusLabel = new Label("XP: +0%", skin);
        speedLabel = new Label("VEL: 0", skin);

        statsPanel.add(titleLabel).center().padBottom(10).row();
        statsPanel.add(healthBonusLabel).left().padBottom(3).row();
        statsPanel.add(kineticLabel).left().padBottom(3).row();
        statsPanel.add(explosiveLabel).left().padBottom(3).row();
        statsPanel.add(fireLabel).left().padBottom(3).row();
        statsPanel.add(poisonLabel).left().padBottom(3).row();
        statsPanel.add(iceLabel).left().padBottom(3).row();      // <-- NUEVO
        statsPanel.add(energyLabel).left().padBottom(3).row();
        statsPanel.add(critLabel).left().padBottom(3).row();
        statsPanel.add(luckLabel).left().padBottom(3).row();
        statsPanel.add(xpBonusLabel).left().padBottom(3).row();
        statsPanel.add(speedLabel).left();

        toggleStatsButton = new Label("STATS", skin);
        toggleStatsButton.setPosition(10, 5);
        toggleStatsButton.setSize(45, 18);
        toggleStatsButton.setColor(new Color(0.3f, 0.3f, 0.8f, 1f));
        toggleStatsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleStatsPanel();
            }
        });

        stage.addActor(statsPanel);
        stage.addActor(toggleStatsButton);
    }

    public void toggleStatsPanel() {
        statsVisible = !statsVisible;
        statsPanel.setVisible(statsVisible);
        toggleStatsButton.setColor(statsVisible ? new Color(0.3f, 0.3f, 0.8f, 1f) : new Color(0.3f, 0.3f, 0.8f, 0.5f));
    }

    public void update(float hp, ExperienceSystem xpSystem, int score, float ab1Cd, float ab2Cd, Player player){

        hpLabel.setText("HP: " + (int)hp);
        levelLabel.setText("LVL " + xpSystem.getLevel());
        xpBar.setValue(xpSystem.getXPPercent());
        fpsLabel.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
        scoreLabel.setText("Puntos: " + score);
        ability1Bar.setValue(ab1Cd);
        ability2Bar.setValue(ab2Cd);

        if (player != null) {
            healthBonusLabel.setText("HP: +" + (int)player.getExtraHealthGained());
            kineticLabel.setText("KIN: +" + (int)(player.getKineticDamageBonus() * 100) + "%");
            explosiveLabel.setText("EXP: +" + (int)(player.getExplosiveDamageBonus() * 100) + "%");
            fireLabel.setText("FUE: +" + (int)(player.getFireDamageBonus() * 100) + "%");
            poisonLabel.setText("VEN: +" + (int)(player.getPoisonDamageBonus() * 100) + "%");
            iceLabel.setText("HIE: +" + (int)(player.getIceDamageBonus() * 100) + "%");       // <-- NUEVO
            energyLabel.setText("ENE: +" + (int)(player.getEnergyDamageBonus() * 100) + "%"); // <-- NUEVO
            critLabel.setText("CRT: +" + (int)(player.getCritChanceBonus() * 100) + "%");
            luckLabel.setText("SUE: +" + (int)player.getLuck());
            xpBonusLabel.setText("XP: +" + (int)((player.getXpMultiplier() - 1) * 100) + "%");
            speedLabel.setText("VEL: " + String.format(java.util.Locale.US, "%.1f", player.getSpeed()));
        }
    }

    public void render(){
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();

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

    public TouchpadInput getTouchpadInput() {
        return touchpadInput;
    }

    public Stage getStage() {
        return stage;
    }

    public void lockAbility2() {
        ability2NameLabel.setText("ROTO");
        ability2NameLabel.setColor(com.badlogic.gdx.graphics.Color.RED);
        ability2Bar.setVisible(false);
        if (ability2Button != null) {
            ability2Button.setVisible(false);
        }
    }
}
