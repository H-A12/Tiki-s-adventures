package com.tikisadventure.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.tikisadventure.core.GameSession;
import com.tikisadventure.localization.ItemNames;
import com.tikisadventure.localization.LanguageManager;
import com.tikisadventure.screens.MenuGodMode.MarqueeSelectBox;
import com.tikisadventure.ui.DeleteWeaponUI;
import com.tikisadventure.ui.button.ButtonFactory;

public class MenuCustomGun {

    public static int MAX_CUSTOM_WEAPONS = 10;

    public interface OnCustomWeaponSaved {
        void onSaved();
    }

    // =========================================================================
    // HELPER: SelectBox que escala su lista flotante
    // =========================================================================
    private static <T> SelectBox<T> crearSelectBoxEscalado(SelectBox.SelectBoxStyle style) {
        return new SelectBox<T>(style) {
            private final com.badlogic.gdx.math.Vector2 tempCoords = new com.badlogic.gdx.math.Vector2();

            @Override
            public void act(float delta) {
                super.act(delta);
                ScrollPane popup = getScrollPane();

                if (popup != null && popup.getParent() != null) {
                    popup.setTransform(true);
                    tempCoords.set(0, 0);
                    localToStageCoordinates(tempCoords);

                    if (popup.getY() >= tempCoords.y) {
                        popup.setOrigin(0, 0);
                    } else {
                        popup.setOrigin(0, popup.getHeight());
                    }
                    popup.setScale(1f, 1f);
                }
            }
        };
    }

    // =========================================================================
    // WIDGET: MARQUESINA SELECTBOX (Scroll con efecto Fade en Bucle)
    // =========================================================================
    private static class MarqueeSelectBox extends Stack {
        public SelectBox<String> selectBox;
        private Label label;
        private ScrollPane textScroller;

        private float scrollX = 0f;
        private float timer = 2.0f;
        private int scrollState = 0;

        public MarqueeSelectBox(SelectBox.SelectBoxStyle baseStyle, Skin skin) {
            super();

            SelectBox.SelectBoxStyle transparentStyle = new SelectBox.SelectBoxStyle(baseStyle);
            transparentStyle.fontColor = new Color(1f, 1f, 1f, 0f);
            transparentStyle.disabledFontColor = new Color(1f, 1f, 1f, 0f);

            selectBox = crearSelectBoxEscalado(transparentStyle);

            label = new Label("", skin, "font-12");
            label.setAlignment(Align.left | Align.center);
            label.setTouchable(Touchable.disabled);

            textScroller = new ScrollPane(label);
            textScroller.setOverscroll(false, false);
            textScroller.setScrollingDisabled(false, true);
            textScroller.setTouchable(Touchable.disabled);

            Container<ScrollPane> clipContainer = new Container<>(textScroller);
            clipContainer.fill();
            clipContainer.padLeft(8f);
            clipContainer.padRight(22f); // Área reservada para la flecha
            clipContainer.setTouchable(Touchable.disabled);

            this.add(selectBox);
            this.add(clipContainer);
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            String selected = selectBox.getSelected();
            if (selected != null) {
                if (!label.getText().toString().equals(selected)) {
                    label.setText(selected);
                    label.pack();
                    scrollX = 0;
                    textScroller.setScrollX(0);
                    scrollState = 0;
                    timer = 2.0f;
                    label.setColor(1, 1, 1, 1f);
                }

                float availableWidth = this.getWidth() - 30f;

                if (label.getWidth() > availableWidth && availableWidth > 0) {
                    float maxScroll = label.getWidth() - availableWidth;

                    switch (scrollState) {
                        case 0: // Espera inicial
                            timer -= delta;
                            if (timer <= 0) scrollState = 1;
                            break;
                        case 1: // Deslizando hacia la izquierda
                            scrollX += 35f * delta;
                            if (scrollX >= maxScroll) {
                                scrollX = maxScroll;
                                scrollState = 2;
                                timer = 1.5f;
                            }
                            textScroller.setScrollX(scrollX);
                            break;
                        case 2: // Espera al final
                            timer -= delta;
                            if (timer <= 0) scrollState = 3;
                            break;
                        case 3: // Fade Out
                            float alphaOut = label.getColor().a;
                            alphaOut -= 3f * delta;
                            if (alphaOut <= 0) {
                                alphaOut = 0;
                                scrollX = 0;
                                textScroller.setScrollX(0);
                                scrollState = 4;
                            }
                            label.setColor(1, 1, 1, alphaOut);
                            break;
                        case 4: // Fade In
                            float alphaIn = label.getColor().a;
                            alphaIn += 3f * delta;
                            if (alphaIn >= 1f) {
                                alphaIn = 1f;
                                scrollState = 0;
                                timer = 2.0f;
                            }
                            label.setColor(1, 1, 1, alphaIn);
                            break;
                    }
                } else {
                    textScroller.setScrollX(0);
                    label.setColor(1, 1, 1, 1f);
                }
            }
        }

