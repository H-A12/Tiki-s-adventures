package com.tikisadventure.systems.powerUps;

import com.badlogic.gdx.Gdx;
import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.entities.player.Player;

public class WeaponUpgradePowerUp extends PowerUp {

    private Weapon specificWeaponInstance; // El arma EXACTA que vamos a mejorar

    public WeaponUpgradePowerUp(Weapon weaponToUpgrade) {
        super(
            "upgrade_" + weaponToUpgrade.getWeaponId(),
            weaponToUpgrade.getName() +
            " Tier " + (weaponToUpgrade.getTier() + 1),
            "Aumenta el daño y la cadencia de fuego.",
            determinarRareza(weaponToUpgrade.getTier())
        );
        this.specificWeaponInstance = weaponToUpgrade;
    }

    // --- NUEVO: Exponemos el arma para que LevelUpUI pueda leer su sprite base ---
    public Weapon getWeapon() {
        return specificWeaponInstance;
    }

    private static Rarity determinarRareza(int tierActual) {
        // Tier 1 -> Sube a 2 (RARO)
        if (tierActual == 1) return Rarity.COMUN;
        // Tier 2 -> Sube a 3 (ESPECIAL)
        if (tierActual == 2) return Rarity.RARO;
        // Tier 3 -> Sube a 4 (EPICO)
        if (tierActual == 3) return Rarity.ESPECIAL;
        // Tier 4 -> Sube a 5 (LEGENDARIO)
        return Rarity.EPICO;
    }

    @Override
    public void apply(Player player) {
        specificWeaponInstance.setTier(specificWeaponInstance.getTier() + 1);

        float danyoActual = specificWeaponInstance.getDamage();
        specificWeaponInstance.setDamage(danyoActual * 1.10f);

        float cdActual = specificWeaponInstance.getCooldown();
        float nuevoCd = cdActual * 0.95f;

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
