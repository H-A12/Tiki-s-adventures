package com.tikisadventure.input;

import java.util.HashMap;

public class InputConfig {
    // Teclado: Accion -> KeyCode
    public HashMap<String, Integer> keyboardMapping = new HashMap<>();
    
    // Mando: Accion -> ButtonCode
    public HashMap<String, Integer> controllerMapping = new HashMap<>();

    public InputConfig() {
        // Inicializar con valores por defecto
        keyboardMapping.put("up", com.badlogic.gdx.Input.Keys.W);
        keyboardMapping.put("down", com.badlogic.gdx.Input.Keys.S);
        keyboardMapping.put("left", com.badlogic.gdx.Input.Keys.A);
        keyboardMapping.put("right", com.badlogic.gdx.Input.Keys.D);
        keyboardMapping.put("interact", com.badlogic.gdx.Input.Keys.E);
        keyboardMapping.put("ability1", com.badlogic.gdx.Input.Keys.SPACE);
        keyboardMapping.put("ability2", com.badlogic.gdx.Input.Buttons.RIGHT); // O un código especial
    }
}
