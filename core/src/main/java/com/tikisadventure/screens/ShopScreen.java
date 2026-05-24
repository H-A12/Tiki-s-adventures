package com.tikisadventure.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.tikisadventure.core.Assets;
import com.tikisadventure.core.SaveManager;
import com.tikisadventure.localization.ItemNames;
import com.tikisadventure.localization.LanguageManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.tikisadventure.ui.button.ButtonFactory;

//Ventana modal de compra con grid de armas/gadgets y confirmacion
public class ShopScreen extends Window {

    //Armas iniciales gratuitas
    private static final String[] STARTING_WEAPONS = {"BallRifle", "FireworkLauncher", "ToothpickShotgun"};
    //ID de arma excluida de la tienda
    private static final String EXCLUDED_WEAPON = "PlantillaArma";

    //Skin, slots, monedas y callback de compra
    private Skin skin;
    private Map<String, ItemSlot> itemSlots;
    private Table coinsRow;
    private Label coinsLabel;
    private Runnable onPurchaseCallback;

    //Pestanas, scroll y estilo
    private TextButton btnTabArmas;
    private TextButton btnTabGadgets;
    private ScrollPane scrollPane;
    private boolean focusSet;
    private Texture btnTiendaTex;
    private TextButton.TextButtonStyle tabStyle;

    //Bundle de UI para un objeto comprable
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

    /** Construir UI completa de tienda con pestanas y scroll. */
    public ShopScreen(Skin skin, Runnable onPurchaseCallback) {
        super("", skin);
        this.skin = skin;
        this.onPurchaseCallback = onPurchaseCallback;
        this.itemSlots = new HashMap<>();

        setModal(true);
        setMovable(true);
        Image bgImage = new Image(new Texture(Gdx.files.internal("Menu/MenuMapas/VentanaTienda.png")));
        setBackground(bgImage.getDrawable());
        btnTiendaTex = new Texture(Gdx.files.internal("Menu/MenuMapas/BotonTienda.png"));

        Table mainTable = new Table();
        mainTable.pad(30, 20, 20, 20);

        //Cabecera
        coinsLabel = new Label(String.valueOf(SaveManager.getProfileData().coins), skin);
        coinsLabel.setAlignment(Align.center);
        Image coinImage = new Image(Assets.getRegion("shared", "UI_assets/coin"));
        coinImage.setSize(28, 28);
        coinsRow = new Table();
        coinsRow.add(coinsLabel).padRight(8);
        coinsRow.add(coinImage).size(28, 28);
        mainTable.add(coinsRow).colspan(2).padBottom(15).row();

        //Grids
        final Table weaponsGrid = new Table();
        populateWeapons(weaponsGrid);

        final Table gadgetsGrid = new Table();
        populateGadgets(gadgetsGrid);

        //Pestanas
        tabStyle = ButtonFactory.getTextBtnStyle();
        btnTabArmas = new TextButton(LanguageManager.t("shop.tab.weapons"), tabStyle);
        ButtonFactory.configure(btnTabArmas, () -> { scrollPane.setActor(weaponsGrid); selectTab(btnTabArmas); });
        btnTabGadgets = new TextButton(LanguageManager.t("shop.tab.gadgets"), tabStyle);
        ButtonFactory.configure(btnTabGadgets, () -> { scrollPane.setActor(gadgetsGrid); selectTab(btnTabGadgets); });

        Table tabTable = new Table();
        tabTable.add(btnTabArmas).width(155).height(40).padRight(12);
        tabTable.add(btnTabGadgets).width(155).height(40);
        mainTable.add(tabTable).colspan(2).padBottom(15).row();

        scrollPane = new ScrollPane(weaponsGrid, skin);
        ScrollPane.ScrollPaneStyle spStyle = new ScrollPane.ScrollPaneStyle(skin.get(ScrollPane.ScrollPaneStyle.class));
        spStyle.vScroll = null;
        spStyle.vScrollKnob = null;
        spStyle.hScroll = null;
        spStyle.hScrollKnob = null;
        scrollPane.setStyle(spStyle);
        scrollPane.setScrollingDisabled(true, false);

        mainTable.add(scrollPane).width(520).height(220).colspan(2).padBottom(15).row();

        //Boton volver
        TextButton btnVolver = ButtonFactory.createTextButton(LanguageManager.t("shop.back"), () -> {
            addAction(Actions.sequence(Actions.fadeOut(0.2f), Actions.removeActor()));
        });
        mainTable.add(btnVolver).colspan(2).width(150).height(35).padBottom(15);

        add(mainTable);

        // Selecciona ARMAS por defecto al abrir
        selectTab(btnTabArmas);
        pack();

        focusSet = false;
    }

