package com.tikisadventure.database.inventory;

import com.badlogic.gdx.Net;
import com.tikisadventure.database.core.AuthCallback;
import com.tikisadventure.database.core.SupabaseClient;

//Clase para desbloquear armas de un jugador en la base de datos.
//Manda una petición POST a una función RPC de Supabase que vincula
//el ID del jugador con el identificador del arma.
public class WeaponRepository {

    public void desbloquearArmaBD(long playerId, String weaponStringId, final AuthCallback callback) {
        //Desbloquear arma en la nube
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
