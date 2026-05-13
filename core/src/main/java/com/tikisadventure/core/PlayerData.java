package com.tikisadventure.core;

import com.badlogic.gdx.utils.Array;
import com.tikisadventure.input.InputConfig;
import java.util.HashMap;

public class PlayerData {
    public InputConfig inputConfig = new InputConfig();
    public long playerId = -1;
    public int coins = 0;
    public HashMap<String, Boolean> ownedWeapons = new HashMap<>();
    public com.badlogic.gdx.utils.ObjectMap<String, Boolean> ownedGadgets = new com.badlogic.gdx.utils.ObjectMap<>();
    public String selectedGadget = "grenade_kinetic";
    public String selectedStartingWeapon = null;

    public boolean unlockedMoko = false;
    public boolean unlockedZuki = false;

    public Array<Integer> topScores = new Array<>();
    public int totalScore = 0;


    public boolean unlockedDesert = false;
    public boolean unlockedCastillo = false;

    public int maxStageForest = 1;
    public int maxWaveForest = 1;
    public int maxStageDesert = 1;
    public int maxWaveDesert = 1;
    public int maxStageCastillo = 1;
    public int maxWaveCastillo = 1;

    public String lastUsername = "";
    public String lastPassword = "";

    public boolean wasLinkedToCloud = false;

    public PlayerData() {
    }
}
