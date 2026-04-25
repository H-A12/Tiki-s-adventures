package com.tikisadventure.database.progress;

import com.badlogic.gdx.Net;
import com.tikisadventure.database.core.AuthCallback;
import com.tikisadventure.database.core.SupabaseClient;

public class ProgressRepository {

    public void actualizarProgreso(String username, long coins, long totalScore, final AuthCallback callback) {
        String jsonBody = "{\"p_name\":\"" + username + "\", \"p_coins\":" + coins + ", \"p_total_score\":" + totalScore + "}";

        SupabaseClient.sendRequest(Net.HttpMethods.POST, "rpc/actualizar_progreso_jugador", jsonBody, new AuthCallback() {
            @Override
            public void onSuccess(String responseString) {
                if (callback != null) callback.onSuccess("Monedas sincronizadas");
                System.out.println("ÉXITO SUPABASE: Progreso guardado en la cuenta de " + username);
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
}
