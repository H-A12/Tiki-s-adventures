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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopScreen extends Window {

    private static final String[] STARTING_WEAPONS = {"BallRifle", "FireworkLauncher", "ToothpickShotgun"};
    private static final String EXCLUDED_WEAPON = "PlantillaArma";

    private Skin skin;
    private Map<String, ItemSlot> itemSlots;
    private Table coinsRow;
    private Label coinsLabel;
    private Runnable onPurchaseCallback;

    // Referencias a los botones de pestaña para poder cambiar su color
    private TextButton btnTabArmas;
    private TextButton btnTabGadgets;

    private static class ItemSlot {
        Button button;
        Image spriteImage;
        Label priceLabel;
        Image coinImage;
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
        setColor(new Color(0.08f, 0.08f, 0.08f, 0.97f));

        Table mainTable = new Table();
        mainTable.pad(15);

        // --- CABECERA ---
        coinsLabel = new Label(String.valueOf(SaveManager.getProfileData().coins), skin);
        coinsLabel.setAlignment(Align.center);
        Image coinImage = new Image(Assets.getRegion("shared", "UI_assets/coin"));
        coinImage.setSize(24, 24);
        coinsRow = new Table();
        coinsRow.add(coinsLabel).padRight(6);
        coinsRow.add(coinImage).size(24, 24);
        mainTable.add(coinsRow).colspan(2).padBottom(10).row();

        // --- PESTAÑAS ---
        btnTabArmas = new TextButton("ARMAS", skin);
        btnTabArmas.addListener(new Assets.HoverCursorListener());
        btnTabGadgets = new TextButton("GADGETS", skin);
        btnTabGadgets.addListener(new Assets.HoverCursorListener());

        Table tabTable = new Table();
        tabTable.add(btnTabArmas).width(120).height(35).padRight(10);
        tabTable.add(btnTabGadgets).width(120).height(35);
        mainTable.add(tabTable).colspan(2).padBottom(10).row();

        // --- GRIDS ---
        final Table weaponsGrid = new Table();
        populateWeapons(weaponsGrid);

        final Table gadgetsGrid = new Table();
        populateGadgets(gadgetsGrid);

        final ScrollPane scrollPane = new ScrollPane(weaponsGrid, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        mainTable.add(scrollPane).width(420).height(240).colspan(2).padBottom(15).row();

        // --- LÓGICA DE PESTAÑAS ---
        btnTabArmas.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                scrollPane.setActor(weaponsGrid);
                selectTab(btnTabArmas);
            }
        });

        btnTabGadgets.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) {
                scrollPane.setActor(gadgetsGrid);
                selectTab(btnTabGadgets);
            }
        });

        // --- BOTÓN VOLVER ---
        TextButton btnVolver = new TextButton("Volver", skin);
        btnVolver.addListener(new Assets.HoverCursorListener());
        btnVolver.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { remove(); }
        });
        mainTable.add(btnVolver).colspan(2).width(150);

        add(mainTable);

        // Selecciona ARMAS por defecto al abrir
        selectTab(btnTabArmas);
        pack();
    }

    /** Aplica color activo al botón seleccionado e inactivo al otro. */
    private void selectTab(TextButton selected) {
        btnTabArmas.setColor(btnTabArmas == selected
            ? new Color(0.75f, 0.75f, 0.75f, 1f)   // activo  → gris claro
            : new Color(0.4f,  0.4f,  0.4f,  1f));  // inactivo → gris oscuro
        btnTabGadgets.setColor(btnTabGadgets == selected
            ? new Color(0.75f, 0.75f, 0.75f, 1f)
            : new Color(0.4f,  0.4f,  0.4f,  1f));
    }

    private void populateWeapons(Table grid) {
        JsonValue weaponsData = new JsonReader().parse(Gdx.files.internal("data/weapons_config.json"));
        JsonValue weapons = weaponsData.get("weapons");

        List<JsonValue> sorted = new ArrayList<>();
        for (JsonValue w : weapons) sorted.add(w);
        Collections.sort(sorted, (a, b) -> Integer.compare(a.getInt("price", 0), b.getInt("price", 0)));

        int column = 0;
        grid.top().pad(5);

        for (JsonValue weaponEntry : sorted) {
            String weaponId  = weaponEntry.name;
            String name      = weaponEntry.getString("name", weaponId);
            int price        = weaponEntry.getInt("price", 0);
            String spriteName = weaponEntry.getString("sprite", "Machinegun");

            if (weaponId.equals(EXCLUDED_WEAPON)) continue;
            if (isStartingWeapon(weaponId)) continue;
            if ("MELEE".equals(weaponEntry.getString("category", ""))) continue;

            boolean owned = SaveManager.isWeaponOwned(weaponId);
            ItemSlot slot = createItemSlot(weaponId, name, price, spriteName, owned, false);
            itemSlots.put(weaponId, slot);

            grid.add(slot.button).size(120, 140).pad(5);
            if (++column >= 3) { grid.row(); column = 0; }
        }
    }

    private void populateGadgets(Table grid) {
        JsonValue abilitiesData = new JsonReader().parse(Gdx.files.internal("data/abilities_config.json"));

        List<JsonValue> sorted = new ArrayList<>();
        for (JsonValue a : abilitiesData) sorted.add(a);
        Collections.sort(sorted, (a, b) -> Integer.compare(a.getInt("price", 150), b.getInt("price", 150)));

        int column = 0;
        grid.top().pad(5);

        for (JsonValue abilityEntry : sorted) {
            String gadgetId = abilityEntry.name;
            if (!gadgetId.equals("grenade_freeze") && !gadgetId.equals("grenade_cactus") &&
                !gadgetId.equals("grenade_sewer")  && !gadgetId.equals("grenade_sheel")  &&
                !gadgetId.equals("grenade_scarecrow") && !gadgetId.equals("grenade_turret")) continue;

            String name = abilityEntry.getString("name", gadgetId);
            int price   = abilityEntry.getInt("price", 150);

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
            if (++column >= 3) { grid.row(); column = 0; }
        }
    }

    private boolean isStartingWeapon(String weaponId) {
        for (String sw : STARTING_WEAPONS) if (sw.equals(weaponId)) return true;
        return false;
    }

    private ItemSlot createItemSlot(String itemId, String name, int price, String spriteName,
                                    boolean owned, boolean isGadget) {
        ItemSlot slot = new ItemSlot();
        slot.itemId   = itemId;
        slot.price    = price;
        slot.owned    = owned;
        slot.isGadget = isGadget;

        // --- Sprite ---
        TextureRegion region = Assets.getRegion("shared", spriteName);
        if (region == null) {
            if (spriteName.startsWith("weapons_assets/")) {
                region = Assets.getRegion("shared", spriteName.replace("weapons_assets/", ""));
            } else if (spriteName.startsWith("particle_assets/")) {
                region = Assets.getRegion("shared", "weapons_assets/" + spriteName.replace("particle_assets/", ""));
            }
        }
        slot.spriteImage = new Image(region != null ? region : Assets.getRegion("shared", "UI_assets/UI_Crosshair"));
        slot.spriteImage.setSize(64, 64);

        // --- Etiqueta de precio / estado ---
        if (owned) {
            slot.priceLabel = new Label("COMPRADO", skin, "font-12");
        } else {
            slot.priceLabel = new Label(String.valueOf(price), skin);
            slot.coinImage  = new Image(Assets.getRegion("shared", "UI_assets/coin"));
            slot.coinImage.setSize(16, 16);
        }
        slot.priceLabel.setAlignment(Align.center);

        // --- Botón (siempre se crea ANTES de asignar colores) ---
        slot.button = new Button(skin);
        slot.button.setSize(100, 120);
        slot.button.addListener(new Assets.HoverCursorListener());

        // --- Layout interno ---
        Table slotTable = new Table();
        Table priceRow  = new Table();
        priceRow.add(slot.priceLabel);
        if (slot.coinImage != null) priceRow.add(slot.coinImage).size(16, 16).padLeft(4);
        slotTable.add(slot.spriteImage).size(64, 64).padTop(10).row();
        slotTable.add(priceRow).padTop(5);
        slot.button.add(slotTable);

        // --- Colores y comportamiento ---
        int currentCoins  = SaveManager.getProfileData().coins;
        boolean canAfford = currentCoins >= price;

        if (owned) {
            slot.button.setColor(new Color(0.38f, 0.38f, 0.38f, 1f));
            slot.button.setDisabled(true);
            slot.spriteImage.setColor(new Color(0.55f, 0.55f, 0.55f, 1f));

        } else if (!canAfford) {
            slot.button.setColor(new Color(0.45f, 0.2f, 0.2f, 1f));
            slot.button.setDisabled(true);
            slot.spriteImage.setColor(new Color(0.5f, 0.35f, 0.35f, 1f));
            slot.coinImage.setColor(new Color(0.6f, 0.3f, 0.3f, 1f));

        } else {
            slot.button.setColor(new Color(0.3f, 0.65f, 0.35f, 1f));
            slot.button.setDisabled(false);

            final int     finalPrice    = price;
            final String  finalItemId   = itemId;
            final String  finalName     = name;
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

        Table content = new Table();
        content.add(new Label(name, skin)).row();

        Table priceRow = new Table();
        priceRow.add(new Label("Precio: " + price, skin)).padRight(4);
        priceRow.add(new Image(Assets.getRegion("shared", "UI_assets/coin"))).size(16, 16);
        content.add(priceRow).padTop(6).row();

        content.add(new Label("Monedas insuficientes", skin)).padTop(10).row();

        Table currentRow = new Table();
        currentRow.add(new Label("Monedas actuales: " + currentCoins, skin)).padRight(4);
        currentRow.add(new Image(Assets.getRegion("shared", "UI_assets/coin"))).size(16, 16);
        content.add(currentRow).padTop(6);

        errorDialog.getContentTable().clear();
        errorDialog.getContentTable().add(content);
        errorDialog.getContentTable().row();
        errorDialog.pack();

        TextButton btnOk = new TextButton("OK", skin);
        btnOk.addListener(new ClickListener() {
            @Override public void clicked(InputEvent event, float x, float y) { errorDialog.hide(); }
        });
        errorDialog.getButtonTable().add(btnOk).size(120, 50).pad(10);
        errorDialog.show(getStage());
    }

    private void showPurchaseConfirmation(String itemId, String name, int price, boolean isGadget) {
        Dialog confirmDialog = new Dialog("", skin);

        Table priceRow = new Table();
        priceRow.add(new Label(String.valueOf(price), skin)).padRight(4);
        priceRow.add(new Image(Assets.getRegion("shared", "UI_assets/coin"))).size(16, 16);

        Table content = new Table();
        content.add(new Label(name, skin)).row();
        content.add(priceRow).padTop(10);

        confirmDialog.getContentTable().clear();
        confirmDialog.getContentTable().add(content);
        confirmDialog.getContentTable().row();
        confirmDialog.pack();

        TextButton btnSi = new TextButton("COMPRAR", skin);
        btnSi.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                confirmDialog.hide();

                boolean purchased = isGadget
                    ? SaveManager.purchaseGadget(itemId, price)
                    : SaveManager.purchaseWeapon(itemId, price);

                if (purchased) {
                    updateItemSlot(itemId);
                    updateCoinsLabel();

                    String currentUser = SaveManager.getLastUsername();
                    if (currentUser != null && !currentUser.isEmpty()) {
                        com.tikisadventure.database.progress.ProgressRepository progRepo =
                            new com.tikisadventure.database.progress.ProgressRepository();
                        progRepo.actualizarProgreso(currentUser,
                            SaveManager.getProfileData().coins,
                            SaveManager.getProfileData().totalScore, null);

                        long playerId = SaveManager.getProfileData().playerId;
                        if (playerId != -1) {
                            if (!isGadget) {
                                com.tikisadventure.database.inventory.WeaponRepository weaponRepo =
                                    new com.tikisadventure.database.inventory.WeaponRepository();
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
            @Override public void clicked(InputEvent event, float x, float y) { confirmDialog.hide(); }
        });

        confirmDialog.getButtonTable().add(btnSi).size(120, 50).pad(10);
        confirmDialog.getButtonTable().add(btnNo).size(120, 50).pad(10);
        confirmDialog.show(getStage());
    }

    private void updateItemSlot(String itemId) {
        ItemSlot slot = itemSlots.get(itemId);
        if (slot == null) return;

        slot.owned = true;
        slot.button.setColor(new Color(0.38f, 0.38f, 0.38f, 1f));
        slot.button.setDisabled(true);
        slot.spriteImage.setColor(new Color(0.55f, 0.55f, 0.55f, 1f));
        if (slot.coinImage != null) { slot.coinImage.remove(); slot.coinImage = null; }
        slot.priceLabel.setStyle(skin.get("font-12", Label.LabelStyle.class));
        slot.priceLabel.setText("COMPRADO");
    }

    public void updateCoinsLabel() {
        coinsLabel.setText(String.valueOf(SaveManager.getProfileData().coins));
    }
}
