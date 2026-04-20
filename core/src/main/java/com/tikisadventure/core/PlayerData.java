package com.tikisadventure.core;

import com.badlogic.gdx.utils.Array;

public class PlayerData {
    //Variables a guardar
    public int coins = 0;

    // Top 5 de puntuaciones
    public Array<Integer> topScores = new Array<>();

    // Constructor
    public PlayerData() {
    }
}
