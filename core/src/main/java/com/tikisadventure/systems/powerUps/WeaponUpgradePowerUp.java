package com.tikisadventure.systems.powerUps;

import com.badlogic.gdx.Gdx;
import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.entities.player.Player;

public class WeaponUpgradePowerUp extends PowerUp {

    private Weapon specificWeaponInstance; // El arma EXACTA que vamos a mejorar

    public WeaponUpgradePowerUp(Weapon weaponToUpgrade) {
        // Llamamos a un método auxiliar para calcular la rareza antes de crear la carta
        super(
            "Mejora " + weaponToUpgrade.getName() + " a Tier " + (weaponToUpgrade.getTier() + 1),
            "Aumenta el daño y la cadencia de fuego del arma.",
            determinarRareza(weaponToUpgrade.getTier())
        );
        this.specificWeaponInstance = weaponToUpgrade;
    }

    // Método estático para calcular la rareza basada en el Tier actual del arma
    private static Rarity determinarRareza(int tierActual) {

        if (tierActual <= 2) {
            return Rarity.RARO;
        }
        else if (tierActual == 3) {
            return Rarity.EPICO;
        }
        else {
            return Rarity.LEGENDARIO;
        }
    }

    @Override
    public void apply(Player player) {
        // 1. Subir Tier
        specificWeaponInstance.setTier(specificWeaponInstance.getTier() + 1);

        // 2. Subir Daño (Aumenta un 25%)
        float danyoActual = specificWeaponInstance.getDamage();
        specificWeaponInstance.setDamage(danyoActual * 1.25f);

        // 3. Mejorar Cadencia (Reduce el cooldown un 10%)
        float cdActual = specificWeaponInstance.getCooldown();
        float nuevoCd = cdActual * 0.90f;

        if (nuevoCd < 0.05f) {
            nuevoCd = 0.05f;
        }
        specificWeaponInstance.setCooldown(nuevoCd);

        Gdx.app.log("POWER UP", "Arma mejorada: " + specificWeaponInstance.getName() +
            " | Nuevo Daño: " + specificWeaponInstance.getDamage() +
            " | Nuevo CD: " + specificWeaponInstance.getCooldown() +
            " | Alcanzado Tier: " + specificWeaponInstance.getTier());
    }
}
