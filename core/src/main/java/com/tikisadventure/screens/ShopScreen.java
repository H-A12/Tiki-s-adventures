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
    private Map<String, ItemSlot> itemSlots;
    private Label coinsLabel;
    private Runnable onPurchaseCallback;

    private static class ItemSlot {
        Button button;
        Image spriteImage;
        Label priceLabel;
        String itemId;
        int price;
        boolean owned;
        boolean isGadget;
    }

    public ShopScreen(Skin skin, Runnable onPurchaseCallback) {
        super("TIENDA", skin);
        this.skin = skin;
        this.onPurchaseCallback = onPurchaseCallback;
        this.itemSlots = new HashMap<>();

        setModal(true);
        setMovable(true);
        setColor(new Color(0.15f, 0.15f, 0.15f, 0.95f));

        Table mainTable = new Table();
        mainTable.pad(15);

        // --- CABECERA ---
        coinsLabel = new Label("Monedas: " + SaveManager.getProfileData().coins, skin);
        coinsLabel.setAlignment(Align.center);
        mainTable.add(coinsLabel).colspan(2).padBottom(10).row();

        // --- PESTAÑAS (TABS) ---
        TextButton btnTabArmas = new TextButton("ARMAS", skin);
        TextButton btnTabGadgets = new TextButton("GADGETS", skin);

        Table tabTable = new Table();
        tabTable.add(btnTabArmas).width(120).height(35).padRight(10);
        tabTable.add(btnTabGadgets).width(120).height(35);
        mainTable.add(tabTable).colspan(2).padBottom(10).row();

        // --- CONTENIDO (GRIDS) ---
        final Table weaponsGrid = new Table();
        populateWeapons(weaponsGrid);

        final Table gadgetsGrid = new Table();
        populateGadgets(gadgetsGrid);

        // Usamos ScrollPane con tamaño fijo para que la ventana no crezca infinitamente
        final ScrollPane scrollPane = new ScrollPane(weaponsGrid, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        mainTable.add(scrollPane).width(420).height(240).colspan(2).padBottom(15).row();

        // --- LÓGICA DE LAS PESTAÑAS ---
        btnTabArmas.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                scrollPane.setActor(weaponsGrid);
            }
        });

        btnTabGadgets.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                scrollPane.setActor(gadgetsGrid);
            }
        });

        // --- BOTÓN VOLVER ---
        TextButton btnVolver = new TextButton("Volver", skin);
        btnVolver.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                remove();
            }
        });
        mainTable.add(btnVolver).colspan(2).width(150);

        add(mainTable);
        pack();
    }

    private void populateWeapons(Table grid) {
        JsonValue weaponsData = new JsonReader().parse(Gdx.files.internal("data/weapons_config.json"));
        JsonValue weapons = weaponsData.get("weapons");

        int column = 0;
        grid.top().pad(5);

        for (JsonValue weaponEntry : weapons) {
            String weaponId = weaponEntry.name;
            String name = weaponEntry.getString("name", weaponId);
            int price = weaponEntry.getInt("price", 0);
            String spriteName = weaponEntry.getString("sprite", "Machinegun");

            if (weaponId.equals(EXCLUDED_WEAPON)) continue;
            if (isStartingWeapon(weaponId)) continue;
            if ("MELEE".equals(weaponEntry.getString("category", ""))) continue;

            boolean owned = SaveManager.isWeaponOwned(weaponId);

            ItemSlot slot = createItemSlot(weaponId, name, price, spriteName, owned, false);
            itemSlots.put(weaponId, slot);

            grid.add(slot.button).size(120, 140).pad(5);
            column++;

            if (column >= 3) {
                grid.row();
                column = 0;
            }
        }
    }

    private void populateGadgets(Table grid) {
        JsonValue abilitiesData = new JsonReader().parse(Gdx.files.internal("data/abilities_config.json"));

        int column = 0;
        grid.top().pad(5);

        for (JsonValue abilityEntry : abilitiesData) {
            String gadgetId = abilityEntry.name;

            if (!gadgetId.equals("grenade_freeze") && !gadgetId.equals("grenade_cactus") &&
                !gadgetId.equals("grenade_sewer") && !gadgetId.equals("grenade_sheel") &&
                !gadgetId.equals("grenade_scarecrow")) continue;

            String name = abilityEntry.getString("name", gadgetId);
            int price = abilityEntry.getInt("price", 150);

            String spriteName = "weapons_assets/Mint_Gum";
            if (abilityEntry.has("effects")) {
                JsonValue effects = abilityEntry.get("effects");
                for (int i = 0; i < effects.size; i++) {
                    if ("THROW".equals(effects.get(i).getString("type"))) {
                        spriteName = effects.get(i).get("params").getString("sprite", spriteName);
                        break;
                    }
                }
            }

            boolean owned = SaveManager.isGadgetOwned(gadgetId);

            ItemSlot slot = createItemSlot(gadgetId, name, price, spriteName, owned, true);
            itemSlots.put(gadgetId, slot);

            grid.add(slot.button).size(120, 140).pad(5);
            column++;

            if (column >= 3) {
                grid.row();
                column = 0;
            }
        }
    }

    private boolean isStartingWeapon(String weaponId) {
        for (String sw : STARTING_WEAPONS) {
            if (sw.equals(weaponId)) return true;
        }
        return false;
    }

    private ItemSlot createItemSlot(String itemId, String name, int price, String spriteName, boolean owned, boolean isGadget) {
        ItemSlot slot = new ItemSlot();
        slot.itemId = itemId;
        slot.price = price;
        slot.owned = owned;
        slot.isGadget = isGadget;

        Table slotTable = new Table();

        TextureRegion region = Assets.getRegion("shared", spriteName);
        if (region == null) {
            if (spriteName.startsWith("weapons_assets/")) {
                region = Assets.getRegion("shared", spriteName.replace("weapons_assets/", ""));
            } else if (spriteName.startsWith("particle_assets/")) {
                String simpleName = spriteName.replace("particle_assets/", "");
                region = Assets.getRegion("shared", "weapons_assets/" + simpleName);
            }
        }

        slot.spriteImage = new Image(region != null ? region : Assets.getRegion("shared", "UI_assets/UI_Crosshair"));
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
            slot.spriteImage.setColor(Color.DARK_GRAY);
        } else if (!canAfford) {
            slot.button.setColor(Color.GRAY);
            slot.button.setDisabled(true);
            slot.spriteImage.setColor(Color.DARK_GRAY);
        } else {
            slot.button.setColor(Color.WHITE);
            slot.button.setDisabled(false);

            final int finalPrice = price;
            final String finalItemId = itemId;
            final String finalName = name;
            final boolean finalIsGadget = isGadget;

            slot.button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (SaveManager.getProfileData().coins < finalPrice) {
                        showInsufficientCoinsDialog(finalName, finalPrice);
                    } else {
                        showPurchaseConfirmation(finalItemId, finalName, finalPrice, finalIsGadget);
                    }
                }
            });
        }

        return slot;
    }

    private void showInsufficientCoinsDialog(String name, int price) {
        int currentCoins = SaveManager.getProfileData().coins;
        Dialog errorDialog = new Dialog("Error", skin);
        errorDialog.text(name + "\nPrecio: " + price + " coins\n\nMonedas insuficientes\nMonedas actuales: " + currentCoins);

        TextButton btnOk = new TextButton("OK", skin);
        btnOk.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                errorDialog.hide();
            }
        });

        errorDialog.getButtonTable().add(btnOk).size(120, 50).pad(10);
        errorDialog.show(getStage());
    }

    private void showPurchaseConfirmation(String itemId, String name, int price, boolean isGadget) {
        int currentCoins = SaveManager.getProfileData().coins;

        Dialog confirmDialog = new Dialog("Confirmar compra", skin);
        confirmDialog.text(name + "\nPrecio: " + price + " coins\n\nMonedas actuales: " + currentCoins);

        TextButton btnSi = new TextButton("COMPRAR", skin);
        btnSi.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                confirmDialog.hide();

                boolean purchased = false;
                if (isGadget) {
                    purchased = SaveManager.purchaseGadget(itemId, price);
                } else {
                    purchased = SaveManager.purchaseWeapon(itemId, price);
                }

                if (purchased) {
                    updateItemSlot(itemId);
                    updateCoinsLabel();

                    String currentUser = SaveManager.getLastUsername();
                    if (currentUser != null && !currentUser.isEmpty()) {
                        com.tikisadventure.database.progress.ProgressRepository progRepo = new com.tikisadventure.database.progress.ProgressRepository();
                        progRepo.actualizarProgreso(currentUser, SaveManager.getProfileData().coins, SaveManager.getProfileData().totalScore, null);

                        long playerId = SaveManager.getProfileData().playerId;
                        if (playerId != -1) {
                            if (!isGadget) {
                                com.tikisadventure.database.inventory.WeaponRepository weaponRepo = new com.tikisadventure.database.inventory.WeaponRepository();
                                weaponRepo.desbloquearArmaBD(playerId, itemId, null);
                            } else {
                                progRepo.desbloquearGadgetBD(playerId, itemId, null);
                            }
                        }
                    }

                    if (onPurchaseCallback != null) onPurchaseCallback.run();
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
        confirmDialog.show(getStage());
    }

    private void updateItemSlot(String itemId) {
        ItemSlot slot = itemSlots.get(itemId);
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
