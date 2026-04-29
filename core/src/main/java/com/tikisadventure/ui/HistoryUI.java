package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
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

public class HistoryUI extends Window {

    private Stage stage;
    private Skin skin;
    private String username;

    private Table tabsTable;
    private Table contentTable;
    private Table listTable;

    private Array<JsonValue> allMatches;

    private Button btnRecientes;
    private Button btnMejores;
    private Button btnHazanas;

    public HistoryUI(Skin skin, Stage stage, String username) {
        super("Historial del Jugador", skin);
        this.skin = skin;
        this.stage = stage;
        this.username = username;

        setModal(true);
        setMovable(false);
        setResizable(false);
        padTop(35);

        // --- CAMBIOS DE TAMAÑO ---
        // Ventana más estrecha (480) y un poco más alta (500) para quitar márgenes feos
        setSize(480, 500);
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2f), Math.round((stage.getHeight() - getHeight()) / 2f));

        // --- TABLAS DE ESTRUCTURA ---
        tabsTable = new Table();
        contentTable = new Table();
        listTable = new Table();
        listTable.top();

        ScrollPane scrollPane = new ScrollPane(listTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        // --- DISTRIBUCIÓN PRINCIPAL ---
        add(tabsTable).fillX().padTop(5).padBottom(10).row();
        add(contentTable).fillX().padBottom(5).row();
        add(scrollPane).expand().fill().row(); // El scrollpane empuja el resto a su sitio

        TextButton btnCerrar = new TextButton("Cerrar", skin);
        btnCerrar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                remove();
            }
        });
        add(btnCerrar).padTop(15).width(120);

        listTable.add(new Label("Cargando base de datos...", skin)).center().pad(50);
    }

    public void show() {
        stage.addActor(this);
        listTable.clearChildren(); // <--- AÑADE ESTO
        listTable.add(new Label("Cargando historial...", skin)).center().pad(50);
        cargarDatos();
    }

    private void cargarDatos() {
        new ProgressRepository().obtenerHistorial(username, new AuthCallback() {
            @Override
            public void onSuccess(String responseString) {
                JsonReader reader = new JsonReader();
                JsonValue matches = reader.parse(responseString);

                allMatches = new Array<>();
                for (JsonValue match : matches) {
                    allMatches.add(match);
                }

                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        construirTabs();
                        mostrarRecientes(); // Pestaña por defecto
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
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

    private void construirTabs() {
        tabsTable.clearChildren();

        // Usamos las variables de clase en lugar de crearlas locales
        btnRecientes = new Button(skin);
        btnMejores = new Button(skin);
        btnHazanas = new Button(skin);

        TextureRegion texRecientes = Assets.getRegion("shared", "UI_assets/LastMatches");
        TextureRegion texMejores = Assets.getRegion("shared", "UI_assets/BestMatches");
        TextureRegion texHazanas = Assets.getRegion("shared", "UI_assets/Feats");

        if (texRecientes != null) {
            Image img = new Image(texRecientes);
            img.setScaling(com.badlogic.gdx.utils.Scaling.fit);
            btnRecientes.add(img).expand().fill().pad(2);
        }
        if (texMejores != null) {
            Image img = new Image(texMejores);
            img.setScaling(com.badlogic.gdx.utils.Scaling.fit);
            btnMejores.add(img).expand().fill().pad(2);
        }
        if (texHazanas != null) {
            Image img = new Image(texHazanas);
            img.setScaling(com.badlogic.gdx.utils.Scaling.fit);
            btnHazanas.add(img).expand().fill().pad(2);
        }

        tabsTable.add(btnRecientes).size(140, 40).padRight(10);
        tabsTable.add(btnMejores).size(140, 40).padRight(10);
        tabsTable.add(btnHazanas).size(140, 40);

        btnRecientes.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { mostrarRecientes(); }
        });
        btnMejores.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { mostrarMejores(); }
        });
        btnHazanas.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { mostrarHazanas(); }
        });
    }

    private void resaltarPestaña(Button pestañaActiva) {
        // Color oscuro para las inactivas y blanco puro (sin oscurecer) para la activa
        com.badlogic.gdx.graphics.Color inactivo = com.badlogic.gdx.graphics.Color.GRAY;
        com.badlogic.gdx.graphics.Color activo = com.badlogic.gdx.graphics.Color.WHITE;

        if (btnRecientes != null) btnRecientes.setColor(inactivo);
        if (btnMejores != null) btnMejores.setColor(inactivo);
        if (btnHazanas != null) btnHazanas.setColor(inactivo);

        if (pestañaActiva != null) {
            pestañaActiva.setColor(activo);
        }
    }

    private void mostrarRecientes() {
        resaltarPestaña(btnRecientes); // Resaltamos esta pestaña
        contentTable.clearChildren();
        listTable.clearChildren();

        // TEXTO EN LUGAR DE ICONO
        Label titulo = new Label("ULTIMAS PARTIDAS", skin);
        titulo.setColor(com.badlogic.gdx.graphics.Color.GREEN);
        contentTable.add(titulo).pad(10);

        if (allMatches.size == 0) {
            listTable.add(new Label("No hay partidas.", skin)).pad(20);
            return;
        }

        int max = Math.min(10, allMatches.size);
        for (int i = 0; i < max; i++) {
            listTable.add(crearBotonPartida(allMatches.get(i))).fillX().expandX().padBottom(10).row();
        }
    }

    private void mostrarMejores() {
        resaltarPestaña(btnMejores); // Resaltamos esta pestaña
        contentTable.clearChildren();
        listTable.clearChildren();

        // TEXTO EN LUGAR DE ICONO
        Label titulo = new Label("MEJORES PARTIDAS", skin);
        titulo.setColor(com.badlogic.gdx.graphics.Color.YELLOW);
        contentTable.add(titulo).pad(10);

        if (allMatches.size == 0) {
            listTable.add(new Label("No hay partidas.", skin)).pad(20);
            return;
        }

        Array<JsonValue> sortedMatches = new Array<>(allMatches);
        sortedMatches.sort(new java.util.Comparator<JsonValue>() {
            @Override
            public int compare(JsonValue a, JsonValue b) {
                return Long.compare(b.getLong("score"), a.getLong("score"));
            }
        });

        int max = Math.min(10, sortedMatches.size);
        for (int i = 0; i < max; i++) {
            listTable.add(crearBotonPartida(sortedMatches.get(i))).fillX().expandX().padBottom(10).row();
        }
    }

    private void mostrarHazanas() {
        resaltarPestaña(btnHazanas); // Resaltamos esta pestaña
        contentTable.clearChildren();
        listTable.clearChildren();

        // TEXTO EN LUGAR DE ICONO
        Label titulo = new Label("MERITOS", skin);
        titulo.setColor(com.badlogic.gdx.graphics.Color.SKY);
        contentTable.add(titulo).pad(10);

        if (allMatches.size == 0) {
            listTable.add(new Label("No hay partidas.", skin)).pad(20);
            return;
        }

        JsonValue maxScore = allMatches.get(0);
        JsonValue maxKills = allMatches.get(0);
        JsonValue maxWave = allMatches.get(0);

        for (JsonValue m : allMatches) {
            if (m.getLong("score") > maxScore.getLong("score")) maxScore = m;
            if (m.getLong("total_killed") > maxKills.getLong("total_killed")) maxKills = m;

            if (m.getLong("stage") > maxWave.getLong("stage") ||
                (m.getLong("stage") == maxWave.getLong("stage") && m.getLong("wave") > maxWave.getLong("wave"))) {
                maxWave = m;
            }
        }

        listTable.add(new Label("Mayor Puntuación:", skin)).left().padBottom(5).row();
        listTable.add(crearBotonPartida(maxScore)).fillX().expandX().padBottom(20).row();

        listTable.add(new Label("Más Enemigos Eliminados:", skin)).left().padBottom(5).row();
        listTable.add(crearBotonPartida(maxKills)).fillX().expandX().padBottom(20).row();

        listTable.add(new Label("Más Lejos Llegado (Nivel/Oleada):", skin)).left().padBottom(5).row();
        listTable.add(crearBotonPartida(maxWave)).fillX().expandX().padBottom(20).row();
    }

    private Button crearBotonPartida(final JsonValue matchData) {
        Button btn = new Button(skin);
        btn.padTop(8).padBottom(8).padLeft(5).padRight(10);

        // --- EXTRACCIÓN DE DATOS ---
        String charName = matchData.get("personaje") != null ? matchData.get("personaje").getString("name", "tiki") : "tiki";
        String mapId = matchData.get("mapa") != null ? matchData.get("mapa").getString("string_id", "bosque") : "bosque";

        // Nuevo: Extraer ID del gadget
        String gadgetId = matchData.get("gadget") != null ? matchData.get("gadget").getString("string_id", "grenade_kinetic") : "grenade_kinetic";

        long score = matchData.getLong("score");
        long stage = matchData.getLong("stage");
        long wave = matchData.getLong("wave");
        long kills = matchData.getLong("total_killed");

        // 1. Sprite Animado (Personaje)
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
        }
        if (charAnim != null) {
            btn.add(new AnimatedImage(charAnim)).size(45, 45).padLeft(5);
        }

        // 2. Icono del Gadget (NUEVO - Entre el personaje y el mapa)
        String gadgetTexturePath = "weapons_assets/Corn"; // Por defecto
        if (gadgetId.contains("explosive")) gadgetTexturePath = "weapons_assets/ShakedCola";
        else if (gadgetId.contains("fire")) gadgetTexturePath = "weapons_assets/Jalapeno";
        else if (gadgetId.contains("freeze")) gadgetTexturePath = "weapons_assets/IceCandy";
        else if (gadgetId.contains("cactus")) gadgetTexturePath = "weapons_assets/Cactus";
        else if (gadgetId.contains("sewer")) gadgetTexturePath = "weapons_assets/Sewer";

        else if (gadgetId.contains("dash")) gadgetTexturePath = "UI_assets/DashIcon";

        TextureRegion gadgetRegion = Assets.getRegion("shared", gadgetTexturePath);
        if (gadgetRegion != null) {
            btn.add(new Image(gadgetRegion)).size(35, 35).padLeft(12);
        }

        // 3. Icono del Mapa
        String mapTextureName = "ForestMatchIcon";
        if (mapId.toLowerCase().contains("desierto")) mapTextureName = "DesertMatchIcon";
        if (mapId.toLowerCase().contains("cueva")) mapTextureName = "CaveMatchIcon";

        TextureRegion mapRegion = Assets.getRegion("shared", "UI_assets/" + mapTextureName);
        if (mapRegion != null) {
            btn.add(new Image(mapRegion)).size(40, 40).padLeft(12);
        }

        // 4. Textos
        Label statsLabel = new Label(" Ptos: " + score + " | Kills: " + kills + " | Lvl: " + stage + "-" + wave, skin);
        statsLabel.setFontScale(0.95f);
        btn.add(statsLabel).expandX().left().padLeft(15);

        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                new MatchDetailsUI(skin, getStage(), matchData).show();
            }
        });

        return btn;
    }
}
