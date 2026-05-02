package com.tikisadventure.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

public class MatchDetailsUI extends Window {

    private Stage stage;

    public MatchDetailsUI(Skin skin, Stage stage, JsonValue matchData) {
        super("Detalles de la Partida", skin);
        this.stage = stage;

        setModal(true);
        setMovable(true);
        setResizable(false);
        padTop(35);
        setSize(400, 450); // Un poco más pequeña que el historial para que se note que está "encima"
        setPosition(Math.round((stage.getWidth() - getWidth()) / 2f), Math.round((stage.getHeight() - getHeight()) / 2f));

        Table contentTable = new Table();
        contentTable.top().pad(10);

        ScrollPane scrollPane = new ScrollPane(contentTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        // --- 1. EXTRACCIÓN DE DATOS BÁSICOS ---
        long score = matchData.getLong("score");
        long stageLvl = matchData.getLong("stage");
        long wave = matchData.getLong("wave");
        long kills = matchData.getLong("total_killed");

        String charName = matchData.get("personaje") != null ? matchData.get("personaje").getString("name", "tiki") : "tiki";
        String mapId = matchData.get("mapa") != null ? matchData.get("mapa").getString("string_id", "bosque") : "bosque";

        // --- PROCESAMIENTO DEL EXTRA DATA ---
        JsonValue extraData = matchData.get("extra_data");
        // A veces Supabase devuelve el JSONB como un String que hay que volver a parsear
        if (extraData != null && extraData.isString()) {
            extraData = new JsonReader().parse(extraData.asString());
        }

        // --- 2. SECCIÓN: INFORMACIÓN GENERAL ---
        Label titleGen = new Label("--- RESUMEN ---", skin);
        titleGen.setColor(Color.CYAN);
        contentTable.add(titleGen).padBottom(5).row();

        contentTable.add(new Label("Personaje: " + charName.toUpperCase(), skin)).left().row();
        contentTable.add(new Label("Mapa: " + mapId.toUpperCase(), skin)).left().row();
        contentTable.add(new Label("Puntuación: " + score, skin)).left().row();
        contentTable.add(new Label("Nivel Alcanzado: " + stageLvl + "-" + wave, skin)).left().row();
        contentTable.add(new Label("Enemigos Totales: " + kills, skin)).left().padBottom(15).row();

        if (extraData != null) {

            // --- 3. SECCIÓN: MEJORAS Y STATS ---
            JsonValue stats = extraData.get("powerup_stats");
            if (stats != null) {
                Label titleStats = new Label("--- ESTADÍSTICAS FINALES ---", skin);
                titleStats.setColor(Color.YELLOW);
                contentTable.add(titleStats).padBottom(5).row();

                contentTable.add(new Label("Vida Extra: +" + stats.getFloat("health_gained", 0), skin)).left().row();
                // Velocidad formateada a un decimal
                contentTable.add(new Label("Velocidad: " + String.format(java.util.Locale.US, "%.1f", stats.getFloat("speed", 0)), skin)).left().row();
                contentTable.add(new Label("Daño Kinético: +" + (int)(stats.getFloat("kin", 0) * 100) + "%", skin)).left().row();
                contentTable.add(new Label("Daño Explosivo: +" + (int)(stats.getFloat("exp", 0) * 100) + "%", skin)).left().row();
                contentTable.add(new Label("Daño Fuego: +" + (int)(stats.getFloat("fue", 0) * 100) + "%", skin)).left().row();
                contentTable.add(new Label("Daño Veneno: +" + (int)(stats.getFloat("ven", 0) * 100) + "%", skin)).left().row();
                // Nuevas stats
                contentTable.add(new Label("Daño Hielo: +" + (int)(stats.getFloat("hie", 0) * 100) + "%", skin)).left().row();
                contentTable.add(new Label("Daño Energía: +" + (int)(stats.getFloat("ene", 0) * 100) + "%", skin)).left().row();

                contentTable.add(new Label("Prob. Crítico: +" + (int)(stats.getFloat("crt", 0) * 100) + "%", skin)).left().row();
                contentTable.add(new Label("Suerte: +" + stats.getFloat("sue", 0), skin)).left().row();
                contentTable.add(new Label("Bonus XP: +" + (int)((stats.getFloat("xp", 1) - 1) * 100) + "%", skin)).left().padBottom(15).row();
            }

            // --- NUEVA SECCIÓN: GADGET EQUIPADO ---
            // Sacamos el nombre legible del gadget (o el ID si no hay relación)
            String gadgetName = matchData.get("gadget") != null ?
                matchData.get("gadget").getString("name", "Desconocido") :
                matchData.getString("gadget_id", "Sin gadget");

            Label titleGadget = new Label("--- GADGET EQUIPADO ---", skin);
            titleGadget.setColor(com.badlogic.gdx.graphics.Color.VIOLET); // Color morado/violeta para diferenciar
            contentTable.add(titleGadget).padBottom(5).row();

            contentTable.add(new Label(gadgetName.toUpperCase(), skin)).left().padBottom(15).row();

            // --- 4. SECCIÓN: ARMAS USADAS ---
            JsonValue weapons = extraData.get("weapons_used");
            if (weapons != null && weapons.isArray() && weapons.size > 0) {
                Label titleWeapons = new Label("--- ARSENAL EQUIPADO ---", skin);
                titleWeapons.setColor(Color.ORANGE);
                contentTable.add(titleWeapons).padBottom(5).row();

                StringBuilder wString = new StringBuilder();
                for (int i = 0; i < weapons.size; i++) {
                    wString.append("- ").append(weapons.getString(i)).append("\n");
                }
                Label wLabel = new Label(wString.toString(), skin);
                contentTable.add(wLabel).left().padBottom(10).row();
            }

            // --- 5. SECCIÓN: REGISTRO DE BAJAS ---
            JsonValue killsDetail = extraData.get("kills_detail");
            if (killsDetail != null && killsDetail.size > 0) {
                Label titleKills = new Label("--- REGISTRO DE BAJAS ---", skin);
                titleKills.setColor(Color.RED);
                contentTable.add(titleKills).padBottom(5).row();

                for (JsonValue entry = killsDetail.child; entry != null; entry = entry.next) {
                    String enemyName = entry.name;
                    int count = entry.asInt();
                    contentTable.add(new Label(enemyName.toUpperCase() + ": " + count, skin)).left().row();
                }
            }
        }

        // Añadimos el contenido central
        add(scrollPane).expand().fill().pad(10).row();

        // Botón Cerrar
        TextButton btnCerrar = new TextButton("Volver", skin);
        btnCerrar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                remove(); // Cierra esta ventana y vuelves a ver la de HistoryUI
            }
        });
        add(btnCerrar).padTop(10).padBottom(10).width(120);
    }

    public void show() {
        stage.addActor(this);
    }
}
