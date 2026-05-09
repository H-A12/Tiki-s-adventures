package com.tikisadventure.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.tikisadventure.database.auth.AuthRepository;
import com.tikisadventure.database.core.AuthCallback;
import com.tikisadventure.core.SaveManager;
import com.tikisadventure.core.Assets;
import com.tikisadventure.ui.SettingsUI;

public class MenuScreen implements Screen {

    public AuthRepository authManager;
    private Game game;
    private Stage estirar;
    private Stage noestirar;
    private Texture buttonTexture, buttonPressedTexture;
    private Texture buttonSalirTexture, buttonSalirPressedTexture;
    private Texture buttonSettings, buttonSettingsPressed;
    private Texture particleTexture;

    private Texture background;
    private SpriteBatch batch;
    private Texture vignetteTexture;
    private Texture blackScreen;
    private com.badlogic.gdx.utils.Array<Particula> particulas;
    private float tiempoSiguienteParticula;
    private static final float TIEMPO_CREACION = 0.5f;

    private Texture menuSideTexture;
    private Image menuSideActor;
    private Table menuTable;
    private Table topRightTable; // <-- NUEVA TABLA PARA LA X

    private ImageButton playButton;
    private ImageButton salirButton;
    private ImageButton configBtn;
    private Window settingsWindow;
    private SettingsUI controlsSettings;
    private Skin uiSkin;

    private Texture texConnected;
    private Texture texDisconnected;
    private com.badlogic.gdx.graphics.g2d.TextureRegion cogRegion;
    private com.badlogic.gdx.graphics.g2d.TextureRegion xRegion;

    private ImageButton accountBtn;
    private AccountScreen accountWindow;
    private Cell<ImageButton> cellAccount;

    public boolean isConnected = false;
    public String username = "";

    private ImageButton historyBtn;
    private TextureRegion historyRegion;
    private Cell<ImageButton> cellHistory;
    private com.tikisadventure.ui.HistoryUI historyWindow;

    private ImageButton leaderboardBtn;
    private TextureRegion leaderboardRegion;
    private Cell<ImageButton> cellLeaderboard;
    private com.tikisadventure.ui.LeaderboardUI leaderboardWindow;

    private Cell<ImageButton> cellConfig, cellSalir, cellPlay;
    private boolean iniciandoPantalla = true;
    private float escalaProporcional = 1f;

    public MenuScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        estirar = new Stage(new StretchViewport(800, 480));
        noestirar = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(noestirar);
        batch = new SpriteBatch();

        background = new Texture(Gdx.files.internal("Menu/fondo_menu.png"));
        menuSideTexture = new Texture(Gdx.files.internal("Menu/MenuSaliente.png"));
        buttonTexture = new Texture(Gdx.files.internal("Menu/ButtonPlay.png"));
        buttonPressedTexture = new Texture(Gdx.files.internal("Menu/ButtonPlayPressed.png"));
        buttonSalirTexture = new Texture(Gdx.files.internal("Menu/ButtonSalir.png"));
        buttonSalirPressedTexture = new Texture(Gdx.files.internal("Menu/ButtonSalirPressed.png"));
        buttonSettings = new Texture(Gdx.files.internal("Menu/settings.png"));
        buttonSettingsPressed = new Texture(Gdx.files.internal("Menu/settingsPressed.png"));
        vignetteTexture = new Texture(Gdx.files.internal("Menu/Filtro.png"));
        particleTexture = new Texture(Gdx.files.internal("Menu/particula.png"));

        texConnected = new Texture(Gdx.files.internal("Menu/Connected.png"));
        texDisconnected = new Texture(Gdx.files.internal("Menu/Disconnected.png"));

        cogRegion = Assets.getRegion("shared", "UI_assets/UI_Cog");
        xRegion = Assets.getRegion("shared", "UI_assets/UI_X");
        historyRegion = Assets.getRegion("shared", "UI_assets/History");

        // --- SEGURO ANTI-CRASHES ---
        leaderboardRegion = Assets.getRegion("shared", "UI_assets/Leaderboard");
        if (leaderboardRegion == null) {
            System.err.println("¡ATENCIÓN! No se ha encontrado la textura 'UI_assets/Leaderboard' en el atlas.");
            leaderboardRegion = historyRegion; // Evita el NullPointerException si falta la imagen
        }