    /** Asignar foco al scroll pane cuando el stage este listo. */
    @Override
    public void act(float delta) {
        super.act(delta);
        if (!focusSet && getStage() != null) {
            getStage().setScrollFocus(scrollPane);
            focusSet = true;
        }
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

    /** Llenar tabla con armas ordenadas por precio. */
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
            String name      = ItemNames.getWeaponName(weaponId);
            int price        = weaponEntry.getInt("price", 0);
            String spriteName = weaponEntry.getString("sprite", "Machinegun");

            if (weaponId.equals(EXCLUDED_WEAPON)) continue;
            if (isStartingWeapon(weaponId)) continue;
            if ("MELEE".equals(weaponEntry.getString("category", ""))) continue;

            boolean owned = SaveManager.isWeaponOwned(weaponId);
            ItemSlot slot = createItemSlot(weaponId, name, price, spriteName, owned, false);
            itemSlots.put(weaponId, slot);

            grid.add(slot.button).size(140, 150).pad(6);
            if (++column >= 3) { grid.row(); column = 0; }
        }
    }

    /** Llenar tabla con gadgets lanzables ordenados por precio. */
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

            String name = ItemNames.getGadgetName(gadgetId);
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

            grid.add(slot.button).size(140, 150).pad(6);
            if (++column >= 3) { grid.row(); column = 0; }
        }
    }

    /** Verificar si es un arma inicial. */
    private boolean isStartingWeapon(String weaponId) {
        for (String sw : STARTING_WEAPONS) if (sw.equals(weaponId)) return true;
        return false;
    }

    /** Crear ItemSlot con sprite, precio, hover y click. */
    private ItemSlot createItemSlot(String itemId, String name, int price, String spriteName,
                                    boolean owned, boolean isGadget) {
        ItemSlot slot = new ItemSlot();
        slot.itemId   = itemId;
        slot.price    = price;
        slot.owned    = owned;
        slot.isGadget = isGadget;

        //Sprite
        TextureRegion region = Assets.getRegion("shared", spriteName);
        if (region == null) {
            if (spriteName.startsWith("weapons_assets/")) {
                region = Assets.getRegion("shared", spriteName.replace("weapons_assets/", ""));
            } else if (spriteName.startsWith("particle_assets/")) {
                region = Assets.getRegion("shared", "weapons_assets/" + spriteName.replace("particle_assets/", ""));
            }
        }
        slot.spriteImage = new Image(region != null ? region : Assets.getRegion("shared", "UI_assets/UI_Crosshair"));
        slot.spriteImage.setSize(72, 72);

        //Precio y estado
        if (owned) {
            slot.priceLabel = new Label(LanguageManager.t("shop.owned"), skin, "font-12");
        } else {
            slot.priceLabel = new Label(String.valueOf(price), skin);
            slot.coinImage  = new Image(Assets.getRegion("shared", "UI_assets/coin"));
            slot.coinImage.setSize(16, 16);
        }
        slot.priceLabel.setAlignment(Align.center);

        //Boton con BotonTienda.png
        Button.ButtonStyle tiendaStyle = new Button.ButtonStyle();
        tiendaStyle.up = new TextureRegionDrawable(new TextureRegion(btnTiendaTex));
        slot.button = new Button(tiendaStyle);
        slot.button.setSize(120, 140);
        slot.spriteImage.setOrigin(Align.center);
        slot.button.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && !slot.button.isDisabled()) {
                    slot.button.clearActions();
                    slot.button.addAction(Actions.color(new Color(0.3f, 0.9f, 0.4f, 1f), 0.15f));
                    slot.spriteImage.clearActions();
                    slot.spriteImage.addAction(Actions.scaleTo(1.15f, 1.15f, 0.15f, Interpolation.sineOut));
                }
                super.enter(event, x, y, pointer, fromActor);
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1 && !slot.button.isDisabled()) {
                    slot.button.clearActions();
                    slot.button.addAction(Actions.color(new Color(0.3f, 0.65f, 0.35f, 1f), 0.15f));
                    slot.spriteImage.clearActions();
                    slot.spriteImage.addAction(Actions.scaleTo(1f, 1f, 0.15f, Interpolation.sineIn));
                }
                super.exit(event, x, y, pointer, toActor);
            }
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (!slot.button.isDisabled()) {
                    slot.button.clearActions();
                    slot.button.addAction(Actions.parallel(
                        Actions.color(new Color(0.9f, 0.2f, 0.2f, 1f), 0.05f),
                        Actions.scaleTo(0.9f, 0.9f, 0.05f, Interpolation.sineOut)
                    ));
                }
                return super.touchDown(event, x, y, pointer, button);
            }
            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                if (!slot.button.isDisabled()) {
                    slot.button.clearActions();
                    slot.button.addAction(Actions.parallel(
                        Actions.color(new Color(0.3f, 0.65f, 0.35f, 1f), 0.1f),
                        Actions.scaleTo(1f, 1f, 0.1f, Interpolation.sineIn)
                    ));
                }
                super.touchUp(event, x, y, pointer, button);
            }
        });

        //Layout interno
        Table slotTable = new Table();
        Table priceRow  = new Table();
        priceRow.add(slot.priceLabel);
        if (slot.coinImage != null) priceRow.add(slot.coinImage).size(16, 16).padLeft(4);
        slotTable.add(slot.spriteImage).size(72, 72).padTop(15).row();
        slotTable.add(priceRow).padTop(5);
        slot.button.add(slotTable);

        //Colores y comportamiento
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

            slot.button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (SaveManager.getProfileData().coins < price) {
                        showInsufficientCoinsDialog(name, price);
                    } else {
                        showPurchaseConfirmation(itemId, name, price, isGadget);
                    }
                }
            });
        }

        return slot;
    }

    /** Mostrar dialogo de monedas insuficientes. */
    private void showInsufficientCoinsDialog(String name, int price) {
        int currentCoins = SaveManager.getProfileData().coins;
        Dialog errorDialog = new Dialog(LanguageManager.t("shop.dialog.error"), skin);

        Table content = new Table();
        content.add(new Label(name, skin)).row();

        Table priceRow = new Table();
        priceRow.add(new Label(LanguageManager.t("shop.price") + " " + price, skin)).padRight(4);
        priceRow.add(new Image(Assets.getRegion("shared", "UI_assets/coin"))).size(16, 16);
        content.add(priceRow).padTop(6).row();

        content.add(new Label(LanguageManager.t("shop.insufficient.coins"), skin)).padTop(10).row();

        Table currentRow = new Table();
        currentRow.add(new Label(LanguageManager.t("shop.current.coins") + " " + currentCoins, skin)).padRight(4);
        currentRow.add(new Image(Assets.getRegion("shared", "UI_assets/coin"))).size(16, 16);
        content.add(currentRow).padTop(6);

        errorDialog.getContentTable().clear();
        errorDialog.getContentTable().add(content);
        errorDialog.getContentTable().row();
        errorDialog.pack();

        TextButton btnOk = ButtonFactory.createTextButton(LanguageManager.t("shop.ok"), () -> errorDialog.hide());
        errorDialog.getButtonTable().add(btnOk).size(120, 50).pad(10);
        errorDialog.show(getStage());
    }

    /** Mostrar confirmacion, procesar compra y refrescar UI. */
    private void showPurchaseConfirmation(String itemId, String name, int price, boolean isGadget) {
        Dialog confirmDialog = new Dialog("", skin);
        Image bgConfirm = new Image(new Texture(Gdx.files.internal("Menu/MenuMapas/VentanaConfirmarCompra.png")));
        confirmDialog.setBackground(bgConfirm.getDrawable());

        Table priceRow = new Table();
        priceRow.add(new Label(String.valueOf(price), skin, "font-27")).padRight(15);
        priceRow.add(new Image(Assets.getRegion("shared", "UI_assets/coin"))).size(36, 36);

        Table content = new Table();
        content.pad(50);
        Label nameLabel = new Label(name, skin, "font-27");
        nameLabel.setAlignment(Align.center);
        content.add(nameLabel).padBottom(20).row();
        content.add(priceRow).padTop(10).row();
        priceRow.getCells().first().padRight(10);

        ItemSlot slot = itemSlots.get(itemId);
        if (slot != null && slot.spriteImage.getDrawable() != null) {
            Image itemSprite = new Image(slot.spriteImage.getDrawable());
            itemSprite.setOrigin(Align.center);
            content.add(itemSprite).size(72, 72).padTop(15).row();
        }

        confirmDialog.getContentTable().clear();
        confirmDialog.getContentTable().add(content);
        confirmDialog.row();
        confirmDialog.pack();

        TextButton btnSi = ButtonFactory.createTextButton(LanguageManager.t("shop.buy"), () -> {
            confirmDialog.hide();
            confirmDialog.padBottom(35).padLeft(20);

            boolean purchased = isGadget
                ? SaveManager.purchaseGadget(itemId, price)
                : SaveManager.purchaseWeapon(itemId, price);

            if (purchased) {
                updateItemSlot(itemId);
                updateCoinsLabel();
                refreshAllSlots();

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
        });

        TextButton btnNo = ButtonFactory.createTextButton(LanguageManager.t("shop.cancel"), () -> confirmDialog.hide());

        confirmDialog.getButtonTable().add(btnSi).size(175, 40).pad(10).padLeft(30).expandX().left();
        confirmDialog.getButtonTable().add(btnNo).size(175, 40).pad(10).padRight(30).expandX().right();
        confirmDialog.getButtonTable().padBottom(35);
        confirmDialog.show(getStage());
    }

    /** Marcar slot como comprado (gris, desactivado). */
    private void updateItemSlot(String itemId) {
        ItemSlot slot = itemSlots.get(itemId);
        if (slot == null) return;

        slot.owned = true;
        slot.button.setColor(new Color(0.38f, 0.38f, 0.38f, 1f));
        slot.button.setDisabled(true);
        slot.spriteImage.setColor(new Color(0.55f, 0.55f, 0.55f, 1f));
        if (slot.coinImage != null) { slot.coinImage.remove(); slot.coinImage = null; }
        Label.LabelStyle font12Style = skin.get("font-12", Label.LabelStyle.class);
        if (font12Style != null) slot.priceLabel.setStyle(font12Style);
        slot.priceLabel.setText(LanguageManager.t("shop.owned"));
    }

    /** Actualizar contador de monedas. */
    public void updateCoinsLabel() {
        coinsLabel.setText(String.valueOf(SaveManager.getProfileData().coins));
    }

    /** Re-evaluar colores de affordability tras compra. */
    private void refreshAllSlots() {
        int currentCoins = SaveManager.getProfileData().coins;
        for (ItemSlot slot : itemSlots.values()) {
            if (slot.owned) continue;
            boolean canAfford = currentCoins >= slot.price;
            if (canAfford) {
                slot.button.setColor(new Color(0.3f, 0.65f, 0.35f, 1f));
                slot.button.setDisabled(false);
                slot.spriteImage.setColor(Color.WHITE);
                if (slot.coinImage != null) slot.coinImage.setColor(Color.WHITE);
            } else {
                slot.button.setColor(new Color(0.45f, 0.2f, 0.2f, 1f));
                slot.button.setDisabled(true);
                slot.spriteImage.setColor(new Color(0.5f, 0.35f, 0.35f, 1f));
                if (slot.coinImage != null) slot.coinImage.setColor(new Color(0.6f, 0.3f, 0.3f, 1f));
            }
        }
    }

    /** Liberar textura de boton. */
    public void dispose() {
        if (btnTiendaTex != null) btnTiendaTex.dispose();
    }
}
