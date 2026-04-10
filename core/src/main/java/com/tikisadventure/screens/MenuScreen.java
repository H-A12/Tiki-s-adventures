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
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;

public class MenuScreen implements Screen {

    private static final float VIRTUAL_WIDTH = 800;
    private static final float VIRTUAL_HEIGHT = 480;

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
    private com.badlogic.gdx.utils.Array<Particula> particulas;
    private float tiempoSiguienteParticula;
    private static final float TIEMPO_CREACION = 0.5f;

    private ImageButton playButton;
    private ImageButton salirButton;
    private ImageButton configBtn;
    private Window settingsWindow;
    private Skin uiSkin;

    public MenuScreen(Game game){
        this.game = game;
    }

    @Override
    public void show() {

        // Importante: Asegúrate de importar com.badlogic.gdx.utils.viewport.StretchViewport;
        estirar = new Stage(new com.badlogic.gdx.utils.viewport.StretchViewport(800, 480));

        noestirar = new Stage(new com.badlogic.gdx.utils.viewport.ScreenViewport());
        Gdx.input.setInputProcessor(noestirar);

        batch = new SpriteBatch();

        // --- CARGA DE TEXTURAS ---
        background = new Texture(Gdx.files.internal("Menu/fondo_menu.png"));
        buttonTexture = new Texture(Gdx.files.internal("Menu/ButtonPlay.png"));
        buttonPressedTexture = new Texture(Gdx.files.internal("Menu/ButtonPlayPressed.png"));
        buttonSalirTexture = new Texture(Gdx.files.internal("Menu/ButtonSalir.png"));
        buttonSalirPressedTexture = new Texture(Gdx.files.internal("Menu/ButtonSalirPressed.png"));
        buttonSettings = new Texture(Gdx.files.internal("Menu/settings.png"));
        vignetteTexture = new Texture(Gdx.files.internal("Menu/Filtro.png"));
        particulas = new com.badlogic.gdx.utils.Array<>();
        tiempoSiguienteParticula = TIEMPO_CREACION;
        particleTexture = new Texture(Gdx.files.internal("Menu/particula.png"));

        // --- CONFIGURACIÓN BOTÓN PLAY ---
        ImageButton.ImageButtonStyle stylePlay = new ImageButton.ImageButtonStyle();
        stylePlay.imageUp = new TextureRegionDrawable(new TextureRegion(buttonTexture));
        stylePlay.imageDown = new TextureRegionDrawable(new TextureRegion(buttonPressedTexture));

        // Usamos la variable de clase (this.playButton), no una local
        this.playButton = new ImageButton(stylePlay);
        this.playButton.setSize(480, 150);

        // Activamos transformaciones para el efecto de escala
        this.playButton.setTransform(true);
        this.playButton.setOrigin(this.playButton.getWidth() / 2f, this.playButton.getHeight() / 2f);

        this.playButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                playButton.clearActions();
                playButton.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(0.9f, 0.9f, 0.1f));
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                playButton.clearActions();
                playButton.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(1f, 1f, 0.1f));
                super.touchUp(event, x, y, pointer, button);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                playButton.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.delay(0.15f),
                    com.badlogic.gdx.scenes.scene2d.actions.Actions.run(new Runnable() {
                        @Override
                        public void run() {
                            game.setScreen(new MenuMapScreen(game));
                        }
                    })
                ));
            }
        });

// No olvides añadirlo al stage al final del show()
        noestirar.addActor(playButton);


        // --- CONFIGURACIÓN BOTÓN SALIR ---
        ImageButton.ImageButtonStyle styleSalir = new ImageButton.ImageButtonStyle();
        styleSalir.imageUp = new TextureRegionDrawable(new TextureRegion(buttonSalirTexture));
        styleSalir.imageDown = new TextureRegionDrawable(new TextureRegion(buttonSalirPressedTexture));

        salirButton = new ImageButton(styleSalir);
        float paddingSalir = 15;
        salirButton.setSize(35, 35);
        salirButton.setPosition(
            noestirar.getViewport().getWorldWidth() - salirButton.getWidth() - paddingSalir,
            noestirar.getViewport().getWorldHeight() - salirButton.getHeight() - paddingSalir
        );

        salirButton.setTransform(true);
        salirButton.setOrigin(32, 32);

        salirButton.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                salirButton.clearActions();
                salirButton.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(0.9f, 0.9f, 0.1f));
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                salirButton.clearActions();
                salirButton.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(1f, 1f, 0.1f));
                super.touchUp(event, x, y, pointer, button);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                mostrarConfirmacionSalir();
            }
        });

        // --- CONFIGURACIÓN BOTÓN AJUSTES (ENGRANAJE) ---
        // 1. Cargamos las texturas
        buttonSettings = new Texture(Gdx.files.internal("Menu/settings.png"));
        buttonSettingsPressed = new Texture(Gdx.files.internal("Menu/settingsPressed.png"));

