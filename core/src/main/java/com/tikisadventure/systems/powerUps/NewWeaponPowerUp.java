package com.tikisadventure.systems.powerUps;

import com.badlogic.gdx.Gdx;
import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.combat.weapons.WeaponFactory;
import com.tikisadventure.entities.player.Player;

public class NewWeaponPowerUp extends PowerUp {

    private String weaponId;
    private WeaponFactory factory; // Guardamos la fábrica de armas

    // Actualizamos el constructor para pedir la fábrica
    public NewWeaponPowerUp(String name, String desc, Rarity rarity, String weaponId, WeaponFactory factory) {
        super(name, desc, rarity);
        this.weaponId = weaponId;
        this.factory = factory;
    }

    @Override
    public void apply(Player player) {
        Weapon newWeapon = factory.createWeapon(weaponId, player);

        if (newWeapon == null) {
            return;
        }

        newWeapon.setTier(1);
        player.getWeaponFactory().addWeapon(newWeapon);
    }

    public String getWeaponId() {
        return weaponId;
    }
}
