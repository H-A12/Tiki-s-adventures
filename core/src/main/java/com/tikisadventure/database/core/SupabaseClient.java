package com.tikisadventure.database.core;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net;
import com.badlogic.gdx.net.HttpRequestBuilder;

public class SupabaseClient {

    public static void sendRequest(String method, String endpoint, String jsonBody, final AuthCallback callback) {
        HttpRequestBuilder requestBuilder = new HttpRequestBuilder();
        Net.HttpRequest httpRequest = requestBuilder.newRequest()
            .method(method)
            .url(DatabaseConfig.SUPABASE_URL + endpoint)
            .header("apikey", DatabaseConfig.SUPABASE_KEY)
            .header("Authorization", "Bearer " + DatabaseConfig.SUPABASE_KEY)
            .header("Content-Type", "application/json")
            .header("Prefer", "return=representation") // Útil para POST
            .build();

        // Si hay cuerpo (JSON), se lo añadimos
        if (jsonBody != null && !jsonBody.isEmpty()) {
            httpRequest.setContent(jsonBody);
        }

        Gdx.net.sendHttpRequest(httpRequest, new Net.HttpResponseListener() {
            @Override
            public void handleHttpResponse(Net.HttpResponse httpResponse) {
                final int statusCode = httpResponse.getStatus().getStatusCode();
                final String responseString = httpResponse.getResultAsString();

                Gdx.app.postRunnable(() -> {
                    // Consideramos éxito los códigos 200 a 299
                    if (statusCode >= 200 && statusCode < 300) {
                        if (callback != null) callback.onSuccess(responseString);
                    } else {
                        // Pasamos el error exacto para poder leerlo
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
