package com.tikisadventure.core;

import com.badlogic.gdx.utils.Array;
import java.util.HashMap;

public class PlayerData {

    public long playerId = -1; // -1 = local

    public int coins = 0;
    public HashMap<String, Boolean> ownedWeapons = new HashMap<>();

    public boolean unlockedMoko = false;
    public boolean unlockedZuki = false;

    public Array<Integer> topScores = new Array<>();
    public int totalScore = 0;

    public int maxWaveForest = 0;
    public int maxWaveDesert = 0;
    public int maxWaveCave = 0;

    public String lastUsername = "";
    public String lastPassword = "";

    public boolean wasLinkedToCloud = false;

    public PlayerData() {
    }
}
