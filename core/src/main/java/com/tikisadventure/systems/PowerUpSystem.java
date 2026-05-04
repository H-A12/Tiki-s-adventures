package com.tikisadventure.systems;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.core.SaveManager;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.systems.powerUps.*;

public class PowerUpSystem {

    // Lista maestra de todos los power ups globales posibles en el juego
    private Array<PowerUp> globalPool = new Array<>();
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
        globalPool.add(new GlobalStatPowerUp("Tornillos", "+3% Daño Kinetico", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.KINETIC_DMG, 0.03f));
        globalPool.add(new GlobalStatPowerUp("Pilas Triple A", "+3% Daño Energía", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.ENERGY_DMG, 0.03f));
        globalPool.add(new GlobalStatPowerUp("Petardos", "+3% Daño Explosivo", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.EXPLOSIVE_DMG, 0.03f));
        globalPool.add(new GlobalStatPowerUp("Salsa picante", "+5% Daño Igneo", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.FIRE_DMG, 0.05f));
        globalPool.add(new GlobalStatPowerUp("Huevo podrido", "+5% Daño Veneno", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.POISON_DMG, 0.05f));
        globalPool.add(new GlobalStatPowerUp("Frigopie", "+4% Daño Hielo", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.ICE_DMG, 0.04f));
        globalPool.add(new GlobalStatPowerUp("Golosinas", "+5% Vida Máxima", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.MAX_HP_PERCENT, 0.05f));
        globalPool.add(new GlobalStatPowerUp("Libro de mates", "+5% Ganancia de XP", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.XP_GAIN_PERCENT, 0.05f));
        globalPool.add(new GlobalStatPowerUp("Sobre de azúcar", "+5% Velocidad de Movimiento", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.SPEED, 0.05f));
        globalPool.add(new GlobalStatPowerUp("Aguja de coser", "+1% Prob. Crítico", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.CRIT_CHANCE, 0.01f));
        globalPool.add(new GlobalStatPowerUp("Tirita usada", "+1% Regeneración", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.LIFE_REGEN, 0.01f));
        globalPool.add(new GlobalStatPowerUp("Imán decorativo", "+6% Atracción XP", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.ATTRACTION_RANGE, 0.06f));


        //Raros
        globalPool.add(new GlobalStatPowerUp("Llave inglesa", "+7% Daño Kinetico", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.KINETIC_DMG, 0.07f));
        globalPool.add(new GlobalStatPowerUp("Batería", "+8% Daño Energía", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.ENERGY_DMG, 0.08f));
        globalPool.add(new GlobalStatPowerUp("Mechero trucado", "+6% Daño Explosivo", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.EXPLOSIVE_DMG, 0.06f));
        globalPool.add(new GlobalStatPowerUp("Caja de cerillas", "+8% Daño Igneo", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.FIRE_DMG, 0.08f));
        globalPool.add(new GlobalStatPowerUp("Friegasuelos", "+9% Daño Veneno", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.POISON_DMG, 0.09f));
        globalPool.add(new GlobalStatPowerUp("Granizado de limón", "+9% Daño Hielo", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.ICE_DMG, 0.09f));
        globalPool.add(new GlobalStatPowerUp("Pera", "+10% Vida Máxima", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.MAX_HP_PERCENT, 0.10f));
        globalPool.add(new GlobalStatPowerUp("Globo terráqueo", "+9% Ganancia de XP", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.XP_GAIN_PERCENT, 0.09f));
        globalPool.add(new GlobalStatPowerUp("Bebida energética", "+10% Velocidad de Movimiento", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.SPEED, 0.10f));
        globalPool.add(new GlobalStatPowerUp("Martillo de carpintero", "+3% Prob. Crítico", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.CRIT_CHANCE, 0.03f));
        globalPool.add(new GlobalStatPowerUp("1ª Ley de Tiki", "+1 de Suerte", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.LUCK, 1f));
        globalPool.add(new GlobalStatPowerUp("Jarabe caducado", "+3% Regeneración", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.LIFE_REGEN, 0.03f));
        globalPool.add(new GlobalStatPowerUp("Chicle del suelo", "+15% Atracción XP", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.ATTRACTION_RANGE, 0.15f));
        globalPool.add(new GlobalStatPowerUp("Pajita de papel", "+1% Robo de vida", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.LIFE_LEECH, 0.01f));


