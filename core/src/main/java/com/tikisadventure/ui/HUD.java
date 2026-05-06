package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.input.TouchpadInput;
import com.tikisadventure.screens.GameScreen;
import com.tikisadventure.systems.ExperienceSystem;
import com.tikisadventure.systems.powerUps.PowerUp;
import com.tikisadventure.core.SaveManager;

public class HUD {

    private LevelUpUI levelUpUI;
    private Stage stage;

    private Label fpsLabel;
    private Label hpLabel;
    private Label levelLabel;
    private Label scoreLabel;

    private ProgressBar xpBar;

    private Table abilityBoxDash;
    private Table abilityBoxGadget;
    private Label dashCooldownLabel;
    private Label gadgetCooldownLabel;
    private com.badlogic.gdx.scenes.scene2d.ui.Image dashIcon;
    private com.badlogic.gdx.scenes.scene2d.ui.Image gadgetIcon;
    private com.badlogic.gdx.scenes.scene2d.ui.Image dashOverlay;
    private com.badlogic.gdx.scenes.scene2d.ui.Image gadgetOverlay;

    private Label dashKeyLabel;
    private Label gadgetKeyLabel;
    private boolean showTouchpads;

    private com.tikisadventure.entities.player.Player player;
    private Touchpad moveTouchpad;
    private Touchpad aimTouchpad;
    private Button interactButton;
    private Button dashButton;
    private Button ability2Button;
    private TouchpadInput touchpadInput;

    private HUDStats hudStats;

    public HUD(Batch batch, com.tikisadventure.entities.player.Player player, boolean showTouchpads) {

        stage = new Stage(new ScreenViewport(), batch);
        this.player = player;
        this.showTouchpads = showTouchpads;

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

        mainTable.add(hpLabel).left().pad(10);
        mainTable.add(levelLabel).center().expandX();
        mainTable.add(fpsLabel).right().pad(10);
        mainTable.add(scoreLabel).center().pad(10);
        mainTable.row();
        mainTable.add(xpBar).colspan(4).expandX().fillX().padLeft(10).padRight(10).padBottom(5);
        mainTable.row();

        mainTable.add().expandY();
        mainTable.row();

        createAbilityBoxes(skin);

        Table abilityTable = new Table();
        abilityTable.setFillParent(true);
        abilityTable.bottom().right();

        abilityTable.add(abilityBoxDash).width(104).height(104).padRight(55).padBottom(12);
        abilityTable.add(abilityBoxGadget).width(143).height(143).padRight(20).padBottom(20);

        stage.addActor(abilityTable);

stage.addActor(mainTable);

        hudStats = new HUDStats(skin, stage);
    }

