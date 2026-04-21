package com.tikisadventure.systems;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.core.SaveManager;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.systems.powerUps.*;

public class PowerUpSystem {

    // Lista maestra de todos los power ups globales posibles en el juego
    private Array<GlobalStatPowerUp> globalPool = new Array<>();
    // Lista maestra de armas que pueden tocar
    private Array<NewWeaponPowerUp> weaponPool = new Array<>();

    private com.tikisadventure.combat.weapons.WeaponFactory weaponFactory;

    public PowerUpSystem(com.tikisadventure.combat.weapons.WeaponFactory factory) {
        this.weaponFactory = factory;
        initPools();
    }

    private void initPools() {

        globalPool.add(new GlobalStatPowerUp("Petardos", "+3% Daño Explosivo", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.EXPLOSIVE_DMG, 0.03f));
        globalPool.add(new GlobalStatPowerUp("Fregasuelos", "+5% Daño Veneno", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.POISON_DMG, 0.05f));
        globalPool.add(new GlobalStatPowerUp("Carpintería", "+1% Prob. Crítico", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.CRIT_CHANCE, 0.01f));
        globalPool.add(new GlobalStatPowerUp("Bricomanía", "+3% Prob. Crítico", PowerUp.Rarity.EPICO, GlobalStatPowerUp.StatType.CRIT_CHANCE, 0.03f));
        globalPool.add(new GlobalStatPowerUp("Golosinas", "+5% Vida Máxima", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.MAX_HP_PERCENT, 0.05f));
        globalPool.add(new GlobalStatPowerUp("ley de Tiki", "+1 de Suerte", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.LUCK, 1f));

        weaponPool.add(new NewWeaponPowerUp("AK-47", "Fusil de asalto rápido", PowerUp.Rarity.COMUN, "MetralletaEjemplo", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Fire Shotgun", "Disparo de perdigones", PowerUp.Rarity.COMUN, "EscopetaEjemplo", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Plasmaneitor", "Rifle de energía láser", PowerUp.Rarity.RARO, "ArmaEnergiaEjemplo", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Lanzallamas", "Quema a los enemigos", PowerUp.Rarity.EPICO, "Lanzallamas", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Sawneitor", "Lanza sierras en zig-zag", PowerUp.Rarity.EPICO, "LanzaSierras", this.weaponFactory));

    }

    /**
     * Devuelve X opciones de Power Ups basados en las reglas de tu juego.
     */
    public Array<PowerUp> rollOptions(Player player, int currentLevel, int optionsCount) {
        Array<PowerUp> options = new Array<>();
        Array<PowerUp> availablePool = new Array<>();

        boolean isWeaponLevel = (currentLevel % 5 == 0);
        int currentWeaponsCount = player.getWeaponFactory().getWeapons().size;

        if (isWeaponLevel && currentWeaponsCount < 6) {
            for (NewWeaponPowerUp wp : weaponPool) {
                if (true) {
                    availablePool.add(wp);
                }
            }
        } else {
            // --- REGLA: RESTO DE NIVELES (MIX GLOBAL + MEJORAS TIER) ---

            // 1. Añadimos todos los globales
            availablePool.addAll(globalPool);

            // 2. Añadimos dinámicamente mejoras de Tier para las armas que YA posee
            for (Weapon w : player.getWeaponFactory().getWeapons()) {
                if (w.getTier() < 5) {
                    // Creamos una carta específica para esta instancia exacta del arma
                    availablePool.add(new WeaponUpgradePowerUp(w));
                }
            }
        }

        // --- RULETA ALEATORIA (Seleccionamos X sin repetir) ---
        // Aquí podrías implementar la lógica de probabilidad según la 'Rarity',
        // de momento hacemos un barajeo básico.
        availablePool.shuffle();
        for (int i = 0; i < Math.min(optionsCount, availablePool.size); i++) {
            options.add(availablePool.get(i));
        }

        return options;
    }
}
