package com.tikisadventure.database.auth;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.tikisadventure.core.PlayerData;
import com.tikisadventure.core.SaveManager;
import com.tikisadventure.database.core.AuthCallback;
import com.tikisadventure.database.core.SupabaseClient;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class AuthRepository {

    public void registrarJugador(final String username, String password, final AuthCallback callback) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String creationDate = sdf.format(new Date());

        PlayerData local = SaveManager.getLocalProfile();
        int currentCoins = local.wasLinkedToCloud ? 0 : local.coins;
        int currentScore = local.wasLinkedToCloud ? 0 : local.totalScore;

        String jsonBody = "{\"name\":\"" + username + "\", \"password\":\"" + password + "\", \"creation_date\":\"" + creationDate + "\", \"coins\":" + currentCoins + ", \"total_score\":" + currentScore + "}";

        SupabaseClient.sendRequest(Net.HttpMethods.POST, "jugador", jsonBody, new AuthCallback() {
            @Override
            public void onSuccess(String responseString) {
                callback.onSuccess("Cuenta creada exitosamente");
            }

            @Override
            public void onError(String errorMessage) {
                if (errorMessage.contains("409")) {
                    callback.onError("El nombre de usuario ya existe.");
                } else {
                    callback.onError("Error al crear cuenta: " + errorMessage);
                }
            }
        });
    }

    public void iniciarSesion(final String username, final String password, final AuthCallback callback) {
        // --- SOLUCIÓN ERROR 400 ---
        // Codificamos la URL para que cambie los espacios por "%20" y evite que la petición HTTP falle.
        String encodedUsername = username;
        try {
            encodedUsername = URLEncoder.encode(username, "UTF-8").replace("+", "%20");
        } catch (Exception e) {
            encodedUsername = username.replace(" ", "%20"); // Respaldo simple
        }

        String endpoint = "jugador?name=eq." + encodedUsername + "&select=id,password,coins,total_score,custom_weapons,jugador_personaje(character_id),jugador_arma(arma(string_id)),jugador_mapa(mapa(string_id)),jugador_gadget(gadget(string_id))&limit=1";
        SupabaseClient.sendRequest(Net.HttpMethods.GET, endpoint, null, new AuthCallback() {
            // ... (el código del medio sigue igual hasta que llegas a extraer el JSON) ...
            @Override
            public void onSuccess(String responseString) {
                JsonReader reader = new JsonReader();
                JsonValue root = reader.parse(responseString);

                if (root.size == 0) {
                    callback.onError("El usuario no existe.");
                    return;
                }

                JsonValue userData = root.get(0);
                String dbPassword = userData.getString("password");

                if (dbPassword.equals(password)) {
                    // AQUÍ ESTÁN LAS VARIABLES QUE SE TE HABÍAN BORRADO
                    long id = userData.getLong("id", -1);
                    long coins = userData.getLong("coins", 0);
                    long globalScore = userData.getLong("total_score", 0);

                    boolean hasMoko = false, hasZuki = false;
                    JsonValue personajes = userData.get("jugador_personaje");
                    if (personajes != null && personajes.isArray()) {
                        for (JsonValue p : personajes) {
                            int charId = p.getInt("character_id", -1);
                            if (charId == 2) hasMoko = true;
                            if (charId == 3) hasZuki = true;
                        }
                    }

                    com.badlogic.gdx.utils.Array<String> armasNube = new com.badlogic.gdx.utils.Array<>();
                    JsonValue armasData = userData.get("jugador_arma");
                    if (armasData != null && armasData.isArray()) {
                        for (JsonValue vinculo : armasData) {
                            JsonValue datosArma = vinculo.get("arma");
                            if (datosArma != null) {
                                armasNube.add(datosArma.getString("string_id"));
                            }
                        }
                    }

                    boolean hasDesert = false, hasCastillo = false;
                    JsonValue mapasData = userData.get("jugador_mapa");
                    if (mapasData != null && mapasData.isArray()) {
                        for (JsonValue m : mapasData) {
                            JsonValue datosMapa = m.get("mapa");
                            if (datosMapa != null) {
                                String mapId = datosMapa.getString("string_id");
                                if ("desierto".equals(mapId)) hasDesert = true;
                                if ("castillo".equals(mapId)) hasCastillo = true;
                            }
                        }
                    }

                    com.badlogic.gdx.utils.Array<String> gadgetsNube = new com.badlogic.gdx.utils.Array<>();
                    JsonValue gadgetsData = userData.get("jugador_gadget");
                    if (gadgetsData != null && gadgetsData.isArray()) {
                        for (JsonValue vinculo : gadgetsData) {
                            JsonValue datosGadget = vinculo.get("gadget");
                            if (datosGadget != null) {
                                gadgetsNube.add(datosGadget.getString("string_id"));
                            }
                        }
                    }

                    // --- 2. EXTRAEMOS EL JSON CON LA CLAVE 'custom_weapons' ---
                    String armasCustomNube = "{}";
                    if (userData.has("custom_weapons")) {
                        JsonValue armasCustomNode = userData.get("custom_weapons");
                        if (armasCustomNode != null && !armasCustomNode.isNull()) {

                            // --- ¡LA SOLUCIÓN! ---
                            // En lugar de .toString(), usamos .toJson(OutputType.json)
                            // Esto evita que LibGDX pegue el nombre "custom_weapons: " al principio del texto
                            if (armasCustomNode.isString()) {
                                armasCustomNube = armasCustomNode.asString();
                            } else {
                                armasCustomNube = armasCustomNode.toJson(com.badlogic.gdx.utils.JsonWriter.OutputType.json);
                            }

                            if (armasCustomNube.equals("null") || armasCustomNube.trim().isEmpty()) {
                                armasCustomNube = "{}";
                            }
                        }
                    }

                    StringBuilder gadgetsBuilder = new StringBuilder();
                    for (int i = 0; i < gadgetsNube.size; i++) {
                        gadgetsBuilder.append(gadgetsNube.get(i));
                        if (i < gadgetsNube.size - 1) gadgetsBuilder.append("#");
                    }

                    StringBuilder armasBuilder = new StringBuilder();
                    for (int i = 0; i < armasNube.size; i++) {
                        armasBuilder.append(armasNube.get(i));
                        if (i < armasNube.size - 1) armasBuilder.append("#");
                    }

                    // --- 3. EMPAQUETAMOS USANDO "|||" PARA NO ROMPER EL JSON ---
                    String packageData = id + "|||" + coins + "|||" + globalScore + "|||" + hasMoko + "|||" + hasZuki + "|||";
                    packageData += armasBuilder.toString() + "|||" + hasDesert + "|||" + hasCastillo + "|||" + gadgetsBuilder.toString() + "|||" + armasCustomNube;

                    callback.onSuccess(packageData);

                } else {
                    callback.onError("Contraseña incorrecta.");
                }
            }
            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }
}
