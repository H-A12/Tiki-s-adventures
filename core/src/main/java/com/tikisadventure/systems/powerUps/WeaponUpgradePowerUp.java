package com.tikisadventure.systems.powerUps;

import com.badlogic.gdx.Gdx;
import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.entities.player.Player;

public class WeaponUpgradePowerUp extends PowerUp {

    private Weapon specificWeaponInstance; // El arma EXACTA que vamos a mejorar

    public WeaponUpgradePowerUp(Weapon weaponToUpgrade) {
        // Generamos el texto dinámicamente según el arma
        super(
            "Mejora " + weaponToUpgrade.getName() + " a Tier " + (weaponToUpgrade.getTier() + 1),
            "Aumenta daño y estadísticas del arma.",
            Rarity.COMUN // Podrías calcular la rareza según el tier
        );
        this.specificWeaponInstance = weaponToUpgrade;
    }

    @Override
    public void apply(Player player) {
        specificWeaponInstance.setTier(specificWeaponInstance.getTier() + 1);

        float danyoActual = specificWeaponInstance.getDamage();
        specificWeaponInstance.setDamage(danyoActual * 1.25f);

        Gdx.app.log("POWER UP", "Arma mejorada: " + specificWeaponInstance.getName() + " | Nuevo daño: " + specificWeaponInstance.getDamage());
    }
}
