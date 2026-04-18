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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.tikisadventure.ui.CharacterPreviewActor;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.tikisadventure.entities.player.CharacterFactory;
import com.tikisadventure.entities.player.CharacterFactory;
import com.tikisadventure.core.GameSession;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class MenuMapScreen implements Screen {

    private final Game game;
    private Stage stage;
    private Skin uiSkin;

    //Gestion de fondos y actores
    private Group grupoFondos;
    private ImagenFondo fondoBosque;
    private ImagenFondo fondoDesierto;
    private ImagenFondo fondoCueva;
    private ImagenFondo fondoMostradoActualmente;

    // Elementos que necesitamos actualizar dinamicamente
    private Label labelDesc;
    private TextButton btnJugar;

    //Para el modo dios
    private boolean godMode = false;
    private TextButton customGodButton;
    private Dialog customGodDialog;

    public MenuMapScreen(Game game) {
        this.game = game;
    }

    // Clase interna para el actor
    // Permite usar actions sobre textura
    private static class ImagenFondo extends Actor {
        private Texture textura;

        public ImagenFondo(Texture textura) {
            this.textura = textura;
            //El actor debe ocupar toda la resolución virtual
            setBounds(0, 0, 800, 480);
        }

        @Override
        public void draw(Batch batch, float parentAlpha) {
            // Dibujamos respetando el color y la trasparencia del actor
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

        //Inicializar fondos
        grupoFondos = new Group();
        //Cargar texturas
        fondoBosque = new ImagenFondo(new Texture(Gdx.files.internal("Menu/fondo_bosque.png")));
        fondoDesierto = new ImagenFondo(new Texture(Gdx.files.internal("Menu/fondo_desierto.png")));
        fondoCueva = new ImagenFondo(new Texture(Gdx.files.internal("Menu/fondo_cueva.png")));

        // Añadir fondos al grupo de fondos
        grupoFondos.addActor(fondoBosque);
        grupoFondos.addActor(fondoDesierto);
        grupoFondos.addActor(fondoCueva);

        // Mostramos solo bosque de forma predeterminada
        fondoDesierto.getColor().a = 0;
        fondoCueva.getColor().a = 0;
        fondoMostradoActualmente = fondoBosque;

        // Añadimos el grupo de fondos al Stage primero para que esté detrás de la UI
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

        final SelectBox<String> selectorMapas = new SelectBox<>(estiloFijo);
        selectorMapas.setItems("Bosque Mucoso", "Desierto Secarocas (X)", "Musgocueva (X)");

        labelDesc = new Label("BOSQUE MUCOSO: El amanecer de la aventura de Tiki.", uiSkin);
        labelDesc.setWrap(true);
        labelDesc.setAlignment(com.badlogic.gdx.utils.Align.topLeft);

        columnaIzquierda.add(new Label("MAPAS", uiSkin)).padBottom(10).row();
        columnaIzquierda.add(selectorMapas).width(220).height(40).padBottom(20).row();
        columnaIzquierda.add(labelDesc).width(220).height(150).top();

        btnJugar = new TextButton("¡EMPEZAR!", uiSkin);
        TextButton btnTienda = new TextButton("TIENDA", uiSkin);

        //Boton de modo dios SI/NO------------------------------------------------------

        CheckBox godModeCheck = new CheckBox("MODO DIOS", uiSkin);

        //Boton parametros de modo dios
        customGodButton = new TextButton("Parametros", uiSkin);
        customGodButton.setVisible(false); // Empieza oculto

        godModeCheck.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                GameSession.godMode = godModeCheck.isChecked();
                customGodButton.setVisible(GameSession.godMode); // Muestra u oculta el botón
                System.out.println("Modo Dios activado: " + GameSession.godMode);
            }
        });
        columnaIzquierda.row(); // Crea una nueva fila
        columnaIzquierda.add(godModeCheck).left().bottom().pad(10);
        columnaIzquierda.row(); // Crea una nueva fila
        columnaIzquierda.add(customGodButton).left().pad(10); // Añade el nuevo botón debajo

        //Ventana de parametros del modo dios
        customGodDialog = new Dialog("Parametros", uiSkin);
        customGodDialog.setModal(true); // Bloquea el input detrás de la ventana
        customGodDialog.setMovable(false); // Evita que se pueda arrastrar
        customGodDialog.getContentTable().add().minSize(400, 250);
        TextButton closeButton = new TextButton("X", uiSkin);
        closeButton.addListener(new ClickListener() {

            @Override
            public void clicked(InputEvent event, float x, float y) {
                customGodDialog.hide(); // Oculta la ventana al pulsar la X
            }
        });
        customGodButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                customGodDialog.show(stage);
            }
        });
        // Añade el botón a la esquina superior derecha de la ventana
        customGodDialog.getTitleTable().add(closeButton).size(30, 30).padRight(8);

        // Lee los datos de las armas ya creadas del json
        JsonValue weaponData = new JsonReader().parse(Gdx.files.internal("data/weapons_config.json"));
        Array<String> weaponNames = new Array<>();

        //Creamos un mapa para vincular el Nombre Visible con el ID del JSON
        final ObjectMap<String, String> weaponNameToIdMap = new ObjectMap<>();

        for (JsonValue weaponEntry : weaponData.get("weapons")) {
            // weaponEntry.name obtiene la CLAVE del nodo en LibGDX (ej: "pistol")
            String weaponId = weaponEntry.name;
            // Obtenemos el nombre visual para la UI (ej: "Pistola Láser")
            String displayName = weaponEntry.getString("name", weaponId);

            weaponNames.add(displayName);
            weaponNameToIdMap.put(displayName, weaponId);
        }

        // Crea el SelectBox
        final SelectBox<String> weaponSelector = new SelectBox<>(uiSkin);
        weaponSelector.setItems(weaponNames);

        // Añade un listener para guardar el arma seleccionada
        weaponSelector.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String selectedWeaponName = weaponSelector.getSelected();
                // AQUÍ ESTÁ EL CAMBIO CLAVE: Guardamos el ID real buscando en nuestro mapa
                GameSession.godModeWeaponId = weaponNameToIdMap.get(selectedWeaponName);
            }
        });

        // Cogemos un arma predeterminada, pasándola también por el mapa
        GameSession.godModeWeaponId = weaponNameToIdMap.get(weaponSelector.getSelected());

        // Añade el selector a la ventana de diálogo
        customGodDialog.getContentTable().clear(); // Limpia el tamaño mínimo que pusimos antes
        customGodDialog.getContentTable().add(new Label("Arma inicial:", uiSkin)).padRight(10);
        customGodDialog.getContentTable().add(weaponSelector).width(250);



        // Seleccion de personaje ----------------------------------------------------

        Table charTable = new Table();
        final ButtonGroup<Button> group = new ButtonGroup<>();

        JsonValue characterData = new JsonReader().parse(Gdx.files.internal("data/player_config.json"));
        for (JsonValue charEntry : characterData.get("characters")) {
            final String id = charEntry.getString("id");

            Animation<TextureRegion> idleAnim = CharacterFactory.getCharacterIdleAnimation(id);
            Button btn = new Button(uiSkin);
            btn.add(new CharacterPreviewActor(idleAnim)).size(48, 48);

            btn.addListener(new ChangeListener() {
                @Override
                public void changed(ChangeEvent event, Actor actor) {
                    if (btn.isChecked()) {
                        GameSession.selectedCharacterId = id;
                        for (Button b : group.getButtons()) {
                            b.setColor(b.isChecked() ? Color.WHITE : new Color(0.5f, 0.5f, 0.5f, 1f));
                        }
                    }
                }
            });
            group.add(btn);
            charTable.add(btn).size(64, 64).pad(10);
        }

        // Establecer seleccionado por defecto
        group.getButtons().first().setChecked(true);
        for (Button b : group.getButtons()) {
            b.setColor(b.isChecked() ? Color.WHITE : new Color(0.5f, 0.5f, 0.5f, 1f));
        }

        // Colocamos el selector debajo del contenido principal, arriba del botón volver
        mainTable.row();
        mainTable.add(charTable).colspan(3).padTop(20);
        mainTable.row();

        mainTable.add(columnaIzquierda).expand().left();
        mainTable.add(btnJugar).size(180, 80).expand().center();
        mainTable.add(btnTienda).size(120, 120).expand().right();
        mainTable.row();

        TextButton btnVolver = new TextButton("Volver", uiSkin);
        mainTable.add(btnVolver).colspan(3).right().padTop(20);


        //Cambio de fondo difuminado ---------------------------------------------
        selectorMapas.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                String seleccionado = selectorMapas.getSelected();

                //Definir nuevo fondo:
                ImagenFondo fondoSiguiente;
                if (seleccionado.contains("Bosque")) {
                        labelDesc.setText("BOSQUE MUCOSO: El amanecer de la aventura de Tiki.");
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

                // Cross-fade o difuminado:
                if (fondoSiguiente != fondoMostradoActualmente) {
                    float duracion = 0.4f; // Tiempo que tarda

                    //Asegurar que el fondo siguiente esta detras
                    fondoSiguiente.getColor().a = 0;
                    fondoSiguiente.toBack(); //Al fondo deo group

                    //fadeIn del nuevo fondo
                    fondoSiguiente.addAction(Actions.fadeIn(duracion));
                    //fadeOut del viejo fondo
                    fondoMostradoActualmente.addAction(Actions.fadeOut(duracion));
                    //Actualizamos
                    fondoMostradoActualmente = fondoSiguiente;
                }
            }
        });

        // Listeners de botones ----------------------------------------------
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

        stage.addActor(mainTable);    }

    //Render simplificado ----------------------------------------------
    @Override
    public void render(float delta) {

        // Limpiamos pantalla
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta); //Actualizar actions
        stage.draw(); //Dibujar el group de fondos y la UI encima en orden correcto:
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void hide() { dispose(); }
    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (uiSkin != null) uiSkin.dispose();

        //Dispose de las nuevas texturas
        if (fondoBosque != null) fondoBosque.textura.dispose();
        if (fondoDesierto != null) fondoDesierto.textura.dispose();
        if (fondoCueva != null) fondoCueva.textura.dispose();
    }
}