    private void createAbilityBoxes(Skin skin) {
        TextureRegion boxBackground = com.tikisadventure.core.Assets.getRegion("shared", "powerUps_assets/iconGunTemplate");
        TextureRegion dashIconTex = com.tikisadventure.core.Assets.getRegion("shared", "UI_assets/DashIcon");

        com.badlogic.gdx.graphics.g2d.TextureRegion overlayRegion = new com.badlogic.gdx.graphics.g2d.TextureRegion(
            com.tikisadventure.core.Assets.getRegion("shared", "powerUps_assets/iconGunTemplate"));

        dashCooldownLabel = new Label("", skin);
        dashCooldownLabel.setFontScale(2.5f);
        dashCooldownLabel.setColor(Color.WHITE);
        dashCooldownLabel.setAlignment(com.badlogic.gdx.utils.Align.center);

        gadgetCooldownLabel = new Label("", skin);
        gadgetCooldownLabel.setFontScale(2.5f);
        gadgetCooldownLabel.setColor(Color.WHITE);
        gadgetCooldownLabel.setAlignment(com.badlogic.gdx.utils.Align.center);

        abilityBoxDash = new Table();
        abilityBoxDash.setSize(104, 104);

        if (boxBackground != null) {
            com.badlogic.gdx.scenes.scene2d.ui.Image bg = new com.badlogic.gdx.scenes.scene2d.ui.Image(boxBackground);
            bg.setSize(104, 104);
            abilityBoxDash.addActor(bg);
        }

        if (dashIconTex != null) {
            dashIcon = new com.badlogic.gdx.scenes.scene2d.ui.Image(dashIconTex);
            dashIcon.setSize(65, 65);
            dashIcon.setPosition(19.5f, 19.5f);
            abilityBoxDash.addActor(dashIcon);
        }

        dashOverlay = new com.badlogic.gdx.scenes.scene2d.ui.Image(overlayRegion);
        dashOverlay.setSize(104, 104);
        dashOverlay.setColor(new Color(0.3f, 0.3f, 0.3f, 0.6f));
        dashOverlay.setPosition(0, 0);
        abilityBoxDash.addActor(dashOverlay);

        abilityBoxDash.addActor(dashCooldownLabel);
        dashCooldownLabel.setWidth(104);
        dashCooldownLabel.setHeight(30);
        dashCooldownLabel.setPosition(0, 50);

        dashKeyLabel = new Label("SPACE", skin);
        dashKeyLabel.setFontScale(0.8f);
        dashKeyLabel.setColor(Color.YELLOW);
        dashKeyLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        abilityBoxDash.addActor(dashKeyLabel);
        dashKeyLabel.setWidth(104);
        dashKeyLabel.setPosition(0, -15);

        abilityBoxGadget = new Table();
        abilityBoxGadget.setSize(143, 143);

        if (boxBackground != null) {
            com.badlogic.gdx.scenes.scene2d.ui.Image bg = new com.badlogic.gdx.scenes.scene2d.ui.Image(boxBackground);
            bg.setSize(143, 143);
            abilityBoxGadget.addActor(bg);
        }

        gadgetIcon = new com.badlogic.gdx.scenes.scene2d.ui.Image();
        gadgetIcon.setSize(65, 65);
        gadgetIcon.setPosition(39, 39);
        abilityBoxGadget.addActor(gadgetIcon);

        updateGadgetIcon(null);

        gadgetOverlay = new com.badlogic.gdx.scenes.scene2d.ui.Image(overlayRegion);
        gadgetOverlay.setSize(143, 143);
        gadgetOverlay.setColor(new Color(0.3f, 0.3f, 0.3f, 0.6f));
        gadgetOverlay.setPosition(0, 0);
        abilityBoxGadget.addActor(gadgetOverlay);

        abilityBoxGadget.addActor(gadgetCooldownLabel);
        gadgetCooldownLabel.setWidth(143);
        gadgetCooldownLabel.setHeight(30);
        gadgetCooldownLabel.setPosition(0, 70);

        gadgetKeyLabel = new Label("RMB", skin);
        gadgetKeyLabel.setFontScale(0.8f);
        gadgetKeyLabel.setColor(Color.YELLOW);
        gadgetKeyLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        abilityBoxGadget.addActor(gadgetKeyLabel);
        gadgetKeyLabel.setWidth(143);
        gadgetKeyLabel.setPosition(0, -15);
    }

    private void updateCooldownDisplay(float cooldownRemaining, Label label, com.badlogic.gdx.scenes.scene2d.ui.Image overlay, Player player, boolean isDash) {
        float boxSize = isDash ? 104f : 143f;

        if (cooldownRemaining > 0) {
            label.setText(String.valueOf((int)Math.ceil(cooldownRemaining)));
            label.setVisible(true);
            overlay.setVisible(true);

            float maxCooldown = 0;
            if (isDash && player != null && player.getProfile() != null && player.getProfile().specialAbility1 != null) {
                maxCooldown = player.getProfile().specialAbility1.getCooldown();
            } else if (!isDash && player != null && player.getProfile() != null && player.getProfile().specialAbility2 != null) {
                maxCooldown = player.getProfile().specialAbility2.getCooldown();
            }

            if (maxCooldown > 0) {
                float percent = cooldownRemaining / maxCooldown;
                float overlayHeight = boxSize * percent;
                overlay.setSize(boxSize, overlayHeight);
                overlay.setPosition(0, 0);
            }
        } else {
            label.setVisible(false);
            overlay.setVisible(false);
        }
    }

