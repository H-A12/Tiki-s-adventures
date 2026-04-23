package com.tikisadventure.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;

import com.badlogic.gdx.scenes.scene2d.Group; // Usaremos un Group para los fondos
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions; // Para las animaciones
import com.badlogic.gdx.graphics.g2d.Animation;
import com.tikisadventure.ui.CharacterPreviewActor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.tikisadventure.entities.player.CharacterFactory;
import com.tikisadventure.core.GameSession;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class MenuMapScreen implements Screen {
    private final Game game;
    private Stage stage;
    private Skin uiSkin;

    // Gestion de fondos y actores
    private Group grupoFondos;
    private ImagenFondo fondoBosque;
    private ImagenFondo fondoDesierto;
    private ImagenFondo fondoCueva;
    private ImagenFondo fondoMostradoActualmente;

    //Elementos actualizados dinámicamente
    private Label labelDesc;
    private TextButton btnJugar;

    //Gestor del menu ModoDios
    private MenuGodMode godModeManager;

    float resetTimer = 0f;

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

        // Inicializamos nuestro gestor del Modo Dios pasándole los recursos
        godModeManager = new MenuGodMode(stage, uiSkin);

        // Inicializar fondos
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

        // Configurar UI
        SelectBox.SelectBoxStyle estiloFijo = new SelectBox.SelectBoxStyle(uiSkin.get(SelectBox.SelectBoxStyle.class));
        estiloFijo.backgroundOpen = estiloFijo.background;
        estiloFijo.backgroundOver = estiloFijo.background;
        if(estiloFijo.overFontColor != null) estiloFijo.overFontColor = estiloFijo.fontColor;

        Table mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.pad(20);

        Table columnaIzquierda = new Table();


        //Logica de la seleccion de mapas con el guardado y desbloqueo:
        final SelectBox<String> selectorMapas = new SelectBox<>(estiloFijo);

        // Lógica de llenado dinámico de mapas
        com.badlogic.gdx.utils.Array<String> mapasDisponibles = new com.badlogic.gdx.utils.Array<>();
        mapasDisponibles.add("Bosque Mucoso"); // El base

        if (com.tikisadventure.core.SaveManager.isMapUnlocked("desierto")) {
            mapasDisponibles.add("Desierto Secarocas");
        }
        if (com.tikisadventure.core.SaveManager.isMapUnlocked("cueva")) {
            mapasDisponibles.add("Musgocueva");
        }

        selectorMapas.setItems(mapasDisponibles); // Solo cargará los desbloqueados

        labelDesc = new Label("BOSQUE MUCOSO: El amanecer de la aventura de Tiki.", uiSkin);
        labelDesc.setWrap(true);
        labelDesc.setAlignment(com.badlogic.gdx.utils.Align.topLeft);

        columnaIzquierda.add(new Label("MAPAS", uiSkin)).padBottom(10).row();
        columnaIzquierda.add(selectorMapas).width(220).height(40).padBottom(20).row();
        columnaIzquierda.add(labelDesc).width(220).height(150).top();

        // Llamamos al gestor del MenuGodMode.java para que inyecte su UI en la columna izquierda
        godModeManager.inyectarInterfaz(columnaIzquierda);

        btnJugar = new TextButton("¡EMPEZAR!", uiSkin);
        TextButton btnTienda = new TextButton("TIENDA", uiSkin);

        // Seleccion de personaje
        Table charTable = new Table();
        final ButtonGroup<Button> group = new ButtonGroup<>();

        JsonValue characterData = new JsonReader().parse(Gdx.files.internal("data/player_config.json"));

        int charIndex = 1; // Para saber si es el personaje 1, 2 o 3

        for (JsonValue charEntry : characterData.get("characters")) {
            final String id = charEntry.getString("id");
            final boolean isUnlocked = com.tikisadventure.core.SaveManager.isCharacterUnlocked(charIndex);

            Animation<TextureRegion> idleAnim = CharacterFactory.getCharacterIdleAnimation(id);
            final Button btn = new Button(uiSkin);

            if (!isUnlocked) {
                // BLOQUEADO: Extraemos el frame 0 (quieto) y lo metemos en una Image normal
                TextureRegion firstFrame = idleAnim.getKeyFrame(0f);
                com.badlogic.gdx.scenes.scene2d.ui.Image staticImage = new com.badlogic.gdx.scenes.scene2d.ui.Image(firstFrame);
                staticImage.setColor(Color.BLACK); // Tiñe el sprite en sí de negro

                btn.add(staticImage).size(48, 48);
                btn.setDisabled(true);
                btn.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
            } else {
                btn.add(new CharacterPreviewActor(idleAnim)).size(48, 48);
            }


            btn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (!isUnlocked) return; // Si está bloqueado, no hace nada

                    // Creamos la ventana modal superpuesta
                    MenuCharacter modal = new MenuCharacter("", uiSkin, id, idleAnim, new Runnable() {
                        @Override
                        public void run() {
                            // ESTO SE EJECUTA SI EL JUGADOR PULSA "SELECCIONAR"
                            btn.setChecked(true); // Marcamos este botón como activo

                            // Actualizamos todos los colores
                            int tempIndex = 1;
                            for (Button b : group.getButtons()) {
                                if (!com.tikisadventure.core.SaveManager.isCharacterUnlocked(tempIndex)) {
                                    b.setColor(new Color(0.5f, 0.5f, 0.5f, 1f));
                                } else {
                                    b.setColor(b.isChecked() ? Color.WHITE : new Color(0.5f, 0.5f, 0.5f, 1f));
                                }
                                tempIndex++;
                            }
                        }
                    });

                    // Centramos el modal matemáticamente en el medio de la pantalla
                    modal.setPosition(Math.round((stage.getWidth() - modal.getWidth()) / 2f),
                        Math.round((stage.getHeight() - modal.getHeight()) / 2f));

                    // Lo añadimos al escenario actual (se dibuja por encima del fondo del mapa)
                    stage.addActor(modal);
                }
            });
            group.add(btn);
            charTable.add(btn).size(64, 64).pad(10);
            charIndex++;
        }

        // Seleccionar el primer botón por defecto y forzar colores
        group.getButtons().first().setChecked(true);
        int tempIndex = 1;
        for (Button b : group.getButtons()) {
            if (!com.tikisadventure.core.SaveManager.isCharacterUnlocked(tempIndex)) {
                b.setColor(Color.BLACK);
            } else {
                b.setColor(b.isChecked() ? Color.WHITE : new Color(0.5f, 0.5f, 0.5f, 1f));
            }
            tempIndex++;
        }

        group.getButtons().first().setChecked(true);
        for (Button b : group.getButtons()) {
            b.setColor(b.isChecked() ? Color.WHITE : new Color(0.5f, 0.5f, 0.5f, 1f));
        }

        mainTable.row();
        mainTable.add(charTable).colspan(3).padTop(20);
        mainTable.row();

        mainTable.add(columnaIzquierda).expand().left();
        mainTable.add(btnJugar).size(180, 80).expand().center();
        mainTable.add(btnTienda).size(120, 120).expand().right();
        mainTable.row();

        TextButton btnVolver = new TextButton("Volver", uiSkin);
        mainTable.add(btnVolver).colspan(3).bottom().right().padBottom(30).padTop(10);


        //Selector de mapas con cambio difuminado
        selectorMapas.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String seleccionado = selectorMapas.getSelected();
                ImagenFondo fondoSiguiente;

                if (seleccionado.contains("Bosque")) {
                    labelDesc.setText("BOSQUE MUCOSO: El amanecer de la aventura de Tiki.");
                    btnJugar.setDisabled(false);
                    btnJugar.setColor(Color.WHITE);
                    GameSession.selectedMapName = "bosque";
                    fondoSiguiente = fondoBosque;
                } else if (seleccionado.contains("Desierto")) {
                    labelDesc.setText("DESIERTO SECAROCAS: Recuerda mantenerte hidratado.");
                    // Lo dejamos activado para que te deje jugar
                    btnJugar.setDisabled(false);
                    btnJugar.setColor(Color.WHITE);
                    GameSession.selectedMapName = "desierto";
                    fondoSiguiente = fondoDesierto;
                } else {
                    labelDesc.setText("MUSGOCUEVA: Todo es muy negro y húmedo aquí dentro...");
                    // Lo dejamos activado para que te deje jugar
                    btnJugar.setDisabled(false);
                    btnJugar.setColor(Color.WHITE);
                    GameSession.selectedMapName = "cueva";
                    fondoSiguiente = fondoCueva;
                }

                if (fondoSiguiente != fondoMostradoActualmente) {
                    float duracion = 0.4f;
                    fondoSiguiente.getColor().a = 0;
                    fondoSiguiente.toBack();
                    fondoSiguiente.addAction(Actions.fadeIn(duracion));
                    fondoMostradoActualmente.addAction(Actions.fadeOut(duracion));
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

        //|||||||||||||||||||||||RESET ESTADISTICAS (DEVELOPER) |||||||||||||||||||||||||||||||||||||
        if (Gdx.input.isKeyPressed(com.badlogic.gdx.Input.Keys.R)) {
            resetTimer += delta;

            if (resetTimer >= 1.0f) {
                // 1. Ponemos TODA la progresión a 0
                com.tikisadventure.core.SaveManager.getProfileData().globalScore = 0;
                com.tikisadventure.core.SaveManager.getProfileData().maxWaveForest = 0;
                com.tikisadventure.core.SaveManager.getProfileData().maxWaveDesert = 0;
                com.tikisadventure.core.SaveManager.getProfileData().maxWaveCave = 0;

                // 2. Guardamos el archivo al instante
                com.tikisadventure.core.SaveManager.saveProfileData();

                Gdx.app.log("DEV", "¡Reset stats!");

                // 3. Recargamos la pantalla
                game.setScreen(new MenuMapScreen(game));
                return;
            }
        } else {
            resetTimer = 0f;
        }
        //|||||||||||||||||||||||Borrar antes de tener el juego listo||||||||||||||||||||||||||||||||||||

        // Limpiamos pantalla y actualizamos el Stage UNA sola vez
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
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
