package com.tikisadventure.input;

import com.badlogic.gdx.Input;
import java.util.LinkedHashMap;
import java.util.Map;

public class InputConfig {
    public Map<String, Integer> keyboardMapping = new LinkedHashMap<>();
    public Map<String, Integer> controllerMapping = new LinkedHashMap<>();

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

        controllerMapping.clear();
        controllerMapping.put("interact", 0); // A button
        controllerMapping.put("dash", 1);     // B button
        controllerMapping.put("ability1", 4); // LB
        controllerMapping.put("ability2", 3); // Y button
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
