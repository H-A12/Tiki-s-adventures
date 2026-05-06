package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.tikisadventure.core.Assets;
import com.tikisadventure.database.core.AuthCallback;
import com.tikisadventure.database.progress.ProgressRepository;

public class LeaderboardUI extends Window {

    private Stage stage;
    private Skin skin;

    private Table tabsTable;
    private Table contentTable;
    private Table listTable;

    private Button btnBosque;
    private Button btnDesierto;
    private Button btnCueva;

    public LeaderboardUI(Skin skin, Stage stage) {
        super("TIKIRANKING", skin);
        this.skin = skin;
        this.stage = stage;

        setModal(true);
        setMovable(false);
        setResizable(false);
        padTop(35);

        setSize(480, 500);
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2f), Math.round((stage.getHeight() - getHeight()) / 2f));

        tabsTable = new Table();
        contentTable = new Table();
        listTable = new Table();
        listTable.top();

        ScrollPane scrollPane = new ScrollPane(listTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        add(tabsTable).fillX().padTop(5).padBottom(10).row();
        add(contentTable).fillX().padBottom(5).row();
        add(scrollPane).expand().fill().row();

        TextButton btnCerrar = new TextButton("Cerrar", skin);
        btnCerrar.addListener(new Assets.HoverCursorListener());
        btnCerrar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                remove();
            }
        });
        add(btnCerrar).padTop(15).width(120);

        construirTabs();
    }

    public void show() {
        stage.addActor(this);
        cargarDatos("bosque", btnBosque, "TOP 50: BOSQUE", Color.GREEN);
    }

    private void construirTabs() {
        tabsTable.clearChildren();

        btnBosque = new Button(skin);
        btnDesierto = new Button(skin);
        btnCueva = new Button(skin);

        TextureRegion texBosque = Assets.getRegion("shared", "UI_assets/ForestMatchIcon");
        TextureRegion texDesierto = Assets.getRegion("shared", "UI_assets/DesertMatchIcon");
        TextureRegion texCueva = Assets.getRegion("shared", "UI_assets/CaveMatchIcon");

        if (texBosque != null) {
            Image img = new Image(texBosque);
            img.setScaling(com.badlogic.gdx.utils.Scaling.fit);
            btnBosque.add(img).expand().fill().pad(2);
        }
        if (texDesierto != null) {
            Image img = new Image(texDesierto);
            img.setScaling(com.badlogic.gdx.utils.Scaling.fit);
            btnDesierto.add(img).expand().fill().pad(2);
        }
        if (texCueva != null) {
            Image img = new Image(texCueva);
            img.setScaling(com.badlogic.gdx.utils.Scaling.fit);
            btnCueva.add(img).expand().fill().pad(2);
        }

        tabsTable.add(btnBosque).size(140, 40).padRight(10);
        tabsTable.add(btnDesierto).size(140, 40).padRight(10);
        tabsTable.add(btnCueva).size(140, 40);

        btnBosque.addListener(new Assets.HoverCursorListener());
        btnBosque.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                cargarDatos("bosque", btnBosque, "TOP 50: BOSQUE", Color.GREEN);
            }
        });
        btnDesierto.addListener(new Assets.HoverCursorListener());
        btnDesierto.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                cargarDatos("desierto", btnDesierto, "TOP 50: DESIERTO", Color.YELLOW);
            }
        });
        btnCueva.addListener(new Assets.HoverCursorListener());
        btnCueva.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                cargarDatos("cueva", btnCueva, "TOP 50: CUEVA", Color.PURPLE);
            }
        });
    }

    private void cargarDatos(final String mapId, Button botonActivo, final String mapName, final Color colorName) {
        resaltarPestaña(botonActivo);
        contentTable.clearChildren();
        listTable.clearChildren();

        Label titulo = new Label(mapName, skin);
        titulo.setColor(colorName);
        contentTable.add(titulo).pad(10);

        listTable.add(new Label("Cargando base de datos...", skin)).center().pad(50);

        new ProgressRepository().obtenerLeaderboard(mapId, new AuthCallback() {
            @Override
            public void onSuccess(String responseString) {
                JsonReader reader = new JsonReader();
                final JsonValue matches = reader.parse(responseString);

                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        listTable.clearChildren();
                        if (matches.size == 0) {
                            listTable.add(new Label("Aún no hay partidas en este mapa.", skin)).pad(20);
                            return;
                        }

                        int rank = 1;
                        for (JsonValue match : matches) {
                            listTable.add(crearBotonPartida(match, rank)).fillX().expandX().padBottom(10).row();
                            rank++;
                        }
                    }
                });
            }

            @Override
            public void onError(final String errorMessage) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        listTable.clearChildren();
                        listTable.add(new Label("Error de red: " + errorMessage, skin)).center();
                    }
                });
            }
        });
    }

    private void resaltarPestaña(Button pestañaActiva) {
        Color inactivo = Color.GRAY;
        Color activo = Color.WHITE;

        if (btnBosque != null) btnBosque.setColor(inactivo);
        if (btnDesierto != null) btnDesierto.setColor(inactivo);
        if (btnCueva != null) btnCueva.setColor(inactivo);

        if (pestañaActiva != null) {
            pestañaActiva.setColor(activo);
        }
    }

    private Button crearBotonPartida(final JsonValue matchData, int rank) {
        Button btn = new Button(skin);
        btn.padTop(8).padBottom(8).padLeft(5).padRight(10);

        // --- EXTRACCIÓN DE DATOS ---
        String playerName = matchData.get("jugador") != null ? matchData.get("jugador").getString("name", "Desconocido") : "Desconocido";
        String charName = matchData.get("personaje") != null ? matchData.get("personaje").getString("name", "tiki") : "tiki";
        String gadgetId = matchData.get("gadget") != null ? matchData.get("gadget").getString("string_id", "grenade_kinetic") : "grenade_kinetic";

        long score = matchData.getLong("score");
        long stage = matchData.getLong("stage");
        long wave = matchData.getLong("wave");

        // --- ESTRUCTURA DEL BOTÓN ---
        // 1. Número de ranking (Oro, Plata, Bronce, o normal)
        Label rankLabel = new Label("#" + rank, skin);
        if(rank == 1) rankLabel.setColor(Color.GOLD);
        else if(rank == 2) rankLabel.setColor(Color.LIGHT_GRAY);
        else if(rank == 3) rankLabel.setColor(Color.CORAL);
        btn.add(rankLabel).padRight(10).width(30);

        // 2. Columna Izquierda: Nombre del jugador y Sprites
        Table leftCol = new Table();
        Label nameLabel = new Label(playerName.toUpperCase(), skin);
        nameLabel.setColor(Color.CYAN);
        leftCol.add(nameLabel).left().padBottom(2).row();

        Table spriteRow = new Table();
        // 2a. Sprite Animado (Personaje)
        String rutaSprite = "player_assets/" + charName.toLowerCase() + "/idle";
        TextureRegion atlasRegion = Assets.getRegion("shared", rutaSprite);
        Animation<TextureRegion> charAnim = null;
        if (atlasRegion != null) {
            int frameSize = 16;
            int frameCount = atlasRegion.getRegionWidth() / frameSize;
            TextureRegion[] frames = new TextureRegion[frameCount];
            for (int i = 0; i < frameCount; i++) {
                frames[i] = new TextureRegion(atlasRegion, i * frameSize, 0, frameSize, frameSize);
            }
            charAnim = new Animation<>(0.15f, frames);
            charAnim.setPlayMode(Animation.PlayMode.LOOP);
            spriteRow.add(new AnimatedImage(charAnim)).size(35, 35).padRight(10);
        }

        // 2b. Icono del Gadget
        String gadgetTexturePath = "weapons_assets/Corn";
        if (gadgetId.contains("explosive")) gadgetTexturePath = "weapons_assets/ShakedCola";
        else if (gadgetId.contains("fire")) gadgetTexturePath = "weapons_assets/Jalapeno";
        else if (gadgetId.contains("freeze")) gadgetTexturePath = "weapons_assets/IceCandy";
        else if (gadgetId.contains("cactus")) gadgetTexturePath = "weapons_assets/Sock";
        else if (gadgetId.contains("sewer")) gadgetTexturePath = "weapons_assets/Sewer";
        else if (gadgetId.contains("sheel")) gadgetTexturePath = "weapons_assets/MagicSheel";
        else if (gadgetId.contains("scarecrow")) gadgetTexturePath = "weapons_assets/Scarecrow";
        else if (gadgetId.contains("turret")) gadgetTexturePath = "weapons_assets/Turret";

        else if (gadgetId.contains("dash")) gadgetTexturePath = "UI_assets/DashIcon";

        TextureRegion gadgetRegion = Assets.getRegion("shared", gadgetTexturePath);
        if (gadgetRegion != null) {
            spriteRow.add(new Image(gadgetRegion)).size(25, 25);
        }

        leftCol.add(spriteRow).left();
        btn.add(leftCol).expandX().left();

        // 3. Puntuación (Centro/Derecha)
        Label scoreLabel = new Label("Ptos: " + score, skin);
        scoreLabel.setColor(Color.YELLOW);
        btn.add(scoreLabel).padRight(20);

        // 4. Lvl/Oleada (Derecha del todo)
        Label stageLabel = new Label("Lvl: " + stage + "-" + wave, skin);
        btn.add(stageLabel).right();

        // --- LISTENER (Reutilizamos la MatchDetailsUI existente) ---
        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                new MatchDetailsUI(skin, getStage(), matchData).show();
            }
        });

        return btn;
    }
}