        particulas = new com.badlogic.gdx.utils.Array<>();
        tiempoSiguienteParticula = TIEMPO_CREACION;

        crearInterfaz();
        crearVentanaAjustes();
        settingsWindow.setVisible(false);

        accountWindow = new AccountScreen(uiSkin, this);
        accountWindow.setVisible(false);
        noestirar.addActor(accountWindow);

        // ESTADOS INICIALES ANIMACIÓN
        float anchoEstimado = menuSideTexture.getWidth();
        menuSideActor.setPosition(-anchoEstimado, 0);
        menuTable.setPosition(-anchoEstimado, 0);
        menuSideActor.getColor().a = 0;
        menuTable.getColor().a = 0;
        if(topRightTable != null) topRightTable.getColor().a = 0; // Ocultamos la X inicialmente

        float delayAparicion = 0.6f;
        float tiempoAnimacion = 0.5f;

        menuSideActor.addAction(Actions.delay(delayAparicion, Actions.parallel(
            Actions.fadeIn(tiempoAnimacion),
            Actions.moveTo(0, 0, tiempoAnimacion, com.badlogic.gdx.math.Interpolation.fade)
        )));

        menuTable.addAction(Actions.delay(delayAparicion, Actions.parallel(
            Actions.fadeIn(tiempoAnimacion),
            Actions.moveTo(0, 0, tiempoAnimacion, com.badlogic.gdx.math.Interpolation.fade)
        )));

        // Animación de aparición para la X
        if(topRightTable != null) {
            topRightTable.addAction(Actions.delay(delayAparicion, Actions.fadeIn(tiempoAnimacion)));
        }

