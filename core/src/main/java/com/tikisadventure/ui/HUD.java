package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.input.TouchpadInput;
import com.tikisadventure.screens.GameScreen;
import com.tikisadventure.systems.ExperienceSystem;
import com.tikisadventure.systems.powerUps.PowerUp;
import com.tikisadventure.audio.AudioManager;
import com.tikisadventure.core.SaveManager;
import com.tikisadventure.input.InputConfig;
import com.tikisadventure.core.Assets;
import com.tikisadventure.localization.LanguageManager;
import com.tikisadventure.ui.FontManager;

public class HUD {

    private LevelUpUI levelUpUI;
    private Stage stage;

    private Label fpsLabel;
    private Label hpLabel;
    private Label levelLabel;
    private Label scoreLabel;

    // NUEVO: Etiqueta para mostrar el número de fase
    private Label stageLabel;

    private Label waveLabel;

    private Array<Label> notifLabels = new Array<>();

    private XpBarActor xpBar;
    private HeartIcon heartIcon;
    private DamageBorderActor damageOverlay;

    private Table abilityBoxDash;
    private Table abilityBoxGadget;
    private Table abilityTable;
    private Label gadgetCooldownLabel;
    private com.badlogic.gdx.scenes.scene2d.ui.Image dashIcon;
    private com.badlogic.gdx.scenes.scene2d.ui.Image gadgetIcon;
    private com.badlogic.gdx.scenes.scene2d.ui.Image dashOverlay;
    private com.badlogic.gdx.scenes.scene2d.ui.Image gadgetOverlay;

    private Label dashKeyLabel;
    private Label dashCooldownLabel;
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

    private Table mainTable;
    private Stack xpStack;
    private Table hpTable;
    private Image dashBoxBg;
    private Image gadgetBoxBg;

    private Skin skin;

    // ==============================================
    // ACTOR 1: ICONO DEL CORAZÓN CON LATIDO CORREGIDO
    // ==============================================
    public static class HeartIcon extends Image {
        private float time = 0f;
        private float currentHp = 100f;
        private float maxHp = 100f;
        public float currentPulse = 0f;

        public HeartIcon(TextureRegion region) {
            super(region);
        }

        @Override
        protected void sizeChanged() {
            super.sizeChanged();
            setOrigin(getWidth() / 2f, getHeight() / 2f);
        }

        public void updateHp(float current, float max) {
            this.currentHp = current;
            this.maxHp = max;
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            if (maxHp <= 0) return;

            float pct = currentHp / maxHp;

            float speed = 1.5f;

            if (pct <= 0.6f && pct > 0.4f) {
                speed = 6f;
            } else if (pct <= 0.4f && pct > 0.1f) {
                speed = 12f;
            } else if (pct <= 0.1f) {
                speed = 20f;
            }

            time += delta * speed;
            currentPulse = Math.abs(MathUtils.sin(time));

            float scale = 1.0f + 0.2f * currentPulse;
            setScale(scale);
        }
    }

    // ==============================================
    // ACTOR NUEVO: BORDES ROJOS DE DAÑO (Vignette Dinámico)
    // ==============================================
    public static class DamageBorderActor extends Actor {
        private com.badlogic.gdx.scenes.scene2d.utils.Drawable rect;

        public DamageBorderActor(Skin skin) {
            rect = skin.newDrawable("rect", new Color(1f, 0f, 0f, 1f));
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            Color c = getColor();
            if (c.a <= 0.01f || getStage() == null) return;

            // Guardar el color real empaquetado
            float oldColor = batch.getPackedColor();

            float w = getStage().getWidth();
            float h = getStage().getHeight();
            float borderThickness = h * 0.08f;

            batch.setColor(c.r, c.g, c.b, c.a * parentAlpha);

            rect.draw(batch, 0, h - borderThickness, w, borderThickness);
            rect.draw(batch, 0, 0, w, borderThickness);
            rect.draw(batch, 0, borderThickness, borderThickness, h - borderThickness * 2);
            rect.draw(batch, w - borderThickness, borderThickness, borderThickness, h - borderThickness * 2);

            // Restaurar el color exacto que tenía antes
            batch.setPackedColor(oldColor);
        }
    }

