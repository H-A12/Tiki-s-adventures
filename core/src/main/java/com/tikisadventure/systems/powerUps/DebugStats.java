package com.tikisadventure.systems.powerUps;

import com.tikisadventure.entities.player.Player;

public class DebugStats {
    public static void add25PercentAllStats(Player player) {
        if (player == null) return;

        float val = 0.25f;

        player.addKineticDamageBonus(val);
        player.addExplosiveDamageBonus(val);
        player.addEnergyDamageBonus(val);
        player.addFireDamageBonus(val);
        player.addIceDamageBonus(val);
        player.addPoisonDamageBonus(val);
        player.addCritChanceBonus(val);
        player.addEvasionChance(val);
        player.addLifeRegenPercent(val);
        player.addLifeLeechPercent(val);
        player.setLuck(player.getLuck() + val);
        player.setXpMultiplier(player.getXpMultiplier() + val);
        player.addSpeedPercent(val);
        player.addAttractionRange(0.5f);
    }
}
