package com.tikisadventure.core;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.input.InputConfig;
import java.util.HashMap;

public class PlayerData {
    public InputConfig inputConfig = new InputConfig();
    public int coins = 0;
    public HashMap<String, Boolean> ownedWeapons = new HashMap<>();

    public boolean unlockedMoko = false;
    public boolean unlockedZuki = false;

    public Array<Integer> topScores = new Array<>();
    public int globalScore = 0;

    public int maxWaveForest = 0;
    public int maxWaveDesert = 0;
    public int maxWaveCave = 0;

    public PlayerData() {
    }
}
