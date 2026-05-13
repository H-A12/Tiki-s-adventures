package com.tikisadventure.systems.powerUps;

import com.badlogic.gdx.utils.ObjectMap;
import com.tikisadventure.entities.player.Player;

public class MultiStatPowerUp extends PowerUp {

    // Un mapa que almacena los tipos de estadística y la cantidad a sumar
    private ObjectMap<GlobalStatPowerUp.StatType, Float> modifiers;

    public MultiStatPowerUp(String name, String desc, Rarity rarity, ObjectMap<GlobalStatPowerUp.StatType, Float> modifiers) {
        super(name, desc, rarity);
        this.modifiers = modifiers;
    }

    @Override
    public void apply(Player player) {
        // TRUCO MAGISTRAL: Reutilizamos tu código actual creando GlobalStatPowerUps temporales.
        // Así nos aseguramos de que todos los logs del sistema y las sumas se ejecuten igual.
        for (ObjectMap.Entry<GlobalStatPowerUp.StatType, Float> entry : modifiers.entries()) {
            GlobalStatPowerUp tempPowerUp = new GlobalStatPowerUp(
                this.getName(),
                "", // No hace falta descripción para esta aplicación interna
                this.getRarity(),
                entry.key,
                entry.value
            );
            tempPowerUp.apply(player);
        }
    }

    // Por si en el futuro necesitas leer qué estadísticas tiene este objeto
    public ObjectMap<GlobalStatPowerUp.StatType, Float> getModifiers() {
        return modifiers;
    }
}
