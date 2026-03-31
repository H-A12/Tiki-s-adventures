package com.tikisadventure.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch; // Cambiado a Batch genérico
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group; // Usaremos un Group para los fondos
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions; // Para las animaciones
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class MenuMapScreen implements Screen {
    private final Game game;
    private Stage stage;
    private Skin uiSkin;

    // --- CAMBIO 1: GESTIÓN DE FONDOS CON ACTORES ---
    private Group grupoFondos; // Contenedor para los fondos
    private ImagenFondo fondoBosque;
    private ImagenFondo fondoDesierto;
    private ImagenFondo fondoCueva;
    private ImagenFondo fondoMostradoActualmente; // Referencia al que se ve ahora

    // Elementos que necesitamos actualizar dinámicamente
    private Label labelDesc;
    private TextButton btnJugar;

    public MenuMapScreen(Game game) {
        this.game = game;
    }

    // --- CAMBIO 2: CLASE INTERNA PARA EL ACTOR DE FONDO ---
    // Esto nos permite usar Actions sobre una textura
    private static class ImagenFondo extends Actor {
        private Texture textura;

        public ImagenFondo(Texture textura) {
            this.textura = textura;
            // Importante: El actor debe ocupar toda la resolución virtual
            setBounds(0, 0, 800, 480);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            // Dibujamos respetando el color y el alfa (transparencia) del actor
            Color color = getColor();
            batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
            batch.draw(textura, getX(), getY(), getWidth(), getHeight());
            // Restauramos el color del batch para no afectar a otros actores
            batch.setColor(Color.WHITE);
        }
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(800, 480));
        Gdx.input.setInputProcessor(stage);

        uiSkin = new Skin(Gdx.files.internal("uiskin.json"));

        // --- CAMBIO 3: INICIALIZACIÓN DE FONDOS ---
        grupoFondos = new Group();
        // Cargamos las texturas. Asegúrate de que estas rutas existen.
        fondoBosque = new ImagenFondo(new Texture(Gdx.files.internal("Menu/fondo_bosque.png")));
        fondoDesierto = new ImagenFondo(new Texture(Gdx.files.internal("Menu/fondo_desierto.png")));
        fondoCueva = new ImagenFondo(new Texture(Gdx.files.internal("Menu/fondo_cueva.png")));

        // Añadimos todos al grupo. El orden importa (el último está encima).
        grupoFondos.addActor(fondoBosque);
        grupoFondos.addActor(fondoDesierto);
        grupoFondos.addActor(fondoCueva);

        // Configuración inicial: Solo mostramos el bosque, los otros invisibles (alfa = 0)
        fondoDesierto.getColor().a = 0;
        fondoCueva.getColor().a = 0;
        fondoMostradoActualmente = fondoBosque;

        // Añadimos el grupo de fondos al Stage PRIMERO para que esté detrás de la UI
        stage.addActor(grupoFondos);


        // --- CONFIGURACIÓN DE LA UI (Igual que antes, con estilo estático) ---
        SelectBox.SelectBoxStyle estiloFijo = new SelectBox.SelectBoxStyle(uiSkin.get(SelectBox.SelectBoxStyle.class));
        estiloFijo.backgroundOpen = estiloFijo.background;
        estiloFijo.backgroundOver = estiloFijo.background;
        if(estiloFijo.overFontColor != null) estiloFijo.overFontColor = estiloFijo.fontColor;

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.pad(20);

        Table columnaIzquierda = new Table();

        final SelectBox<String> selectorMapas = new SelectBox<>(estiloFijo);
        selectorMapas.setItems("Bosque", "Desierto (Bloqueado)", "Cueva (Bloqueado)");

        labelDesc = new Label("BOSQUE: Un lugar lleno de peligros y tesoros ocultos.", uiSkin);
        labelDesc.setWrap(true);
        labelDesc.setAlignment(com.badlogic.gdx.utils.Align.topLeft);

        columnaIzquierda.add(new Label("SELECCIONA MAPA", uiSkin)).padBottom(10).row();
        columnaIzquierda.add(selectorMapas).width(220).height(40).padBottom(20).row();
        columnaIzquierda.add(labelDesc).width(220).height(150).top();

        btnJugar = new TextButton("¡EMPEZAR!", uiSkin);
        TextButton btnTienda = new TextButton("TIENDA", uiSkin);

        mainTable.add(columnaIzquierda).expand().left();
        mainTable.add(btnJugar).size(180, 80).expand().center();
        mainTable.add(btnTienda).size(120, 120).expand().right();
        mainTable.row();

        TextButton btnVolver = new TextButton("Volver", uiSkin);
        mainTable.add(btnVolver).colspan(3).right().padTop(20);


        // --- CAMBIO 4: LÓGICA DE CAMBIO DE FONDO CON DIFUMINADO ---
        selectorMapas.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String seleccionado = selectorMapas.getSelected();

                // Definimos cuál será el nuevo fondo
                ImagenFondo fondoSiguiente;
                if (seleccionado.equals("Bosque")) {
                    labelDesc.setText("BOSQUE: Un lugar lleno de peligros y tesoros ocultos.");
                    btnJugar.setDisabled(false);
                    btnJugar.setColor(Color.WHITE);
                    fondoSiguiente = fondoBosque;
                } else if (seleccionado.contains("Desierto")) {
                    labelDesc.setText("??? (Desierto)");
                    btnJugar.setDisabled(true);
                    btnJugar.setColor(0.5f, 0.5f, 0.5f, 0.5f);
                    fondoSiguiente = fondoDesierto;
                } else {
                    labelDesc.setText("??? (Cueva)");
                    btnJugar.setDisabled(true);
                    btnJugar.setColor(0.5f, 0.5f, 0.5f, 0.5f);
                    fondoSiguiente = fondoCueva;
                }

                // --- LA MAGIA DEL DIFUMINADO (Cross-Fade) ---
                if (fondoSiguiente != fondoMostradoActualmente) {
                    float duracion = 0.4f; // Tiempo que tarda el difuminado en segundos

                    // 1. Aseguramos que el fondo siguiente esté detrás pero sea invisible (alfa 0)
                    fondoSiguiente.getColor().a = 0;
                    fondoSiguiente.toBack(); // Lo movemos al fondo del Group

                    // 2. Iniciamos el fadeIn del nuevo fondo (aparece lentamente detrás)
                    fondoSiguiente.addAction(Actions.fadeIn(duracion));

                    // 3. Iniciamos el fadeOut del fondo actual (desaparece lentamente encima)
                    fondoMostradoActualmente.addAction(Actions.fadeOut(duracion));

                    // 4. Actualizamos la referencia
                    fondoMostradoActualmente = fondoSiguiente;
                }
            }
        });

        // Listeners de botones (Igual que antes)
        btnJugar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!btnJugar.isDisabled()) {
                    game.setScreen(new GameScreen(game));
                }
            }
        });

        btnVolver.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MenuScreen(game));
            }
        });

        stage.addActor(mainTable);
    }

    // --- CAMBIO 5: RENDER SIMPLIFICADO ---
    @Override
    public void render(float delta) {
        // Limpiamos pantalla
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // YA NO DIBUJAMOS EL FONDO AQUÍ CON BATCH.DRAW.
        // El stage.draw() se encarga de dibujar el Group de fondos y luego la UI encima.

        stage.act(delta); // Importante: actualiza las Actions (animaciones)
        stage.draw(); // Dibuja todo en el orden correcto
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void hide() { dispose(); }
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (uiSkin != null) uiSkin.dispose();

        // --- CAMBIO 6: DISPOSE DE LAS NUEVAS TEXTURAS ---
        if (fondoBosque != null) fondoBosque.textura.dispose();
        if (fondoDesierto != null) fondoDesierto.textura.dispose();
        if (fondoCueva != null) fondoCueva.textura.dispose();
        // El SpriteBatch genérico ya no es necesario aquí
    }
}