    public void updateGadgetIcon(String gadgetId) {
        if (gadgetId == null || gadgetId.isEmpty()) {
            gadgetId = SaveManager.getEquippedGadget();
        }
        if (gadgetId == null || gadgetId.isEmpty()) {
            gadgetId = "grenade_kinetic";
        }

        String iconPath = getGadgetIconPath(gadgetId);
        if (iconPath != null) {
            com.badlogic.gdx.graphics.g2d.TextureRegion tex = com.tikisadventure.core.Assets.getRegion("shared", iconPath);
            if (tex != null) {
                gadgetIcon.setDrawable(new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(tex));
            }
        }
    }

    public void setGadgetId(String gadgetId) {
        updateGadgetIcon(gadgetId);
    }

    private String getGadgetIconPath(String gadgetId) {
        if (gadgetId.contains("dash")) return "UI_assets/DashIcon";
        else if (gadgetId.contains("grenade_kinetic")) return "weapons_assets/Corn";
        else if (gadgetId.contains("grenade_explosive")) return "weapons_assets/ShakedCola";
        else if (gadgetId.contains("grenade_fire")) return "weapons_assets/Jalapeno";
        else if (gadgetId.contains("grenade_freeze")) return "weapons_assets/IceCandy";
        else if (gadgetId.contains("grenade_poison")) return "weapons_assets/PoisonFlask";
        else if (gadgetId.contains("cactus")) return "weapons_assets/Sock";
        else if (gadgetId.contains("sewer")) return "weapons_assets/Sewer";
        else if (gadgetId.contains("sheel")) return "weapons_assets/MagicSheel";
        else if (gadgetId.contains("scarecrow")) return "weapons_assets/Scarecrow";
        else if (gadgetId.contains("turret")) return "weapons_assets/Turret";
        else if (gadgetId.contains("mine")) return "weapons_assets/Mine";
        else return "weapons_assets/GrenadeLauncher";
    }

    public void update(float hp, ExperienceSystem xpSystem, int score, float dashCooldown, float gadgetCooldown, Player player){

        hpLabel.setText("HP: " + (int)hp);
        levelLabel.setText("LVL " + xpSystem.getLevel());
        xpBar.setValue(xpSystem.getXPPercent());
        fpsLabel.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
        scoreLabel.setText("Puntos: " + score);

        updateCooldownDisplay(dashCooldown, dashCooldownLabel, dashOverlay, player, true);
        updateCooldownDisplay(gadgetCooldown, gadgetCooldownLabel, gadgetOverlay, player, false);

        boolean showKeys = !showTouchpads;
        if (dashKeyLabel != null) dashKeyLabel.setVisible(showKeys);
        if (gadgetKeyLabel != null) gadgetKeyLabel.setVisible(showKeys);

        if (player != null && hudStats != null) {
            hudStats.updateStats(player);
        }
    }

    public void render(){
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
        if (hudStats != null) {
            hudStats.render();
        }

    }

    public void resize(int width, int height){
        stage.getViewport().update(width, height, true);
    }

    public void showLevelUpWindow(Array<PowerUp> opciones, com.tikisadventure.systems.PowerUpSystem system, int level) {
        levelUpUI.show(stage.getWidth(), stage.getHeight(), opciones, player, system, level);

        if (hudStats != null) {
            hudStats.bringToFront();
        }
    }

    private InputMultiplexer savedInputMultiplexer;

    public void setInputMultiplexer(InputMultiplexer multiplexer) {
        this.savedInputMultiplexer = multiplexer;
    }

    private void cerrarVentanaNivel() {
        player.getExperienceSystem().consumeLevel();
        if (player.getExperienceSystem().getLevelsPending() <= 0) {
            GameScreen.isGamePaused = false;
            levelUpUI.setVisible(false);
            if (savedInputMultiplexer != null) {
                Gdx.input.setInputProcessor(savedInputMultiplexer);
                savedInputMultiplexer = null;
            }
        }
    }

    public TouchpadInput getTouchpadInput() {
        return touchpadInput;
    }

    public Stage getStage() {
        return stage;
    }

    public void toggleStatsPanel() {
        if (hudStats != null) {
            hudStats.toggleStatsPanel();
        }
    }

    public void lockAbility2() {
        gadgetCooldownLabel.setText("ROTO");
        gadgetCooldownLabel.setColor(com.badlogic.gdx.graphics.Color.RED);
        gadgetOverlay.setColor(new Color(0.5f, 0f, 0f, 0.6f));
        if (ability2Button != null) {
            ability2Button.setVisible(false);
        }
    }
}
