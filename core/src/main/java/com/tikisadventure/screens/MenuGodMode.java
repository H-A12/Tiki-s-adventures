package com.tikisadventure.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
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
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.tikisadventure.core.Assets;
import com.tikisadventure.core.GameSession;
import com.tikisadventure.ui.DeleteWeaponUI;

public class MenuGodMode {

    private final Stage stage;
    private final Skin uiSkin;

    private TextButton customGodButton;
    private Dialog customGodDialog;

    private CheckBox godModeCheck;
    private TikibotAnimActor tikibotIcon;
    private MarqueeSelectBox[] weaponSelectors;
    private ObjectMap<String, String> weaponNameToIdMap = new ObjectMap<>();
    private SelectBox.SelectBoxStyle smallSelectStyle;
    private Texture texBotonCrear, texBotonEliminar, texTickV, texGodModeButton;

    public MenuGodMode(Stage stage, Skin uiSkin) {
        texBotonCrear = new Texture(Gdx.files.internal("Menu/MenuMapas/BotonCrearArmas.png"));
        texBotonEliminar = new Texture(Gdx.files.internal("Menu/MenuMapas/BotonEliminarArmas.png"));
        texTickV = new Texture(Gdx.files.internal("sprites/shared/UI_assets/UI_V.png"));
        texGodModeButton = new Texture(Gdx.files.internal("Menu/MenuMapas/GodModeButton.png"));
        this.stage = stage;
        this.uiSkin = uiSkin;
        GameSession.loadCustomWeapons();

        SelectBox.SelectBoxStyle baseStyle = uiSkin.get(SelectBox.SelectBoxStyle.class);
        smallSelectStyle = new SelectBox.SelectBoxStyle(baseStyle);
        smallSelectStyle.font = uiSkin.get("font-12", Label.LabelStyle.class).font;
        smallSelectStyle.listStyle = new List.ListStyle(baseStyle.listStyle);
        smallSelectStyle.listStyle.font = uiSkin.get("font-12", Label.LabelStyle.class).font;

        if (baseStyle.listStyle.selection != null) {
            Drawable selectionCopy = uiSkin.newDrawable(baseStyle.listStyle.selection);
            selectionCopy.setTopHeight(6f);
            selectionCopy.setBottomHeight(6f);
            selectionCopy.setLeftWidth(5f);
            smallSelectStyle.listStyle.selection = selectionCopy;
        }

        crearVentanaModoDios();
    }

    public void inyectarInterfaz(Table tablaDestino) {
        inyectarInterfaz(tablaDestino, null);
    }

