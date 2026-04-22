package com.tikisadventure.systems;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.core.SaveManager;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.systems.powerUps.*;

public class PowerUpSystem {

    private static final String[] STARTING_WEAPONS = {"MetralletaEjemplo", "LanzaCohetesEjemplo", "EscopetaEjemplo"};

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
        globalPool.add(new GlobalStatPowerUp("Cerillas", "+3% Ataque Igneo", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.KINETIC_DMG, 0.03f));
        globalPool.add(new GlobalStatPowerUp("Petardos", "+3% Ataque Explosivo", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.EXPLOSIVE_DMG, 0.03f));
        globalPool.add(new GlobalStatPowerUp("Golosinas", "+5% Vida Máxima", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.MAX_HP_PERCENT, 0.05f));
        globalPool.add(new GlobalStatPowerUp("Libro de escuela", "+5% Ganancia de XP", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.XP_GAIN_PERCENT, 0.05f));
        globalPool.add(new GlobalStatPowerUp("Sobre de azúcar", "+5% Velocidad de Movimiento", PowerUp.Rarity.COMUN, GlobalStatPowerUp.StatType.SPEED, 0.05f));

        //Raros
        globalPool.add(new GlobalStatPowerUp("Friegasuelos", "+5% Ataque Veneno", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.POISON_DMG, 0.05f));
        globalPool.add(new GlobalStatPowerUp("Carpintería", "+1% Prob. Crítico", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.CRIT_CHANCE, 0.01f));
        globalPool.add(new GlobalStatPowerUp("ley de Tiki", "+1 de Suerte", PowerUp.Rarity.RARO, GlobalStatPowerUp.StatType.LUCK, 1f));

        //Epicos


        globalPool.add(new GlobalStatPowerUp("Bricomanía", "+3% Prob. Crítico", PowerUp.Rarity.EPICO, GlobalStatPowerUp.StatType.CRIT_CHANCE, 0.03f));

        //Los de armas
        weaponPool.add(new NewWeaponPowerUp("AK-47", "Fusil de asalto rápido", PowerUp.Rarity.COMUN, "MetralletaEjemplo", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Fire Shotgun", "Disparo de perdigones", PowerUp.Rarity.COMUN, "EscopetaEjemplo", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Plasmaneitor", "Rifle de energía láser", PowerUp.Rarity.RARO, "ArmaEnergiaEjemplo", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Extintor trucado", "Hace lo inverso a un extintor normal", PowerUp.Rarity.EPICO, "Lanzallamas", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Sawneitor", "Lanza sierras en zig-zag", PowerUp.Rarity.EPICO, "LanzaSierras", this.weaponFactory));
        weaponPool.add(new NewWeaponPowerUp("Banana", "Gran fuente de potasio", PowerUp.Rarity.RARO, "Banana", this.weaponFactory));

    }

    /**
     * Devuelve X opciones de Power Ups basados en las reglas de tu juego.
     */
    /**
     * Devuelve X opciones de Power Ups basados en las reglas de tu juego.
     */
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
                if (isStartingWeapon(wp.getWeaponId()) || SaveManager.isWeaponOwned(wp.getWeaponId())) {
                    availablePool.add(wp);
                }
            }
        } else {
            // --- REGLA: RESTO DE NIVELES (MIX GLOBAL + MEJORAS TIER) ---

            // 1. ANALIZAR LA BUILD DEL JUGADOR (Armas y Habilidades)
            boolean canUsePoison = false;
            boolean canUseFire = false;
            boolean canUseExplosive = false;

            // A) Analizar las habilidades del personaje (a través de su nombre o id)
            String ab1 = player.getProfile().ability1Name;
            String ab2 = player.getProfile().ability2Name;

            // Buscamos palabras clave en las habilidades basadas en tu JSON
            if (ab1 != null && ab1.contains("Incendiaria")) canUseFire = true;
            if (ab2 != null && ab2.contains("Incendiaria")) canUseFire = true;

            if (ab1 != null && ab1.contains("Explosiva")) canUseExplosive = true;
            if (ab2 != null && ab2.contains("Explosiva")) canUseExplosive = true;

            // B) Analizar las armas equipadas y sus modificadores
            for (Weapon w : player.getWeaponFactory().getWeapons()) {
                // Comprobamos el tipo de daño principal
                if (w.getDamageType() == com.tikisadventure.combat.DamageType.POISON) canUsePoison = true;
                if (w.getDamageType() == com.tikisadventure.combat.DamageType.FIRE) canUseFire = true;

                // Comprobamos los modificadores (por ejemplo, el LanzaCohetes es KINETIC pero tiene ExplosiveModifier)
                for (com.tikisadventure.combat.weapons.ProjectileModifier mod : w.getModifiers()) {
                    if (mod instanceof com.tikisadventure.combat.weapons.modifiers.ExplosiveModifier) {
                        canUseExplosive = true;
                    }
                    // Si tienes un PoisonModifier o BurningModifier como clase, también puedes añadirlo aquí:
                    // if (mod instanceof com.tikisadventure.combat.weapons.modifiers.BurningModifier) canUseFire = true;
                }
            }

            // 2. AÑADIMOS LOS GLOBALES FILTRADOS
            for (GlobalStatPowerUp globalUp : globalPool) {

                // Filtro Veneno
                if (globalUp.getStatType() == GlobalStatPowerUp.StatType.POISON_DMG && !canUsePoison) {
                    continue;
                }

                // Filtro Fuego
                // Asegúrate de que el StatType de fuego se llame así en tu enum
                if (globalUp.getStatType() == GlobalStatPowerUp.StatType.FIRE_DMG && !canUseFire) {
                    continue;
                }

                // Filtro Explosivo
                if (globalUp.getStatType() == GlobalStatPowerUp.StatType.EXPLOSIVE_DMG && !canUseExplosive) {
                    continue;
                }

                availablePool.add(globalUp);
            }

            // 3. Añadimos dinámicamente mejoras de Tier para las armas que YA posee
            for (Weapon w : player.getWeaponFactory().getWeapons()) {
                if (w.getTier() < 5) {
                    availablePool.add(new WeaponUpgradePowerUp(w));
                }
            }
        }

        // --- RULETA ALEATORIA ---
        availablePool.shuffle();
        for (int i = 0; i < Math.min(optionsCount, availablePool.size); i++) {
            options.add(availablePool.get(i));
        }

        return options;
    }

    private boolean isStartingWeapon(String weaponId) {
        for (String sw : STARTING_WEAPONS) {
            if (sw.equals(weaponId)) return true;
        }
        return false;
    }
}