    // ==============================================
    // ACTOR 3: BARRA DE XP DINÁMICA
    // ==============================================
    public static class XpBarActor extends Actor {
        private TextureRegion fillRegion;
        private TextureRegion borderRegion;
        private TextureRegion currentFill;

        private float targetPercent = 0f;
        private float currentPercent = 0f;
        private int level = 1;
        private boolean isRainbow = false;
        private float time = 0f;

        private Color barColor = new Color(Color.CYAN);
        private com.badlogic.gdx.scenes.scene2d.utils.Drawable fallbackBorder;

        public XpBarActor(Skin skin) {
            fillRegion = Assets.getRegion("shared", "UI_assets/progressLevelBar");
            borderRegion = Assets.getRegion("shared", "UI_assets/progressLevelBarBorder");

            if (fillRegion != null) {
                currentFill = new TextureRegion(fillRegion);
            }
            if (borderRegion == null) {
                fallbackBorder = skin.newDrawable("rect", Color.BLACK);
            }
        }

        public void update(float percent, int level, boolean pendingLevelUp) {
            this.targetPercent = pendingLevelUp ? 1.0f : Math.max(0f, Math.min(1f, percent));
            this.level = level;
            this.isRainbow = pendingLevelUp;
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            time += delta;

            if (targetPercent < currentPercent && !isRainbow) {
                currentPercent = 0f;
            }

            currentPercent = MathUtils.lerp(currentPercent, targetPercent, delta * 8f);

            if (fillRegion != null) {
                int fillW = Math.max(1, (int)(fillRegion.getRegionWidth() * currentPercent));
                currentFill.setRegion(fillRegion, 0, 0, fillW, fillRegion.getRegionHeight());
            }

            if (isRainbow) {
                float hue = (time * 300f) % 360f;
                barColor.fromHsv(hue, 1f, 1f);
            } else {
                if ((level + 1) % 5 == 0) {
                    barColor.set(new Color(0.8f, 0.4f, 1.0f, 1f));
                } else {
                    barColor.set(Color.CYAN);
                }
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            Color oldColor = batch.getColor();

            batch.setColor(getColor().r, getColor().g, getColor().b, getColor().a * parentAlpha);
            if (currentFill != null && currentPercent > 0) {
                batch.setColor(barColor.r, barColor.g, barColor.b, barColor.a * parentAlpha);
                float pad = 2f;
                float drawWidth = (getWidth() - pad * 2) * currentPercent;
                batch.draw(currentFill, getX() + pad, getY() + pad, drawWidth, getHeight() - pad * 2);
            }

            batch.setColor(getColor().r, getColor().g, getColor().b, getColor().a * parentAlpha);

            if (borderRegion != null) {
                batch.draw(borderRegion, getX(), getY(), getWidth(), getHeight());
            } else if (fallbackBorder != null) {
                fallbackBorder.draw(batch, getX(), getY(), getWidth(), getHeight());
            }

            batch.setColor(oldColor);
        }
    }
    // ----------------------------------------------

