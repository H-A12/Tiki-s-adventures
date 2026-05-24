package com.tikisadventure.database.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;

//Clase que envía peticiones HTTP a la API REST de Supabase.
//Construye la request con la URL, la API key y el cuerpo JSON,
//y maneja la respuesta (éxito o error) mediante un callback.
//Se usa desde los repositorios para hablar con la base de datos.
public class SupabaseClient {

    //Enviar petición HTTP a Supabase
    public static void sendRequest(String method, String endpoint, String jsonBody, final AuthCallback callback) {
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        Net.HttpRequest httpRequest = requestBuilder.newRequest()
            .method(method)
            .url(DatabaseConfig.SUPABASE_URL + endpoint)
            .header("apikey", DatabaseConfig.SUPABASE_KEY)
            .header("Authorization", "Bearer " + DatabaseConfig.SUPABASE_KEY)
            .header("Content-Type", "application/json")
            .header("Prefer", "return=representation")
            .build();

        //Añadir cuerpo JSON si existe
        if (jsonBody != null && !jsonBody.isEmpty()) {
            httpRequest.setContent(jsonBody);
        }

        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                final int statusCode = httpResponse.getStatus().getStatusCode();
                final String responseString = httpResponse.getResultAsString();

                Gdx.app.postRunnable(() -> {
                    //Códigos 200-299 = éxito
                    if (statusCode >= 200 && statusCode < 300) {
                        if (callback != null) callback.onSuccess(responseString);
                    } else {
                        if (callback != null) callback.onError("Status " + statusCode + ": " + responseString);
                    }
                });
            }

            @Override
            public void failed(Throwable t) {
                Gdx.app.postRunnable(() -> {
                    if (callback != null) callback.onError("Error de red: " + t.getMessage());
                });
            }

            @Override
            public void cancelled() { }
        });
    }
}
