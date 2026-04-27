package com.tikisadventure.database.progress;

import com.badlogic.gdx.Net;
import com.tikisadventure.database.core.AuthCallback;
import com.tikisadventure.database.core.SupabaseClient;

public class ProgressRepository {

    public void actualizarProgreso(final String username, long coins, long totalScore, final AuthCallback callback) {
        com.badlogic.gdx.utils.Json jsonTool = new com.badlogic.gdx.utils.Json();

        // --- ¡LÍNEAS VITALES PARA SUPABASE! ---
        // Obliga a LibGDX a usar estándar JSON estricto (con comillas dobles)
        jsonTool.setOutputType(com.badlogic.gdx.utils.JsonWriter.OutputType.json);
        jsonTool.setTypeName(null);

        String armasJson = jsonTool.toJson(com.tikisadventure.core.GameSession.customWeapons);

        if (armasJson == null || armasJson.isEmpty()) armasJson = "{}";

        String jsonBody = "{\"p_name\":\"" + username + "\", \"p_coins\":" + coins +
            ", \"p_total_score\":" + totalScore + ", \"p_custom_weapons\":" + armasJson + "}";

        // Log temporal para ver qué estamos enviando exactamente
        System.out.println("ENVIANDO A SUPABASE: " + jsonBody);

        SupabaseClient.sendRequest(Net.HttpMethods.POST, "rpc/actualizar_progreso_jugador", jsonBody, new AuthCallback() {
            @Override
            public void onSuccess(String responseString) {
                if (callback != null) callback.onSuccess("Progreso sincronizado");
                System.out.println("ÉXITO SUPABASE: Progreso y ARMAS CUSTOM guardados de " + username);
            }

            @Override
            public void onError(String errorMessage) {
                if (callback != null) callback.onError("Error: " + errorMessage);
                System.out.println("ERROR SUPABASE RPC: " + errorMessage);
            }
        });
    }

    public void desbloquearPersonajeBD(long playerId, int characterId, final AuthCallback callback) {
        String jsonBody = "{\"p_player_id\":" + playerId + ", \"p_char_id\":" + characterId + "}";

        SupabaseClient.sendRequest(Net.HttpMethods.POST, "rpc/desbloquear_personaje", jsonBody, new AuthCallback() {
            @Override
            public void onSuccess(String responseString) {
                if (callback != null) callback.onSuccess("Personaje vinculado");
                System.out.println("ÉXITO SUPABASE: Personaje ID " + characterId + " vinculado al jugador " + playerId);
            }

            @Override
            public void onError(String errorMessage) {
                if (callback != null) callback.onError("Error: " + errorMessage);
                System.out.println("ERROR SUPABASE PERSONAJE: " + errorMessage);
            }
        });
    }

    public void desbloquearMapaBD(long playerId, String mapStringId, final AuthCallback callback) {
        String jsonBody = "{\"p_player_id\":" + playerId + ", \"p_map_string_id\":\"" + mapStringId + "\"}";

        com.tikisadventure.database.core.SupabaseClient.sendRequest(Net.HttpMethods.POST, "rpc/desbloquear_mapa", jsonBody, new AuthCallback() {
            @Override
            public void onSuccess(String responseString) {
                if (callback != null) callback.onSuccess("Mapa desbloqueado");
                System.out.println("ÉXITO SUPABASE: Mapa " + mapStringId + " vinculado al jugador " + playerId);
            }
            @Override
            public void onError(String errorMessage) {
                if (callback != null) callback.onError(errorMessage);
                System.out.println("ERROR SUPABASE MAPAS: " + errorMessage);
            }
        });
    }

    public void desbloquearGadgetBD(long playerId, String gadgetStringId, final AuthCallback callback) {
        String jsonBody = "{\"p_player_id\":" + playerId + ", \"p_gadget_string_id\":\"" + gadgetStringId + "\"}";

        com.tikisadventure.database.core.SupabaseClient.sendRequest(Net.HttpMethods.POST, "rpc/desbloquear_gadget", jsonBody, new AuthCallback() {
            @Override
            public void onSuccess(String responseString) {
                if (callback != null) callback.onSuccess("Gadget desbloqueado");
                System.out.println("ÉXITO SUPABASE: Gadget " + gadgetStringId + " vinculado al jugador " + playerId);
            }
            @Override
            public void onError(String errorMessage) {
                if (callback != null) callback.onError(errorMessage);
                System.out.println("ERROR SUPABASE GADGETS: " + errorMessage);
            }
        });
    }
}
