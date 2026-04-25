package com.tikisadventure.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.tikisadventure.core.Assets;
import com.tikisadventure.core.SaveManager;
import java.util.HashMap;
import java.util.Map;

public class ShopScreen extends Window {

    private static final String[] STARTING_WEAPONS = {"MetralletaEjemplo", "LanzaCohetesEjemplo", "EscopetaEjemplo"};
    private static final String EXCLUDED_WEAPON = "PlantillaArma";

    private Skin skin;
    private Map<String, WeaponSlot> weaponSlots;
    private Label coinsLabel;
    private Runnable onPurchaseCallback;

    private static class WeaponSlot {
        Button button;
        Image spriteImage;
        Label priceLabel;
        String weaponId;
        int price;
        boolean owned;
    }

    public ShopScreen(Skin skin, Runnable onPurchaseCallback) {
        super("TIENDA", skin);
        this.skin = skin;
        this.onPurchaseCallback = onPurchaseCallback;
        this.weaponSlots = new HashMap<>();

        setModal(true);
        setMovable(true);
        setColor(new Color(0.15f, 0.15f, 0.15f, 0.95f));

        Table mainTable = new Table();
        mainTable.pad(20);

        coinsLabel = new Label("Monedas: " + SaveManager.getProfileData().coins, skin);
        coinsLabel.setAlignment(Align.center);
        mainTable.add(coinsLabel).colspan(3).padBottom(15).row();

        loadWeapons(mainTable);

        TextButton btnVolver = new TextButton("Volver", skin);
        btnVolver.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                remove();
            }
        });
        mainTable.add(btnVolver).colspan(3).padTop(15);

        add(mainTable);
        pack();
    }

    private void loadWeapons(Table table) {
        JsonValue weaponsData = new JsonReader().parse(Gdx.files.internal("data/weapons_config.json"));
        JsonValue weapons = weaponsData.get("weapons");

        Table weaponsGrid = new Table();
        int column = 0;
        int maxColumns = 3;

        for (JsonValue weaponEntry : weapons) {
            String weaponId = weaponEntry.name;
            String name = weaponEntry.getString("name", weaponId);
            int price = weaponEntry.getInt("price", 0);
            String spriteName = weaponEntry.getString("sprite", "Machinegun");

            if (weaponId.equals(EXCLUDED_WEAPON)) continue;
            if (isStartingWeapon(weaponId)) continue;
            if ("MELEE".equals(weaponEntry.getString("category", ""))) continue;

            boolean owned = SaveManager.isWeaponOwned(weaponId);

            WeaponSlot slot = createWeaponSlot(weaponId, name, price, spriteName, owned);
            weaponSlots.put(weaponId, slot);

            weaponsGrid.add(slot.button).size(120, 140).pad(10);
            column++;

            if (column >= maxColumns) {
                weaponsGrid.row();
                column = 0;
            }
        }

        table.add(weaponsGrid).colspan(3);
    }

    private boolean isStartingWeapon(String weaponId) {
        for (String sw : STARTING_WEAPONS) {
            if (sw.equals(weaponId)) return true;
        }
        return false;
    }

    private WeaponSlot createWeaponSlot(String weaponId, String name, int price, String spriteName, boolean owned) {
        WeaponSlot slot = new WeaponSlot();
        slot.weaponId = weaponId;
        slot.price = price;
        slot.owned = owned;

        Table slotTable = new Table();

        TextureRegion weaponRegion = Assets.getRegion("shared", spriteName);
        if (weaponRegion == null) {
            if (spriteName.startsWith("weapons_assets/")) {
                weaponRegion = Assets.getRegion("shared", spriteName.replace("weapons_assets/", ""));
            } else if (spriteName.startsWith("particle_assets/")) {
                String simpleName = spriteName.replace("particle_assets/", "");
                weaponRegion = Assets.getRegion("shared", "weapons_assets/" + simpleName);
            } else {
                weaponRegion = Assets.getRegion("shared", "weapons_assets/" + spriteName);
            }
        }
        slot.spriteImage = new Image(weaponRegion != null ? weaponRegion : Assets.getRegion("shared", "UI_assets/UI_Crosshair"));
        slot.spriteImage.setSize(64, 64);

        int currentCoins = SaveManager.getProfileData().coins;
        boolean canAfford = currentCoins >= price;

        slot.priceLabel = new Label(owned ? "COMPRADO" : price + " coins", skin);
        slot.priceLabel.setAlignment(Align.center);

        slot.button = new Button(skin);
        slot.button.setSize(100, 120);

        slotTable.add(slot.spriteImage).size(64, 64).padTop(10).row();
        slotTable.add(slot.priceLabel).padTop(5);

        slot.button.add(slotTable);

        if (owned) {
            slot.button.setColor(Color.DARK_GRAY);
            slot.button.setDisabled(true);
            slot.button.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
            slot.spriteImage.setColor(Color.DARK_GRAY);
        } else if (!canAfford) {
            slot.button.setColor(Color.GRAY);
            slot.button.setDisabled(true);
            slot.button.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.disabled);
            slot.spriteImage.setColor(Color.DARK_GRAY);
        } else {
            slot.button.setColor(Color.WHITE);
            slot.button.setDisabled(false);
            slot.button.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
            final int finalPrice = price;
            final String finalWeaponId = weaponId;
            final String finalName = name;

            slot.button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (SaveManager.getProfileData().coins < finalPrice) {
                        showInsufficientCoinsDialog(finalName, finalPrice);
                    } else {
                        showPurchaseConfirmation(finalWeaponId, finalName, finalPrice);
                    }
                }
            });
        }

        return slot;
    }

    private void showInsufficientCoinsDialog(String name, int price) {
        int currentCoins = SaveManager.getProfileData().coins;
        Dialog errorDialog = new Dialog("Error", skin);
        errorDialog.text(name + "\nPrecio: " + price + " coins" +
            "\n\nMonedas insuficientes" +
            "\nMonedas actuales: " + currentCoins);

        TextButton btnOk = new TextButton("OK", skin);
        btnOk.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                errorDialog.hide();
            }
        });

        errorDialog.getButtonTable().add(btnOk).size(120, 50).pad(10);
        errorDialog.pack();
        errorDialog.setPosition(Math.round((getStage().getWidth() - errorDialog.getWidth()) / 2f),
            Math.round((getStage().getHeight() - errorDialog.getHeight()) / 2f));
        errorDialog.show(getStage());
    }

    private void showPurchaseConfirmation(String weaponId, String name, int price) {
        int currentCoins = SaveManager.getProfileData().coins;

        Dialog confirmDialog = new Dialog("Confirmar compra", skin) {
            @Override
            protected void result(Object object) {
                if ((boolean) object) {
                    if (SaveManager.purchaseWeapon(weaponId, price)) {
                        updateWeaponSlot(weaponId);
                        updateCoinsLabel();

                        // --- NUEVO: Sincronizar el gasto con la nube ---
                        String currentUser = SaveManager.getLastUsername();
                        if (currentUser != null && !currentUser.isEmpty()) {
                            com.tikisadventure.database.progress.ProgressRepository progRepo = new com.tikisadventure.database.progress.ProgressRepository();
                            progRepo.actualizarProgreso(currentUser, SaveManager.getProfileData().coins, SaveManager.getProfileData().totalScore, null);
                        }

                        if (onPurchaseCallback != null) {
                            onPurchaseCallback.run();
                        }
                    }
                }
            }
        };

        confirmDialog.text(name + "\nPrecio: " + price + " coins\n\nMonedas actuales: " + currentCoins);

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = skin.getFont("default-font");

        TextButton btnSi = new TextButton("COMPRAR", skin);
        btnSi.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                confirmDialog.hide();
                if (SaveManager.purchaseWeapon(weaponId, price)) {
                    updateWeaponSlot(weaponId);
                    updateCoinsLabel();

                    // Sincronizar gasto con la nube
                    String currentUser = SaveManager.getLastUsername();
                    if (currentUser != null && !currentUser.isEmpty()) {
                        // 1. Sincronizamos las monedas restadas
                        com.tikisadventure.database.progress.ProgressRepository progRepo = new com.tikisadventure.database.progress.ProgressRepository();
                        progRepo.actualizarProgreso(currentUser, SaveManager.getProfileData().coins, SaveManager.getProfileData().totalScore, null);

                        // 2. Sincronizamos la nueva arma
                        long playerId = SaveManager.getProfileData().playerId;
                        if (playerId != -1) {
                            com.tikisadventure.database.inventory.WeaponRepository weaponRepo = new com.tikisadventure.database.inventory.WeaponRepository();
                            weaponRepo.desbloquearArmaBD(playerId, weaponId, null);
                        }
                    }

                    if (onPurchaseCallback != null) {
                        onPurchaseCallback.run();
                    }
                }
            }
        });

        TextButton btnNo = new TextButton("CANCELAR", skin);
        btnNo.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                confirmDialog.hide();
            }
        });

        confirmDialog.getButtonTable().add(btnSi).size(120, 50).pad(10);
        confirmDialog.getButtonTable().add(btnNo).size(120, 50).pad(10);

        confirmDialog.pack();
        confirmDialog.setPosition(Math.round((getStage().getWidth() - confirmDialog.getWidth()) / 2f),
            Math.round((getStage().getHeight() - confirmDialog.getHeight()) / 2f));
        confirmDialog.show(getStage());
    }

    private void updateWeaponSlot(String weaponId) {
        WeaponSlot slot = weaponSlots.get(weaponId);
        if (slot != null) {
            slot.owned = true;
            slot.button.setColor(Color.DARK_GRAY);
            slot.button.setDisabled(true);
            slot.spriteImage.setColor(Color.DARK_GRAY);
            slot.priceLabel.setText("COMPRADO");
        }
    }

    public void updateCoinsLabel() {
        coinsLabel.setText("Monedas: " + SaveManager.getProfileData().coins);
    }
}
