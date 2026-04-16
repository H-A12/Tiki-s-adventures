package com.tikisadventure.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.tikisadventure.core.GameSession;
import com.tikisadventure.entities.player.CharacterFactory;
import com.tikisadventure.ui.CharacterPreviewActor;

public class MenuMapScreen implements Screen {
    private final Game game;
    private Stage stage;
    private Skin uiSkin;

    private Group grupoFondos;
    private ImagenFondo fondoBosque, fondoDesierto, fondoCueva, fondoMostradoActualmente;
    private Label labelDesc;
    private TextButton btnJugar;

    // Nueva textura para el fading
    private Texture blackTexture;

    public MenuMapScreen(Game game) {
        this.game = game;

        // Creamos la textura aquí mismo
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        this.blackTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    @Override
    public void show() {
        stage = new Stage(new FitViewport(800, 480));
        Gdx.input.setInputProcessor(stage);

        uiSkin = new Skin(Gdx.files.internal("uiskin.json"));

        // 1. CONFIGURACIÓN DE FONDOS
        grupoFondos = new Group();
        fondoBosque = new ImagenFondo(new Texture(Gdx.files.internal("Menu/MenuMapas/fondo_bosque.png")));
        fondoDesierto = new ImagenFondo(new Texture(Gdx.files.internal("Menu/MenuMapas/fondo_desierto.png")));
        fondoCueva = new ImagenFondo(new Texture(Gdx.files.internal("Menu/MenuMapas/fondo_cueva.png")));

        fondoDesierto.getColor().a = 0;
        fondoCueva.getColor().a = 0;
        fondoBosque.getColor().a = 1;

        grupoFondos.addActor(fondoBosque);
        grupoFondos.addActor(fondoDesierto);
        grupoFondos.addActor(fondoCueva);
        fondoMostradoActualmente = fondoBosque;
        stage.addActor(grupoFondos);

        // 2. CREACIÓN DE LA INTERFAZ
        crearTablaInterfaz();

        // 3. CREAR EL TELÓN AL FINAL (Para que esté encima de todo)
        final Image telonInicial = new Image(blackTexture);
        telonInicial.setSize(stage.getWidth(), stage.getHeight());
        telonInicial.setColor(Color.BLACK); // Empezamos en negro sólido

        // IMPORTANTE: Evita que el usuario toque botones mientras el telón desaparece
        telonInicial.setTouchable(Touchable.enabled);

        stage.addActor(telonInicial);
        telonInicial.toFront(); // Doble seguridad: lo mandamos al frente

        // 4. ANIMACIÓN LARGA
        telonInicial.addAction(Actions.sequence(
            Actions.delay(0.1f),
            Actions.fadeOut(0.5f),
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    // Al terminar el fade, permitimos clics en el menú
                    telonInicial.setTouchable(Touchable.disabled);
                }
            }),
            Actions.removeActor()      // Lo eliminamos para ahorrar memoria
        ));
    }

    private void crearTablaInterfaz() {
        Table mainTable = new Table();
        mainTable.setFillParent(true);

        // --- COLUMNA IZQUIERDA (MAPAS) ---
        Table tablaMapas = new Table();
        final ButtonGroup<Button> grupoMapas = new ButtonGroup<>();
        final Button btnBosque = crearBotonMapa("Menu/MenuMapas/icon_bosque.png");
        final Button btnDesierto = crearBotonMapa("Menu/MenuMapas/icon_desierto.png");
        final Button btnCueva = crearBotonMapa("Menu/MenuMapas/icon_cueva.png");

        grupoMapas.add(btnBosque, btnDesierto, btnCueva);
        btnBosque.setChecked(true);
        btnBosque.setScale(1.2f); // Escala inicial

        labelDesc = new Label("BOSQUE: Peligros y tesoros ocultos.", uiSkin);
        labelDesc.setWrap(true);

        tablaMapas.add(new Label("SELECCIONA MAPA", uiSkin)).padBottom(20).center().row();
        tablaMapas.add(btnBosque).size(80, 80).padBottom(10).center().row();
        tablaMapas.add(btnDesierto).size(80, 80).padBottom(10).center().row();
        tablaMapas.add(btnCueva).size(80, 80).padBottom(20).center().row();
        tablaMapas.add(labelDesc).width(200).center();

        // Listeners de Mapas (Corregidos con tus fondos)
        btnBosque.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (btnBosque.isChecked()) {
                    actualizarSeleccion(fondoBosque, "BOSQUE: Peligros y tesoros.");
                    btnBosque.addAction(Actions.scaleTo(1.2f, 1.2f, 0.1f));
                } else btnBosque.addAction(Actions.scaleTo(1.0f, 1.0f, 0.1f));
            }
        });
        btnDesierto.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (btnDesierto.isChecked()) {
                    actualizarSeleccion(fondoDesierto, "DESIERTO: Bloqueado.");
                    btnDesierto.addAction(Actions.scaleTo(1.2f, 1.2f, 0.1f));
                } else btnDesierto.addAction(Actions.scaleTo(1.0f, 1.0f, 0.1f));
            }
        });
        btnCueva.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                if (btnCueva.isChecked()) {
                    actualizarSeleccion(fondoCueva, "CUEVA: Bloqueado.");
                    btnCueva.addAction(Actions.scaleTo(1.2f, 1.2f, 0.1f));
                } else btnCueva.addAction(Actions.scaleTo(1.0f, 1.0f, 0.1f));
            }
        });

        // --- SELECTOR PERSONAJES ---
        Table charTable = new Table();
        final ButtonGroup<Button> groupChars = new ButtonGroup<>();
        JsonValue characterData = new JsonReader().parse(Gdx.files.internal("data/player_config.json"));
        for (JsonValue charEntry : characterData.get("characters")) {
            final String id = charEntry.getString("id");
            Animation<TextureRegion> idleAnim = CharacterFactory.getCharacterIdleAnimation(id);
            Button btnChar = new Button(uiSkin);
            btnChar.add(new CharacterPreviewActor(idleAnim)).size(48, 48);
            btnChar.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (btnChar.isChecked()) {
                        GameSession.selectedCharacterId = id;
                        for (Button b : groupChars.getButtons()) b.setColor(b.isChecked() ? Color.WHITE : new Color(0.5f, 0.5f, 0.5f, 1f));
                    }
                }
            });
            groupChars.add(btnChar);
            charTable.add(btnChar).size(64, 64).pad(10);
        }
        if (groupChars.getButtons().size > 0) groupChars.getButtons().first().setChecked(true);

        // --- BOTONES ACCIÓN ---
        btnJugar = new TextButton("¡EMPEZAR!", uiSkin);
        btnJugar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!btnJugar.isDisabled()) {
                    // EJECUTAR FADING HACIA NEGRO ANTES DE CAMBIAR
                    ejecutarFading(false, new Runnable() {
                        @Override
                        public void run() {
                            game.setScreen(new GameScreen(game));
                        }
                    });
                }
            }
        });

        TextButton btnVolver = new TextButton("Volver", uiSkin);
        btnVolver.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // 1. Bloqueamos toques para evitar clics dobles
                btnVolver.setTouchable(Touchable.disabled);

                // 2. Fundido a negro (false = salir)
                ejecutarFading(false, new Runnable() {
                    @Override
                    public void run() {
                        // SOLO cambiamos de pantalla cuando ya está todo en negro
                        game.setScreen(new MenuScreen(game));
                    }
                });
            }
        });

        // Montaje de Tabla
        mainTable.add(tablaMapas).width(250).padLeft(40).padTop(20).top();
        mainTable.add(btnJugar).size(180, 80).expandX().center().padTop(60);
        mainTable.row();
        mainTable.add(charTable).colspan(2).expandY().center().padBottom(40);
        stage.addActor(mainTable);

        // Botón volver independiente
        btnVolver.setSize(120, 50);
        btnVolver.setPosition(stage.getWidth() - btnVolver.getWidth() - 40, 40);
        stage.addActor(btnVolver);

        mainTable.layout();
        btnBosque.setOrigin(Align.center);
        btnDesierto.setOrigin(Align.center);
        btnCueva.setOrigin(Align.center);
    }

    private void ejecutarFading(boolean entrar, final Runnable accionAlTerminar) {
        final Image fadeOverlay = new Image(blackTexture);
        fadeOverlay.setSize(stage.getWidth(), stage.getHeight());
        fadeOverlay.setPosition(0, 0);
        fadeOverlay.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);

        fadeOverlay.getColor().a = entrar ? 1f : 0f;
        float alphaDestino = entrar ? 0f : 1f;

        fadeOverlay.addAction(Actions.sequence(
            Actions.alpha(alphaDestino, 0.8f),
            // Añadimos un delay mínimo de seguridad para que el negro sea total
            Actions.delay(0.05f),
            Actions.run(new Runnable() {
                @Override
                public void run() {
                    if (accionAlTerminar != null) {
                        // Si hay una acción (cambio de pantalla), la ejecutamos
                        accionAlTerminar.run();
                        // OJO: No removemos el fadeOverlay aquí si vamos a cambiar de pantalla.
                        // Al cambiar de pantalla, esta instancia de MenuMapScreen se destruye sola.
                    } else {
                        // Si no hay acción (estamos entrando), entonces sí lo quitamos
                        fadeOverlay.remove();
                    }
                }
            })
        ));
        stage.addActor(fadeOverlay); // Se añade al final, quedando por encima de la UI
    }

    private void actualizarSeleccion(ImagenFondo siguiente, String desc) {
        labelDesc.setText(desc);
        boolean bloqueado = desc.contains("Bloqueado");
        btnJugar.setDisabled(bloqueado);
        btnJugar.setColor(bloqueado ? new Color(1, 1, 1, 0.5f) : Color.WHITE);

        if (siguiente != fondoMostradoActualmente) {
            siguiente.toFront();
            siguiente.addAction(Actions.fadeIn(0.4f));
            fondoMostradoActualmente.addAction(Actions.fadeOut(0.4f));
            fondoMostradoActualmente = siguiente;
        }
    }

    private Button crearBotonMapa(String ruta) {
        Texture textura = new Texture(Gdx.files.internal(ruta));
        textura.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        TextureRegionDrawable draw = new TextureRegionDrawable(new TextureRegion(textura));
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        style.imageUp = draw;
        ImageButton btn = new ImageButton(style);
        btn.setTransform(true);
        btn.setOrigin(40, 40);
        return btn;
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    private static class ImagenFondo extends Actor {
        private Texture textura;
        public ImagenFondo(Texture t) { this.textura = t; setBounds(0, 0, 800, 480); }
        @Override
        public void draw(Batch batch, float alpha) {
            batch.setColor(getColor().r, getColor().g, getColor().b, getColor().a * alpha);
            batch.draw(textura, getX(), getY(), getWidth(), getHeight());
            batch.setColor(Color.WHITE);
        }
    }

    @Override public void resize(int w, int h) { stage.getViewport().update(w, h, true); }
    @Override public void hide() { }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void dispose() {
        stage.dispose();
        uiSkin.dispose();
        fondoBosque.textura.dispose();
        fondoDesierto.textura.dispose();
        fondoCueva.textura.dispose();
        if (blackTexture != null) blackTexture.dispose();
    }
}
