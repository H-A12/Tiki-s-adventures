package com.tikisadventure.developer;

public class DeveloperTricks {

    // ============================================================
    // H — Sube todas las stats del jugador un 25%
    // Ubicacion original: GameScreen.java updateSystemEvents()
    // ============================================================
    // public static void boostPlayerStats(Player player) {
    //     if (player != null) {
    //         DebugStats.add25PercentAllStats(player);
    //     }
    // }

    // ============================================================
    // K — Suicida al jugador (pone health a 0)
    // Ubicacion original: GameScreen.java updateSystemEvents()
    // ============================================================
    // public static void suicidePlayer(Player player) {
    //     if (player != null && player.getHealthComponent() != null) {
    //         player.getHealthComponent().currentHealth = 0;
    //     }
    // }

    // ============================================================
    // C — Re-roll de opciones de powerUps durante level up
    // Ubicacion original: LevelUpUI.java act()
    // ============================================================
    // public static void rerollPowerUps(PowerUpSystem powerUpSystem, Player player, int currentLevel) {
    //     if (powerUpSystem != null && player != null) {
    //         Array<PowerUp> nuevasOpciones = powerUpSystem.rollOptions(player, currentLevel, 3);
    //         // buildCardsUI(nuevasOpciones) requiere referencia a LevelUpUI
    //     }
    // }
}
