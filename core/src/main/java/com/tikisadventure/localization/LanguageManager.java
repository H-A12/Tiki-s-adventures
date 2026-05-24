package com.tikisadventure.localization;

import com.badlogic.gdx.Gdx;
import com.tikisadventure.core.SaveManager;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

//Gestion de textos traducidos segun el idioma
public class LanguageManager {

    private static LanguageManager instance;

    private Properties texts;
    private String currentLang;

    private LanguageManager() {
        texts = new Properties();
    }

    //Obtener singleton
    public static LanguageManager getInstance() {
        if (instance == null) {
            instance = new LanguageManager();
        }
        return instance;
    }

    //Cargar idioma guardado y sus textos
    public void init() {
        currentLang = SaveManager.getLanguage();
        loadBundle();
    }

    //Cargar archivo .properties del idioma
    private void loadBundle() {
        texts.clear();
        String lang = "es".equals(currentLang) ? "es" : "en";
        try {
            Reader reader = new InputStreamReader(
                Gdx.files.internal("data/localization/texts_" + lang + ".properties").read(),
                StandardCharsets.UTF_8
            );
            texts.load(reader);
            reader.close();
        } catch (Exception e) {
            Gdx.app.error("LanguageManager", "Error loading texts_" + lang + ".properties", e);
            //Fallback a espanol
            if (!"es".equals(lang)) {
                try {
                    Reader reader2 = new InputStreamReader(
                        Gdx.files.internal("data/localization/texts_es.properties").read(),
                        StandardCharsets.UTF_8
                    );
                    texts.load(reader2);
                    reader2.close();
                } catch (Exception e2) {
                    Gdx.app.error("LanguageManager", "Error loading fallback texts_es.properties", e2);
                }
            }
        }
    }

    //Obtener texto traducido por clave
    public String get(String key) {
        String value = texts.getProperty(key);
        if (value == null) {
            return key;
        }
        return value;
    }

    //Obtener texto con formato
    public String get(String key, Object... args) {
        String value = get(key);
        return String.format(value, args);
    }

    //Cambiar idioma y recargar textos
    public void setLanguage(String lang) {
        if (lang.equals(currentLang)) return;
        currentLang = lang;
        SaveManager.saveLanguage(lang);
        loadBundle();
    }

    public String getLanguage() {
        return currentLang;
    }

    public boolean isEnglish() {
        return "en".equals(currentLang);
    }

    public boolean isSpanish() {
        return "es".equals(currentLang);
    }

    //Metodo estatico para obtener texto
    public static String t(String key) {
        return getInstance().get(key);
    }

    //Metodo estatico con formato
    public static String t(String key, Object... args) {
        return getInstance().get(key, args);
    }
}