        public String getSelected() { return selectBox.getSelected(); }
        public void setSelectedIndex(int index) { selectBox.setSelectedIndex(index); }
        public void setSelected(String item) { selectBox.setSelected(item); }
        public void setItems(Array<String> items) { selectBox.setItems(items); }
        public void setItems(String... items) { selectBox.setItems(items); }
        public void setMaxListCount(int count) { selectBox.setMaxListCount(count); }
        public void addListener(ChangeListener listener) { selectBox.addListener(listener); }
    }
    // =========================================================================

    public static void mostrar(Stage stage, final Skin skin, final OnCustomWeaponSaved callback) {
        final Dialog dialog = new Dialog("", skin);
        Image bgImage = new Image(new Texture(Gdx.files.internal("Menu/MenuMapas/VentanaCreadorArmas.png")));
        dialog.setBackground(bgImage.getDrawable());
        dialog.setModal(true);
        dialog.setMovable(true);

        dialog.getTitleTable().padTop(20).padBottom(10);
        // --- ESTILO DE DESPLEGABLE REDUCIDO ---
        SelectBox.SelectBoxStyle baseStyle = skin.get(SelectBox.SelectBoxStyle.class);
        Label.LabelStyle font12Style = skin.get("font-12", Label.LabelStyle.class);
        if (font12Style == null) font12Style = skin.get(Label.LabelStyle.class);
        SelectBox.SelectBoxStyle smallSelectStyle = new SelectBox.SelectBoxStyle(baseStyle);
        smallSelectStyle.font = font12Style != null ? font12Style.font : null;
        smallSelectStyle.listStyle = new List.ListStyle(baseStyle.listStyle);
        smallSelectStyle.listStyle.font = font12Style != null ? font12Style.font : null;

        if (baseStyle.listStyle.selection != null) {
            Drawable selectionCopy = skin.newDrawable(baseStyle.listStyle.selection);
            selectionCopy.setTopHeight(6f);
            selectionCopy.setBottomHeight(6f);
            selectionCopy.setLeftWidth(5f);
            smallSelectStyle.listStyle.selection = selectionCopy;
        }

        int randomCustomName = com.badlogic.gdx.math.MathUtils.random(100, 9999);
        final TextField nameField = new TextField("Custom" + randomCustomName, skin);
        final TextField damageField = new TextField("10", skin);
        final TextField cdField = new TextField("0.5", skin);
        final TextField critField = new TextField("0.1", skin);
        final TextField penetrationField = new TextField("0", skin);

        final MarqueeSelectBox typeBox = new MarqueeSelectBox(smallSelectStyle, skin);
        typeBox.setItems("KINETIC", "EXPLOSIVE", "ENERGY", "FIRE", "POISON", "ICE");
        typeBox.setMaxListCount(6);

        // --- Mapeado skins armas (internal ID -> sprite path) ---
        final ObjectMap<String, String> spriteMap = new ObjectMap<>();
        spriteMap.put("handgun", "weapons_assets/Handgun");
        spriteMap.put("ballrifle", "weapons_assets/BallRifle");
        spriteMap.put("rocketlauncher", "weapons_assets/RocketLauncher");
        spriteMap.put("toothpickshotgun", "weapons_assets/ToothpickShotgun");
        spriteMap.put("nailgun", "weapons_assets/NailGun");
        spriteMap.put("disclauncher", "weapons_assets/DiscLauncher");
        spriteMap.put("tennislauncher", "weapons_assets/TennisLauncher");
        spriteMap.put("extinguisher", "weapons_assets/Extinguisher");
        spriteMap.put("icegrinder", "weapons_assets/IceGrinder");
        spriteMap.put("rottenfish", "weapons_assets/RottenFish");
        spriteMap.put("banana", "weapons_assets/Banana");
        spriteMap.put("saxophone", "weapons_assets/Saxophone");
        spriteMap.put("lasergun", "weapons_assets/LaserGun");
        spriteMap.put("sword", "weapons_assets/Sword");

        final MarqueeSelectBox spriteBox = new MarqueeSelectBox(smallSelectStyle, skin);
        spriteBox.setItems(ItemNames.getAllWeaponSkinNames());
        spriteBox.setSelected(ItemNames.getAllWeaponSkinNames().get(0));
        spriteBox.setMaxListCount(6);

        // --- Mapeado skins balas (internal ID -> sprite path) ---
        final ObjectMap<String, String> projectileMap = new ObjectMap<>();
        projectileMap.put("gray_bullet", "particle_assets/GrayBullet");
        projectileMap.put("green_bullet", "particle_assets/GreenBullet");
        projectileMap.put("red_bullet", "particle_assets/RedBullet");
        projectileMap.put("white_bullet", "particle_assets/WhiteBullet");
        projectileMap.put("yellow_bullet", "particle_assets/YellowBullet");
        projectileMap.put("blue_bullet", "particle_assets/BlueBullet");
        projectileMap.put("blue_laser", "particle_assets/BlueLaser");
        projectileMap.put("bullet_casing", "particle_assets/BulletCasing");
        projectileMap.put("saw_bullet", "particle_assets/SawBullet");
        projectileMap.put("shotgun_casing", "particle_assets/ShotgunCasing");
        projectileMap.put("spark_bullet", "particle_assets/SparkBullet");
        projectileMap.put("toothpick_bullet", "particle_assets/ToothpickBullet");
        projectileMap.put("tennis_bullet", "particle_assets/TennisBullet");
        projectileMap.put("popcorn", "particle_assets/popcorn");
        projectileMap.put("ice_bullet", "particle_assets/IceBullet");
        projectileMap.put("flame_bullet", "particle_assets/FlameBullet");
        projectileMap.put("music_note", "particle_assets/MusicNote");
        projectileMap.put("disc", "particle_assets/Disc");
        projectileMap.put("rocket_bullet", "particle_assets/RocketBullet");
        projectileMap.put("spike_fish", "particle_assets/SpikeFish");
        projectileMap.put("banana", "weapons_assets/Banana");
        projectileMap.put("pebble", "particle_assets/Ground_pebbles");
        projectileMap.put("fur_ball", "particle_assets/TurretBullet");

        final MarqueeSelectBox projectileBox = new MarqueeSelectBox(smallSelectStyle, skin);
        projectileBox.setItems(ItemNames.getAllProjectileSkinNames());
        projectileBox.setSelected(ItemNames.getAllProjectileSkinNames().get(0));
        projectileBox.setMaxListCount(6);

        // --- Efecto de Estado ---
        final MarqueeSelectBox effectBox = new MarqueeSelectBox(smallSelectStyle, skin);
        effectBox.setItems(ItemNames.getAllEffectNames());
        effectBox.setSelected(ItemNames.getAllEffectNames().get(0));
        effectBox.setMaxListCount(4);

        // --- Mapeo de comportamiento (Behavior) ---
        final MarqueeSelectBox behaviorBox = new MarqueeSelectBox(smallSelectStyle, skin);
        behaviorBox.setItems(ItemNames.getAllBehaviorNames());
        behaviorBox.setSelected(ItemNames.getAllBehaviorNames().get(0));
        behaviorBox.setMaxListCount(6);

        // --- LÓGICA DE AUTO-RELLENADO DE PENETRACIÓN ---
        behaviorBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selectedId = ItemNames.getBehaviorIdByDisplay(behaviorBox.getSelected());
                switch (selectedId) {
                    case "boomerang":
                        penetrationField.setText("999");
                        break;
                    case "bounce":
                        penetrationField.setText("5");
                        break;
                    case "chain":
                        penetrationField.setText("3");
                        break;
                    case "explosive":
                    case "normal":
                    case "zigzag":
                    case "shotgun":
                    case "triple":
                        penetrationField.setText("0");
                        break;
                }
            }
        });

        Table content = dialog.getContentTable();
        content.pad(70, 50, 25, 50);

        // FILA 1: Nombre y Penetración
        content.add(new Label(LanguageManager.t("customgun.name"), skin, "font-12")).right().padRight(8);
        content.add(nameField).width(130).left();
        content.add(new Label(LanguageManager.t("customgun.penetration"), skin, "font-12")).right().padRight(8).padLeft(8);
        content.add(penetrationField).width(130).left().row();

        // FILA 2: Skins (Estética)
        content.add(new Label(LanguageManager.t("customgun.skin.weapon"), skin, "font-12")).right().padRight(8).padTop(8);
        content.add(spriteBox).width(130).padTop(8);
        content.add(new Label(LanguageManager.t("customgun.skin.bullet"), skin, "font-12")).right().padRight(8).padTop(8).padLeft(8);
        content.add(projectileBox).width(130).padTop(8).row();

        // FILA 3: Daño y Tipo
        content.add(new Label(LanguageManager.t("customgun.damage"), skin, "font-12")).right().padRight(8).padTop(8);
        content.add(damageField).width(130).padTop(8);
        content.add(new Label(LanguageManager.t("customgun.damage.type"), skin, "font-12")).right().padRight(8).padTop(8).padLeft(8);
        content.add(typeBox).width(130).padTop(8).row();

        // FILA 4: Modificadores
        content.add(new Label(LanguageManager.t("customgun.effect"), skin, "font-12")).right().padRight(8).padTop(8);
        content.add(effectBox).width(130).padTop(8);
        content.add(new Label(LanguageManager.t("customgun.movement"), skin, "font-12")).right().padRight(8).padTop(8).padLeft(8);
        content.add(behaviorBox).width(130).padTop(8).row();

        // FILA 5: Cadencia y Crítico
        content.add(new Label(LanguageManager.t("customgun.cooldown"), skin, "font-12")).right().padRight(8).padTop(8);
        content.add(cdField).width(130).padTop(8);
        content.add(new Label(LanguageManager.t("customgun.critical"), skin, "font-12")).right().padRight(8).padTop(8).padLeft(8);
        content.add(critField).width(130).padTop(8).row();

        dialog.getButtonTable().pad(15, 0, 30, 0);
        dialog.getButtonTable().center();

        final Runnable ejecutarGuardado = new Runnable() {
            @Override
            public void run() {
                GameSession.CustomWeaponConfig conf = new GameSession.CustomWeaponConfig();
                conf.id = "custom_" + System.currentTimeMillis();
                conf.name = nameField.getText();
                conf.damageType = typeBox.getSelected();
                conf.sprite = spriteMap.get(ItemNames.getWeaponSkinIdByDisplay(spriteBox.getSelected()));
                conf.projectileSprite = projectileMap.get(ItemNames.getProjectileSkinIdByDisplay(projectileBox.getSelected()));

                conf.bulletEffect = ItemNames.getEffectIdByDisplay(effectBox.getSelected());
                conf.bulletBehavior = ItemNames.getBehaviorIdByDisplay(behaviorBox.getSelected());

                try { conf.damage = Float.parseFloat(damageField.getText()); }
                catch (NumberFormatException e) { conf.damage = 10f; }

                try { conf.cd = Float.parseFloat(cdField.getText()); }
                catch (NumberFormatException e) { conf.cd = 0.5f; }

                try {
                    float crit = Float.parseFloat(critField.getText());
                    if (crit > 1.0f) crit = 1.0f;
                    if (crit < 0.0f) crit = 0.0f;
                    conf.critChance = crit;
                } catch (NumberFormatException e) { conf.critChance = 0.05f; }

                try {
                    conf.penetration = Integer.parseInt(penetrationField.getText());
                } catch (NumberFormatException e) {
                    conf.penetration = 0;
                }

                GameSession.customWeapons.put(conf.id, conf);
                GameSession.saveCustomWeapons();

                String currentUser = com.tikisadventure.core.SaveManager.getLastUsername();
                if (currentUser != null && !currentUser.isEmpty()) {
                    long coins = com.tikisadventure.core.SaveManager.getProfileData().coins;
                    long score = com.tikisadventure.core.SaveManager.getProfileData().totalScore;
                    new com.tikisadventure.database.progress.ProgressRepository()
                        .actualizarProgreso(currentUser, coins, score, null);
                }

                dialog.addAction(Actions.sequence(Actions.fadeOut(0.2f), Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        dialog.hide();
                        if (callback != null) callback.onSaved();
                    }
                })));
            }
        };

        TextButton btnGuardar = ButtonFactory.createTextButton(LanguageManager.t("customgun.save"), () -> {
            if (GameSession.customWeapons.size >= MAX_CUSTOM_WEAPONS) {
                String mensajeAviso = LanguageManager.t("customgun.limit.warning", String.valueOf(MAX_CUSTOM_WEAPONS));
                new DeleteWeaponUI(skin, stage, mensajeAviso, ejecutarGuardado).show();
            } else {
                ejecutarGuardado.run();
            }
        });
        TextButton btnCancelar = ButtonFactory.createTextButton(LanguageManager.t("customgun.cancel"), () -> {
            dialog.addAction(Actions.sequence(Actions.fadeOut(0.2f), Actions.run(new Runnable() {
                @Override
                public void run() {
                    dialog.hide();
                }
            })));
        });

        dialog.getButtonTable().add(btnGuardar).width(170).pad(10);
        dialog.getButtonTable().add(btnCancelar).width(180).pad(10);
        dialog.pack();
        dialog.getColor().a = 0f;
        dialog.addAction(Actions.fadeIn(0.2f));
        dialog.show(stage);
    }
}