        // TELÓN NEGRO
        if (blackScreen == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.BLACK);
            pixmap.fill();
            blackScreen = new Texture(pixmap);
            pixmap.dispose();
        }

        iniciandoPantalla = true;

        final Image telonInmediato = new Image(blackScreen);
        telonInmediato.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        telonInmediato.setTouchable(Touchable.disabled);
        noestirar.addActor(telonInmediato);

        telonInmediato.addAction(Actions.sequence(
            Actions.delay(0.1f),
            Actions.run(() -> iniciandoPantalla = false),
            Actions.fadeOut(0.5f),
            Actions.removeActor()
        ));

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // AUTOLOGIN
        authManager = new AuthRepository();
        String savedUser = SaveManager.getLastUsername();
        String savedPass = SaveManager.getLastPassword();

        if (!savedUser.isEmpty() && !savedPass.isEmpty()) {
            authManager.iniciarSesion(savedUser, savedPass, new AuthCallback() {
                @Override
                public void onSuccess(String loginMessage) {
                    String[] datosNube = loginMessage.split("\\|\\|\\|", -1);
                    long playerId = Long.parseLong(datosNube[0]);
                    int cloudCoins = Integer.parseInt(datosNube[1]);
                    int cloudScore = Integer.parseInt(datosNube[2]);
                    boolean moko = Boolean.parseBoolean(datosNube[3]);
                    boolean zuki = Boolean.parseBoolean(datosNube[4]);

                    com.badlogic.gdx.utils.Array<String> armasNubeArray = new com.badlogic.gdx.utils.Array<>();
                    if (datosNube.length > 5 && !datosNube[5].isEmpty()) {
                        String[] armasList = datosNube[5].split("#");
                        for (String armaStr : armasList) {
                            armasNubeArray.add(armaStr);
                        }
                    }

                    boolean mapDesert = Boolean.parseBoolean(datosNube[6]);
                    boolean mapCave = Boolean.parseBoolean(datosNube[7]);

                    com.badlogic.gdx.utils.Array<String> gadgetsNubeArray = new com.badlogic.gdx.utils.Array<>();
                    if (datosNube.length > 8 && !datosNube[8].isEmpty()) {
                        String[] gadgetsList = datosNube[8].split("#");
                        for (String gStr : gadgetsList) {
                            gadgetsNubeArray.add(gStr);
                        }
                    }

                    String armasCustomJson = datosNube.length > 9 ? datosNube[9] : "{}";
                    if (armasCustomJson == null || armasCustomJson.equals("null") || armasCustomJson.trim().isEmpty()) {
                        armasCustomJson = "{}";
                    }

                    try {
                        @SuppressWarnings("unchecked")
                        com.badlogic.gdx.utils.Json jsonTool = new com.badlogic.gdx.utils.Json();
                        @SuppressWarnings("unchecked")
                        com.badlogic.gdx.utils.ObjectMap<String, com.tikisadventure.core.GameSession.CustomWeaponConfig> mapNube =
                            (com.badlogic.gdx.utils.ObjectMap<String, com.tikisadventure.core.GameSession.CustomWeaponConfig>)
                                jsonTool.fromJson(com.badlogic.gdx.utils.ObjectMap.class, com.tikisadventure.core.GameSession.CustomWeaponConfig.class, armasCustomJson);

                        if (mapNube != null) {
                            com.tikisadventure.core.GameSession.customWeapons = mapNube;
                        } else {
                            com.tikisadventure.core.GameSession.customWeapons.clear();
                        }
                        com.tikisadventure.core.GameSession.saveCustomWeapons();
                    } catch (Exception e) {
                        System.out.println("Error parseando armas custom desde la nube: " + e.getMessage());
                        com.tikisadventure.core.GameSession.customWeapons.clear();
                    }

                    isConnected = true;
                    username = savedUser;

                    SaveManager.aplicarDatosNube(playerId, cloudCoins, cloudScore, moko, zuki);
                    SaveManager.aplicarArmasNube(armasNubeArray);
                    SaveManager.aplicarMapasNube(mapDesert, mapCave);
                    SaveManager.aplicarGadgetsNube(gadgetsNubeArray);

                    actualizarSpriteCuenta();

                    if (accountWindow != null) accountWindow.actualizarInterfaz();
                    System.out.println("Autologin exitoso para: " + username + " con ID: " + playerId);
                }

                @Override
                public void onError(String errorMessage) {
                    System.out.println("Autologin fallido: " + errorMessage);
                }
            });
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        for (Particula p : particulas) {
            p.actualizar(delta);
            p.dibujar(batch, particleTexture);
        }

        if (iniciandoPantalla) {
            batch.draw(blackScreen, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        batch.end();

        noestirar.getViewport().apply();
        noestirar.act(delta);
        noestirar.draw();
    }

    @Override
    public void resize(int width, int height) {
        noestirar.getViewport().update(width, height, true);
        float w = noestirar.getViewport().getWorldWidth();
        float h = noestirar.getViewport().getWorldHeight();
        escalaProporcional = w / 800f;

        if (menuSideActor != null) {
            menuSideActor.setSize(w * 0.35f, h);
            if (menuSideActor.getActions().size == 0) menuSideActor.setPosition(0, 0);
        }

        if (menuTable != null && cellPlay != null) {
            menuTable.setSize(menuSideActor.getWidth(), h);

            // Tamaños proporcionales
            float nuevoSizeIcono = 50f * escalaProporcional;
            float nuevoAnchoPlay = 240f * escalaProporcional;
            float nuevoAltoPlay = 100f * escalaProporcional;

            // --- MÁRGENES PROPORCIONALES ---
            float padSuperiorIconos = 10f * escalaProporcional;
            float padPrimeraIzquierda = 20f * escalaProporcional;
            float padEntreIconos = 15f * escalaProporcional;
            float padSuperiorPlay = 260f * escalaProporcional + 30f;

            // Tamaños Base
            cellConfig.size(nuevoSizeIcono);
            cellAccount.size(nuevoSizeIcono);
            cellPlay.size(nuevoAnchoPlay, nuevoAltoPlay);

            // Aplicamos márgenes
            cellConfig.padTop(padSuperiorIconos).padLeft(padPrimeraIzquierda);
            cellAccount.padTop(padSuperiorIconos).padLeft(padEntreIconos);

            if (cellHistory != null) {
                cellHistory.size(nuevoSizeIcono);
                cellHistory.padTop(padSuperiorIconos).padLeft(padEntreIconos);
                historyBtn.setOrigin(nuevoSizeIcono / 2, nuevoSizeIcono / 2);
            }

            if (cellLeaderboard != null) {
                cellLeaderboard.size(nuevoSizeIcono);
                cellLeaderboard.padTop(padSuperiorIconos).padLeft(padEntreIconos);
                leaderboardBtn.setOrigin(nuevoSizeIcono / 2, nuevoSizeIcono / 2);
            }

            // Margen para la X (panel derecho)
            if (cellSalir != null) {
                cellSalir.size(nuevoSizeIcono);
                cellSalir.padTop(padSuperiorIconos).padRight(30f * escalaProporcional);
                salirButton.setOrigin(nuevoSizeIcono / 2, nuevoSizeIcono / 2);
            }

            cellPlay.padTop(padSuperiorPlay);

            configBtn.setOrigin(nuevoSizeIcono / 2, nuevoSizeIcono / 2);
            playButton.setOrigin(nuevoAnchoPlay / 2, nuevoAltoPlay / 2);
            accountBtn.setOrigin(nuevoSizeIcono / 2, nuevoSizeIcono / 2);

            menuTable.invalidateHierarchy();
            if (topRightTable != null) topRightTable.invalidateHierarchy();

            if (menuTable.getActions().size == 0) menuTable.setPosition(0, 0);

            if (settingsWindow != null) {
                settingsWindow.setTransform(true);
                settingsWindow.setOrigin(com.badlogic.gdx.utils.Align.topLeft);
                settingsWindow.setScale(escalaProporcional * 0.6f);

                if (settingsWindow.isVisible()) {
                    posicionarVentanaAjustes();
                }
            }

            if (controlsSettings != null && controlsSettings.isVisible()) {
                controlsSettings.setPosition(w / 2f - controlsSettings.getWidth() / 2f,
                    h / 2f - controlsSettings.getHeight() / 2f);
            }
        }
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void dispose() {
        estirar.dispose();
        noestirar.dispose();
        batch.dispose();
        buttonTexture.dispose();
        buttonPressedTexture.dispose();
        buttonSalirTexture.dispose();
        buttonSalirPressedTexture.dispose();
        buttonSettings.dispose();
        background.dispose();
        if (blackScreen != null) blackScreen.dispose();
        if (vignetteTexture != null) vignetteTexture.dispose();
        if (texConnected != null) texConnected.dispose();
        if (texDisconnected != null) texDisconnected.dispose();
    }

    private void mostrarConfirmacionSalir() {
        BitmapFont font = new BitmapFont();
        font.getData().setScale(1.2f);

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0.8f);
        pixmap.fill();
        TextureRegionDrawable fondoNegro = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        pixmap.dispose();

        Texture iconoTex = new Texture(Gdx.files.internal("Menu/icono_alerta.png"));
        com.badlogic.gdx.scenes.scene2d.ui.Image imagenCentral = new com.badlogic.gdx.scenes.scene2d.ui.Image(iconoTex);

        Window.WindowStyle windowStyle = new Window.WindowStyle();
        windowStyle.titleFont = font;
        windowStyle.background = fondoNegro;

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.up = fondoNegro;

        Dialog dialog = new Dialog("", windowStyle) {
            @Override
            protected void result(Object object) {
                if (object instanceof Boolean && (boolean) object) {
                    Gdx.app.exit();
                }
            }
        };

        dialog.text("¿Seguro que quieres salir?", labelStyle);
        dialog.getContentTable().row();
        dialog.getContentTable().add(imagenCentral).size(80, 80).pad(20);
        dialog.getContentTable().row();
        dialog.button(" SÍ ", true, buttonStyle);
        dialog.button(" NO ", false, buttonStyle);

        dialog.pad(40);
        dialog.show(noestirar);
    }

    private void crearVentanaAjustes() {
        uiSkin = new Skin(Gdx.files.internal("uiskin.json"));
        settingsWindow = new Window("", uiSkin);
        settingsWindow.setMovable(false);
        settingsWindow.setModal(false);
        settingsWindow.pad(45, 40, 35, 40);

        Image bgImage = new Image(new Texture(Gdx.files.internal("Menu/VentanaConfiguracion.png")));
        settingsWindow.setBackground(bgImage.getDrawable());

        TextButton.TextButtonStyle txtBtnStyle = new TextButton.TextButtonStyle();
        txtBtnStyle.up = new TextureRegionDrawable(new TextureRegion(new Texture(Gdx.files.internal("Menu/BotonText.png"))));
        txtBtnStyle.font = uiSkin.getFont("default-font");

        TextButton btnEsp = new TextButton("ESP", txtBtnStyle);
        TextButton btnEng = new TextButton("ENG", txtBtnStyle);
        final Slider volumeSlider = new Slider(0, 1, 0.1f, false, uiSkin);
        volumeSlider.setValue(0.5f);

        SelectBox<String> resSelector = new SelectBox<>(uiSkin);
        resSelector.setItems("800x480", "1280x720", "1920x1080");
        resSelector.setSelectedIndex(1);

        settingsWindow.defaults().pad(5).space(8);
        settingsWindow.add("Idioma:").left();
        settingsWindow.add(btnEsp).size(55, 30);
        settingsWindow.add(btnEng).size(55, 30);
        settingsWindow.row();

        settingsWindow.add("Volumen:").left();
        settingsWindow.add(volumeSlider).colspan(2).fillX();
        settingsWindow.row();

        settingsWindow.add("Pantalla:").left();
        settingsWindow.add(resSelector).colspan(2).fillX();
        settingsWindow.row();

        TextButton btnControles = new TextButton("Controles", txtBtnStyle);
        btnControles.pad(6f, 14f, 6f, 14f);
        TextButton btnCerrar = new TextButton("Cerrar", txtBtnStyle);
        btnCerrar.pad(6f, 14f, 6f, 14f);
        Table btnRow = new Table();
        btnRow.add(btnCerrar).uniform().fillX();
        btnRow.add(btnControles).uniform().fillX().spaceLeft(14);
        settingsWindow.add(btnRow).colspan(3).fillX().padTop(12);

        settingsWindow.pack();
        settingsWindow.setVisible(false);
        noestirar.addActor(settingsWindow);

        controlsSettings = new SettingsUI(uiSkin, new Runnable() {
            @Override
            public void run() {
                settingsWindow.setVisible(true);
                settingsWindow.setTransform(true);
                settingsWindow.setOrigin(com.badlogic.gdx.utils.Align.topLeft);
                settingsWindow.setScale(escalaProporcional * 0.6f);
                posicionarVentanaAjustes();
                settingsWindow.clearActions();
                settingsWindow.getColor().a = 0;
                settingsWindow.addAction(Actions.fadeIn(0.2f));
            }
        });
        controlsSettings.setVisible(false);
        noestirar.addActor(controlsSettings);

        btnControles.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settingsWindow.addAction(Actions.sequence(
                    Actions.fadeOut(0.15f),
                    Actions.visible(false),
                    Actions.run(() -> {
                        controlsSettings.setVisible(true);
                        controlsSettings.setPosition(Gdx.graphics.getWidth() / 2f - controlsSettings.getWidth() / 2f,
                            Gdx.graphics.getHeight() / 2f - controlsSettings.getHeight() / 2f);
                        controlsSettings.clearActions();
                        controlsSettings.getColor().a = 0;
                        controlsSettings.addAction(Actions.fadeIn(0.2f));
                    })
                ));
            }
        });

        btnCerrar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settingsWindow.addAction(Actions.sequence(
                    Actions.fadeOut(0.2f),
                    Actions.visible(false)
                ));
            }
        });

        resSelector.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String seleccion = resSelector.getSelected();
                String[] partes = seleccion.split("x");
                int nuevoAncho = Integer.parseInt(partes[0]);
                int nuevoAlto = Integer.parseInt(partes[1]);

                SaveManager.saveResolution(nuevoAncho, nuevoAlto);
                Gdx.graphics.setWindowedMode(nuevoAncho, nuevoAlto);
                noestirar.getViewport().update(nuevoAncho, nuevoAlto, true);
            }
        });
    }

    private void posicionarVentanaAjustes() {
        if (settingsWindow == null || configBtn == null) return;
        menuTable.validate();
        com.badlogic.gdx.math.Vector2 coords = new com.badlogic.gdx.math.Vector2(0, 0);
        configBtn.localToStageCoordinates(coords);
        settingsWindow.setPosition(coords.x, coords.y - 10, com.badlogic.gdx.utils.Align.topLeft);
    }

    private class Particula {
        float x, y;
        float velocidadY;
        float vida;
        float velocidadVida;
        float tamaño;

        public Particula() {
            x = MathUtils.random(0, Gdx.graphics.getWidth());
            y = MathUtils.random(0, Gdx.graphics.getHeight() / 2f);
            velocidadY = MathUtils.random(20, 100);
            vida = 1.0f;
            velocidadVida = MathUtils.random(0.2f, 0.5f);
            tamaño = MathUtils.random(2, 4);
        }

        public void actualizar(float delta) {
            y += velocidadY * delta;
            vida -= velocidadVida * delta;
        }

        public boolean estaMuerta() {
            return vida <= 0 || y > Gdx.graphics.getHeight();
        }

        public void dibujar(SpriteBatch batch, Texture textura) {
            batch.setColor(1, 1, 1, vida);
            batch.draw(textura, x, y, tamaño, tamaño);
            batch.setColor(Color.WHITE);
        }
    }

    private void crearInterfaz() {
        menuSideActor = new Image(menuSideTexture);
        menuSideActor.setColor(1, 1, 1, 0.7f);

        ImageButton.ImageButtonStyle stylePlay = new ImageButton.ImageButtonStyle();
        stylePlay.imageUp = new TextureRegionDrawable(new TextureRegion(buttonTexture));
        stylePlay.imageDown = new TextureRegionDrawable(new TextureRegion(buttonPressedTexture));

        ImageButton.ImageButtonStyle styleConfig = new ImageButton.ImageButtonStyle();
        styleConfig.imageUp = new TextureRegionDrawable(cogRegion);
        styleConfig.imageDown = new TextureRegionDrawable(cogRegion);

        ImageButton.ImageButtonStyle styleSalir = new ImageButton.ImageButtonStyle();
        styleSalir.imageUp = new TextureRegionDrawable(xRegion);
        styleSalir.imageDown = new TextureRegionDrawable(xRegion);

        ImageButton.ImageButtonStyle styleAccount = new ImageButton.ImageButtonStyle();
        styleAccount.imageUp = new TextureRegionDrawable(new TextureRegion(texDisconnected));

        ImageButton.ImageButtonStyle styleHistory = new ImageButton.ImageButtonStyle();
        styleHistory.imageUp = new TextureRegionDrawable(historyRegion);
        styleHistory.imageDown = new TextureRegionDrawable(historyRegion);

        ImageButton.ImageButtonStyle styleLeaderboard = new ImageButton.ImageButtonStyle();
        styleLeaderboard.imageUp = new TextureRegionDrawable(leaderboardRegion);
        styleLeaderboard.imageDown = new TextureRegionDrawable(leaderboardRegion);

        playButton = new ImageButton(stylePlay);
        configBtn = new ImageButton(styleConfig);
        salirButton = new ImageButton(styleSalir);
        accountBtn = new ImageButton(styleAccount);
        historyBtn = new ImageButton(styleHistory);
        leaderboardBtn = new ImageButton(styleLeaderboard);

        configurarBoton(playButton, "play");
        configurarBoton(configBtn, "config");
        configurarBoton(salirButton, "salir");
        configurarBoton(accountBtn, "account");
        configurarBoton(historyBtn, "history");
        configurarBoton(leaderboardBtn, "leaderboard");

        playButton.getImageCell().expand().fill();
        configBtn.getImageCell().expand().fill();
        salirButton.getImageCell().expand().fill();
        accountBtn.getImageCell().expand().fill();
        historyBtn.getImageCell().expand().fill();
        leaderboardBtn.getImageCell().expand().fill();

        actualizarSpriteCuenta();

        // --- PANEL IZQUIERDO (4 iconos + botón Jugar) ---
        menuTable = new Table();
        menuTable.left().top();

        cellConfig = menuTable.add(configBtn).padTop(30).padLeft(30).left();
        cellAccount = menuTable.add(accountBtn).padTop(30).padLeft(20).left();
        cellHistory = menuTable.add(historyBtn).padTop(30).padLeft(20).left();
        cellLeaderboard = menuTable.add(leaderboardBtn).padTop(30).padLeft(20).left();

        menuTable.row();
        cellPlay = menuTable.add(playButton).colspan(4).padTop(200).center(); // Colspan bajado a 4

        noestirar.addActor(menuSideActor);
        noestirar.addActor(menuTable);
        menuTable.pack();

        // --- PANEL DERECHO (Solo para la X) ---
        topRightTable = new Table();
        topRightTable.setFillParent(true);
        topRightTable.top().right();

        cellSalir = topRightTable.add(salirButton).padTop(30).padRight(30);
        noestirar.addActor(topRightTable);
    }

    private void configurarBoton(final ImageButton btn, final String tipo) {
        btn.setTransform(true);
        btn.setOrigin(btn.getWidth() / 2f, btn.getHeight() / 2f);

        btn.addListener(new Assets.HoverCursorListener());

        btn.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                btn.setOrigin(btn.getWidth() / 2f, btn.getHeight() / 2f);
                btn.clearActions();
                btn.addAction(Actions.scaleTo(0.9f, 0.9f, 0.1f));
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                btn.clearActions();
                btn.addAction(Actions.scaleTo(1f, 1f, 0.1f));
                super.touchUp(event, x, y, pointer, button);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                switch (tipo) {
                    case "play":
                        btn.setDisabled(true);
                        ejecutarFading(false, new Runnable() {
                            @Override
                            public void run() {
                                game.setScreen(new MenuMapScreen(game));
                            }
                        });
                        break;

                    case "config":
                        if (!settingsWindow.isVisible()) {
                            settingsWindow.setVisible(true);
                            settingsWindow.setTransform(true);
                            settingsWindow.setOrigin(com.badlogic.gdx.utils.Align.topLeft);
                            settingsWindow.setScale(escalaProporcional * 0.6f);
                            posicionarVentanaAjustes();
                            settingsWindow.clearActions();
                            settingsWindow.getColor().a = 0;
                            settingsWindow.addAction(Actions.fadeIn(0.2f));
                        } else {
                            settingsWindow.addAction(Actions.sequence(
                                Actions.fadeOut(0.2f),
                                Actions.visible(false)
                            ));
                        }
                        break;

                    case "salir":
                        mostrarConfirmacionSalir();
                        break;

                    case "account":
                        if (!accountWindow.isVisible()) {
                            accountWindow.actualizarInterfaz();
                            accountWindow.setVisible(true);
                            accountWindow.setPosition(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, com.badlogic.gdx.utils.Align.center);
                            accountWindow.clearActions();
                            accountWindow.getColor().a = 0;
                            accountWindow.addAction(Actions.fadeIn(0.2f));
                        } else {
                            accountWindow.addAction(Actions.sequence(
                                Actions.fadeOut(0.2f),
                                Actions.visible(false)
                            ));
                        }
                        break;

                    case "history":
                        if (isConnected) {
                            if (historyWindow == null) {
                                historyWindow = new com.tikisadventure.ui.HistoryUI(uiSkin, noestirar, username);
                            }
                            historyWindow.show();
                        }
                        break;

                    case "leaderboard":
                        if (isConnected) {
                            if (leaderboardWindow == null) {
                                leaderboardWindow = new com.tikisadventure.ui.LeaderboardUI(uiSkin, noestirar);
                            }
                            leaderboardWindow.show();
                        }
                        break;
                }
            }
        });
    }

    private void ejecutarFading(boolean entrar, final Runnable accionAlTerminar) {
        final Image fadeOverlay = new Image(blackScreen);
        fadeOverlay.setSize(noestirar.getWidth(), noestirar.getHeight());
        fadeOverlay.setPosition(0, 0);

        if (entrar) {
            fadeOverlay.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        } else {
            fadeOverlay.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
        }

        fadeOverlay.getColor().a = entrar ? 1f : 0f;
        float alphaDestino = entrar ? 0f : 1f;

        fadeOverlay.addAction(Actions.sequence(
            Actions.alpha(alphaDestino, 0.5f),
            Actions.run(() -> {
                if (accionAlTerminar != null) accionAlTerminar.run();
                if (entrar) fadeOverlay.remove();
            })
        ));

        noestirar.addActor(fadeOverlay);
    }

    public void actualizarSpriteCuenta() {
        ImageButton.ImageButtonStyle style = accountBtn.getStyle();

        if (isConnected) {
            style.imageUp = new TextureRegionDrawable(new TextureRegion(texConnected));
        } else {
            style.imageUp = new TextureRegionDrawable(new TextureRegion(texDisconnected));
        }

        accountBtn.setStyle(style);
        accountBtn.setColor(com.badlogic.gdx.graphics.Color.WHITE);

        historyWindow = null;
        leaderboardWindow = null;

        // Ocultamos ambos botones si jugamos en local
        if (historyBtn != null) historyBtn.setVisible(isConnected);
        if (leaderboardBtn != null) leaderboardBtn.setVisible(isConnected);
    }

    public AuthRepository getAuthManager() {
        return authManager;
    }
}
