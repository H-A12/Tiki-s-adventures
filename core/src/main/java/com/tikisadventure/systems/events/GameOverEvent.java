package com.tikisadventure.systems.events;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.tikisadventure.combat.weapons.Weapon;
import com.tikisadventure.core.GameSession;
import com.tikisadventure.core.SaveManager;
import com.tikisadventure.database.progress.ProgressRepository;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.floors.FloorManager;
import com.tikisadventure.systems.WaveSystem;

public class GameOverEvent {

    public static void processGameOver(Player player, FloorManager floorManager, WaveSystem waveSystem, String waveSectionName) {
        if (GameSession.godMode) return;

        SaveManager.addScoreRankProfileData(player.getScore());

        int stageAlcanzado = floorManager.getCurrentFloor();
        int waveAlcanzada = waveSystem.getCurrentWaveNumber();
        SaveManager.updateMaxProgress(waveSectionName, stageAlcanzado, waveAlcanzada);

        int score = player.getScore();
        if (score > 0) {
            int base = score / 100;
            int multiplier = (int)(Math.random() * 7) + 7;
            int coinsEarned = base * multiplier;
            SaveManager.addCoins(coinsEarned);
        }

        String currentUser = SaveManager.getLastUsername();

        if (currentUser != null && !currentUser.isEmpty()) {
            ProgressRepository progRepo = new ProgressRepository();
            progRepo.actualizarProgreso(currentUser, SaveManager.getProfileData().coins, SaveManager.getProfileData().totalScore, null);

            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{");

            jsonBuilder.append("\"powerup_stats\": {");
            jsonBuilder.append("\"hp\":").append(player.getHealthComponent().maxHealth).append(",");
            jsonBuilder.append("\"kin\":").append(player.getKineticDamageBonus()).append(",");
            jsonBuilder.append("\"exp\":").append(player.getExplosiveDamageBonus()).append(",");
            jsonBuilder.append("\"fue\":").append(player.getFireDamageBonus()).append(",");
            jsonBuilder.append("\"ven\":").append(player.getPoisonDamageBonus()).append(",");
            jsonBuilder.append("\"hie\":").append(player.getIceDamageBonus()).append(",");
            jsonBuilder.append("\"ene\":").append(player.getEnergyDamageBonus()).append(",");
            jsonBuilder.append("\"crt\":").append(player.getCritChanceBonus()).append(",");
            jsonBuilder.append("\"sue\":").append(player.getLuck()).append(",");
            jsonBuilder.append("\"xp\":").append(player.getXpMultiplier()).append(",");
            jsonBuilder.append("\"vel\":").append(player.getSpeed()).append(",");
            jsonBuilder.append("\"atr\":").append(player.getAttractionRange()).append(",");
            jsonBuilder.append("\"rob\":").append(player.getLifeLeechPercent()).append(",");
            jsonBuilder.append("\"reg\":").append(player.getLifeRegenPercent()).append(",");
            jsonBuilder.append("\"eva\":").append(player.getEvasionChance());
            jsonBuilder.append("},");

            jsonBuilder.append("\"weapons_used\": [");
            Array<Weapon> armas = player.getWeaponFactory().getWeapons();
            for (int i = 0; i < armas.size; i++) {
                jsonBuilder.append("\"").append(armas.get(i).getName()).append("\"");
                if (i < armas.size - 1) jsonBuilder.append(",");
            }
            jsonBuilder.append("],");

            jsonBuilder.append("\"kills_detail\": {");
            int count = 0;
            for (ObjectMap.Entry<String, Integer> entry : player.killDetails) {
                jsonBuilder.append("\"").append(entry.key).append("\":").append(entry.value);
                count++;
                if (count < player.killDetails.size) jsonBuilder.append(",");
            }
            jsonBuilder.append("}");

            jsonBuilder.append("}");
            String extraDataJson = jsonBuilder.toString();

            String charId = GameSession.selectedCharacterId;
            String gadgetId = SaveManager.getEquippedGadget();
            if (gadgetId == null || gadgetId.isEmpty()) gadgetId = "grenade_kinetic";

            progRepo.guardarPartidaBD(
                currentUser, waveSectionName, charId, gadgetId,
                score, stageAlcanzado, waveAlcanzada, player.totalKills,
                extraDataJson, null
            );
        }
    }
}