    public HUD(Batch batch, com.tikisadventure.entities.player.Player player, boolean showTouchpads) {

        // CAMBIO CRÍTICO: Usamos FitViewport para que LibGDX escale todo automáticamente
        stage = new Stage(new FitViewport(1280, 720), batch);
        this.player = player;
        this.showTouchpads = showTouchpads;

        this.skin = FontManager.getGlobalSkin();

        damageOverlay = new DamageBorderActor(this.skin);
        damageOverlay.setTouchable(Touchable.disabled);
        damageOverlay.getColor().a = 0f;
        stage.addActor(damageOverlay);

        if (showTouchpads) {
            // CAMBIO: Usamos el ancho base (1280) en lugar del tamaño físico de la pantalla
            float hudWidth = 1280f;

            Touchpad.TouchpadStyle touchpadStyle = new Touchpad.TouchpadStyle();
            touchpadStyle.background = this.skin.newDrawable("rect", new Color(1f, 1f, 1f, 0.3f));
            touchpadStyle.knob = this.skin.newDrawable("check-on", new Color(1f, 1f, 1f, 0.5f));

            moveTouchpad = new Touchpad(10, touchpadStyle);
            moveTouchpad.setBounds(15, 15, 120, 120);
            stage.addActor(moveTouchpad);

            aimTouchpad = new Touchpad(10, touchpadStyle);
            aimTouchpad.setBounds(hudWidth - 135, 15, 120, 120);
            stage.addActor(aimTouchpad);

            ImageButton.ImageButtonStyle buttonStyle = new ImageButton.ImageButtonStyle();
            buttonStyle.imageUp = this.skin.newDrawable("rect", new Color(0.3f, 0.8f, 0.3f, 0.8f));
            buttonStyle.imageOver = this.skin.newDrawable("rect", new Color(0.4f, 0.9f, 0.4f, 0.9f));
            buttonStyle.imageDown = this.skin.newDrawable("rect", new Color(0.2f, 0.7f, 0.2f, 0.9f));

            interactButton = new ImageButton(buttonStyle);
            interactButton.setSize(50, 50);
            interactButton.setPosition(hudWidth - 270, 15);
            stage.addActor(interactButton);

            ImageButton.ImageButtonStyle dashButtonStyle = new ImageButton.ImageButtonStyle();
            dashButtonStyle.imageUp = this.skin.newDrawable("rect", new Color(0.3f, 0.3f, 0.8f, 0.8f));
            dashButtonStyle.imageOver = this.skin.newDrawable("rect", new Color(0.4f, 0.4f, 0.9f, 0.9f));
            dashButtonStyle.imageDown = this.skin.newDrawable("rect", new Color(0.2f, 0.2f, 0.7f, 0.9f));

            dashButton = new ImageButton(dashButtonStyle);
            dashButton.setSize(50, 50);
            dashButton.setPosition(hudWidth - 220, 15);
            stage.addActor(dashButton);

            ImageButton.ImageButtonStyle ability2ButtonStyle = new ImageButton.ImageButtonStyle();
            ability2ButtonStyle.imageUp = this.skin.newDrawable("rect", new Color(0.8f, 0.3f, 0.3f, 0.8f));
            ability2ButtonStyle.imageOver = this.skin.newDrawable("rect", new Color(0.9f, 0.4f, 0.4f, 0.9f));
            ability2ButtonStyle.imageDown = this.skin.newDrawable("rect", new Color(0.7f, 0.2f, 0.2f, 0.9f));

            ability2Button = new ImageButton(ability2ButtonStyle);
            ability2Button.setSize(50, 50);
            ability2Button.setPosition(hudWidth - 270, 75);
            stage.addActor(ability2Button);

            touchpadInput = new TouchpadInput(moveTouchpad, aimTouchpad, interactButton, dashButton, ability2Button);
        }

        levelUpUI = new LevelUpUI(this.skin, new Runnable() {
            @Override
            public void run() {
                cerrarVentanaNivel();
            }
        });
        levelUpUI.setVisible(false);
        stage.addActor(levelUpUI);

        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.top();

        xpBar = new XpBarActor(this.skin);

        levelLabel = new Label(LanguageManager.t("hud.lvl") + " 1", this.skin, "font-21");
        levelLabel.setAlignment(Align.center);

        xpStack = new Stack();
        xpStack.add(xpBar);

        Table levelCenterTable = new Table();
        levelCenterTable.add(levelLabel).center();
        xpStack.add(levelCenterTable);

        hpTable = new Table();
        TextureRegion hpRegion = Assets.getRegion("shared", "stats_asset/statLife");
        if (hpRegion != null) {
            heartIcon = new HeartIcon(hpRegion);
            hpTable.add(heartIcon).size(48, 48).padRight(12);
        }
        hpLabel = new Label("0", this.skin, "font-30");
        hpTable.add(hpLabel);

        scoreLabel = new Label(LanguageManager.t("hud.score") + " 0", this.skin);
        fpsLabel = new Label("FPS: 0", this.skin);
        fpsLabel.setVisible(SaveManager.getProfileData().showFps);

        mainTable.add(xpStack).colspan(3).expandX().fillX().height(26).padTop(8).padLeft(8).padRight(8).row();

        mainTable.add(hpTable).left().padTop(6).padLeft(12);
        mainTable.add().center().padTop(6).expandX();
        mainTable.add(fpsLabel).right().padTop(6).padRight(12);
        mainTable.row();

        mainTable.add().expandY();
        mainTable.row();

        createAbilityBoxes(this.skin);

        abilityTable = new Table();
        abilityTable.setFillParent(true);

        abilityTable.add().expandY().colspan(2).row();

        abilityTable.add(abilityBoxDash).width(104).height(104).padBottom(20).padRight(5);
        abilityTable.add(abilityBoxGadget).width(104).height(104).padBottom(20).padLeft(5);

        stage.addActor(abilityTable);
        stage.addActor(mainTable);

        hudStats = new HUDStats(this.skin, stage);

        stageLabel = new Label("", this.skin, "font-38");
        stageLabel.setAlignment(Align.center);
        stageLabel.getColor().a = 0f;
        stage.addActor(stageLabel);

        waveLabel = new Label("", this.skin, "font-16");
        waveLabel.setColor(Color.RED);
        stage.addActor(waveLabel);
    }

