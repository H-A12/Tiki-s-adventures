package com.tikisadventure.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.tikisadventure.ui.CharacterPreviewActor;
import com.tikisadventure.ui.GadgetUI;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.tikisadventure.entities.player.CharacterFactory;
import com.tikisadventure.core.GameSession;
import com.tikisadventure.core.SaveManager;
import com.tikisadventure.core.Assets;
import com.tikisadventure.ui.FontManager;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class MenuMapScreen implements Screen {

    private final Game game;
    private Stage stage;
    private Skin uiSkin;
    private SpriteBatch batch;
    private Texture[] texIconosMapas;

    private Group grupoFondos;
    private ImagenFondo fondoBosque, fondoDesierto, fondoCastillo;
    private Texture menuSalienteTex;
    private NinePatchDrawable panelBackground;
    private Table ventanaIzquierda, ventanaDerecha;
    private Image iconMapa;
    private Texture texIconBosque, texIconDesierto, texIconCastillo;
    private int mapaActualIndex = 0;
    private final String[] nombresMapas = {"BOSQUE MUCOSO", "DESIERTO SECAROCAS", "CASTILLO ATERRADOR"};
    private final String[] descripcionesMapas = {
        "BOSQUE MUCOSO: El amanecer de la aventura de Tiki.",
        "DESIERTO SECAROCAS: Recuerda mantenerte hidratado.",
        "CASTILLO ATERRADOR: Las paredes tienen ojos..."
    };

    private Label labelTituloMapa, labelDesc;
    private TextButton btnJugar, btnVolver, btnTienda;
    private Image btnFlechaAbajo;
    private Texture texJugar, texTienda, texVolver, texFlecha;
    private Texture blackScreen;
    private boolean iniciandoPantalla = true;
    private MenuGodMode godModeManager;
    private ButtonGroup<Button> characterButtonGroup;
    private String lastSelectedBeforeGodMode;
    private final Array<String> charIdList = new Array<>();
    private float resetTimer = 0f;
    private GadgetUI gadgetUI;

    public MenuMapScreen(Game game) {
        this.game = game;
        this.batch = new SpriteBatch();
    }

    private static class ImagenFondo extends Actor {

        private Texture textura;
        public ImagenFondo(Texture textura) {
            this.textura = textura;
            setBounds(0, 0, 800, 480);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            Color color = getColor();
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
            batch.draw(textura, getX(), getY(), getWidth(), getHeight());
            batch.setColor(Color.WHITE);
        }
    }


    @Override
    public void show() {
        stage = new Stage(new StretchViewport(800, 480));
        Gdx.input.setInputProcessor(stage);

        // Carga correcta de la Skin Global
        uiSkin = FontManager.getGlobalSkin();

        if (batch == null) batch = new SpriteBatch();

        godModeManager = new MenuGodMode(stage, uiSkin);

        grupoFondos = new Group();

        texIconBosque = new Texture(Gdx.files.internal("sprites/shared/UI_assets/ForestMatchIcon.png"));
        texIconDesierto = new Texture(Gdx.files.internal("sprites/shared/UI_assets/DesertMatchIcon.png"));
        texIconCastillo = new Texture(Gdx.files.internal("sprites/shared/UI_assets/CastilloMatchIcon.png"));

        texIconosMapas = new Texture[]{texIconBosque, texIconDesierto, texIconCastillo};

        fondoBosque = new ImagenFondo(new Texture(Gdx.files.internal("Menu/MenuMapas/fondo_bosque.png")));
        fondoDesierto = new ImagenFondo(new Texture(Gdx.files.internal("Menu/MenuMapas/fondo_desierto.png")));
        fondoCastillo = new ImagenFondo(new Texture(Gdx.files.internal("Menu/MenuMapas/fondo_castillo.png")));

        fondoBosque.setPosition(0, 0);
        fondoDesierto.setPosition(0, -480);
        fondoCastillo.setPosition(0, -960);

        grupoFondos.addActor(fondoBosque);
        grupoFondos.addActor(fondoDesierto);
        grupoFondos.addActor(fondoCastillo);

        stage.addActor(grupoFondos);

        texJugar = new Texture(Gdx.files.internal("Menu/ButtonPlay.png"));
        texTienda = new Texture(Gdx.files.internal("Menu/MenuMapas/buttonTienda.png"));
        texVolver = new Texture(Gdx.files.internal("Menu/MenuMapas/buttonVolver.png"));
        texFlecha = new Texture(Gdx.files.internal("Menu/MenuMapas/flecha_down.png"));

        menuSalienteTex = new Texture(Gdx.files.internal("Menu/MenuSaliente.png"));

        NinePatch patch = new NinePatch(menuSalienteTex, 16, 16, 16, 16);
        panelBackground = new NinePatchDrawable(patch);

        if (blackScreen == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.BLACK);
            pixmap.fill();

            blackScreen = new Texture(pixmap);
            pixmap.dispose();
        }

        crearInterfaz();

        iniciandoPantalla = true;
        final Image telonInmediato = new Image(blackScreen);

        telonInmediato.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        telonInmediato.setTouchable(Touchable.disabled);
        telonInmediato.getColor().a = 1f;

        stage.addActor(telonInmediato);

        telonInmediato.addAction(Actions.sequence(

            Actions.delay(0.1f),
            Actions.run(() -> iniciandoPantalla = false),
            Actions.fadeOut(0.5f),
            Actions.removeActor()
        ));

        ejecutarFading(true, null);
    }

    private void crearInterfaz() {

        TextButton.TextButtonStyle styleJugar = new TextButton.TextButtonStyle(null, null, null, uiSkin.getFont("default-font"));
        styleJugar.up = new TextureRegionDrawable(new TextureRegion(texJugar));

        TextButton.TextButtonStyle styleTienda = new TextButton.TextButtonStyle(null, null, null, uiSkin.getFont("default-font"));
        styleTienda.up = new TextureRegionDrawable(new TextureRegion(texTienda));

        TextButton.TextButtonStyle styleVolver = new TextButton.TextButtonStyle(null, null, null, uiSkin.getFont("default-font"));
        styleVolver.up = new TextureRegionDrawable(new TextureRegion(texVolver));

        ventanaIzquierda = new Table();
        ventanaIzquierda.setBackground(panelBackground);
        ventanaIzquierda.top().left().pad(20);


        Table tituloTable = new Table();
        labelTituloMapa = new Label(nombresMapas[0], uiSkin, "font-21");
        labelTituloMapa.setColor(Color.RED);
        iconMapa = new Image(texIconBosque);

        tituloTable.add(labelTituloMapa).left();
        tituloTable.add(iconMapa).size(45, 50).padLeft(15);

        ventanaIzquierda.add(tituloTable).left().row();


        labelDesc = new Label(descripcionesMapas[0], uiSkin);
        labelDesc.setWrap(true);

        ventanaIzquierda.add(labelDesc).width(260).padTop(10).left().row();


        Table charTable = new Table();
        characterButtonGroup = new ButtonGroup<>();
        JsonValue characterData = new JsonReader().parse(Gdx.files.internal("data/player_config.json"));
        int charIndex = 1;

        for (JsonValue charEntry : characterData.get("characters")) {

            final String id = charEntry.getString("id");

            if (id.equals("TikiBot")) { charIndex++; continue; }

            charIdList.add(id);
            final boolean isUnlocked = SaveManager.isCharacterUnlocked(charIndex);
            Animation<TextureRegion> idleAnim = CharacterFactory.getCharacterIdleAnimation(id);

            final Button btn = new Button(uiSkin);

            if (!isUnlocked) {
                Image staticImage = new Image(idleAnim.getKeyFrame(0f));
                staticImage.setColor(Color.BLACK);
                btn.add(staticImage).size(40, 40);
            } else {
                btn.add(new CharacterPreviewActor(idleAnim)).size(40, 40);
            }

            btn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (GameSession.godMode) {
                        godModeManager.vibrateCheckbox();
                        return;
                    }
                    if (!isUnlocked) return;

                    MenuCharacter modal = new MenuCharacter("", uiSkin, id, idleAnim, () -> {
                        btn.setChecked(true);
                        actualizarColoresPersonajes(characterButtonGroup);
                        GameSession.selectedCharacterId = id;
                    });
                    modal.setPosition((stage.getWidth() - modal.getWidth()) / 2, (stage.getHeight() - modal.getHeight()) / 2);

                    stage.addActor(modal);
                }

            });

            characterButtonGroup.add(btn);
            charTable.add(btn).size(50, 50).pad(2);
            charIndex++;
        }
        ventanaIzquierda.add(charTable).padTop(20).left().row();

        gadgetUI = new GadgetUI(stage, uiSkin);
        Button btnGadget = gadgetUI.getButton();
        ventanaIzquierda.add(btnGadget).size(50, 50).padTop(10).left().row();

        Table tableGod = new Table();
        godModeManager.inyectarInterfaz(tableGod, () -> onGodModeToggle());
        ventanaIzquierda.add(tableGod).left().padTop(10);

        ventanaIzquierda.pack();

        float alturaFija = 450;

        ventanaIzquierda.setHeight(alturaFija);
        ventanaIzquierda.top();
        ventanaIzquierda.setPosition(15, 15);

        stage.addActor(ventanaIzquierda);

        ventanaDerecha = new Table();
        ventanaDerecha.setBackground(panelBackground);

        ventanaDerecha.right().pad(20);

        btnVolver = new TextButton("", styleVolver);
        btnTienda = new TextButton("", styleTienda);
        btnJugar = new TextButton("", styleJugar);

        ventanaDerecha.add(btnVolver).size(50, 50).expandX().align(Align.topRight);
        ventanaDerecha.row();

        ventanaDerecha.add(btnTienda).size(85, 85).expand().align(Align.right);
        ventanaDerecha.row();

        ventanaDerecha.add(btnJugar).size(160, 85).expandX().align(Align.bottomRight);
        ventanaDerecha.pack();

        ventanaDerecha.setWidth(200);
        ventanaDerecha.setHeight(450);
        ventanaDerecha.setPosition(800 - ventanaDerecha.getWidth() - 15, 15);

        stage.addActor(ventanaDerecha);

        btnFlechaAbajo = new Image(texFlecha);
        btnFlechaAbajo.setSize(50, 30);
        btnFlechaAbajo.setPosition((800 - 50) / 2f, 10);
        btnFlechaAbajo.setOrigin(Align.center);

        stage.addActor(btnFlechaAbajo);

        btnFlechaAbajo.addAction(Actions.forever(Actions.sequence(
            Actions.moveBy(0, 8, 0.6f, Interpolation.sine),
            Actions.moveBy(0, -8, 0.6f, Interpolation.sine)
        )));


        btnFlechaAbajo.addListener(new Assets.HoverCursorListener());
        btnFlechaAbajo.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                cambiarSiguienteMapa();
            }
        });

        configurarListenerBoton(btnJugar, () -> {
            btnJugar.setDisabled(true);
            ejecutarFading(false, () -> game.setScreen(new GameScreen(game)));
        });

        configurarListenerBoton(btnVolver, () -> {
            btnVolver.setDisabled(true);
            ejecutarFading(false, () -> game.setScreen(new MenuScreen(game)));
        });

        final Runnable actualizarTiendaCallback = gadgetUI::updateEquippedGadgetIcon;
        configurarListenerBoton(btnTienda, () -> {

            ShopScreen shop = new ShopScreen(uiSkin, actualizarTiendaCallback);
            shop.setPosition((800 - shop.getWidth()) / 2, (480 - shop.getHeight()) / 2);
            stage.addActor(shop);
        });
        if (GameSession.godMode) {
            lastSelectedBeforeGodMode = GameSession.selectedCharacterId;
            uncheckAllCharacters();
        }
        actualizarColoresPersonajes(characterButtonGroup);
        actualizarInterfazMapa(0);
    }


    private void configurarListenerBoton(final Button btn, final Runnable accion) {

        btn.clearListeners();

        btn.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && !btn.isDisabled()) {
                    btn.clearActions();
                    btn.addAction(Actions.parallel(

                        Actions.scaleTo(1.05f, 1.05f, 0.1f, Interpolation.sineOut),
                        Actions.color(new Color(0.8f, 0.8f, 0.8f, 1f), 0.1f)
                    ));
                }
                super.enter(event, x, y, pointer, fromActor);
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1 && !btn.isDisabled()) {

                    btn.clearActions();
                    btn.addAction(Actions.parallel(
                        Actions.scaleTo(1f, 1f, 0.1f, Interpolation.sineIn),
                        Actions.color(Color.WHITE, 0.1f)
                    ));
                }
                super.exit(event, x, y, pointer, toActor);
            }
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (!btn.isDisabled()) {
                    btn.clearActions();
                    btn.addAction(Actions.parallel(
                        Actions.scaleTo(0.9f, 0.9f, 0.05f, Interpolation.sineOut),
                        Actions.color(new Color(0.5f, 0.5f, 0.5f, 1f), 0.05f)
                    ));
                }
                return super.touchDown(event, x, y, pointer, button);
            }


            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (!btn.isDisabled()) {
                    btn.clearActions();
                    if (isOver()) {
                        btn.addAction(Actions.parallel(
                            Actions.scaleTo(1.05f, 1.05f, 0.1f, Interpolation.sineIn),
                            Actions.color(new Color(0.8f, 0.8f, 0.8f, 1f), 0.1f)
                        ));

                    } else {
                        btn.addAction(Actions.parallel(
                            Actions.scaleTo(1f, 1f, 0.1f, Interpolation.sineIn),
                            Actions.color(Color.WHITE, 0.1f)
                        ));
                    }
                }
                super.touchUp(event, x, y, pointer, button);
            }


            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!btn.isDisabled()) accion.run();
            }
        });
    }


    private void cambiarSiguienteMapa() {

        if (grupoFondos.getActions().size > 0) {
            grupoFondos.clearActions();
            getFondo(mapaActualIndex).getColor().a = 1f;
            grupoFondos.setY(mapaActualIndex * 480);
        }

        int anteriorIndex = mapaActualIndex;
        mapaActualIndex = (mapaActualIndex + 1) % 3;
        float duracion = 0.3f;

        getFondo(anteriorIndex).getColor().a = 1f;
        getFondo(mapaActualIndex).getColor().a = 0f;

        getFondo(anteriorIndex).addAction(Actions.fadeOut(duracion, Interpolation.sineOut));
        getFondo(mapaActualIndex).addAction(Actions.fadeIn(duracion, Interpolation.sineIn));

        if (anteriorIndex == 2 && mapaActualIndex == 0) {
            fondoBosque.setPosition(0, -1440);
            grupoFondos.addAction(Actions.sequence(
                Actions.moveTo(0, 1440, duracion, Interpolation.pow3Out),
                Actions.run(() -> {
                    grupoFondos.setY(0);
                    fondoBosque.setPosition(0, 0);
                    fondoDesierto.setPosition(0, -480);
                    fondoCastillo.setPosition(0, -960);
                    fondoBosque.getColor().a = 1f;
                })
            ));
        } else {
            float destinoY = mapaActualIndex * 480;
            grupoFondos.addAction(Actions.moveTo(0, destinoY, duracion, Interpolation.pow3Out));
        }

        labelTituloMapa.setText(nombresMapas[mapaActualIndex]);
        actualizarInterfazMapa(mapaActualIndex);

        btnFlechaAbajo.addAction(Actions.sequence(
            Actions.scaleTo(1.2f, 0.8f, 0.05f),
            Actions.scaleTo(1f, 1f, 0.1f)
        ));
    }

    private ImagenFondo getFondo(int index) {
        switch (index) {
            case 0:
                return fondoBosque;
            case 1:
                return fondoDesierto;
            case 2:
                return fondoCastillo;
            default:
                return fondoBosque;
        }
    }

    private void ejecutarFading(boolean entrar, final Runnable accionAlTerminar) {
        final Image fadeOverlay = new Image(blackScreen);
        fadeOverlay.setSize(stage.getWidth(), stage.getHeight());
        fadeOverlay.setPosition(0, 0);
        fadeOverlay.getColor().a = entrar ? 1f : 0f;
        if (!entrar) fadeOverlay.setTouchable(Touchable.enabled);

        fadeOverlay.addAction(Actions.sequence(
            Actions.alpha(entrar ? 0f : 1f, 0.5f),
            Actions.run(() -> {
                if (accionAlTerminar != null) accionAlTerminar.run();
                if (entrar) fadeOverlay.remove();
            })
        ));
        stage.addActor(fadeOverlay);
    }

    private void actualizarInterfazMapa(int index) {
        String clave = (index == 1) ? "desierto" : (index == 2) ? "castillo" : "bosque";
        boolean isUnlocked = SaveManager.isMapUnlocked(clave);

        labelTituloMapa.setText(isUnlocked ? nombresMapas[index] : "BLOQUEADO");
        labelDesc.setText(isUnlocked ? descripcionesMapas[index] : "Supera el nivel anterior...");

        if (iconMapa != null) {
            iconMapa.setDrawable(new TextureRegionDrawable(new TextureRegion(texIconosMapas[index])));
        }

        float anchoInicial = ventanaIzquierda.getWidth();
        float altoInicial = ventanaIzquierda.getHeight();

        ventanaIzquierda.pack();
        float anchoDestino = Math.max(ventanaIzquierda.getWidth(), 300);
        float altoDestino = Math.min(ventanaIzquierda.getHeight(), 450);

        ventanaIzquierda.setSize(anchoInicial, altoInicial);
        ventanaIzquierda.invalidate();

        float puntoFijoSuperiorY = 465;

        ventanaIzquierda.clearActions();
        ventanaIzquierda.addAction(Actions.parallel(
            Actions.sizeTo(anchoDestino, altoDestino, 0.4f, Interpolation.pow2Out),
            Actions.moveTo(15, puntoFijoSuperiorY - altoDestino, 0.4f, Interpolation.pow2Out)
        ));

        GameSession.selectedMapName = clave;
    }


    private void actualizarColoresPersonajes(ButtonGroup<Button> group) {
        int i = 1;
        for (Button b : group.getButtons()) {
            if (!SaveManager.isCharacterUnlocked(i)) b.setColor(Color.DARK_GRAY);
            else b.setColor(b.isChecked() ? Color.WHITE : Color.GRAY);
            i++;
        }
    }

    private void onGodModeToggle() {
        if (GameSession.godMode) {
            lastSelectedBeforeGodMode = GameSession.selectedCharacterId;
            uncheckAllCharacters();
        } else {
            restoreLastCharacter();
        }
    }

    private void uncheckAllCharacters() {
        characterButtonGroup.setMinCheckCount(0);
        for (Button b : characterButtonGroup.getButtons()) {
            b.setChecked(false);
        }
        characterButtonGroup.setMinCheckCount(1);
        actualizarColoresPersonajes(characterButtonGroup);
    }

    private void restoreLastCharacter() {
        if (lastSelectedBeforeGodMode == null) return;
        GameSession.selectedCharacterId = lastSelectedBeforeGodMode;
        int idx = charIdList.indexOf(lastSelectedBeforeGodMode, false);
        if (idx >= 0 && idx < characterButtonGroup.getButtons().size) {
            Button b = characterButtonGroup.getButtons().get(idx);
            b.setChecked(true);
        }
        actualizarColoresPersonajes(characterButtonGroup);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        if (ventanaIzquierda.hasActions()) {
            ventanaIzquierda.invalidateHierarchy();
        }
        stage.act(delta);
        stage.draw();

        if (Gdx.input.isKeyJustPressed(Input.Keys.F11)) {
            toggleFullscreen();
        }

        if (Gdx.input.isKeyPressed(Input.Keys.R)) {
            resetTimer += delta;
            if (resetTimer >= 1.0f) {
                SaveManager.getProfileData().totalScore = 0;
                SaveManager.saveProfileData();
                game.setScreen(new MenuMapScreen(game));
            }
        } else resetTimer = 0f;
    }

    private void toggleFullscreen() {
        if (Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setWindowedMode(1280, 720);
            SaveManager.saveFullscreen(false);
            SaveManager.saveResolution(1280, 720);
        } else {
            Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            SaveManager.saveFullscreen(true);
        }
        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();
        stage.getViewport().update(w, h, true);
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    @Override
    public void hide() {
        dispose();
    }
    @Override
    public void pause() {
    }
    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        // CAMBIO PRINCIPAL: Eliminado uiSkin.dispose();
        if (batch != null) batch.dispose();
        if (fondoBosque != null) fondoBosque.textura.dispose();
        if (fondoDesierto != null) fondoDesierto.textura.dispose();
        if (fondoCastillo != null) fondoCastillo.textura.dispose();
        if (texIconBosque != null) texIconBosque.dispose();
        if (texIconDesierto != null) texIconDesierto.dispose();
        if (texIconCastillo != null) texIconCastillo.dispose();
        if (blackScreen != null) blackScreen.dispose();
        if (gadgetUI != null) gadgetUI.dispose();
        if (godModeManager != null) godModeManager.dispose();
        Texture[] texs = {texJugar, texTienda, texVolver, texFlecha};
        if (menuSalienteTex != null) menuSalienteTex.dispose();
        for (Texture t : texs) if (t != null) t.dispose();
    }
}
