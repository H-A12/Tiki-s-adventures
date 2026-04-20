package com.tikisadventure.core;

import com.badlogic.gdx.utils.Array;

public class PlayerData {
    //Variables a guardar
    public int coins = 0;

    // Top 5 de puntuaciones
    public Array<Integer> topScores = new Array<>();
    public int globalScore = 0; //Puntuacion global acumulada

    public int maxWaveForest = 0;
    public int maxWaveDesert = 0;
    public int maxWaveCave = 0;

    // Constructor
    public PlayerData() {
    }
}
