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

    //Los powerUps existentes (excepto los de Tier de arma)
    private void initPools() {

        //Los de stats
        //Comunes
        globalPool.add(new GlobalStatPowerUp("Cachibaches", "+4% Ataque Kinetico", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.KINETIC_DMG, 0.04f));
        globalPool.add(new GlobalStatPowerUp("Cerillas", "+3% Ataque Igneo", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.FIRE_DMG, 0.03f));
        globalPool.add(new GlobalStatPowerUp("Petardos", "+3% Ataque Explosivo", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.EXPLOSIVE_DMG, 0.03f));
        globalPool.add(new GlobalStatPowerUp("Golosinas", "+5% Vida Máxima", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.MAX_HP_PERCENT, 0.05f));
        globalPool.add(new GlobalStatPowerUp("Libro de escuela", "+5% Ganancia de XP", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.XP_GAIN_PERCENT, 0.05f));
        globalPool.add(new GlobalStatPowerUp("Sobre de azúcar", "+5% Velocidad de Movimiento", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.SPEED, 0.05f));
        globalPool.add(new GlobalStatPowerUp("Frigopie", "+3% Ataque de Hielo", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.ICE_DMG, 0.03f));
        globalPool.add(new GlobalStatPowerUp("Pilas AAA", "+4% Ataque de Energía", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.ENERGY_DMG, 0.04f));

        //Raros
        globalPool.add(new GlobalStatPowerUp("Friegasuelos", "+5% Ataque Veneno", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.POISON_DMG, 0.05f));
        globalPool.add(new GlobalStatPowerUp("Carpintería", "+1% Prob. Crítico", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.CRIT_CHANCE, 0.01f));
        globalPool.add(new GlobalStatPowerUp("ley de Tiki", "+1 de Suerte", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.LUCK, 1f));

        //Epicos
        globalPool.add(new GlobalStatPowerUp("Bricomanía", "+3% Prob. Crítico", PowerUp.Rarity.EPICO, GlobalStatPowerUp.StatType.CRIT_CHANCE, 0.03f));

        //Los de armas
        weaponPool.add(new NewWeaponPowerUp("Fusil de bolas", "Lanza bolas de parques infantiles.", PowerUp.Rarity.COMUN, "BallRifle", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Escupepalillos", "Disparo una gran cantidad de moldadientes.", PowerUp.Rarity.COMUN, "ThootpickShotgun", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Pirocohete", "Hora de los fuegos artificiales.", PowerUp.Rarity.COMUN, "FireworkLauncher", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Clavolleta", "Dispara clavos a gran velocidad.", PowerUp.Rarity.COMUN, "SubmachineGun", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Lanzapelotas", "Lanza pelotas de tenis que rebotan.", PowerUp.Rarity.COMUN, "TennisLauncher", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Triturahielo", "Refresca y ralentiza a los enemigos", PowerUp.Rarity.COMUN, "IceGrinder", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Extintor trucado", "Hace lo inverso a un extintor normal", PowerUp.Rarity.EPICO, "Lanzallamas", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Lanzadiscos", "Sus CDs golpean en cadena a los enemigos", PowerUp.Rarity.EPICO, "LanzaSierras", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Banana", "Gran fuente de potasio, nunca te abandonará", PowerUp.Rarity.RARO, "Banana", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Pudripez", "Pez putrefacto que causa indigestion.", PowerUp.Rarity.COMUN, "PezGlobo", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Saxofon", "Los enemigos odiaran tu musica.", PowerUp.Rarity.COMUN, "Saxophone", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Enchufe alcalino", "Un rayo letal de gran alcance.", PowerUp.Rarity.COMUN, "BatteryPlugger", this.weaponFactory));

    }

    public Array<PowerUp> rollOptions(Player player, int currentLevel, int optionsCount) {
        Array<PowerUp> options = new Array<>();
        Array<PowerUp> availablePool = new Array<>();

        boolean isWeaponLevel = (currentLevel % 5 == 0);
        int currentWeaponsCount = player.getWeaponFactory().getWeapons().size;

        if (isWeaponLevel && currentWeaponsCount < 6) {
            for (NewWeaponPowerUp wp : weaponPool) {
                if (isStartingWeaponOfUnlockedCharacter(wp.getWeaponId()) || SaveManager.isWeaponOwned(wp.getWeaponId())) {
                    availablePool.add(wp);
                }
            }
        } else {
            // --- REGLA: RESTO DE NIVELES (MIX GLOBAL + MEJORAS TIER) ---

            // 1. AÑADIMOS LOS GLOBALES FILTRADOS
            for (GlobalStatPowerUp globalUp : globalPool) {

                // Filtramos dependencias elementales usando el nuevo método del Player
                if (globalUp.getStatType() == GlobalStatPowerUp.StatType.POISON_DMG &&
                    !player.hasDamageTypeEquipped(com.tikisadventure.combat.DamageType.POISON)) {
                    continue;
                }

                if (globalUp.getStatType() == GlobalStatPowerUp.StatType.FIRE_DMG &&
                    !player.hasDamageTypeEquipped(com.tikisadventure.combat.DamageType.FIRE)) {
                    continue;
                }

                if (globalUp.getStatType() == GlobalStatPowerUp.StatType.EXPLOSIVE_DMG &&
                    !player.hasDamageTypeEquipped(com.tikisadventure.combat.DamageType.EXPLOSIVE)) {
                    continue;
                }

                if (globalUp.getStatType() == GlobalStatPowerUp.StatType.ICE_DMG &&
                    !player.hasDamageTypeEquipped(com.tikisadventure.combat.DamageType.ICE)) {
                    continue;
                }

                if (globalUp.getStatType() == GlobalStatPowerUp.StatType.ENERGY_DMG &&
                    !player.hasDamageTypeEquipped(com.tikisadventure.combat.DamageType.ENERGY)) {
                    continue;
                }

                // Si en el futuro añades Power Ups de Hielo (ICE_DMG) o Energía (ENERGY_DMG), añades la comprobación aquí igual.
                availablePool.add(globalUp);
            }

            // 2. AÑADIMOS DINÁMICAMENTE MEJORAS DE TIER PARA LAS ARMAS QUE YA POSEE
            for (Weapon w : player.getWeaponFactory().getWeapons()) {
                if (w.getTier() < 5) {
                    availablePool.add(new WeaponUpgradePowerUp(w));
                }
            }
        }

        // --- RULETA DE PESOS (Weighted Random) ---
        int numToSelect = Math.min(optionsCount, availablePool.size);

        for (int i = 0; i < numToSelect; i++) {
            // 1. Calcular el peso total de la pool actual
            int totalWeight = 0;
            for (PowerUp p : availablePool) {
                totalWeight += p.getRarity().weight;
            }

            // Si por alguna razón la pool se vacía, salimos por seguridad
            if (totalWeight <= 0) break;

            // 2. Tirar un número aleatorio entre 0 y el peso total
            int randomValue = com.badlogic.gdx.math.MathUtils.random(0, totalWeight - 1);
            int currentWeightSum = 0;

            // 3. Buscar a qué PowerUp le ha tocado el premio
            for (int j = 0; j < availablePool.size; j++) {
                PowerUp p = availablePool.get(j);
                currentWeightSum += p.getRarity().weight;

                if (currentWeightSum > randomValue) {
                    // ¡Encontrado! Lo añadimos a las opciones que verá el jugador
                    options.add(p);
                    // Lo eliminamos de la pool temporal para que no salga repetido
                    availablePool.removeIndex(j);

                    // --- NUEVA CONDICIÓN: MAX 1 MEJORA DE TIER DE ARMA ---
                    // Si acaba de salir una mejora de arma, purgamos el resto del pool
                    if (p instanceof WeaponUpgradePowerUp) {
                        // Iteramos hacia atrás al borrar elementos de un Array para evitar errores de índice
                        for (int k = availablePool.size - 1; k >= 0; k--) {
                            if (availablePool.get(k) instanceof WeaponUpgradePowerUp) {
                                availablePool.removeIndex(k);
                            }
                        }
                    }

                    break; // Pasamos a sacar la siguiente opción
                }
            }
        }

        return options;

    }

    private boolean isStartingWeaponOfUnlockedCharacter(String weaponId) {
        if (weaponId.equals("BallRifle")) return true;
        if (weaponId.equals("FireworkLauncher")) return SaveManager.isCharacterUnlocked(2);
        if (weaponId.equals("ThootpickShotgun")) return SaveManager.isCharacterUnlocked(3);
        return false;
    }
}
