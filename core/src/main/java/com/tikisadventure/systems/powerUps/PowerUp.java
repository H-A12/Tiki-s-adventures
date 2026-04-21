package com.tikisadventure.systems.powerUps;

import com.tikisadventure.entities.player.Player;

//Clase base de PowerUps
public abstract class PowerUp {
    public enum Rarity { COMUN(100), RARO(50), EPICO(20), LEGENDARIO(5);
        public final int weight; // Peso para la probabilidad
        Rarity(int weight) { this.weight = weight; }
    }

    private String name;
    private String description;
    private Rarity rarity;

    public PowerUp(String name, String description, Rarity rarity) {
        this.name = name;
        this.description = description;
        this.rarity = rarity;
    }

    public abstract void apply(Player player);

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Rarity getRarity() { return rarity; }
}