        //Especiales
        globalPool.add(new GlobalStatPowerUp("Taladro", "+15% Daño Kinetico", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.KINETIC_DMG, 0.15f));
        globalPool.add(new GlobalStatPowerUp("Pinzas de arranque", "+16% Daño Energía", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.ENERGY_DMG, 0.16f));
        globalPool.add(new GlobalStatPowerUp("Bidón de gasolina", "+13% Daño Explosivo", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.EXPLOSIVE_DMG, 0.13f));
        globalPool.add(new GlobalStatPowerUp("Soplete doméstico", "+18% Daño Igneo", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.FIRE_DMG, 0.18f));
        globalPool.add(new GlobalStatPowerUp("Seta del jardín", "+20% Daño Veneno", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.POISON_DMG, 0.20f));
        globalPool.add(new GlobalStatPowerUp("Sr Nievefría", "+20% Daño Hielo", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.ICE_DMG, 0.20f));
        globalPool.add(new GlobalStatPowerUp("Hamburguesa sin tomate", "+17% Vida Máxima", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.MAX_HP_PERCENT, 0.17f));
        globalPool.add(new GlobalStatPowerUp("Piezas de puzzle", "+15% Ganancia de XP", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.XP_GAIN_PERCENT, 0.15f));
        globalPool.add(new GlobalStatPowerUp("Patines viejos", "+18% Velocidad de Movimiento", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.SPEED, 0.18f));
        globalPool.add(new GlobalStatPowerUp("Dardos", "+5% Prob. Crítico", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.CRIT_CHANCE, 0.05f));
        globalPool.add(new GlobalStatPowerUp("2ª Ley de Tiki", "+3 de Suerte", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.LUCK, 3f));
        globalPool.add(new GlobalStatPowerUp("Bote de miel", "+5% Regeneración", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.LIFE_REGEN, 0.05f));
        globalPool.add(new GlobalStatPowerUp("Colonia de papá", "+25% Atracción XP", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.ATTRACTION_RANGE, 0.25f));
        globalPool.add(new GlobalStatPowerUp("Esponja del abuelo", "+3% Robo de vida", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.LIFE_LEECH, 0.03f));


        //Épicos (2 stats)
        ObjectMap<GlobalStatPowerUp.StatType, Float> gasTank = new ObjectMap<>();
        gasTank.put(GlobalStatPowerUp.StatType.EXPLOSIVE_DMG, 0.15f);
        gasTank.put(GlobalStatPowerUp.StatType.FIRE_DMG, 0.20f);
        globalPool.add(new MultiStatPowerUp("Tanque de gas", "+15% Daño explosivo y +20% Daño Igneo", PowerUp.Rarity.EPICO, gasTank));

        ObjectMap<GlobalStatPowerUp.StatType, Float> prehistoricVirus = new ObjectMap<>();
        prehistoricVirus.put(GlobalStatPowerUp.StatType.POISON_DMG, 0.22f);
        prehistoricVirus.put(GlobalStatPowerUp.StatType.ICE_DMG, 0.24f);
        globalPool.add(new MultiStatPowerUp("Virus prehistórico", "+22% Daño Veneno y +24% Daño Hielo", PowerUp.Rarity.EPICO, prehistoricVirus));

        ObjectMap<GlobalStatPowerUp.StatType, Float> sailorSuit = new ObjectMap<>();
        sailorSuit.put(GlobalStatPowerUp.StatType.MAX_HP_PERCENT, 0.20f);
        sailorSuit.put(GlobalStatPowerUp.StatType.LIFE_REGEN, 0.05f);
        globalPool.add(new MultiStatPowerUp("Traje de marinero", "+20% Vida máxima y +5% Regeneración", PowerUp.Rarity.EPICO, sailorSuit));