    public void showStageMessage(int stageNumber) {
        stageLabel.setText(LanguageManager.t("hud.stage") + " " + stageNumber);
        stageLabel.pack();

        stageLabel.setPosition(stage.getWidth() / 2f, stage.getHeight() * 0.75f, Align.center);
        stageLabel.toFront();

        stageLabel.clearActions();
        stageLabel.addAction(Actions.sequence(
            Actions.alpha(0f),
            Actions.fadeIn(0.5f),
            Actions.delay(2f),
            Actions.fadeOut(1.5f)
        ));
    }

    private void pushNotification(String text, Color color) {
        Label label = new Label(text, skin, "font-21");
        label.setColor(color);
        label.pack();
        float baseY = 50f;
        for (Label l : notifLabels) {
            if (l.getColor().a > 0.01f) {
                baseY = Math.max(baseY, l.getY() + l.getHeight() + 5f);
            }
        }
        label.setPosition(stage.getWidth() - label.getWidth() - 20f, baseY);
        stage.addActor(label);
        notifLabels.add(label);
        label.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
            com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn(0.3f),
            com.badlogic.gdx.scenes.scene2d.actions.Actions.delay(2f),
            com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut(1.5f),
            com.badlogic.gdx.scenes.scene2d.actions.Actions.run(() -> {
                stage.getActors().removeValue(label, true);
                notifLabels.removeValue(label, true);
            })
        ));
    }

    public void showStatNotification(String text) {
        pushNotification(text, com.badlogic.gdx.graphics.Color.GREEN);
    }

    public void showCoinNotification(String text) {
        pushNotification(text, com.badlogic.gdx.graphics.Color.YELLOW);
    }

    private void createAbilityBoxes(Skin skin) {
        TextureRegion boxBackground = Assets.getRegion("shared", "powerUps_assets/iconGunTemplate");
        TextureRegion dashIconTex = Assets.getRegion("shared", "UI_assets/DashIcon");

        com.badlogic.gdx.graphics.g2d.TextureRegion overlayRegion = new com.badlogic.gdx.graphics.g2d.TextureRegion(
            Assets.getRegion("shared", "powerUps_assets/iconGunTemplate"));

        dashCooldownLabel = new Label("", skin, "font-38");
        dashCooldownLabel.setColor(Color.WHITE);
        dashCooldownLabel.setAlignment(com.badlogic.gdx.utils.Align.center);

        gadgetCooldownLabel = new Label("", skin, "font-38");
        gadgetCooldownLabel.setColor(Color.WHITE);
        gadgetCooldownLabel.setAlignment(com.badlogic.gdx.utils.Align.center);

        abilityBoxDash = new Table();
        abilityBoxDash.setSize(104, 104);

        if (boxBackground != null) {
            dashBoxBg = new com.badlogic.gdx.scenes.scene2d.ui.Image(boxBackground);
            dashBoxBg.setSize(104, 104);
            abilityBoxDash.addActor(dashBoxBg);
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

        int ability1Code = SaveManager.getProfileData().inputConfig.keyboardMapping.get("ability1");
        dashKeyLabel = new Label(InputConfig.getKeyName(ability1Code), skin, "font-12");
        dashKeyLabel.setColor(Color.YELLOW);
        dashKeyLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        abilityBoxDash.addActor(dashKeyLabel);
        dashKeyLabel.setWidth(104);
        dashKeyLabel.setPosition(0, -15);

        abilityBoxGadget = new Table();
        abilityBoxGadget.setSize(104, 104);

        if (boxBackground != null) {
            gadgetBoxBg = new com.badlogic.gdx.scenes.scene2d.ui.Image(boxBackground);
            gadgetBoxBg.setSize(104, 104);
            abilityBoxGadget.addActor(gadgetBoxBg);
        }

        gadgetIcon = new com.badlogic.gdx.scenes.scene2d.ui.Image();
        gadgetIcon.setSize(65, 65);
        gadgetIcon.setPosition(19.5f, 19.5f);
        abilityBoxGadget.addActor(gadgetIcon);

        updateGadgetIcon(null);

        gadgetOverlay = new com.badlogic.gdx.scenes.scene2d.ui.Image(overlayRegion);
        gadgetOverlay.setSize(104, 104);
        gadgetOverlay.setColor(new Color(0.3f, 0.3f, 0.3f, 0.6f));
        gadgetOverlay.setPosition(0, 0);
        abilityBoxGadget.addActor(gadgetOverlay);

        abilityBoxGadget.addActor(gadgetCooldownLabel);
        gadgetCooldownLabel.setWidth(104);
        gadgetCooldownLabel.setHeight(30);
        gadgetCooldownLabel.setPosition(0, 50);

        int ability2Code = SaveManager.getProfileData().inputConfig.keyboardMapping.get("ability2");
        gadgetKeyLabel = new Label(InputConfig.getKeyName(ability2Code), skin, "font-12");
        gadgetKeyLabel.setColor(Color.YELLOW);
        gadgetKeyLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        abilityBoxGadget.addActor(gadgetKeyLabel);
        gadgetKeyLabel.setWidth(104);
        gadgetKeyLabel.setPosition(0, -15);
    }

    private void updateCooldownDisplay(float cooldownRemaining, Label label, com.badlogic.gdx.scenes.scene2d.ui.Image overlay, Player player, boolean isDash) {
        float boxSize = 104f;

        if (cooldownRemaining > 0) {
            label.setText(String.valueOf((int)Math.ceil(cooldownRemaining)));
            label.setVisible(true);

            overlay.setVisible(true);
            overlay.setSize(boxSize, boxSize);
            overlay.setPosition(0, 0);
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
            com.badlogic.gdx.graphics.g2d.TextureRegion tex = Assets.getRegion("shared", iconPath);
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

    public void update(float hp, ExperienceSystem xpSystem, int score, float dashCooldown, float gadgetCooldown, Player player, int waveNumber){

        hpLabel.setText(String.valueOf((int)hp));

        if (player != null && player.getHealthComponent() != null) {
            float maxHp = player.getHealthComponent().maxHealth;
            if (heartIcon != null) {
                heartIcon.updateHp(hp, maxHp);
            }

            float hpPct = hp / maxHp;
            if (hpPct < 0.5f && hp > 0) {
                float dangerIntensity = (0.5f - hpPct) / 0.5f;
                float alpha = dangerIntensity * 0.45f;

                if (heartIcon != null) {
                    alpha += (heartIcon.currentPulse * 0.2f * dangerIntensity);
                }
                damageOverlay.getColor().a = MathUtils.clamp(alpha, 0f, 1f);
            } else {
                damageOverlay.getColor().a = 0f;
            }
        }

        levelLabel.setText(LanguageManager.t("hud.lvl") + " " + xpSystem.getLevel());

        boolean hasPendingLevelUp = xpSystem.getLevelsPending() > 0;
        xpBar.update(xpSystem.getXPPercent(), xpSystem.getLevel(), hasPendingLevelUp);

        fpsLabel.setVisible(SaveManager.getProfileData().showFps);
        fpsLabel.setText("FPS: " + Gdx.graphics.getFramesPerSecond());
        scoreLabel.setText(LanguageManager.t("hud.score") + " " + score);

        updateCooldownDisplay(dashCooldown, dashCooldownLabel, dashOverlay, player, true);
        updateCooldownDisplay(gadgetCooldown, gadgetCooldownLabel, gadgetOverlay, player, false);

        boolean showKeys = !showTouchpads;
        if (dashKeyLabel != null) {
            int a1Code = SaveManager.getProfileData().inputConfig.keyboardMapping.get("ability1");
            dashKeyLabel.setText(InputConfig.getKeyName(a1Code));
            dashKeyLabel.setVisible(showKeys);
        }
        if (gadgetKeyLabel != null) {
            int a2Code = SaveManager.getProfileData().inputConfig.keyboardMapping.get("ability2");
            gadgetKeyLabel.setText(InputConfig.getKeyName(a2Code));
            gadgetKeyLabel.setVisible(showKeys);
        }

        if (player != null && hudStats != null) {
            hudStats.updateStats(player);
        }

        waveLabel.setText(LanguageManager.t("hud.wave") + " " + waveNumber);
        waveLabel.pack();
        waveLabel.setPosition(stage.getWidth() - waveLabel.getPrefWidth() - 20f, 20f);
    }

    public void render() {
        stage.act();
        stage.getBatch().setColor(Color.WHITE);
        stage.draw();
        if (hudStats != null) hudStats.render();
    }

    public void resize(int width, int height){
        // FitViewport se encarga ahora de escalar y centrar todo automáticamente
        stage.getViewport().update(width, height, true);

        if (levelUpUI != null && levelUpUI.isVisible()) {
            levelUpUI.centerOnStage(stage.getWidth(), stage.getHeight());
        }

        if (stageLabel != null) {
            stageLabel.setPosition(stage.getWidth() / 2f, stage.getHeight() * 0.75f, Align.center);
        }
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
        GameScreen.isGamePaused = false;
        AudioManager.unduckFromPause();
        levelUpUI.setVisible(false);
        if (savedInputMultiplexer != null) {
            Gdx.input.setInputProcessor(savedInputMultiplexer);
            savedInputMultiplexer = null;
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
        abilityBoxGadget.setVisible(false);
        if (ability2Button != null) {
            ability2Button.setVisible(false);
        }

        if (abilityTable != null) {
            abilityTable.clearChildren();
            abilityTable.add().expandY().row();
            abilityTable.add(abilityBoxDash).width(104f).height(104f).padBottom(20f);
        }
    }

    public Skin getSkin() {
        return this.skin;
    }
}
