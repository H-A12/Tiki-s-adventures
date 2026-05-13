package com.tikisadventure.database.inventory;

import com.badlogic.gdx.Net;
import com.tikisadventure.database.core.AuthCallback;
import com.tikisadventure.database.core.SupabaseClient;

public class WeaponRepository {

    public void desbloquearArmaBD(long playerId, String weaponStringId, final AuthCallback callback) {
        // Le pasamos el ID del jugador y el nombre en clave del arma (Ej: "PezGlobo")
        String jsonBody = "{\"p_player_id\":" + playerId + ", \"p_weapon_string_id\":\"" + weaponStringId + "\"}";

        SupabaseClient.sendRequest(Net.HttpMethods.POST, "rpc/desbloquear_arma", jsonBody, new AuthCallback() {
            @Override
            public void onSuccess(String responseString) {
                if (callback != null) callback.onSuccess("Arma guardada en la nube");
                System.out.println("ÉXITO SUPABASE: Arma " + weaponStringId + " guardada para el jugador " + playerId);
            }

            @Override
            public void onError(String errorMessage) {
                if (callback != null) callback.onError(errorMessage);
                System.out.println("ERROR SUPABASE ARMAS: " + errorMessage);
            }
        });
    }
}
