package com.tikisadventure.input;

import com.badlogic.gdx.Input;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class InputConfig {
    private static final Set<Integer> STANDARD_KEYS = new HashSet<>();

    static {
        for (int k = Input.Keys.A; k <= Input.Keys.Z; k++) {
            STANDARD_KEYS.add(k);
        }
        for (int k = Input.Keys.NUM_0; k <= Input.Keys.NUM_9; k++) {
            STANDARD_KEYS.add(k);
        }
        for (int k = Input.Keys.NUMPAD_0; k <= Input.Keys.NUMPAD_9; k++) {
            STANDARD_KEYS.add(k);
        }
        STANDARD_KEYS.add(Input.Keys.UP);
        STANDARD_KEYS.add(Input.Keys.DOWN);
        STANDARD_KEYS.add(Input.Keys.LEFT);
        STANDARD_KEYS.add(Input.Keys.RIGHT);
        STANDARD_KEYS.add(Input.Keys.HOME);
        STANDARD_KEYS.add(Input.Keys.END);
        STANDARD_KEYS.add(Input.Keys.PAGE_UP);
        STANDARD_KEYS.add(Input.Keys.PAGE_DOWN);
        STANDARD_KEYS.add(Input.Keys.INSERT);
        STANDARD_KEYS.add(Input.Keys.FORWARD_DEL);
        STANDARD_KEYS.add(Input.Keys.SHIFT_LEFT);
        STANDARD_KEYS.add(Input.Keys.SHIFT_RIGHT);
        STANDARD_KEYS.add(Input.Keys.CONTROL_LEFT);
        STANDARD_KEYS.add(Input.Keys.CONTROL_RIGHT);
        STANDARD_KEYS.add(Input.Keys.ALT_LEFT);
        STANDARD_KEYS.add(Input.Keys.ALT_RIGHT);
        STANDARD_KEYS.add(Input.Keys.SPACE);
        STANDARD_KEYS.add(Input.Keys.ENTER);
        STANDARD_KEYS.add(Input.Keys.TAB);
        STANDARD_KEYS.add(Input.Keys.BACKSPACE);
        STANDARD_KEYS.add(Input.Keys.ESCAPE);
        STANDARD_KEYS.add(Input.Keys.CAPS_LOCK);
        STANDARD_KEYS.add(Input.Keys.MINUS);
        STANDARD_KEYS.add(Input.Keys.EQUALS);
        STANDARD_KEYS.add(Input.Keys.LEFT_BRACKET);
        STANDARD_KEYS.add(Input.Keys.RIGHT_BRACKET);
        STANDARD_KEYS.add(Input.Keys.SEMICOLON);
        STANDARD_KEYS.add(Input.Keys.APOSTROPHE);
        STANDARD_KEYS.add(Input.Keys.GRAVE);
        STANDARD_KEYS.add(Input.Keys.COMMA);
        STANDARD_KEYS.add(Input.Keys.PERIOD);
        STANDARD_KEYS.add(Input.Keys.SLASH);
        STANDARD_KEYS.add(Input.Keys.BACKSLASH);
    }

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
            return code >= 0 && code <= 4;
        } else {
            return STANDARD_KEYS.contains(code);
        }
    }
}
