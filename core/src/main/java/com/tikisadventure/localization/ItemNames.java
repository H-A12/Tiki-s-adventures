package com.tikisadventure.localization;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

//Nombres traducidos de armas, proyectiles y efectos
public class ItemNames {

    //Nombre de arma por ID
    public static String getWeaponName(String weaponId) {
        return LanguageManager.t("weapon.name." + weaponId);
    }

    //Nombre de gadget por ID
    public static String getGadgetName(String gadgetId) {
        return LanguageManager.t("gadget.name." + gadgetId);
    }

    //IDs de skins de armas
    public static final String[] WEAPON_SKIN_IDS = {
        "handgun", "ballrifle", "rocketlauncher", "toothpickshotgun",
        "nailgun", "disclauncher", "tennislauncher", "extinguisher",
        "icegrinder", "rottenfish", "banana", "saxophone", "lasergun", "sword"
    };

    //Lista de nombres de skins de armas
    public static Array<String> getAllWeaponSkinNames() {
        Array<String> names = new Array<>();
        for (String id : WEAPON_SKIN_IDS) {
            names.add(LanguageManager.t("customgun.skin.weapon." + id));
        }
        return names;
    }

    //Buscar ID de skin por su nombre
    public static String getWeaponSkinIdByDisplay(String displayName) {
        for (String id : WEAPON_SKIN_IDS) {
            if (LanguageManager.t("customgun.skin.weapon." + id).equals(displayName)) {
                return id;
            }
        }
        return "handgun";
    }

    //IDs de skins de proyectiles
    public static final String[] PROJECTILE_SKIN_IDS = {
        "gray_bullet", "green_bullet", "red_bullet", "white_bullet",
        "yellow_bullet", "blue_bullet", "blue_laser", "bullet_casing",
        "saw_bullet", "shotgun_casing", "spark_bullet", "toothpick_bullet",
        "tennis_bullet", "popcorn", "ice_bullet", "flame_bullet",
        "music_note", "disc", "rocket_bullet", "spike_fish",
        "banana", "pebble", "fur_ball"
    };

    //Lista de nombres de skins de proyectiles
    public static Array<String> getAllProjectileSkinNames() {
        Array<String> names = new Array<>();
        for (String id : PROJECTILE_SKIN_IDS) {
            names.add(LanguageManager.t("customgun.skin.projectile." + id));
        }
        return names;
    }

    //Buscar ID de proyectil por su nombre
    public static String getProjectileSkinIdByDisplay(String displayName) {
        for (String id : PROJECTILE_SKIN_IDS) {
            if (LanguageManager.t("customgun.skin.projectile." + id).equals(displayName)) {
                return id;
            }
        }
        return "gray_bullet";
    }

    //IDs de efectos
    public static final String[] EFFECT_IDS = {"none", "burn", "poison", "freeze"};

    //Lista de nombres de efectos
    public static Array<String> getAllEffectNames() {
        Array<String> names = new Array<>();
        for (String id : EFFECT_IDS) {
            names.add(LanguageManager.t("customgun.effect." + id));
        }
        return names;
    }

    //Buscar ID de efecto por su nombre
    public static String getEffectIdByDisplay(String displayName) {
        for (String id : EFFECT_IDS) {
            if (LanguageManager.t("customgun.effect." + id).equals(displayName)) {
                return id;
            }
        }
        return "none";
    }

    //IDs de comportamientos
    public static final String[] BEHAVIOR_IDS = {
        "normal", "bounce", "zigzag", "shotgun", "explosive", "chain", "boomerang", "triple"
    };

    //Lista de nombres de comportamientos
    public static Array<String> getAllBehaviorNames() {
        Array<String> names = new Array<>();
        for (String id : BEHAVIOR_IDS) {
            names.add(LanguageManager.t("customgun.behavior." + id));
        }
        return names;
    }

    //Buscar ID de comportamiento por su nombre
    public static String getBehaviorIdByDisplay(String displayName) {
        for (String id : BEHAVIOR_IDS) {
            if (LanguageManager.t("customgun.behavior." + id).equals(displayName)) {
                return id;
            }
        }
        return "normal";
    }
}
