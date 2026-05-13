package com.tikisadventure.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.tikisadventure.core.GameSession;
import com.tikisadventure.screens.MenuGodMode.MarqueeSelectBox;
import com.tikisadventure.ui.DeleteWeaponUI;

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
        SelectBox.SelectBoxStyle smallSelectStyle = new SelectBox.SelectBoxStyle(baseStyle);
        smallSelectStyle.font = skin.get("font-12", Label.LabelStyle.class).font;
        smallSelectStyle.listStyle = new List.ListStyle(baseStyle.listStyle);
        smallSelectStyle.listStyle.font = skin.get("font-12", Label.LabelStyle.class).font;

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

        // --- Mapeado skins armas ---
        final ObjectMap<String, String> spriteMap = new ObjectMap<>();
        spriteMap.put("Pistola", "weapons_assets/Handgun");
        spriteMap.put("Fusil de bolas", "weapons_assets/BallRifle");
        spriteMap.put("Pirocohete", "weapons_assets/RocketLauncher");
        spriteMap.put("Escupepalillos", "weapons_assets/ToothpickShotgun");
        spriteMap.put("Clavolleta", "weapons_assets/NailGun");
        spriteMap.put("Lanzadiscos", "weapons_assets/DiscLauncher");
        spriteMap.put("Lanzapelotas", "weapons_assets/TennisLauncher");
        spriteMap.put("Extintor trucado", "weapons_assets/Extinguisher");
        spriteMap.put("Triturahielo", "weapons_assets/IceGrinder");
        spriteMap.put("Putripez", "weapons_assets/RottenFish");
        spriteMap.put("Banana", "weapons_assets/Banana");
        spriteMap.put("Saxofon", "weapons_assets/Saxophone");
        spriteMap.put("Arma laser", "weapons_assets/LaserGun");
        spriteMap.put("Espada", "weapons_assets/Sword");

        final MarqueeSelectBox spriteBox = new MarqueeSelectBox(smallSelectStyle, skin);
        spriteBox.setItems(
            "Pistola", "Fusil de bolas", "Pirocohete", "Escupepalillos",
            "Clavolleta", "Lanzadiscos", "Lanzapelotas", "Extintor trucado",
            "Triturahielo", "Putripez", "Banana", "Saxofon", "Arma laser", "Espada"
        );
        spriteBox.setSelected("Pistola");
        spriteBox.setMaxListCount(6);

        // --- Mapeado skins balas ---
        final ObjectMap<String, String> projectileMap = new ObjectMap<>();
        projectileMap.put("Bala gris", "particle_assets/GrayBullet");
        projectileMap.put("Bala verde", "particle_assets/GreenBullet");
        projectileMap.put("Bala roja", "particle_assets/RedBullet");
        projectileMap.put("Bala blanca", "particle_assets/WhiteBullet");
        projectileMap.put("Bala amarilla", "particle_assets/YellowBullet");
        projectileMap.put("Bala azul", "particle_assets/BlueBullet");
        projectileMap.put("Laser azul", "particle_assets/BlueLaser");
        projectileMap.put("Casquillo amarillo", "particle_assets/BulletCasing");
        projectileMap.put("Sierra", "particle_assets/SawBullet");
        projectileMap.put("Casquillo rojo", "particle_assets/ShotgunCasing");
        projectileMap.put("Cortocircuito", "particle_assets/SparkBullet");
        projectileMap.put("Palillo", "particle_assets/ToothpickBullet");
        projectileMap.put("Pelota de tenis", "particle_assets/TennisBullet");
        projectileMap.put("Palomita", "particle_assets/popcorn");
        projectileMap.put("Escarcha", "particle_assets/IceBullet");
        projectileMap.put("Llamarada", "particle_assets/FlameBullet");
        projectileMap.put("Nota musical", "particle_assets/MusicNote");
        projectileMap.put("Disco", "particle_assets/Disc");
        projectileMap.put("Petardo", "particle_assets/RocketBullet");
        projectileMap.put("Pua de pez", "particle_assets/SpikeFish");
        projectileMap.put("Banana", "weapons_assets/Banana");
        projectileMap.put("Piedra", "particle_assets/Ground_pebbles");
        projectileMap.put("Bola de pelo", "particle_assets/TurretBullet");

        final MarqueeSelectBox projectileBox = new MarqueeSelectBox(smallSelectStyle, skin);
        projectileBox.setItems(
            "Bala gris", "Bala verde", "Bala roja", "Bala blanca",
            "Bala amarilla", "Bala azul", "Laser azul", "Casquillo amarillo",
            "Sierra", "Casquillo rojo", "Cortocircuito", "Palillo",
            "Pelota de tenis", "Palomita", "Escarcha", "Llamarada",
            "Nota musical", "Disco", "Petardo", "Pua de pez",
            "Banana", "Piedra", "Bola de pelo"
        );
        projectileBox.setSelected("Bala gris");
        projectileBox.setMaxListCount(6);

        // --- Efecto de Estado ---
        final MarqueeSelectBox effectBox = new MarqueeSelectBox(smallSelectStyle, skin);
        effectBox.setItems("Ninguno", "Quemadura", "Veneno", "Congelacion");
        effectBox.setSelected("Ninguno");
        effectBox.setMaxListCount(4);

        // --- Mapeo de comportamiento (Behavior) ---
        final MarqueeSelectBox behaviorBox = new MarqueeSelectBox(smallSelectStyle, skin);
        behaviorBox.setItems("Normal", "Rebote", "Zigzag", "Perdigones", "Explosiva", "Cadena", "Boomerang", "Triple");
        behaviorBox.setSelected("Normal");
        behaviorBox.setMaxListCount(6);

        // --- LÓGICA DE AUTO-RELLENADO DE PENETRACIÓN ---
        behaviorBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selected = behaviorBox.getSelected();
                switch (selected) {
                    case "Boomerang":
                        penetrationField.setText("999");
                        break;
                    case "Rebote":
                        penetrationField.setText("5");
                        break;
                    case "Cadena":
                        penetrationField.setText("3");
                        break;
                    case "Explosiva":
                    case "Normal":
                    case "Zigzag":
                    case "Perdigones":
                    case "Triple":
                        penetrationField.setText("0");
                        break;
                }
            }
        });

        Table content = dialog.getContentTable();
        content.pad(70, 50, 25, 50);

        // FILA 1: Nombre y Penetración
        content.add(new Label("Nombre:", skin, "font-12")).right().padRight(8);
        content.add(nameField).width(130).left();
        content.add(new Label("Penetración:", skin, "font-12")).right().padRight(8).padLeft(8);
        content.add(penetrationField).width(130).left().row();

        // FILA 2: Skins (Estética)
        content.add(new Label("Skin Arma:", skin, "font-12")).right().padRight(8).padTop(8);
        content.add(spriteBox).width(130).padTop(8);
        content.add(new Label("Skin Bala:", skin, "font-12")).right().padRight(8).padTop(8).padLeft(8);
        content.add(projectileBox).width(130).padTop(8).row();

        // FILA 3: Daño y Tipo
        content.add(new Label("Daño:", skin, "font-12")).right().padRight(8).padTop(8);
        content.add(damageField).width(130).padTop(8);
        content.add(new Label("Tipo Daño:", skin, "font-12")).right().padRight(8).padTop(8).padLeft(8);
        content.add(typeBox).width(130).padTop(8).row();

        // FILA 4: Modificadores
        content.add(new Label("Efecto:", skin, "font-12")).right().padRight(8).padTop(8);
        content.add(effectBox).width(130).padTop(8);
        content.add(new Label("Movimiento:", skin, "font-12")).right().padRight(8).padTop(8).padLeft(8);
        content.add(behaviorBox).width(130).padTop(8).row();

        // FILA 5: Cadencia y Crítico
        content.add(new Label("Cd (FPS):", skin, "font-12")).right().padRight(8).padTop(8);
        content.add(cdField).width(130).padTop(8);
        content.add(new Label("Crítico:", skin, "font-12")).right().padRight(8).padTop(8).padLeft(8);
        content.add(critField).width(130).padTop(8).row();

        dialog.getButtonTable().pad(15, 0, 30, 0);
        dialog.getButtonTable().center();

        TextButton btnGuardar = new TextButton("Guardar", skin);
        TextButton btnCancelar = new TextButton("Cancelar", skin);

        TextureRegionDrawable botonText = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("Menu/BotonText.png"))));
        TextButton.TextButtonStyle btnGuion = new TextButton.TextButtonStyle(botonText, botonText, botonText, skin.get("font-14", Label.LabelStyle.class).font);
        btnGuion.pressedOffsetX = 0;
        btnGuion.pressedOffsetY = 0;
        btnGuardar.setStyle(btnGuion);
        btnCancelar.setStyle(btnGuion);

        final Runnable ejecutarGuardado = new Runnable() {
            @Override
            public void run() {
                GameSession.CustomWeaponConfig conf = new GameSession.CustomWeaponConfig();
                conf.id = "custom_" + System.currentTimeMillis();
                conf.name = nameField.getText();
                conf.damageType = typeBox.getSelected();
                conf.sprite = spriteMap.get(spriteBox.getSelected());
                conf.projectileSprite = projectileMap.get(projectileBox.getSelected());

                conf.bulletEffect = effectBox.getSelected();
                conf.bulletBehavior = behaviorBox.getSelected();

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

        btnGuardar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (GameSession.customWeapons.size >= MAX_CUSTOM_WEAPONS) {
                    String mensajeAviso = "No puedes tener mas de " + MAX_CUSTOM_WEAPONS + " armas guardadas, elimina una para continuar.";
                    new DeleteWeaponUI(skin, stage, mensajeAviso, new Runnable() {
                        @Override
                        public void run() {
                            ejecutarGuardado.run();
                        }
                    }).show();
                } else {
                    ejecutarGuardado.run();
                }
            }
        });

        btnCancelar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                dialog.addAction(Actions.sequence(Actions.fadeOut(0.2f), Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        dialog.hide();
                    }
                })));
            }
        });

        dialog.getButtonTable().add(btnGuardar).width(170).pad(10);
        dialog.getButtonTable().add(btnCancelar).width(180).pad(10);
        dialog.pack();
        dialog.getColor().a = 0f;
        dialog.addAction(Actions.fadeIn(0.2f));
        dialog.show(stage);
    }
}
