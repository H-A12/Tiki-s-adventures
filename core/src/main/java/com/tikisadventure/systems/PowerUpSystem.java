package com.tikisadventure.systems;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.tikisadventure.combat.DamageType;
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

    private void initPools() {
        // Comunes
        globalPool.add(new GlobalStatPowerUp("commonScrews", "Tornillos", "+3% Daño Kinetico", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.KINETIC_DMG, 0.03f));
        globalPool.add(new GlobalStatPowerUp("commonBatteries", "Pilas Triple A", "+3% Daño Energía", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.ENERGY_DMG, 0.03f));
        globalPool.add(new GlobalStatPowerUp("commonFirecrackers", "Petardos", "+3% Daño Explosivo", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.EXPLOSIVE_DMG, 0.03f));
        globalPool.add(new GlobalStatPowerUp("commonHotSauce", "Salsa picante", "+5% Daño Igneo", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.FIRE_DMG, 0.05f));
        globalPool.add(new GlobalStatPowerUp("commonRottenEgg", "Huevo podrido", "+5% Daño Veneno", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.POISON_DMG, 0.05f));
        globalPool.add(new GlobalStatPowerUp("commonFootIceCream", "Frigopie", "+4% Daño Hielo", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.ICE_DMG, 0.04f));
        globalPool.add(new GlobalStatPowerUp("commonCandy", "Golosinas", "+5% Vida Máxima", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.MAX_HP_PERCENT, 0.05f));
        globalPool.add(new GlobalStatPowerUp("commonMathsBook", "Libro de mates", "+5% Ganancia de XP", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.XP_GAIN_PERCENT, 0.05f));
        globalPool.add(new GlobalStatPowerUp("commonSugarPacket", "Sobre de azúcar", "+5% Velocidad de Movimiento", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.SPEED, 0.05f));
        globalPool.add(new GlobalStatPowerUp("commonNeedle", "Aguja de coser", "+1% Prob. Crítico", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.CRIT_CHANCE, 0.01f));
        globalPool.add(new GlobalStatPowerUp("commonBandAid", "Tirita usada", "+1% Regeneración", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.LIFE_REGEN, 0.01f));
        globalPool.add(new GlobalStatPowerUp("commonMagnet", "Imán decorativo", "+6% Atracción", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.ATTRACTION_RANGE, 0.06f));

        // Raros
        globalPool.add(new GlobalStatPowerUp("rareWrench", "Llave inglesa", "+7% Daño Kinetico", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.KINETIC_DMG, 0.07f));
        globalPool.add(new GlobalStatPowerUp("rareBattery", "Batería", "+8% Daño Energía", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.ENERGY_DMG, 0.08f));
        globalPool.add(new GlobalStatPowerUp("rareLighter", "Mechero trucado", "+6% Daño Explosivo", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.EXPLOSIVE_DMG, 0.06f));
        globalPool.add(new GlobalStatPowerUp("rareMatchbox", "Caja de cerillas", "+8% Daño Igneo", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.FIRE_DMG, 0.08f));
        globalPool.add(new GlobalStatPowerUp("rareFloorCleaner", "Friegasuelos", "+9% Daño Veneno", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.POISON_DMG, 0.09f));
        globalPool.add(new GlobalStatPowerUp("rareLemonGranita", "Granizado de limón", "+9% Daño Hielo", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.ICE_DMG, 0.09f));
        globalPool.add(new GlobalStatPowerUp("rarePear", "Pera", "+10% Vida Máxima", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.MAX_HP_PERCENT, 0.10f));
        globalPool.add(new GlobalStatPowerUp("rareGlobe", "Globo terráqueo", "+9% Ganancia de XP", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.XP_GAIN_PERCENT, 0.09f));
        globalPool.add(new GlobalStatPowerUp("rareEnergyDrink", "Bebida energética", "+10% Velocidad de Movimiento", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.SPEED, 0.10f));
        globalPool.add(new GlobalStatPowerUp("rareCarpentry", "Martillo de carpintero", "+3% Prob. Crítico", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.CRIT_CHANCE, 0.03f));
        globalPool.add(new GlobalStatPowerUp("rareTikiLaw", "1ª Ley de Tiki", "+1% de Suerte", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.LUCK, 0.01f));
        globalPool.add(new GlobalStatPowerUp("rareSyrup", "Jarabe caducado", "+3% Regeneración", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.LIFE_REGEN, 0.03f));
        globalPool.add(new GlobalStatPowerUp("rareGum", "Chicle del suelo", "+15% Atracción", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.ATTRACTION_RANGE, 0.15f));
        globalPool.add(new GlobalStatPowerUp("rareStraw", "Pajita de papel", "+1% Robo de vida", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.LIFE_LEECH, 0.01f));

        // Especiales
        globalPool.add(new GlobalStatPowerUp("especialDrill", "Taladro", "+15% Daño Kinetico", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.KINETIC_DMG, 0.15f));
        globalPool.add(new GlobalStatPowerUp("especialClamps", "Pinzas de arranque", "+16% Daño Energía", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.ENERGY_DMG, 0.16f));
        globalPool.add(new GlobalStatPowerUp("especialPetrolCan", "Bidón de gasolina", "+13% Daño Explosivo", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.EXPLOSIVE_DMG, 0.13f));
        globalPool.add(new GlobalStatPowerUp("especialBlowtorch", "Soplete doméstico", "+18% Daño Igneo", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.FIRE_DMG, 0.18f));
        globalPool.add(new GlobalStatPowerUp("especialMushroom", "Seta del jardín", "+20% Daño Veneno", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.POISON_DMG, 0.20f));
        globalPool.add(new GlobalStatPowerUp("especialSnowman", "Sr Nievefría", "+20% Daño Hielo", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.ICE_DMG, 0.20f));
        globalPool.add(new GlobalStatPowerUp("especialHamburguer", "Hamburguesa sin tomate", "+17% Vida Máxima", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.MAX_HP_PERCENT, 0.17f));
        globalPool.add(new GlobalStatPowerUp("especialPuzzle", "Piezas de puzzle", "+15% Ganancia de XP", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.XP_GAIN_PERCENT, 0.15f));
        globalPool.add(new GlobalStatPowerUp("especialSkates", "Patines viejos", "+18% Velocidad de Movimiento", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.SPEED, 0.18f));
        globalPool.add(new GlobalStatPowerUp("especialDarts", "Dardos", "+5% Prob. Crítico", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.CRIT_CHANCE, 0.05f));
        globalPool.add(new GlobalStatPowerUp("especialTikiLaw", "2ª Ley de Tiki", "+3% de Suerte", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.LUCK, 0.03f));
        globalPool.add(new GlobalStatPowerUp("especialHoney", "Bote de miel", "+5% Regeneración", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.LIFE_REGEN, 0.05f));
        globalPool.add(new GlobalStatPowerUp("especialCologne", "Colonia de papá", "+25% Atracción", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.ATTRACTION_RANGE, 0.25f));
        globalPool.add(new GlobalStatPowerUp("especialSponge", "Esponja del abuelo", "+3% Robo de vida", PowerUp.Rarity.ESPECIAL, GlobalStatPowerUp.StatType.LIFE_LEECH, 0.03f));

        // Épicos (2 stats)
        ObjectMap<GlobalStatPowerUp.StatType, Float> gasTank = new ObjectMap<>();
        gasTank.put(GlobalStatPowerUp.StatType.EXPLOSIVE_DMG, 0.15f);
        gasTank.put(GlobalStatPowerUp.StatType.FIRE_DMG, 0.20f);
        globalPool.add(new MultiStatPowerUp("epicGasCan", "Tanque de gas", "+15% Daño explosivo y +20% Daño Igneo", PowerUp.Rarity.EPICO, gasTank));

        ObjectMap<GlobalStatPowerUp.StatType, Float> prehistoricVirus = new ObjectMap<>();
        prehistoricVirus.put(GlobalStatPowerUp.StatType.POISON_DMG, 0.22f);
        prehistoricVirus.put(GlobalStatPowerUp.StatType.ICE_DMG, 0.24f);
        globalPool.add(new MultiStatPowerUp("epicPrehistoricVirus", "Virus prehistórico", "+22% Daño Veneno y +24% Daño Hielo", PowerUp.Rarity.EPICO, prehistoricVirus));

        ObjectMap<GlobalStatPowerUp.StatType, Float> sailorSuit = new ObjectMap<>();
        sailorSuit.put(GlobalStatPowerUp.StatType.MAX_HP_PERCENT, 0.20f);
        sailorSuit.put(GlobalStatPowerUp.StatType.LIFE_REGEN, 0.07f);
        globalPool.add(new MultiStatPowerUp("epicSailorSuit", "Traje de marinero", "+20% Vida máxima y +7% Regeneración", PowerUp.Rarity.EPICO, sailorSuit));

        ObjectMap<GlobalStatPowerUp.StatType, Float> washingMachineMotor = new ObjectMap<>();
        washingMachineMotor.put(GlobalStatPowerUp.StatType.KINETIC_DMG, 0.18f);
        washingMachineMotor.put(GlobalStatPowerUp.StatType.ENERGY_DMG, 0.20f);
        globalPool.add(new MultiStatPowerUp("epicMotor", "Motor de la lavadora", "+18% Daño Kinetico y +20% Daño Energía", PowerUp.Rarity.EPICO, washingMachineMotor));

        ObjectMap<GlobalStatPowerUp.StatType, Float> butterflyNet = new ObjectMap<>();
        butterflyNet.put(GlobalStatPowerUp.StatType.LIFE_LEECH, 0.05f);
        butterflyNet.put(GlobalStatPowerUp.StatType.CRIT_CHANCE, 0.07f);
        globalPool.add(new MultiStatPowerUp("epicNet", "Cazamariposas", "+5% Robo de vida y +7% Prob. Crítico", PowerUp.Rarity.EPICO, butterflyNet));

        ObjectMap<GlobalStatPowerUp.StatType, Float> robotCleaner = new ObjectMap<>();
        robotCleaner.put(GlobalStatPowerUp.StatType.ATTRACTION_RANGE, 0.27f);
        robotCleaner.put(GlobalStatPowerUp.StatType.SPEED, 0.20f);
        globalPool.add(new MultiStatPowerUp("epicRobotCleaner", "Aspirador roomba", "+27% Atracción y +20% Velocidad de Movimiento", PowerUp.Rarity.EPICO, robotCleaner));

        // Legendarios (3 stats)
        ObjectMap<GlobalStatPowerUp.StatType, Float> thirdTikiLaw = new ObjectMap<>();
        thirdTikiLaw.put(GlobalStatPowerUp.StatType.EVASION, 0.15f);
        thirdTikiLaw.put(GlobalStatPowerUp.StatType.LUCK, 0.05f);
        thirdTikiLaw.put(GlobalStatPowerUp.StatType.LIFE_REGEN, 0.07f);
        globalPool.add(new MultiStatPowerUp("legendaryTikiLaw", "3ª Ley de Tiki", "+15% Evasión, +5% de Suerte y +7% Regeneración", PowerUp.Rarity.LEGENDARIO, thirdTikiLaw));

        globalPool.add(new GlobalStatPowerUp("legendaryParcheesi", "Parchís", "+25% Daño elemental", PowerUp.Rarity.LEGENDARIO, GlobalStatPowerUp.StatType.ELEMENTAL_DMG, 0.25f));

        ObjectMap<GlobalStatPowerUp.StatType, Float> brokenMask = new ObjectMap<>();
        brokenMask.put(GlobalStatPowerUp.StatType.LIFE_LEECH, 0.07f);
        brokenMask.put(GlobalStatPowerUp.StatType.MAX_HP_PERCENT, 0.25f);
        brokenMask.put(GlobalStatPowerUp.StatType.SPEED, 0.20f);
        globalPool.add(new MultiStatPowerUp("legendaryBrokenMask", "Máscara rota temerosa", "+7% Robo de vida, +25% Vida máxima y +20% Velocidad de Movimiento", PowerUp.Rarity.LEGENDARIO, brokenMask));

        // Los de armas
        weaponPool.add(new NewWeaponPowerUp("weapon_BallRifle", "Fusil de bolas", "Lanza bolas de parques infantiles.", PowerUp.Rarity.RARO, "BallRifle", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("weapon_ToothpickShotgun", "Escupepalillos", "Disparo una gran cantidad de moldadientes.", PowerUp.Rarity.RARO, "ToothpickShotgun", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("weapon_FireworkLauncher", "Pirocohete", "Hora de los fuegos artificiales.", PowerUp.Rarity.RARO, "FireworkLauncher", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("weapon_SubmachineGun", "Clavolleta", "Dispara clavos a gran velocidad.", PowerUp.Rarity.ESPECIAL, "SubmachineGun", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("weapon_TennisLauncher", "Lanzapelotas", "Lanza pelotas de tenis que rebotan.", PowerUp.Rarity.EPICO, "TennisLauncher", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("weapon_IceGrinder", "Triturahielo", "Refresca y ralentiza a los enemigos", PowerUp.Rarity.ESPECIAL, "IceGrinder", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("weapon_Lanzallamas", "Extintor trucado", "Hace lo inverso a un extintor normal", PowerUp.Rarity.EPICO, "Lanzallamas", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("weapon_LanzaSierras", "Lanzadiscos", "Sus CDs golpean en cadena a los enemigos", PowerUp.Rarity.ESPECIAL, "LanzaSierras", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("weapon_Boomerang", "Banana", "Gran fuente de potasio, nunca te abandonará", PowerUp.Rarity.ESPECIAL, "Boomerang", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("weapon_PezGlobo", "Putripez", "Pez putrefacto que causa indigestion.", PowerUp.Rarity.ESPECIAL, "PezGlobo", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("weapon_Saxophone", "Saxofon", "Los enemigos odiaran tu musica.", PowerUp.Rarity.ESPECIAL, "Saxophone", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("weapon_BatteryPlugger", "Enchufe alcalino", "Un rayo letal de gran alcance.", PowerUp.Rarity.EPICO, "BatteryPlugger", this.weaponFactory));
    }

    public Array<PowerUp> rollOptions(Player player, int currentLevel, int optionsCount) {
        Array<PowerUp> options = new Array<>();
        Array<PowerUp> availablePool = new Array<>();

        boolean isWeaponLevel = (currentLevel % 5 == 0);
        int currentWeaponsCount = player.getWeaponFactory().getWeapons().size;

        if (isWeaponLevel && currentWeaponsCount < 6) {

            // --- 1. LÓGICA DE ARMA GARANTIZADA POR REPETICIÓN ---
            Array<Weapon> equippedWeapons = player.getWeaponFactory().getWeapons();
            ObjectMap<String, Integer> weaponCounts = new ObjectMap<>();

            // Contamos cuántas veces está repetida cada arma
            for (Weapon w : equippedWeapons) {
                String wName = w.getName();
                weaponCounts.put(wName, weaponCounts.get(wName, 0) + 1);
            }

            String mostFrequentName = null;
            int maxCount = 0;
            // Identificamos el arma inicial para romper empates (es la de índice 0)
            String startingWeaponName = equippedWeapons.size > 0 ? equippedWeapons.get(0).getName() : null;

            for (ObjectMap.Entry<String, Integer> entry : weaponCounts.entries()) {
                if (entry.value > maxCount) {
                    maxCount = entry.value;
                    mostFrequentName = entry.key;
                } else if (entry.value == maxCount) {
                    // En caso de empate, si una de las empatadas es la inicial, gana la inicial
                    if (entry.key.equals(startingWeaponName)) {
                        mostFrequentName = entry.key;
                    }
                }
            }

            // Buscamos el PowerUp exacto que corresponde al arma ganadora
            NewWeaponPowerUp guaranteedWeapon = null;
            if (mostFrequentName != null) {
                for (NewWeaponPowerUp wp : weaponPool) {
                    if (wp.getName().equals(mostFrequentName)) {
                        guaranteedWeapon = wp;
                        break;
                    }
                }
            }

            // Rellenamos la pool disponible asegurándonos de NO meter el arma garantizada
            for (NewWeaponPowerUp wp : weaponPool) {
                if (isStartingWeaponOfUnlockedCharacter(wp.getWeaponId()) || SaveManager.isWeaponOwned(wp.getWeaponId())) {
                    if (guaranteedWeapon != null && wp.getName().equals(guaranteedWeapon.getName())) {
                        continue;
                    }
                    availablePool.add(wp);
                }
            }

            // Añadimos la garantizada directamente a las opciones mostradas al jugador
            if (guaranteedWeapon != null) {
                options.add(guaranteedWeapon);
                optionsCount--; // Ahora solo nos falta buscar (optionsCount - 1) armas aleatorias
            }

        } else {
            // --- REGLA: RESTO DE NIVELES (MIX GLOBAL + MEJORAS TIER) ---

            // 1. AÑADIMOS LOS GLOBALES FILTRADOS
            for (PowerUp globalUp : globalPool) {
                if (globalUp instanceof GlobalStatPowerUp) {
                    GlobalStatPowerUp statUp = (GlobalStatPowerUp) globalUp;
                    if (statUp.getStatType() == GlobalStatPowerUp.StatType.KINETIC_DMG && !player.hasDamageTypeEquipped(com.tikisadventure.combat.DamageType.KINETIC)) continue;
                    if (statUp.getStatType() == GlobalStatPowerUp.StatType.POISON_DMG && !player.hasDamageTypeEquipped(com.tikisadventure.combat.DamageType.POISON)) continue;
                    if (statUp.getStatType() == GlobalStatPowerUp.StatType.FIRE_DMG && !player.hasDamageTypeEquipped(com.tikisadventure.combat.DamageType.FIRE)) continue;
                    if (statUp.getStatType() == GlobalStatPowerUp.StatType.EXPLOSIVE_DMG && !player.hasDamageTypeEquipped(com.tikisadventure.combat.DamageType.EXPLOSIVE)) continue;
                    if (statUp.getStatType() == GlobalStatPowerUp.StatType.ICE_DMG && !player.hasDamageTypeEquipped(com.tikisadventure.combat.DamageType.ICE)) continue;
                    if (statUp.getStatType() == GlobalStatPowerUp.StatType.ENERGY_DMG && !player.hasDamageTypeEquipped(com.tikisadventure.combat.DamageType.ENERGY)) continue;
                    if (player.isStatCapped(statUp.getStatType())) continue;
                } else if (globalUp instanceof MultiStatPowerUp) {
                    MultiStatPowerUp multiUp = (MultiStatPowerUp) globalUp;
                    boolean tieneTiposDanio = false;
                    boolean tieneAlMenosUnTipo = false;
                    boolean todasCapped = true;
                    for (GlobalStatPowerUp.StatType stat : multiUp.getModifiers().keys()) {
                        if (!player.isStatCapped(stat)) {
                            todasCapped = false;
                        }
                        DamageType tipo = mapStatTypeToDamageType(stat);
                        if (tipo != null) {
                            tieneTiposDanio = true;
                            if (player.hasDamageTypeEquipped(tipo)) {
                                tieneAlMenosUnTipo = true;
                            }
                        }
                    }
                    if (tieneTiposDanio && !tieneAlMenosUnTipo) continue;
                    if (todasCapped) continue;
                }
                availablePool.add(globalUp);
            }

            // 2. AÑADIMOS DINÁMICAMENTE MEJORAS DE TIER
            for (Weapon w : player.getWeaponFactory().getWeapons()) {
                if (w.getTier() < 5) {
                    availablePool.add(new WeaponUpgradePowerUp(w));
                }
            }
        }

        // --- RULETA DE PESOS CON SISTEMA DE SUERTE ---
        int numToSelect = Math.min(optionsCount, availablePool.size);
        float luckPercent = Math.min(1.0f, Math.max(0.0f, player.getLuck())); // Clampeado entre 0 y 1 (0% a 100%)

        for (int i = 0; i < numToSelect; i++) {

            // 1. Tirada de Suerte para esta carta específica
            boolean isLuckyRoll = com.badlogic.gdx.math.MathUtils.random() < luckPercent;

            Array<PowerUp> filteredPool = new Array<>();

            // 2. Filtramos la pool en base a si hemos tenido suerte o no
            for (PowerUp p : availablePool) {
                if (isLuckyRoll) {
                    // Si hubo suerte, SOLO metemos a la pool los Épicos, Legendarios o Mejoras de Arma de Tier Alto
                    if (p.getRarity() == PowerUp.Rarity.EPICO ||
                        p.getRarity() == PowerUp.Rarity.LEGENDARIO ||
                        (p instanceof WeaponUpgradePowerUp)) { // Mantenemos las mejoras de armas porque son vitales
                        filteredPool.add(p);
                    }
                } else {
                    // Si no hubo suerte (o se falló el porcentaje), juegan todos normalmente
                    filteredPool.add(p);
                }
            }

            // Fallback de seguridad: Si con suerte nos quedamos sin cartas (ej: ya tiene todos los épicos/legendarios)
            if (filteredPool.isEmpty()) {
                filteredPool.addAll(availablePool);
            }

            // 3. Calcular el peso total de la pool filtrada
            int totalWeight = 0;
            for (PowerUp p : filteredPool) {
                int pesoReal = p.getRarity().weight;
                if (p instanceof WeaponUpgradePowerUp) {
                    pesoReal = Math.max(1, pesoReal / 2);
                }
                totalWeight += pesoReal;
            }

            if (totalWeight <= 0) break;

            // 4. Tirar los dados clásicos
            int randomValue = com.badlogic.gdx.math.MathUtils.random(0, totalWeight - 1);
            int currentWeightSum = 0;

            // 5. Buscar el ganador
            for (int j = 0; j < filteredPool.size; j++) {
                PowerUp p = filteredPool.get(j);

                int pesoReal = p.getRarity().weight;
                if (p instanceof WeaponUpgradePowerUp) {
                    pesoReal = Math.max(1, pesoReal / 2);
                }

                currentWeightSum += pesoReal;

                if (currentWeightSum > randomValue) {
                    options.add(p);
                    availablePool.removeValue(p, true); // Lo borramos de la pool GENERAL para que no se repita en la siguiente carta

                    // --- CONDICIÓN BLINDADA: MÁXIMO 1 MEJORA DE TIER POR LEVEL UP ---
                    if (p instanceof WeaponUpgradePowerUp) {
                        for (int k = availablePool.size - 1; k >= 0; k--) {
                            if (availablePool.get(k) instanceof WeaponUpgradePowerUp) {
                                availablePool.removeIndex(k);
                            }
                        }
                    }

                    break; // Pasamos a la siguiente carta
                }
            }
        }

        return options;
    }

    private DamageType mapStatTypeToDamageType(GlobalStatPowerUp.StatType stat) {
        switch(stat) {
            case KINETIC_DMG: return DamageType.KINETIC;
            case FIRE_DMG: return DamageType.FIRE;
            case POISON_DMG: return DamageType.POISON;
            case EXPLOSIVE_DMG: return DamageType.EXPLOSIVE;
            case ICE_DMG: return DamageType.ICE;
            case ENERGY_DMG: return DamageType.ENERGY;
            default: return null;
        }
    }

    private boolean isStartingWeaponOfUnlockedCharacter(String weaponId) {
        if (weaponId.equals("BallRifle")) return true;
        if (weaponId.equals("FireworkLauncher")) return SaveManager.isCharacterUnlocked(2);
        if (weaponId.equals("ToothpickShotgun")) return SaveManager.isCharacterUnlocked(3);
        return false;
    }
}