    public void inyectarInterfaz(Table tablaDestino, final Runnable onToggle) {
        godModeCheck = new CheckBox("MODO DIOS", uiSkin);
        godModeCheck.setChecked(GameSession.godMode);
        TextureRegionDrawable tickOnGod = new TextureRegionDrawable(new TextureRegion(texTickV));
        tickOnGod.setMinWidth(24);
        tickOnGod.setMinHeight(24);
        CheckBox.CheckBoxStyle godStyle = new CheckBox.CheckBoxStyle(uiSkin.get(CheckBox.CheckBoxStyle.class));
        godStyle.checkboxOn = tickOnGod;
        godModeCheck.setStyle(godStyle);
        godModeCheck.getCells().get(0).padRight(10);

        customGodButton = new TextButton("Parametros", uiSkin);
        customGodButton.setVisible(GameSession.godMode);

        TextureRegionDrawable botonText = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("Menu/BotonText.png"))));
        TextButton.TextButtonStyle paramStyle = new TextButton.TextButtonStyle(botonText, botonText, botonText, uiSkin.get("font-13", Label.LabelStyle.class).font);
        customGodButton.setStyle(paramStyle);
        customGodButton.getStyle().pressedOffsetX = 0;
        customGodButton.getStyle().pressedOffsetY = 0;

        TextureRegion texCreate = Assets.getRegion("shared", "UI_assets/CreateWeapon");
        TextureRegion texDelete = Assets.getRegion("shared", "UI_assets/DeleteWeapon");

        if (texCreate == null) texCreate = Assets.getRegion("shared", "UI_assets/UI_Crosshair");
        if (texDelete == null) texDelete = Assets.getRegion("shared", "UI_assets/UI_Crosshair");

        Button.ButtonStyle crearStyle = new Button.ButtonStyle();
        crearStyle.up = new TextureRegionDrawable(new TextureRegion(texBotonCrear));
        final Button btnCrearArma = new Button(crearStyle);
        Image imgCreate = new Image(texCreate);
        imgCreate.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        btnCrearArma.add(imgCreate).size(32, 32).expand().center();

        Button.ButtonStyle eliminarStyle = new Button.ButtonStyle();
        eliminarStyle.up = new TextureRegionDrawable(new TextureRegion(texBotonEliminar));
        final Button btnBorrarArma = new Button(eliminarStyle);
        Image imgDelete = new Image(texDelete);
        imgDelete.setScaling(com.badlogic.gdx.utils.Scaling.fit);
        btnBorrarArma.add(imgDelete).size(32, 32).expand().center();

        btnCrearArma.setVisible(GameSession.godMode);
        btnBorrarArma.setVisible(GameSession.godMode);

        TextureRegion idleStrip = Assets.getRegion("tikibot", "player_assets/tikibot/idle");
        TextureRegion[] idleFrames = new TextureRegion[12];
        for (int i = 0; i < 12; i++) {
            idleFrames[i] = new TextureRegion(idleStrip, i * 16, 0, 16, 16);
        }
        Animation<TextureRegion> tikibotIdleAnim = new Animation<TextureRegion>(0.15f, idleFrames);
        tikibotIcon = new TikibotAnimActor(tikibotIdleAnim);
        tikibotIcon.setVisible(GameSession.godMode);
        tikibotIcon.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                tikibotIcon.playOnce();
            }
        });

        Table checkRow = new Table();
        checkRow.setBackground(new TextureRegionDrawable(new TextureRegion(texGodModeButton)));
        checkRow.pad(8, 12, 8, 12);
        checkRow.add(godModeCheck).left();
        checkRow.add(tikibotIcon).size(36, 36).padLeft(7);

        godModeCheck.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSession.godMode = godModeCheck.isChecked();
                customGodButton.setVisible(GameSession.godMode);
                btnCrearArma.setVisible(GameSession.godMode);
                btnBorrarArma.setVisible(GameSession.godMode);
                tikibotIcon.setVisible(GameSession.godMode);
                if (onToggle != null) onToggle.run();
            }
        });

        customGodButton.addListener(new Assets.HoverCursorListener());
        customGodButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                customGodDialog.getColor().a = 0f;
                customGodDialog.addAction(Actions.fadeIn(0.2f));
                customGodDialog.show(stage);
            }
        });

        btnCrearArma.addListener(new Assets.HoverCursorListener());
        btnCrearArma.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                MenuCustomGun.mostrar(stage, uiSkin, new MenuCustomGun.OnCustomWeaponSaved() {
                    @Override
                    public void onSaved() {
                        actualizarDesplegablesArmas();
                    }
                });
            }
        });

        btnBorrarArma.addListener(new Assets.HoverCursorListener());
        btnBorrarArma.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                new DeleteWeaponUI(uiSkin, stage, new Runnable() {
                    @Override
                    public void run() {
                        actualizarDesplegablesArmas();
                    }
                }).show();
            }
        });

        Table botonesCustomTable = new Table();
        botonesCustomTable.add(btnCrearArma).size(40, 40).padRight(10);
        botonesCustomTable.add(btnBorrarArma).size(40, 40);

        tablaDestino.row();
        tablaDestino.add(checkRow).left().bottom().pad(10);
        tablaDestino.row();
        tablaDestino.add(customGodButton).width(160).height(30).left().padLeft(10);
        tablaDestino.row();
        tablaDestino.add(botonesCustomTable).left().padLeft(10).padTop(5);
    }

    public void vibrateCheckbox() {
        if (godModeCheck == null) return;
        godModeCheck.clearActions();

        final Label label = godModeCheck.getLabel();
        godModeCheck.setColor(Color.RED);
        label.setColor(Color.RED);

        godModeCheck.addAction(Actions.sequence(
            Actions.repeat(5, Actions.sequence(
                Actions.moveBy(2f, 0, 0.025f),
                Actions.moveBy(-2f, 0, 0.025f)
            )),
            Actions.run(() -> {
                godModeCheck.setColor(Color.WHITE);
                label.setColor(Color.WHITE);
            })
        ));
    }

    // =========================================================================
    // HELPER BASE: SelectBox que escala su lista flotante al tamaño de la ventana
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
    // NUEVO WIDGET: MARQUESINA SELECTBOX (Scroll con efecto Fade en Bucle)
    // =========================================================================
    public static class MarqueeSelectBox extends Stack {
        public SelectBox<String> selectBox;
        private Label label;
        private ScrollPane textScroller;

        private float scrollX = 0f;
        private float timer = 2.0f;
        // 0: Espera inicio, 1: Deslizando, 2: Espera final, 3: Fade Out, 4: Fade In
        private int scrollState = 0;

        public MarqueeSelectBox(SelectBox.SelectBoxStyle baseStyle, Skin skin) {
            super();

            // Hacemos el texto de la caja original completamente transparente
            SelectBox.SelectBoxStyle transparentStyle = new SelectBox.SelectBoxStyle(baseStyle);
            transparentStyle.fontColor = new Color(1f, 1f, 1f, 0f);
            transparentStyle.disabledFontColor = new Color(1f, 1f, 1f, 0f);

            selectBox = crearSelectBoxEscalado(transparentStyle);

            // Etiqueta visible
            label = new Label("", skin, "font-12");
            label.setAlignment(Align.left | Align.center);
            label.setTouchable(Touchable.disabled);

            // Usamos un ScrollPane para ocultar el texto excedente
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
                // Si el texto cambia (el usuario selecciona otra cosa), reseteamos todo
                if (!label.getText().toString().equals(selected)) {
                    label.setText(selected);
                    label.pack();
                    scrollX = 0;
                    textScroller.setScrollX(0);
                    scrollState = 0;
                    timer = 2.0f;
                    label.setColor(1, 1, 1, 1f); // Visibilidad al 100%
                }

                float availableWidth = this.getWidth() - 30f; // pad(8) + pad(22)

                if (label.getWidth() > availableWidth && availableWidth > 0) {
                    float maxScroll = label.getWidth() - availableWidth;

                    // Máquina de estados para la animación en bucle
                    switch (scrollState) {
                        case 0: // Espera inicial
                            timer -= delta;
                            if (timer <= 0) scrollState = 1;
                            break;
                        case 1: // Deslizando hacia la izquierda
                            scrollX += 35f * delta; // Velocidad de lectura
                            if (scrollX >= maxScroll) {
                                scrollX = maxScroll;
                                scrollState = 2;
                                timer = 1.0f; // Pausa para leer el final
                            }
                            textScroller.setScrollX(scrollX);
                            break;
                        case 2: // Espera al final
                            timer -= delta;
                            if (timer <= 0) scrollState = 3;
                            break;
                        case 3: // Desvanecimiento (Fade Out)
                            float alphaOut = label.getColor().a;
                            alphaOut -= 3f * delta; // Se desvanece rápido (aprox 0.3s)
                            if (alphaOut <= 0) {
                                alphaOut = 0;
                                scrollX = 0; // Salta al inicio mientras es invisible
                                textScroller.setScrollX(0);
                                scrollState = 4;
                            }
                            label.setColor(1, 1, 1, alphaOut);
                            break;
                        case 4: // Reaparición (Fade In)
                            float alphaIn = label.getColor().a;
                            alphaIn += 3f * delta;
                            if (alphaIn >= 1f) {
                                alphaIn = 1f;
                                scrollState = 0; // Reinicia el ciclo
                                timer = 2.0f; // Pausa antes de volver a deslizar
                            }
                            label.setColor(1, 1, 1, alphaIn);
                            break;
                    }
                } else {
                    textScroller.setScrollX(0);
                    label.setColor(1, 1, 1, 1f); // Mantiene el texto visible y fijo si cabe entero
                }
            }
        }

        // Delegados para que funcione como un SelectBox normal
        public String getSelected() { return selectBox.getSelected(); }
        public void setSelectedIndex(int index) { selectBox.setSelectedIndex(index); }
        public void setSelected(String item) { selectBox.setSelected(item); }
        public void setItems(Array<String> items) { selectBox.setItems(items); }
        public void setItems(String... items) { selectBox.setItems(items); }
        public void setMaxListCount(int count) { selectBox.setMaxListCount(count); }
        public void addListener(ChangeListener listener) { selectBox.addListener(listener); }
    }

    @SuppressWarnings("unchecked")
    private void crearVentanaModoDios() {
        customGodDialog = new Dialog("", uiSkin);
        Image bgParams = new Image(new Texture(Gdx.files.internal("Menu/MenuMapas/VentanaParametros.png")));
        customGodDialog.setBackground(bgParams.getDrawable());
        customGodDialog.setModal(true);
        customGodDialog.setMovable(false);
        customGodDialog.pad(35, 50, 35, 50);

        TextButton closeButton = new TextButton("X", uiSkin);
        closeButton.getStyle().up = null;
        closeButton.getStyle().down = null;
        closeButton.getStyle().over = null;
        closeButton.addListener(new Assets.HoverCursorListener());
        closeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                customGodDialog.addAction(Actions.sequence(Actions.fadeOut(0.2f), Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        customGodDialog.hide();
                    }
                })));
            }
        });
        customGodDialog.getTitleTable().add(closeButton).size(30, 25).padRight(-55).padTop(6);

        // --- ARMAS ---
        weaponSelectors = new MarqueeSelectBox[6];
        Table tablaArmas = new Table();

        JsonValue weaponData = new JsonReader().parse(Gdx.files.internal("data/weapons_config.json"));
        Array<String> weaponNames = new Array<>();
        weaponNameToIdMap.clear();

        weaponNames.add("Sin arma");
        weaponNameToIdMap.put("Sin arma", "");

        for (JsonValue weaponEntry : weaponData.get("weapons")) {
            String weaponId = weaponEntry.name;
            if (weaponId.contains("Plantilla")) continue;

            String displayName = weaponEntry.getString("name", weaponId);
            weaponNames.add(displayName);
            weaponNameToIdMap.put(displayName, weaponId);
        }

        for (GameSession.CustomWeaponConfig custom : GameSession.customWeapons.values()) {
            String displayCustomName = custom.name + " [C]";
            weaponNames.add(displayCustomName);
            weaponNameToIdMap.put(displayCustomName, custom.id);
        }

        for (int i = 0; i < 6; i++) {
            weaponSelectors[i] = new MarqueeSelectBox(smallSelectStyle, uiSkin);
            weaponSelectors[i].setMaxListCount(6);
            weaponSelectors[i].setItems(weaponNames);

            if (i == 0) {
                weaponSelectors[i].setSelectedIndex(1);
            } else {
                weaponSelectors[i].setSelectedIndex(0);
            }

            final int index = i;
            weaponSelectors[i].addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    String selectedName = weaponSelectors[index].getSelected();
                    String selectedId = weaponNameToIdMap.get(selectedName);
                    GameSession.godModeWeapons[index] = selectedId;
                }
            });

            GameSession.godModeWeapons[i] = weaponNameToIdMap.get(weaponSelectors[i].getSelected());

            tablaArmas.add(new Label("Arma " + (i + 1) + ":", uiSkin, "font-12")).padRight(5).right();
            tablaArmas.add(weaponSelectors[i]).width(230).padRight(10).padBottom(8);
            if (i % 2 == 1) tablaArmas.row();
        }

        // --- MULTIPLICADOR DE DAÑO ---
        final ObjectMap<String, Float> multiplicadoresMap = new ObjectMap<>();
        multiplicadoresMap.put("x0.25", 0.25f);
        multiplicadoresMap.put("x0.5", 0.5f);
        multiplicadoresMap.put("x1.0", 1.0f);
        multiplicadoresMap.put("x1.5", 1.5f);
        multiplicadoresMap.put("x2.0", 2.0f);
        multiplicadoresMap.put("x3.0", 3.0f);
        multiplicadoresMap.put("x5.0", 5.0f);
        multiplicadoresMap.put("x10.0", 10.0f);

        final MarqueeSelectBox damageSelector = new MarqueeSelectBox(smallSelectStyle, uiSkin);
        damageSelector.setItems("x0.25", "x0.5", "x1.0", "x1.5", "x2.0", "x3.0", "x5.0", "x10.0");
        damageSelector.setSelected("x1.0");

        damageSelector.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSession.godModeDamageMultiplier = multiplicadoresMap.get(damageSelector.getSelected());
            }
        });
        GameSession.godModeDamageMultiplier = multiplicadoresMap.get(damageSelector.getSelected());

        // --- VIDA PERSONAJE ---
        final MarqueeSelectBox healthSelector = new MarqueeSelectBox(smallSelectStyle, uiSkin);
        healthSelector.setItems("1", "25", "50", "100", "200", "500", "1000", "Inmortal");
        healthSelector.setSelected("100");

        healthSelector.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selection = healthSelector.getSelected();
                if (selection.equalsIgnoreCase("Inmortal")) {
                    GameSession.godModeIsImmortal = true;
                    GameSession.godModeHealthValue = 9999f;
                } else {
                    GameSession.godModeIsImmortal = false;
                    GameSession.godModeHealthValue = Float.parseFloat(selection);
                }
            }
        });
        GameSession.godModeHealthValue = 100f;
        GameSession.godModeIsImmortal = false;

        // --- VELOCIDAD DEL PERSONAJE ---
        final MarqueeSelectBox speedSelector = new MarqueeSelectBox(smallSelectStyle, uiSkin);
        speedSelector.setItems("1", "3", "5", "7", "10", "15", "30");
        speedSelector.setSelected("5");

        speedSelector.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSession.godModeSpeedValue = Float.parseFloat(speedSelector.getSelected());
            }
        });
        GameSession.godModeSpeedValue = 5.0f;

        // --- GADGETS (Habilidad 2) ---
        JsonValue abilityData = new JsonReader().parse(Gdx.files.internal("data/abilities_config.json"));
        Array<String> gadgetNames = new Array<>();
        final ObjectMap<String, String> gadgetNameToIdMap = new ObjectMap<>();

        for (JsonValue abilityEntry : abilityData) {
            String abilityId = abilityEntry.name;
            if (abilityId.toLowerCase().contains("dash")) continue;

            String displayName = abilityEntry.getString("name", abilityId);
            gadgetNames.add(displayName);
            gadgetNameToIdMap.put(displayName, abilityId);
        }

        final MarqueeSelectBox gadgetSelector = new MarqueeSelectBox(smallSelectStyle, uiSkin);
        gadgetSelector.setItems(gadgetNames);
        gadgetSelector.setMaxListCount(6);

        gadgetSelector.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSession.godModeAbility2Id = gadgetNameToIdMap.get(gadgetSelector.getSelected());
            }
        });

        GameSession.godModeAbility1Id = null;
        GameSession.godModeAbility2Id = gadgetNameToIdMap.get(gadgetSelector.getSelected());

        // --- MONTAJE VENTANA PARAMETROS ---
        customGodDialog.getContentTable().clear();
        customGodDialog.getContentTable().padTop(10);

        customGodDialog.getContentTable().add(tablaArmas).colspan(2).padBottom(15).row();

        Table tablaAtributos = new Table();

        tablaAtributos.add(new Label("Gadget:", uiSkin, "font-12")).padRight(10).right();
        tablaAtributos.add(gadgetSelector).width(180).padBottom(8).left().row();

        tablaAtributos.add(new Label("Damage:", uiSkin, "font-12")).padRight(10).right();
        tablaAtributos.add(damageSelector).width(180).padBottom(8).left().row();

        tablaAtributos.add(new Label("Vida:", uiSkin, "font-12")).padRight(10).right();
        tablaAtributos.add(healthSelector).width(180).padBottom(8).left().row();

        tablaAtributos.add(new Label("Velocidad:", uiSkin, "font-12")).padRight(10).right();
        tablaAtributos.add(speedSelector).width(180).padBottom(8).left().row();

        customGodDialog.getContentTable().add(tablaAtributos).colspan(2).center();
    }

    @SuppressWarnings("unchecked")
    private void actualizarDesplegablesArmas() {
        if (weaponSelectors == null) return;

        Array<String> weaponNames = new Array<>();
        weaponNameToIdMap.clear();

        weaponNames.add("Sin arma");
        weaponNameToIdMap.put("Sin arma", "");

        JsonValue weaponData = new JsonReader().parse(Gdx.files.internal("data/weapons_config.json"));
        for (JsonValue weaponEntry : weaponData.get("weapons")) {
            String weaponId = weaponEntry.name;
            if (weaponId.contains("Plantilla")) continue;

            String displayName = weaponEntry.getString("name", weaponId);
            weaponNames.add(displayName);
            weaponNameToIdMap.put(displayName, weaponId);
        }

        for (GameSession.CustomWeaponConfig custom : GameSession.customWeapons.values()) {
            String displayCustomName = custom.name + " [C]";
            weaponNames.add(displayCustomName);
            weaponNameToIdMap.put(displayCustomName, custom.id);
        }

        for (int i = 0; i < 6; i++) {
            String seleccionPrevia = weaponSelectors[i].getSelected();
            weaponSelectors[i].setItems(weaponNames);

            if (seleccionPrevia != null && weaponNames.contains(seleccionPrevia, false)) {
                weaponSelectors[i].setSelected(seleccionPrevia);
            } else {
                if (i == 0) weaponSelectors[i].setSelectedIndex(1);
                else weaponSelectors[i].setSelectedIndex(0);
            }
        }
    }

    public void dispose() {
        if (texBotonCrear != null) texBotonCrear.dispose();
        if (texBotonEliminar != null) texBotonEliminar.dispose();
        if (texGodModeButton != null) texGodModeButton.dispose();
    }

    private static class TikibotAnimActor extends Actor {
        private final Animation<TextureRegion> animIdle;
        private float stateTime;
        private boolean playing;

        public TikibotAnimActor(Animation<TextureRegion> animIdle) {
            this.animIdle = animIdle;
            this.stateTime = 0;
            this.playing = false;
        }

        public void playOnce() {
            stateTime = 0;
            playing = true;
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            if (playing) {
                stateTime += delta;
                if (animIdle.isAnimationFinished(stateTime)) {
                    playing = false;
                }
            }
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            if (animIdle == null) return;
            TextureRegion frame = playing ? animIdle.getKeyFrame(stateTime, false) : animIdle.getKeyFrame(0);
            batch.setColor(getColor().r, getColor().g, getColor().b, getColor().a * parentAlpha);
            batch.draw(frame, getX(), getY(), getWidth(), getHeight());
        }
    }
}
