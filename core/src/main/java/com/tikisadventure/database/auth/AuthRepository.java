package com.tikisadventure.database.auth;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.tikisadventure.core.PlayerData;
import com.tikisadventure.core.SaveManager;
import com.tikisadventure.database.core.AuthCallback;
import com.tikisadventure.database.core.SupabaseClient;

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

        // Llamamos a nuestro nuevo cliente
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
        // NUEVO: Añadido el join doble para las armas -> jugador_arma(arma(string_id))
        String endpoint = "jugador?name=eq." + username + "&select=id,password,coins,total_score,jugador_personaje(character_id),jugador_arma(arma(string_id)),jugador_mapa(mapa(string_id))&limit=1";

        SupabaseClient.sendRequest(Net.HttpMethods.GET, endpoint, null, new AuthCallback() {
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

                    // NUEVO: Extraemos la lista de armas (string_id)
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

                    // NUEVO: Extraer mapas
                    boolean hasDesert = false, hasCave = false;
                    JsonValue mapasData = userData.get("jugador_mapa");
                    if (mapasData != null && mapasData.isArray()) {
                        for (JsonValue m : mapasData) {
                            JsonValue datosMapa = m.get("mapa");
                            if (datosMapa != null) {
                                String mapId = datosMapa.getString("string_id");
                                if ("desierto".equals(mapId)) hasDesert = true;
                                if ("cueva".equals(mapId)) hasCave = true;
                            }
                        }
                    }

                    // NUEVO: Construimos el paquete para devolverlo (añadiendo las armas al final unidas por un '#')
                    String packageData = id + "," + coins + "," + globalScore + "," + hasMoko + "," + hasZuki + ",";
                    packageData += String.join("#", armasNube) + "," + hasDesert + "," + hasCave;

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
