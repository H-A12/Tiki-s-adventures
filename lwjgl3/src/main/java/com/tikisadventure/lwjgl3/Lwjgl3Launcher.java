package com.tikisadventure.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.tikisadventure.core.TikiGame;

    /** Lanza la aplicación de escritorio en modo (LWJGL3) */
public class Lwjgl3Launcher {
    public static void main(String[] args) {
        if (StartupHelper.startNewJvmIfRequired()) return; // This handles macOS support and helps on Windows.
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new TikiGame(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();

        //Titulo de la ventana del juego
        configuration.setTitle("Tiki's Adventure");

        //Limita los FPS
        configuration.useVsync(true);

        //Remueve esto y desactiva el Vsync para tener fps ilimitados, util para testear, pero arriesgado
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);

        //Estos archivos se pueden cambiar. Ruta: lwjgl3/src/main/resources/
        configuration.setWindowedMode(1280, 720);

        // Tamaño mínimo, evita colpasar la ventana mñas allá de este punto
        configuration.setWindowSizeLimits(640, 385, -1, -1);

        //Icono del juego en diferentes escalas
        configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");

        /* Mejora la compatibilidad con máquinas Windows que tienen drivers OpenGL defectuosos, Macs
        con Apple Silicon que de todos modos tienen que emular compatibilidad con OpenGL, y más. */
        configuration.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20, 0, 0);

        return configuration;
    }
}
