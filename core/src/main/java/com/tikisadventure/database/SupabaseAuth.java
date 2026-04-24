package com.tikisadventure.database;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class SupabaseAuth {

    private final Json json;

    public SupabaseAuth() {
        this.json = new Json();
    }

    // --- REGISTRO DE USUARIO ---
    public void registrarJugador(final String username, String password, final AuthCallback callback) {
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();

        // Generar fecha actual en formato ISO 8601 (compatible con PostgreSQL)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String creationDate = sdf.format(new Date());

        // Crear el JSON con los datos para la tabla 'jugador'
        String jsonBody = "{\"name\":\"" + username + "\", \"password\":\"" + password + "\", \"creation_date\":\"" + creationDate + "\"}";

        Net.HttpRequest httpRequest = requestBuilder.newRequest()
            .method(Net.HttpMethods.POST)
            .url(DatabaseConfig.SUPABASE_URL + "jugador")
            .header("apikey", DatabaseConfig.SUPABASE_KEY)
            .header("Authorization", "Bearer " + DatabaseConfig.SUPABASE_KEY)
            .header("Content-Type", "application/json")
            .header("Prefer", "return=representation") // Para que nos devuelva los datos insertados
            .content(jsonBody)
            .build();

        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                int statusCode = httpResponse.getStatus().getStatusCode();
                final String responseString = httpResponse.getResultAsString();

                // Las respuestas HTTP llegan en un hilo secundario.
                // Para tocar la interfaz, VOLVEMOS al hilo principal de libGDX
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (statusCode == 201) { // 201 = Created
                            callback.onSuccess("Cuenta creada exitosamente");
                        } else if (statusCode == 409) { // 409 = Conflict (el UNIQUE de la base de datos saltó)
                            callback.onError("El nombre de usuario ya existe.");
                        } else {
                            callback.onError("Error al crear cuenta: " + statusCode);
                        }
                    }
                });
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError("Error de conexión: " + t.getMessage());
                    }
                });
            }

            @Override
            public void cancelled() {
                // No implementado
            }
        });
    }

    // --- INICIO DE SESIÓN ---
    public void iniciarSesion(final String username, final String password, final AuthCallback callback) {
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();

        // Hacemos un GET buscando al jugador por nombre. limit=1 optimiza la búsqueda.
        String url = DatabaseConfig.SUPABASE_URL + "jugador?name=eq." + username + "&select=*&limit=1";

        Net.HttpRequest httpRequest = requestBuilder.newRequest()
            .method(Net.HttpMethods.GET)
            .url(url)
            .header("apikey", DatabaseConfig.SUPABASE_KEY)
            .header("Authorization", "Bearer " + DatabaseConfig.SUPABASE_KEY)
            .build();

        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                int statusCode = httpResponse.getStatus().getStatusCode();
                String responseString = httpResponse.getResultAsString();

                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        if (statusCode == 200) {
                            JsonReader reader = new JsonReader();
                            JsonValue root = reader.parse(responseString);

                            // Supabase devuelve un array []. Si está vacío, el usuario no existe.
                            if (root.size == 0) {
                                callback.onError("El usuario no existe.");
                                return;
                            }

                            // Obtenemos el primer (y único) resultado
                            JsonValue userData = root.get(0);
                            String dbPassword = userData.getString("password");

                            // Comprobamos la contraseña
                            if (dbPassword.equals(password)) {
                                callback.onSuccess("Login exitoso");
                            } else {
                                callback.onError("Contraseña incorrecta.");
                            }
                        } else {
                            callback.onError("Error en el servidor: " + statusCode);
                        }
                    }
                });
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError("Error de conexión: " + t.getMessage());
                    }
                });
            }

            @Override
            public void cancelled() { }
        });
    }
}