// 2. Creamos el estilo con ambos estados
        ImageButton.ImageButtonStyle styleConfig = new ImageButton.ImageButtonStyle();
        styleConfig.imageUp = new TextureRegionDrawable(new TextureRegion(buttonSettings));
        styleConfig.imageDown = new TextureRegionDrawable(new TextureRegion(buttonSettingsPressed));

        configBtn = new ImageButton(styleConfig);
        configBtn.setSize(35, 35);
        configBtn.setTransform(true);
        configBtn.setOrigin(configBtn.getWidth() / 2f, configBtn.getHeight() / 2f);

// 3. Añadimos el Listener con Animación de Escala + Abrir Menú
        configBtn.addListener(new ClickListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                configBtn.clearActions();
                configBtn.addAction(Actions.scaleTo(0.9f, 0.9f, 0.1f));
                return super.touchDown(event, x, y, pointer, button);
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                configBtn.clearActions();
                configBtn.addAction(Actions.scaleTo(1f, 1f, 0.1f));
                super.touchUp(event, x, y, pointer, button);
            }

            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!settingsWindow.isVisible()) {
                    settingsWindow.setVisible(true);
                    settingsWindow.getColor().a = 0;
                    settingsWindow.addAction(Actions.fadeIn(0.3f));
                } else {
                    settingsWindow.addAction(Actions.sequence(
                        Actions.fadeOut(0.3f),
                        Actions.visible(false)
                    ));
                }
            }
        });

        // --- VENTANA DE AJUSTES ---
        crearVentanaAjustes();
        settingsWindow.setVisible(false);

        // --- ORDEN DE CAPAS (Z-INDEX) ---
        // 1. Capa inferior: Botones principales del menú
        noestirar.addActor(playButton);
        noestirar.addActor(salirButton);

        // 2. Capa media: La ventana de ajustes
        noestirar.addActor(settingsWindow);

        // 3. Capa superior: El botón de engranaje (SIEMPRE ENCIMA)
        // Al añadirlo el último, se asegura de que el menú no lo tape al abrirse
        noestirar.addActor(configBtn);

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.begin();
        // 1. Fondo
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // 2. NUEVO: Lógica de Partículas
        tiempoSiguienteParticula -= delta;
        if (tiempoSiguienteParticula <= 0) {
            particulas.add(new Particula());
            tiempoSiguienteParticula = TIEMPO_CREACION;
        }

        for (int i = 0; i < particulas.size; i++) {
            Particula p = particulas.get(i);
            p.actualizar(delta);
            if (p.estaMuerta()) {
                particulas.removeIndex(i);
                i--;
            } else {
                p.dibujar(batch, particleTexture);
            }
        }

        // 3. Viñeta (Dibujada después para que oscurezca las partículas también)
        batch.setColor(0, 0, 0, 0.8f);
        batch.draw(vignetteTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setColor(Color.WHITE);
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

        // 1. Escala normal (para el PlayButton)
        float escalaNormal = w / VIRTUAL_WIDTH;

        // 2. Escala atenuada (para los Iconos):
        // Usamos Math.sqrt (raíz cuadrada) para que si la pantalla crece mucho,
        // el icono no crezca tanto.
        float escalaIconos = (float) Math.sqrt(escalaNormal);

        // El pad también debería ser un poco más conservador
        float pad = 15 * escalaIconos;

        // --- CONFIGURACIÓN (Escala atenuada) ---
        if (configBtn != null) {
            float sizeCfg = 48 * escalaIconos; // Crece menos
            configBtn.setSize(sizeCfg, sizeCfg);
            configBtn.setOrigin(sizeCfg / 2f, sizeCfg / 2f);
            configBtn.setPosition(pad, h - configBtn.getHeight() - pad);
        }

        // --- VENTANA AJUSTES (Escala atenuada) ---
        if (settingsWindow != null && configBtn != null) {
            // Mantenemos la ventana proporcional a los iconos
            settingsWindow.setScale(escalaIconos * 0.8f);
            settingsWindow.setTransform(true);
            settingsWindow.setPosition(pad, configBtn.getY() - (settingsWindow.getHeight() * settingsWindow.getScaleY()) - 10);
        }

        // --- SALIR (Escala atenuada) ---
        if (salirButton != null) {
            float sizeSalir = 48 * escalaIconos; // Crece menos
            salirButton.setSize(sizeSalir, sizeSalir);
            salirButton.setOrigin(sizeSalir / 2f, sizeSalir / 2f);
            salirButton.setPosition(w - salirButton.getWidth() - pad, h - salirButton.getHeight() - pad);
        }

        // --- PLAY BUTTON (Escala completa) ---
        if (playButton != null) {
            // Este sigue usando la escala original para destacar
            playButton.setSize(380 * escalaNormal, 120 * escalaNormal);
            playButton.setOrigin(playButton.getWidth() / 2f, playButton.getHeight() / 2f);
            playButton.setPosition(w / 2f - playButton.getWidth() / 2f, h / 2f - playButton.getHeight() / 2f - (60 * escalaNormal));
        }
    }

    @Override public void pause(){}
    @Override public void resume(){}
    @Override public void hide(){}

    @Override
    public void dispose(){
        estirar.dispose();
        noestirar.dispose();
        batch.dispose();
        buttonTexture.dispose();
        buttonPressedTexture.dispose();
        buttonSalirTexture.dispose();
        buttonSalirPressedTexture.dispose();
        buttonSettings.dispose();
        background.dispose();
        if (vignetteTexture != null) vignetteTexture.dispose();
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
        // 1. Cargamos el Skin (esto evita el error de "exit value 1")
        // Asumiendo que bajaste uno y lo llamaste 'uiskin.json'
        uiSkin = new Skin(Gdx.files.internal("uiskin.json"));

        // 2. Creamos la ventana usando el estilo del Skin
        settingsWindow = new Window("", uiSkin);
        settingsWindow.setMovable(false);
        settingsWindow.setModal(false);
        settingsWindow.padTop(30);

        // 3. Creamos los componentes (ya no hay que configurar estilos a mano)
        TextButton btnEsp = new TextButton("ESP", uiSkin);
        TextButton btnEng = new TextButton("ENG", uiSkin);

        // Slider (Volumen)
        final Slider volumeSlider = new Slider(0, 1, 0.1f, false, uiSkin);
        volumeSlider.setValue(0.5f); // 50% por defecto

        // SelectBox (Resoluciones)
        SelectBox<String> resSelector = new SelectBox<>(uiSkin);
        resSelector.setItems("800x480", "1280x720", "1920x1080");

        // Botón Login
        TextButton btnLogin = new TextButton("LOGIN", uiSkin);

        // 4. Organización con Tabla (Muy importante para que no se amontone)
        settingsWindow.defaults().pad(5).space(10); // Margen general para todo

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

        settingsWindow.add(btnLogin).colspan(3).padTop(15).fillX();

        // 5. Tamaño y Posición
        settingsWindow.pack(); // Ajusta el tamaño automáticamente al contenido
        settingsWindow.setPosition(5, VIRTUAL_HEIGHT - settingsWindow.getHeight() - 75); // Justo debajo del botón de configuración

        noestirar.addActor(settingsWindow);

        // AQUI VA EL LISTENER:
        resSelector.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String seleccion = resSelector.getSelected();
                String[] partes = seleccion.split("x");
                int nuevoAncho = Integer.parseInt(partes[0]);
                int nuevoAlto = Integer.parseInt(partes[1]);

                // 1. Cambiamos el tamaño de la ventana
                Gdx.graphics.setWindowedMode(nuevoAncho, nuevoAlto);

                // 2. IMPORTANTE: Al hacer el update del viewport,
                // LibGDX llamará automáticamente al método resize(width, height) de tu pantalla.
                noestirar.getViewport().update(nuevoAncho, nuevoAlto, true);

                // BORRA AQUÍ cualquier settingsWindow.setPosition(...)
            }
        });

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
}
