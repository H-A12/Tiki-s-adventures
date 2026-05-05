package com.tikisadventure.input;

import com.badlogic.gdx.Input;
import java.util.LinkedHashMap;
import java.util.Map;

public class InputConfig {
    public Map<String, Integer> keyboardMapping = new LinkedHashMap<>();
    public Map<String, Integer> gamepadButtonMapping = new LinkedHashMap<>();
    public Map<String, Integer> gamepadAxisMapping = new LinkedHashMap<>();
    public static final float CONFIG_DEADZONE = 0.5f;

    public InputConfig() {
        resetToDefaults();
    }

    public void resetToDefaults() {
        keyboardMapping.clear();
        keyboardMapping.put("up", Input.Keys.W);
        keyboardMapping.put("down", Input.Keys.S);
        keyboardMapping.put("left", Input.Keys.A);
        keyboardMapping.put("right", Input.Keys.D);
        keyboardMapping.put("interact", Input.Keys.E);
        keyboardMapping.put("ability1", Input.Keys.SPACE);
        keyboardMapping.put("ability2", Input.Buttons.RIGHT);
        keyboardMapping.put("manualAim", Input.Buttons.LEFT);
        keyboardMapping.put("toggleAutoFire", Input.Keys.F);

        gamepadButtonMapping.clear();
        gamepadButtonMapping.put("interact", 0); // A
        gamepadButtonMapping.put("ability1", 4); // LB
        gamepadButtonMapping.put("ability2", 3); // Y
        gamepadButtonMapping.put("toggleAutoFire", 6); // Back/Select
        
        gamepadAxisMapping.clear();
        gamepadAxisMapping.put("moveX", 0);
        gamepadAxisMapping.put("moveY", 1);
        gamepadAxisMapping.put("aimX", 2);
        gamepadAxisMapping.put("aimY", 3);
    }

    public static boolean isValidInput(int code, boolean isButton) {
        if (isButton) {
            // Botones de ratón válidos en libGDX suelen ser 0-4
            return code >= 0 && code <= 4;
        } else {
            // Teclas de teclado válidas
            return code >= 0 && code <= Input.Keys.MAX_KEYCODE;
        }
    }
}
