package com.tikisadventure.input;

import com.badlogic.gdx.Input;
import java.util.LinkedHashMap;
import java.util.Map;

public class InputConfig {
    public Map<String, Integer> keyboardMapping = new LinkedHashMap<>();
    public float mouseSize = 1.0f;

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
        keyboardMapping.put("toggleStats", Input.Keys.TAB);
    }

    public void ensureDefaults() {
        if (!keyboardMapping.containsKey("toggleStats")) {
            keyboardMapping.put("toggleStats", Input.Keys.TAB);
        }
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
