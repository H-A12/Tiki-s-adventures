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

    // Estados públicos para que AccountScreen los pueda leer/modificar
    public boolean isConnected = false;
    public String username = "";

    public MenuScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        // 1. Inicialización básica
        estirar = new Stage(new StretchViewport(800, 480));
        noestirar = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(noestirar);
        batch = new SpriteBatch();

        // 2. Carga de Assets (Hacerlo rápido antes de mostrar nada)
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

        // Carga de Texturas de Cuenta
        texConnected = new Texture(Gdx.files.internal("Menu/Connected.png"));
        texDisconnected = new Texture(Gdx.files.internal("Menu/Disconnected.png"));

        // Carga de sprites del atlas para botones
        cogRegion = Assets.getRegion("shared", "UI_assets/UI_Cog");
        xRegion = Assets.getRegion("shared", "UI_assets/UI_X");

        particulas = new com.badlogic.gdx.utils.Array<>();
        tiempoSiguienteParticula = TIEMPO_CREACION;

        // 3. Crear la interfaz (Tablas y botones)
        crearInterfaz();
        crearVentanaAjustes();
        settingsWindow.setVisible(false);

        // Debajo de crearVentanaAjustes();
        accountWindow = new AccountScreen(uiSkin, this);
        accountWindow.setVisible(false);
        noestirar.addActor(accountWindow);

        // 4. Preparar estados iniciales de animación
        float anchoEstimado = menuSideTexture.getWidth();
        menuSideActor.setPosition(-anchoEstimado, 0);
        menuTable.setPosition(-anchoEstimado, 0);
        menuSideActor.getColor().a = 0;
        menuTable.getColor().a = 0;

        // 5. Lanzar animaciones de los elementos (con delay)
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

        // 6. EL TELÓN NEGRO (EL "TRUCO" FINAL)
        if (blackScreen == null) {
            Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pixmap.setColor(Color.BLACK);
            pixmap.fill();
            blackScreen = new Texture(pixmap);
            pixmap.dispose();
        }

        iniciandoPantalla = true; // Forzamos el estado al entrar

        final Image telonInmediato = new Image(blackScreen);
        // Usamos Gdx.graphics para asegurar que cubra toda la ventana física
        telonInmediato.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        telonInmediato.setTouchable(Touchable.disabled);

        // Lo añadimos al final: esto garantiza que esté por encima de la tabla y el fondo
        noestirar.addActor(telonInmediato);

        telonInmediato.addAction(Actions.sequence(
            Actions.delay(0.1f),
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    iniciandoPantalla = false; // El Batch deja de dibujar el negro manual
                }
            }),
            Actions.fadeOut(0.5f),
            Actions.removeActor()
        ));

        // IMPORTANTE: He borrado la llamada a ejecutarFading(true, null) que tenías al final.
        // Ya no la necesitas y era la que causaba el conflicto.

        // 7. Ajustar tamaños
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // 8. AUTOLOGIN
        authManager = new AuthRepository();
        String savedUser = SaveManager.getLastUsername();
        String savedPass = SaveManager.getLastPassword();

        // Si tenemos datos guardados, intentamos loguear en segundo plano
        if (!savedUser.isEmpty() && !savedPass.isEmpty()) {
            authManager.iniciarSesion(savedUser, savedPass, new AuthCallback() {
                @Override
                public void onSuccess(String message) {
                    // --- NUEVO: Extraemos todos los datos como en AccountScreen ---
                    String[] datosNube = message.split(",", -1);
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

                    isConnected = true;
                    username = savedUser;

                    // Aplicamos el ID y forzamos bloqueos/desbloqueos
                    com.tikisadventure.core.SaveManager.aplicarDatosNube(playerId, cloudCoins, cloudScore, moko, zuki);
                    com.tikisadventure.core.SaveManager.aplicarArmasNube(armasNubeArray);
                    com.tikisadventure.core.SaveManager.aplicarMapasNube(mapDesert, mapCave);
                    com.tikisadventure.core.SaveManager.aplicarGadgetsNube(gadgetsNubeArray); // <--- APLICAR

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

    private boolean iniciandoPantalla = true; // Nueva variable de clase

    @Override
    public void render(float delta) {
        // 1. Limpieza normal
        ScreenUtils.clear(0, 0, 0, 1);

        // 2. Dibujo del fondo y partículas (SpriteBatch)
        batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        for (Particula p : particulas) {
            p.actualizar(delta);
            p.dibujar(batch, particleTexture);
        }

        // --- TRUCO ANTIPARPADEO ---
        // Si estamos iniciando, dibujamos un cuadro negro directamente sobre el Batch
        // Esto ocurre ANTES que el Stage y asegura que no haya ni un frame de parpadeo.
        if (iniciandoPantalla) {
            batch.draw(blackScreen, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
        batch.end();

        // 3. UI
        noestirar.getViewport().apply();
        noestirar.act(delta);
        noestirar.draw();
    }

    @Override
    public void resize(int width, int height) {
        noestirar.getViewport().update(width, height, true);
        float w = noestirar.getViewport().getWorldWidth();
        float h = noestirar.getViewport().getWorldHeight();
        float escalaProporcional = w / 800f;

        if (menuSideActor != null) {
            menuSideActor.setSize(w * 0.35f, h);
            if (menuSideActor.getActions().size == 0) menuSideActor.setPosition(0, 0);
        }

        if (menuTable != null && cellPlay != null) {
            menuTable.setSize(menuSideActor.getWidth(), h);

            // 1. Tamaños proporcionales
            float nuevoSizeIcono = 50f * escalaProporcional;
            float nuevoAnchoPlay = 240f * escalaProporcional;
            float nuevoAltoPlay = 100f * escalaProporcional;

            // 2. MÁRGENES PROPORCIONALES (Aquí está el truco)
            float padSuperiorIconos = 10f * escalaProporcional;
            float padLateralIconos = 10f * escalaProporcional;
            float padSuperiorPlay = 260f * escalaProporcional + 30f;

            // Aplicamos tamaños
            cellConfig.size(nuevoSizeIcono);
            cellSalir.size(nuevoSizeIcono);
            cellAccount.size(nuevoSizeIcono);
            cellPlay.size(nuevoAnchoPlay, nuevoAltoPlay);

            // Aplicamos márgenes (Esto evita que se desplacen de su sitio)
            cellConfig.padTop(padSuperiorIconos).padLeft(padLateralIconos);
            cellSalir.padTop(padSuperiorIconos).padRight(padLateralIconos);
            cellAccount.padTop(padSuperiorIconos).padLeft(15f * escalaProporcional);
            cellPlay.padTop(padSuperiorPlay);

            // 3. Reajuste de centros para animaciones
            configBtn.setOrigin(nuevoSizeIcono / 2, nuevoSizeIcono / 2);
            salirButton.setOrigin(nuevoSizeIcono / 2, nuevoSizeIcono / 2);
            playButton.setOrigin(nuevoAnchoPlay / 2, nuevoAltoPlay / 2);
            accountBtn.setOrigin(nuevoSizeIcono / 2, nuevoSizeIcono / 2);

            menuTable.invalidateHierarchy();

            if (menuTable.getActions().size == 0) menuTable.setPosition(0, 0);

            // --- AJUSTE DE VENTANA DE CONFIGURACIÓN ---
            settingsWindow.setTransform(true);
            settingsWindow.setScale(escalaProporcional - 0.1f);

            if (settingsWindow != null) {
                // En lugar de setScale, cambiamos el tamaño real (Ancho base 300)
                float nuevoAncho = 300f * escalaProporcional;
                settingsWindow.setSize(nuevoAncho, settingsWindow.getPrefHeight());

                // Importante: No escales la ventana, deja que el tamaño real haga el trabajo
                settingsWindow.setTransform(false);
                settingsWindow.setScale(1f);

                if (settingsWindow.isVisible()) {
                    posicionarVentanaAjustes();
                }
            }
        }
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

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
        // 1. Recursos básicos: Fuente y Pixmap para el fondo negro
        BitmapFont font = new BitmapFont();
        font.getData().setScale(1.2f); // Texto un poco más grande

        // Creamos el fondo negro semitransparente (0.8f de opacidad)
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0.8f);
        pixmap.fill();
        TextureRegionDrawable fondoNegro = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
        pixmap.dispose(); // Ya no lo necesitamos tras crear la textura

        // 2. Cargamos la imagen central (icono)
        // Asegúrate de tener "alerta.png" en tu carpeta assets
        Texture iconoTex = new Texture(Gdx.files.internal("Menu/icono_alerta.png"));
        com.badlogic.gdx.scenes.scene2d.ui.Image imagenCentral = new com.badlogic.gdx.scenes.scene2d.ui.Image(iconoTex);

        // 3. Definimos los Estilos
        Window.WindowStyle windowStyle = new Window.WindowStyle();
        windowStyle.titleFont = font;
        windowStyle.background = fondoNegro; // <--- AQUÍ pones el fondo negro

        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;

        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        // Opcional: darle un fondo un poco más claro a los botones para que resalten
        buttonStyle.up = fondoNegro;

        // 4. Construcción del Diálogo
        Dialog dialog = new Dialog("", windowStyle) {
            @Override
            protected void result(Object object) {
                if ((boolean) object) {
                    Gdx.app.exit();
                }
                // Al terminar, liberamos la textura del icono si no se usa más
                iconoTex.dispose();
            }
        };

        // Añadimos el texto
        dialog.text("¿Seguro que quieres salir?", labelStyle);

        // Añadimos la imagen en la fila de en medio
        dialog.getContentTable().row();
        dialog.getContentTable().add(imagenCentral).size(80, 80).pad(20);
        dialog.getContentTable().row();

        // Añadimos los botones
        dialog.button(" SÍ ", true, buttonStyle);
        dialog.button(" NO ", false, buttonStyle);

        // Ajustes finales de tamaño y aparición
        dialog.pad(40); // Espaciado interno general
        dialog.show(noestirar);
    }

    private void crearVentanaAjustes() {
        // 1. Cargamos el Skin (Asegúrate de que el archivo exista en assets)
        uiSkin = new Skin(Gdx.files.internal("uiskin.json"));

        // 2. Creamos la ventana
        settingsWindow = new Window("", uiSkin);
        settingsWindow.setMovable(false);
        settingsWindow.setModal(false);
        settingsWindow.padTop(30);

        // 3. Componentes
        TextButton btnEsp = new TextButton("ESP", uiSkin);
        TextButton btnEng = new TextButton("ENG", uiSkin);
        final Slider volumeSlider = new Slider(0, 1, 0.1f, false, uiSkin);
        volumeSlider.setValue(0.5f);

        SelectBox<String> resSelector = new SelectBox<>(uiSkin);
        resSelector.setItems("800x480", "1280x720", "1920x1080");
        resSelector.setSelectedIndex(1);

        // 4. Organización
        settingsWindow.defaults().pad(5).space(10);
        settingsWindow.add("Idioma:").left();
        settingsWindow.add(btnEsp).size(50, 30);
        settingsWindow.add(btnEng).size(50, 30);
        settingsWindow.row();

        settingsWindow.add("Volumen:").left();
        settingsWindow.add(volumeSlider).colspan(2).fillX();
        settingsWindow.row();

        settingsWindow.add("Pantalla:").left();
        settingsWindow.add(resSelector).colspan(2).fillX();
        settingsWindow.row();

        TextButton btnControles = new TextButton("Controles", uiSkin);
        settingsWindow.add(btnControles).colspan(3).padTop(15).fillX();

        // --- CORRECCIÓN 1: Tamaño y Visibilidad ---
        settingsWindow.pack(); // Calcula el tamaño basado en el contenido
        settingsWindow.setVisible(false); // La ocultamos por defecto hasta que se pulse Config

        // NO usamos setPosition aquí, se encargará el método clicked del botón config

        noestirar.addActor(settingsWindow);

        // Inicializar controlsSettings
        controlsSettings = new SettingsUI(uiSkin);
        controlsSettings.setVisible(false);
        noestirar.addActor(controlsSettings);

        // Listener para el botón de Controles
        btnControles.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                settingsWindow.setVisible(false);
                controlsSettings.setVisible(true);
                controlsSettings.setPosition(Gdx.graphics.getWidth() / 2f - controlsSettings.getWidth() / 2f,
                                              Gdx.graphics.getHeight() / 2f - controlsSettings.getHeight() / 2f);
            }
        });

        // --- CORRECCIÓN 2: Listener de resolución ---
        resSelector.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String seleccion = resSelector.getSelected();
                String[] partes = seleccion.split("x");
                int nuevoAncho = Integer.parseInt(partes[0]);
                int nuevoAlto = Integer.parseInt(partes[1]);

                // NUEVO: Guardar resolución en SaveManager
                SaveManager.saveResolution(nuevoAncho, nuevoAlto);

                Gdx.graphics.setWindowedMode(nuevoAncho, nuevoAlto);

                // Actualizamos el viewport inmediatamente
                noestirar.getViewport().update(nuevoAncho, nuevoAlto, true);

                // Forzamos a la ventana a recalcular su tamaño por si el texto del skin cambia
                settingsWindow.pack();
            }
        });
    }

    private void posicionarVentanaAjustes() {
        if (settingsWindow == null || configBtn == null) return;

        // 1. Forzamos a que la tabla y ventana calculen sus medidas actuales
        menuTable.validate();
        settingsWindow.pack();

        // 2. Obtenemos la posición del botón (esquina inferior izquierda)
        com.badlogic.gdx.math.Vector2 coords = new com.badlogic.gdx.math.Vector2(0, 0);
        configBtn.localToStageCoordinates(coords);

        // 3. Posicionamiento
        // X: Alineada con el botón
        // Y: Usamos la Y del botón (que es su base) y Align.topLeft para que la ventana
        //    empiece ahí y crezca hacia abajo.
        settingsWindow.setPosition(coords.x, coords.y - 10, com.badlogic.gdx.utils.Align.topLeft);
    }

    private class Particula {
        float x, y;
        float velocidadY;
        float vida; // De 1.0 (nueva) a 0.0 (desaparecida)
        float velocidadVida; // Qué tan rápido desaparece
        float tamaño;

        public Particula() {
            // Aparece en cualquier parte del ancho de la ventana
            x = MathUtils.random(0, Gdx.graphics.getWidth());

            // Aparece en la mitad inferior de la ventana (desde 0 hasta alto/2)
            y = MathUtils.random(0, Gdx.graphics.getHeight() / 2f);

            // Velocidad hacia arriba aleatoria
            velocidadY = MathUtils.random(20, 100); // píxeles por segundo

            vida = 1.0f;
            // Tarda entre 2 y 5 segundos en desaparecer
            velocidadVida = MathUtils.random(0.2f, 0.5f);

            tamaño = MathUtils.random(2, 4); // Tamaño aleatorio
        }

        public void actualizar(float delta) {
            y += velocidadY * delta; // Mover hacia arriba
            vida -= velocidadVida * delta; // Envejecer
        }

        public boolean estaMuerta() {
            return vida <= 0 || y > Gdx.graphics.getHeight();
        }

        public void dibujar(SpriteBatch batch, Texture textura) {
            // Usamos la vida como alfa (transparencia) para que desaparezca
            batch.setColor(1, 1, 1, vida);
            batch.draw(textura, x, y, tamaño, tamaño);
            batch.setColor(Color.WHITE); // Resetear color
        }
    }

    private Cell<ImageButton> cellConfig, cellSalir, cellPlay;

    private void crearInterfaz() {
        // 1. Fondo del Menú Lateral
        menuSideActor = new Image(menuSideTexture);
        menuSideActor.setColor(1, 1, 1, 0.7f);

        // 2. Definición de Estilos (Los que ya tenías)
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
        // Empezamos asumiendo que estamos en local
        styleAccount.imageUp = new TextureRegionDrawable(new TextureRegion(texDisconnected));

        // Instanciar y configurar
        accountBtn = new ImageButton(styleAccount);
        configurarBoton(accountBtn, "account");
        accountBtn.getImageCell().expand().fill();

        actualizarSpriteCuenta(); // Llamamos al nuevo método

        // 3. Crear botones y configurar efectos
        playButton = new ImageButton(stylePlay);
        configBtn = new ImageButton(styleConfig);
        salirButton = new ImageButton(styleSalir);

        configurarBoton(playButton, "play");
        configurarBoton(configBtn, "config");
        configurarBoton(salirButton, "salir");

        playButton.getImageCell().expand().fill();
        configBtn.getImageCell().expand().fill();
        salirButton.getImageCell().expand().fill();

        // 4. ORGANIZACIÓN EN TABLA (Formato específico)
        menuTable = new Table();
        menuTable.left().top();

        cellConfig = menuTable.add(configBtn).padTop(30).padLeft(30).left();
        cellAccount = menuTable.add(accountBtn).padTop(30).padLeft(20).left();
        menuTable.add().expandX();
        cellSalir = menuTable.add(salirButton).padTop(30).padRight(30).right();

        menuTable.row();

        cellPlay = menuTable.add(playButton).colspan(4).padTop(200).center();

        noestirar.addActor(menuSideActor);
        noestirar.addActor(menuTable);
        menuTable.pack();
    }

    private void configurarBoton(final ImageButton btn, final String tipo) {
        // 1. IMPORTANTE: Habilitar transformaciones para que el escalado funcione desde el centro
        btn.setTransform(true);

        // Esperamos a que el botón tenga su tamaño definido para poner el origen en el centro
        // Si el tamaño es 0 aún, se ajustará en el primer render, pero esto es una buena práctica:
        btn.setOrigin(btn.getWidth() / 2f, btn.getHeight() / 2f);

        btn.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                // Actualizamos el origen justo antes de la animación
                btn.setOrigin(btn.getWidth() / 2f, btn.getHeight() / 2f);

                btn.clearActions();
                btn.addAction(Actions.scaleTo(0.9f, 0.9f, 0.1f));
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                // Efecto visual: vuelve a su tamaño normal al soltar
                btn.clearActions();
                btn.addAction(Actions.scaleTo(1f, 1f, 0.1f));
                super.touchUp(event, x, y, pointer, button);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                // 2. Lógica según el tipo de botón
                switch (tipo) {
                    case "play":
                        // Desactivamos el botón para evitar que el usuario pulse mil veces mientras funde a negro
                        btn.setDisabled(true);

                        // Primero hacemos el fundido a negro (false = salir)
                        ejecutarFading(false, new Runnable() {
                            @Override
                            public void run() {
                                // Este código SOLO se ejecuta cuando la pantalla ya está 100% NEGRA
                                game.setScreen(new MenuMapScreen(game));
                            }
                        });
                        break;

                    case "config":
                        if (!settingsWindow.isVisible()) {
                            settingsWindow.setVisible(true);
                            // Quitamos cualquier escala previa
                            settingsWindow.setScale(1f);

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
                            accountWindow.actualizarInterfaz(); // Refresca los datos por si cambiaron
                            accountWindow.setVisible(true);
                            // Lo centramos en pantalla
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
                }
            }
        });
    }
    private void ejecutarFading(boolean entrar, final Runnable accionAlTerminar) {
        final Image fadeOverlay = new Image(blackScreen);
        fadeOverlay.setSize(noestirar.getWidth(), noestirar.getHeight());
        fadeOverlay.setPosition(0, 0);

        // --- ESTA ES LA CLAVE ---
        // Si estamos ENTRANDO (el negro desaparece), el telón no debe captar clics
        // Si estamos SALIENDO (el negro aparece), bloqueamos los clics para que el usuario no pulse nada más
        if (entrar) {
            fadeOverlay.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
        } else {
            fadeOverlay.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
        }

        fadeOverlay.getColor().a = entrar ? 1f : 0f;
        float alphaDestino = entrar ? 0f : 1f;

        fadeOverlay.addAction(Actions.sequence(
            Actions.alpha(alphaDestino, 0.5f),
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    if (accionAlTerminar != null) accionAlTerminar.run();

                    // Si la transición era de ENTRADA (el negro se fue),
                    // eliminamos el actor para que no consuma recursos.
                    if (entrar) {
                        fadeOverlay.remove();
                    }
                }
            })
        ));

        noestirar.addActor(fadeOverlay);
    }

    public void actualizarSpriteCuenta() {
        // Obtenemos el estilo actual del botón
        ImageButton.ImageButtonStyle style = accountBtn.getStyle();

        if (isConnected) {
            style.imageUp = new TextureRegionDrawable(new TextureRegion(texConnected));
        } else {
            style.imageUp = new TextureRegionDrawable(new TextureRegion(texDisconnected));
        }

        // Aplicamos el estilo modificado
        accountBtn.setStyle(style);
        accountBtn.setColor(com.badlogic.gdx.graphics.Color.WHITE);
    }

    public AuthRepository getAuthManager() {
        return authManager;
    }

}
