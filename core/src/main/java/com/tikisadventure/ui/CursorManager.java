package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.tikisadventure.core.Assets;
import com.tikisadventure.input.InputHandler;
import com.tikisadventure.entities.player.Player;

public class CursorManager {
    private VirtualCursorActor cursorActor;
    private TextureRegion crosshairRegion;

    public CursorManager() {
        this.cursorActor = new VirtualCursorActor();
        this.crosshairRegion = Assets.getRegion("shared", "UI_assets/UI_Crosshair");
    }

    public void update(InputHandler handler, Player player, boolean manualAimHeld, Vector2 mouseWorld) {
        boolean isController = handler.lastInputSource == InputHandler.InputSource.CONTROLLER;
        boolean isManualAiming = manualAimHeld || (player != null && player.isAiming());
        
        boolean shouldBeVisible = isController || isManualAiming;
        cursorActor.setVisible(shouldBeVisible);
        
        if (!shouldBeVisible) return;

        if (isManualAiming && mouseWorld != null) {
            // Ratón/Apuntado manual
            cursorActor.setPosition(mouseWorld.x - cursorActor.getWidth() / 2f, mouseWorld.y - cursorActor.getHeight() / 2f);
        } else if (isController) {
            // Mando
            if (player != null && player.isAiming()) {
                Vector2 aimPos = player.getAimingTarget();
                cursorActor.setPosition(aimPos.x - cursorActor.getWidth() / 2f, aimPos.y - cursorActor.getHeight() / 2f);
            } else {
                // Modo Menú: Mover cursor con AimDirection
                // Convertimos el porcentaje del stick (-1 a 1) a coordenadas de pantalla
                float screenX = (handler.aimDirection.x + 1) / 2f * Gdx.graphics.getWidth();
                float screenY = (handler.aimDirection.y + 1) / 2f * Gdx.graphics.getHeight();
                cursorActor.setPosition(screenX - cursorActor.getWidth() / 2f, screenY - cursorActor.getHeight() / 2f);
            }
        }
    }

    public void draw(Batch batch, float parentAlpha) {
        if (cursorActor.isVisible()) {
            cursorActor.draw(batch, parentAlpha);
        }
    }

    public VirtualCursorActor getCursorActor() {
        return cursorActor;
    }
}
