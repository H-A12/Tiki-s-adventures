package com.tikisadventure.core;

import com.badlogic.gdx.utils.Array;
import java.util.HashMap;

public class PlayerData {

    public long playerId = -1; // -1 = local

    public int coins = 0;
    public HashMap<String, Boolean> ownedWeapons = new HashMap<>();
    public java.util.HashMap<String, Boolean> ownedGadgets = new java.util.HashMap<>();
    public String selectedGadget = "grenade_kinetic"; // Gadget equipado por defecto

    public boolean unlockedMoko = false;
    public boolean unlockedZuki = false;

    public Array<Integer> topScores = new Array<>();
    public int totalScore = 0;


    public boolean unlockedDesert = false;
    public boolean unlockedCave = false;

    public int maxStageForest = 1;
    public int maxWaveForest = 1;
    public int maxStageDesert = 1;
    public int maxWaveDesert = 1;
    public int maxStageCave = 1;
    public int maxWaveCave = 1;

    public String lastUsername = "";
    public String lastPassword = "";

    public boolean wasLinkedToCloud = false;

    public PlayerData() {
    }
}
