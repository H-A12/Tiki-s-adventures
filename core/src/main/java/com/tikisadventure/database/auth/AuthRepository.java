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
        String endpoint = "jugador?name=eq." + username + "&select=id,password,coins,total_score,jugador_personaje(character_id)&limit=1";

        // Llamamos a nuestro nuevo cliente (Pasamos null como JSON porque es un GET)
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

                    boolean hasMoko = false;
                    boolean hasZuki = false;
                    JsonValue personajes = userData.get("jugador_personaje");
                    if (personajes != null && personajes.isArray()) {
                        for (JsonValue p : personajes) {
                            int charId = p.getInt("character_id", -1);
                            if (charId == 2) hasMoko = true;
                            if (charId == 3) hasZuki = true;
                        }
                    }

                    callback.onSuccess(id + "," + coins + "," + globalScore + "," + hasMoko + "," + hasZuki);
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
