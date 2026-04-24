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

        // Obtenemos las monedas que tiene actualmente en local
        int currentCoins = com.tikisadventure.core.SaveManager.getProfileData().coins;

        String jsonBody = "{\"name\":\"" + username + "\", \"password\":\"" + password + "\", \"creation_date\":\"" + creationDate + "\", \"coins\":" + currentCoins + "}";

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

    // --- ACTUALIZAR MONEDAS EN LA NUBE (VÍA RPC) ---
    public void actualizarMonedas(String username, long coins, final AuthCallback callback) {
        com.badlogic.gdx.net.HttpRequestBuilder requestBuilder = new com.badlogic.gdx.net.HttpRequestBuilder();

        // Ahora el JSON coincide con los parámetros (p_name y p_coins) de nuestra nueva función SQL
        String jsonBody = "{\"p_name\":\"" + username + "\", \"p_coins\":" + coins + "}";

        com.badlogic.gdx.Net.HttpRequest httpRequest = requestBuilder.newRequest()
            .method(com.badlogic.gdx.Net.HttpMethods.POST) // POST real y legal
            .url(DatabaseConfig.SUPABASE_URL + "rpc/actualizar_monedas_jugador") // Llamamos a la función
            .header("apikey", DatabaseConfig.SUPABASE_KEY)
            .header("Authorization", "Bearer " + DatabaseConfig.SUPABASE_KEY)
            .header("Content-Type", "application/json")
            .content(jsonBody)
            .build();

        com.badlogic.gdx.Gdx.net.sendHttpRequest(httpRequest, new com.badlogic.gdx.Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(com.badlogic.gdx.Net.HttpResponse httpResponse) {
                final int statusCode = httpResponse.getStatus().getStatusCode();
                final String responseString = httpResponse.getResultAsString();

                com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                    // Los RPC exitosos que no devuelven nada (void) suelen devolver un 204 o 200
                    if (statusCode >= 200 && statusCode < 300) {
                        if (callback != null) callback.onSuccess("Monedas sincronizadas");
                        System.out.println("ÉXITO SUPABASE: Monedas guardadas (" + coins + ") en la cuenta de " + username);
                    } else {
                        if (callback != null) callback.onError("Error: " + statusCode);
                        System.out.println("ERROR SUPABASE RPC (" + statusCode + "): " + responseString);
                    }
                });
            }
            @Override
            public void failed(Throwable t) {
                System.out.println("ERROR CRÍTICO DE RED: " + t.getMessage());
            }
            @Override
            public void cancelled() { }
        });
    }

    // --- MODIFICAR INICIO DE SESIÓN PARA TRAER MONEDAS ---
    // Añadiremos un callback especial o usaremos el String para pasar el dato
    public void iniciarSesion(final String username, final String password, final AuthCallback callback) {
        com.badlogic.gdx.net.HttpRequestBuilder requestBuilder = new com.badlogic.gdx.net.HttpRequestBuilder();
        String url = DatabaseConfig.SUPABASE_URL + "jugador?name=eq." + username + "&select=*&limit=1";

        com.badlogic.gdx.Net.HttpRequest httpRequest = requestBuilder.newRequest()
            .method(com.badlogic.gdx.Net.HttpMethods.GET)
            .url(url)
            .header("apikey", DatabaseConfig.SUPABASE_KEY)
            .header("Authorization", "Bearer " + DatabaseConfig.SUPABASE_KEY)
            .build();

        com.badlogic.gdx.Gdx.net.sendHttpRequest(httpRequest, new com.badlogic.gdx.Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(com.badlogic.gdx.Net.HttpResponse httpResponse) {
                int statusCode = httpResponse.getStatus().getStatusCode();
                String responseString = httpResponse.getResultAsString();

                com.badlogic.gdx.Gdx.app.postRunnable(() -> {
                    if (statusCode == 200) {
                        com.badlogic.gdx.utils.JsonReader reader = new com.badlogic.gdx.utils.JsonReader();
                        com.badlogic.gdx.utils.JsonValue root = reader.parse(responseString);

                        if (root.size == 0) {
                            callback.onError("El usuario no existe.");
                            return;
                        }

                        com.badlogic.gdx.utils.JsonValue userData = root.get(0);
                        String dbPassword = userData.getString("password");

                        if (dbPassword.equals(password)) {
                            // Extraemos las monedas de la DB
                            JsonValue coinsValue = userData.get("coins");
                            long coins = (coinsValue == null || coinsValue.isNull()) ? 0 : coinsValue.asLong();
                            // Las pasamos en el mensaje de éxito separadas por un token o similar
                            // O simplemente las devolvemos como String para que la UI las procese
                            callback.onSuccess(String.valueOf(coins));
                        } else {
                            callback.onError("Contraseña incorrecta.");
                        }
                    } else {
                        callback.onError("Error: " + statusCode);
                    }
                });
            }
            @Override public void failed(Throwable t) { /* ... */ }
            @Override public void cancelled() { }
        });
    }
}