        //Legendarios (3 stats)
        ObjectMap<GlobalStatPowerUp.StatType, Float> thirdTikiLaw = new ObjectMap<>();
        thirdTikiLaw.put(GlobalStatPowerUp.StatType.EVASION, 0.25f);
        thirdTikiLaw.put(GlobalStatPowerUp.StatType.LUCK, 5f);
        thirdTikiLaw.put(GlobalStatPowerUp.StatType.XP_GAIN_PERCENT, 0.20f);
        globalPool.add(new MultiStatPowerUp("3ª Ley de Tiki", "+25% Evasión, +5 de Suerte y +20% XP", PowerUp.Rarity.LEGENDARIO, thirdTikiLaw));



        //Los de armas
        weaponPool.add(new NewWeaponPowerUp("Fusil de bolas", "Lanza bolas de parques infantiles.", PowerUp.Rarity.RARO, "BallRifle", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Escupepalillos", "Disparo una gran cantidad de moldadientes.", PowerUp.Rarity.RARO, "ThootpickShotgun", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Pirocohete", "Hora de los fuegos artificiales.", PowerUp.Rarity.RARO, "FireworkLauncher", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Clavolleta", "Dispara clavos a gran velocidad.", PowerUp.Rarity.ESPECIAL, "SubmachineGun", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Lanzapelotas", "Lanza pelotas de tenis que rebotan.", PowerUp.Rarity.EPICO, "TennisLauncher", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Triturahielo", "Refresca y ralentiza a los enemigos", PowerUp.Rarity.ESPECIAL, "IceGrinder", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Extintor trucado", "Hace lo inverso a un extintor normal", PowerUp.Rarity.EPICO, "Lanzallamas", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Lanzadiscos", "Sus CDs golpean en cadena a los enemigos", PowerUp.Rarity.ESPECIAL, "LanzaSierras", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Banana", "Gran fuente de potasio, nunca te abandonará", PowerUp.Rarity.ESPECIAL, "Banana", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Pudripez", "Pez putrefacto que causa indigestion.", PowerUp.Rarity.ESPECIAL, "PezGlobo", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Saxofon", "Los enemigos odiaran tu musica.", PowerUp.Rarity.ESPECIAL, "Saxophone", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Enchufe alcalino", "Un rayo letal de gran alcance.", PowerUp.Rarity.EPICO, "BatteryPlugger", this.weaponFactory));

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
            for (PowerUp globalUp : globalPool) {

                // Verificamos si es de tipo GlobalStatPowerUp para hacer los filtros de daño elemental
                if (globalUp instanceof GlobalStatPowerUp) {
                    GlobalStatPowerUp statUp = (GlobalStatPowerUp) globalUp;

                    if (statUp.getStatType() == GlobalStatPowerUp.StatType.POISON_DMG &&
                        !player.hasDamageTypeEquipped(com.tikisadventure.combat.DamageType.POISON)) {
                        continue;
                    }
                    if (statUp.getStatType() == GlobalStatPowerUp.StatType.FIRE_DMG &&
                        !player.hasDamageTypeEquipped(com.tikisadventure.combat.DamageType.FIRE)) {
                        continue;
                    }
                    if (statUp.getStatType() == GlobalStatPowerUp.StatType.EXPLOSIVE_DMG &&
                        !player.hasDamageTypeEquipped(com.tikisadventure.combat.DamageType.EXPLOSIVE)) {
                        continue;
                    }
                    if (statUp.getStatType() == GlobalStatPowerUp.StatType.ICE_DMG &&
                        !player.hasDamageTypeEquipped(com.tikisadventure.combat.DamageType.ICE)) {
                        continue;
                    }
                    if (statUp.getStatType() == GlobalStatPowerUp.StatType.ENERGY_DMG &&
                        !player.hasDamageTypeEquipped(com.tikisadventure.combat.DamageType.ENERGY)) {
                        continue;
                    }
                }

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
        if (weaponId.equals("ToothpickShotgun")) return SaveManager.isCharacterUnlocked(3);
        return false;
    }
}
