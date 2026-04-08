package com.tikisadventure.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScalingViewport;

import javax.swing.event.ChangeEvent;

public class MenuScreen implements Screen {

    private static final float VIRTUAL_WIDTH = 800;
    private static final float VIRTUAL_HEIGHT = 480;

    private Game game;
    private Stage stage;
    private Texture buttonTexture;
    private Texture buttonPressedTexture;
    private Texture buttonSalirTexture;
    private  Texture buttonSalirPressedTexture;
    private Texture buttonSettings;

    private Texture background;
    private SpriteBatch batch;

    private ImageButton configBtn;
    private Window settingsWindow;
    private Skin uiSkin;

    public MenuScreen(Game game){
        this.game = game;
    }

    @Override
    public void show() {
        float virtualWidth = VIRTUAL_WIDTH;
        float virtualHeight = VIRTUAL_HEIGHT;

        stage = new Stage(new ScalingViewport(Scaling.stretch, 800, 480));
        Gdx.input.setInputProcessor(stage);

        batch = new SpriteBatch();

        // --- CARGA DE TEXTURAS ---
        background = new Texture(Gdx.files.internal("Menu/fondo_menu.png"));
        buttonTexture = new Texture(Gdx.files.internal("Menu/ButtonPlay.png"));
        buttonPressedTexture = new Texture(Gdx.files.internal("Menu/ButtonPlayPressed.png"));
        buttonSalirTexture = new Texture(Gdx.files.internal("Menu/ButtonSalir.png"));
        buttonSalirPressedTexture = new Texture(Gdx.files.internal("Menu/ButtonSalirPressed.png"));
        buttonSettings = new Texture(Gdx.files.internal("Menu/settings.png"));

        // --- CONFIGURACIÓN BOTÓN PLAY ---
        ImageButton.ImageButtonStyle stylePlay = new ImageButton.ImageButtonStyle();
        stylePlay.imageUp = new TextureRegionDrawable(new TextureRegion(buttonTexture));
        stylePlay.imageDown = new TextureRegionDrawable(new TextureRegion(buttonPressedTexture));

        ImageButton playButton = new ImageButton(stylePlay);
        playButton.setSize(200, 80);
        playButton.setPosition(virtualWidth / 2f - 100, virtualHeight / 2f - 150);

        playButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                game.setScreen(new MenuMapScreen(game));
            }

        });

        // --- CONFIGURACIÓN BOTÓN SALIR ---
        ImageButton.ImageButtonStyle styleSalir = new ImageButton.ImageButtonStyle();
        styleSalir.imageUp = new TextureRegionDrawable(new TextureRegion(buttonSalirTexture));
        styleSalir.imageDown = new TextureRegionDrawable(new TextureRegion(buttonSalirPressedTexture));

        ImageButton salirButton = new ImageButton(styleSalir);
        salirButton.setSize(200, 80);
        salirButton.setPosition(virtualWidth / 2f + 260, virtualHeight / 2f + 155);

        salirButton.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y){
                mostrarConfirmacionSalir();
            }
        });

        // --- CONFIGURACIÓN BOTÓN AJUSTES (ENGRANAJE) ---
        TextureRegionDrawable icon = new TextureRegionDrawable(new TextureRegion(buttonSettings));
        configBtn = new ImageButton(icon);
        configBtn.setSize(50, 50);
        configBtn.setPosition(10, VIRTUAL_HEIGHT - 73);

        configBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!settingsWindow.isVisible()) {
                    settingsWindow.setVisible(true);
                    settingsWindow.getColor().a = 0;
                    settingsWindow.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeIn(0.3f));
                } else {
                    settingsWindow.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut(0.3f),
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.visible(false)
                    ));
                }
            }
        });

        // --- VENTANA DE AJUSTES ---
        crearVentanaAjustes();
        settingsWindow.setVisible(false);

        // --- ORDEN DE CAPAS (Z-INDEX) ---
        // 1. Capa inferior: Botones principales del menú
        stage.addActor(playButton);
        stage.addActor(salirButton);

        // 2. Capa media: La ventana de ajustes
        stage.addActor(settingsWindow);

        // 3. Capa superior: El botón de engranaje (SIEMPRE ENCIMA)
        // Al añadirlo el último, se asegura de que el menú no lo tape al abrirse
        stage.addActor(configBtn);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        // 3. Importante: Para el fondo manual, usa la cámara del stage
        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();
        batch.draw(background, 0, 0, VIRTUAL_WIDTH, VIRTUAL_HEIGHT);
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        // 4. Esto actualiza el viewport y centra la cámara
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause(){}
    @Override public void resume(){}
    @Override public void hide(){}

    @Override
    public void dispose(){
        stage.dispose();
        batch.dispose();
        buttonTexture.dispose();
        buttonPressedTexture.dispose();
        buttonSalirTexture.dispose();
        buttonSalirPressedTexture.dispose();
        background.dispose();
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
        dialog.show(stage);
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

        stage.addActor(settingsWindow);

        // AQUI VA EL LISTENER:
        resSelector.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String seleccion = resSelector.getSelected();

                // Dividimos el texto para obtener ancho y alto
                // Ejemplo: "1280x720" -> ["1280", "720"]
                String[] partes = seleccion.split("x");
                int nuevoAncho = Integer.parseInt(partes[0]);
                int nuevoAlto = Integer.parseInt(partes[1]);

                // 1. Cambiamos el tamaño de la ventana física
                Gdx.graphics.setWindowedMode(nuevoAncho, nuevoAlto);

                // 2. Actualizamos el Viewport para que la UI no se descoloque
                // El 'true' centra la cámara en la nueva resolución
                stage.getViewport().update(nuevoAncho, nuevoAlto, true);

                // 3. (Opcional) Re-posicionar la ventana de ajustes si se movió
                settingsWindow.setPosition(5, VIRTUAL_HEIGHT - settingsWindow.getHeight() - 75);
            }
        });
    }

}
