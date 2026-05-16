package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.core.Assets;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.systems.powerUps.PowerUp;
import com.tikisadventure.systems.powerUps.NewWeaponPowerUp;
import com.tikisadventure.systems.PowerUpSystem;

public class LevelUpUI extends Window {

    private final Runnable onChoiceMade;
    private final Skin skin;

    // --- VARIABLES DE CONTROL ---
    private PowerUpSystem powerUpSystem;
    private int currentLevel;
    private Player currentPlayer;
    private float lastStageWidth;
    private float lastStageHeight;

    // NUEVO: La mesa segura donde pondremos las cartas
    private Table mainContainer;
    // NUEVO: El seguro para evitar doble clic
    private boolean isProcessingChoice = false;

    public LevelUpUI(Skin skin, Runnable onChoiceMade) {
        super("", skin);
        this.skin = skin;
        this.onChoiceMade = onChoiceMade;

        setModal(true);
        setMovable(false);

        NinePatch ninePatch = new NinePatch(skin.getRegion("select"), 4, 4, 0, 24);
        NinePatchDrawable background = new NinePatchDrawable(ninePatch);
        ninePatch.setColor(new Color(0, 0, 0, 0.85f));
        setBackground(background);

        // NUEVO: Creamos el contenedor principal y lo acoplamos a la ventana
        mainContainer = new Table();
        add(mainContainer).expand().fill();
    }

    public void show(float stageWidth, float stageHeight, Array<PowerUp> opciones, Player player, PowerUpSystem system, int level) {
        this.lastStageWidth = stageWidth;
        this.lastStageHeight = stageHeight;
        this.currentPlayer = player;
        this.powerUpSystem = system;
        this.currentLevel = level;

        // NUEVO: Reiniciamos el candado de los clics
        this.isProcessingChoice = false;

        // NUEVO: Desatascamos el ratón del Stage por si se quedó pillado de la anterior vez
        if (getStage() != null) {
            getStage().unfocusAll();
        }

        buildCardsUI(opciones);

        setVisible(true);
        toFront();
        centerOnStage(stageWidth, stageHeight);
    }

    public void centerOnStage(float stageWidth, float stageHeight) {
        lastStageWidth = stageWidth;
        lastStageHeight = stageHeight;
        setPosition(
            Math.round((lastStageWidth - getWidth()) / 2f),
            Math.round((lastStageHeight - getHeight()) / 2f)
        );
    }

    private void buildCardsUI(Array<PowerUp> opciones) {
        // MODIFICADO: Ahora limpiamos la "mesa" interior, no la ventana entera
        mainContainer.clearChildren();

        Table content = new Table();
        content.pad(30);

        Label title = new Label("¡LEVEL UP!", skin, "font-38");
        content.add(title).padBottom(40).row();

        Table optionsTable = new Table();

        for (PowerUp opcion : opciones) {
            if (opcion == null) continue;
            optionsTable.add(powerUpCardButton(opcion, currentPlayer)).pad(15).width(240).height(320);
        }

        content.add(optionsTable).padBottom(30).row();

        // MODIFICADO: Añadimos el contenido a la "mesa"
        mainContainer.add(content);
        pack();
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.C)) {
            if (powerUpSystem != null && currentPlayer != null) {
                Gdx.app.log("DEBUG", "¡Reroll de cartas activado (Tecla C)!");
                Array<PowerUp> nuevasOpciones = powerUpSystem.rollOptions(currentPlayer, currentLevel, 3);
                buildCardsUI(nuevasOpciones);
            }
        }
    }

    private String getIconPath(String powerUpName) {
        switch (powerUpName) {
            case "Tornillos": return "powerUps_assets/commonScrews";
            case "Pilas Triple A": return "powerUps_assets/commonBatteries";
            case "Petardos": return "powerUps_assets/commonFirecrackers";
            case "Salsa picante": return "powerUps_assets/commonHotSauce";
            case "Huevo podrido": return "powerUps_assets/commonRottenEgg";
            case "Frigopie": return "powerUps_assets/commonFootIceCream";
            case "Golosinas": return "powerUps_assets/commonCandy";
            case "Libro de mates": return "powerUps_assets/commonMathsBook";
            case "Sobre de azúcar": return "powerUps_assets/commonSugarPacket";
            case "Aguja de coser": return "powerUps_assets/commonNeedle";
            case "Tirita usada": return "powerUps_assets/commonBandAid";
            case "Imán decorativo": return "powerUps_assets/commonMagnet";

            case "Llave inglesa": return "powerUps_assets/rareWrench";
            case "Batería": return "powerUps_assets/rareBattery";
            case "Mechero trucado": return "powerUps_assets/rareLighter";
            case "Caja de cerillas": return "powerUps_assets/rareMatchbox";
            case "Friegasuelos": return "powerUps_assets/rareFloorCleaner";
            case "Granizado de limón": return "powerUps_assets/rareLemonGranita";
            case "Pera": return "powerUps_assets/rarePear";
            case "Globo terráqueo": return "powerUps_assets/rareGlobe";
            case "Bebida energética": return "powerUps_assets/rareEnergyDrink";
            case "Martillo de carpintero": return "powerUps_assets/rareCarpentry";
            case "1ª Ley de Tiki": return "powerUps_assets/rareTikiLaw";
            case "Jarabe caducado": return "powerUps_assets/rareSyrup";
            case "Pajita de papel": return "powerUps_assets/rareStraw";
            case "Chicle del suelo": return "powerUps_assets/rareGum";

            case "Taladro": return "powerUps_assets/especialDrill";
            case "Pinzas de arranque": return "powerUps_assets/especialClamps";
            case "Bidón de gasolina": return "powerUps_assets/especialPetrolCan";
            case "Soplete doméstico": return "powerUps_assets/especialBlowtorch";
            case "Seta del jardín": return "powerUps_assets/especialMushroom";
            case "Sr Nievefría": return "powerUps_assets/especialSnowman";
            case "Hamburguesa sin tomate": return "powerUps_assets/especialHamburguer";
            case "Piezas de puzzle": return "powerUps_assets/especialPuzzle";
            case "Patines viejos": return "powerUps_assets/especialSkates";
            case "Dardos": return "powerUps_assets/especialDarts";
            case "2ª Ley de Tiki": return "powerUps_assets/especialTikiLaw";
            case "Colonia de papá": return "powerUps_assets/especialCologne";
            case "Bote de miel": return "powerUps_assets/especialHoney";
            case "Esponja del abuelo": return "powerUps_assets/especialSponge";

            case "Tanque de gas": return "powerUps_assets/epicGasCan";
            case "Virus prehistórico": return "powerUps_assets/epicPrehistoricVirus";
            case "Traje de marinero": return "powerUps_assets/epicSailorSuit";
            case "Motor de la lavadora": return "powerUps_assets/epicMotor";
            case "Cazamariposas": return "powerUps_assets/epicNet";
            case "Aspirador roomba": return "powerUps_assets/epicRobotCleaner";

            case "3ª Ley de Tiki": return "powerUps_assets/legendaryTikiLaw";
            case "Parchís": return "powerUps_assets/legendaryParcheesi";
            case "Máscara rota temerosa": return "powerUps_assets/legendaryBrokenMask";

            case "Fusil de bolas": return "weapons_assets/BallRifle";
            case "Escupepalillos": return "weapons_assets/ToothpickShotgun";
            case "Pirocohete": return "weapons_assets/RocketLauncher";
            case "Clavolleta": return "weapons_assets/NailGun";
            case "Lanzapelotas": return "weapons_assets/TennisLauncher";
            case "Triturahielo": return "weapons_assets/IceGrinder";
            case "Extintor trucado": return "weapons_assets/Extinguisher";
            case "Lanzadiscos": return "weapons_assets/DiscLauncher";
            case "Banana": return "weapons_assets/Banana";
            case "Putripez": return "weapons_assets/RottenFish";
            case "Saxofon": return "weapons_assets/Saxophone";
            case "Enchufe alcalino": return "weapons_assets/BatteryPlugger";

            default: return null;
        }
    }

    private Table powerUpCardButton(final PowerUp powerUpElegido, final Player player) {
        String titulo = powerUpElegido.getName() != null ? powerUpElegido.getName() : "Desconocido";
        String desc = powerUpElegido.getDescription() != null ? powerUpElegido.getDescription() : "";
        String rareza = powerUpElegido.getRarity() != null ? powerUpElegido.getRarity().name() : "COMUN";

        String cardPath = "powerUps_assets/powerUpCommonTemplate";
        String iconBgPath = "powerUps_assets/iconCommonTemplate";

        if (powerUpElegido.getRarity() != null) {
            switch (powerUpElegido.getRarity()) {
                case COMUN: cardPath = "powerUps_assets/powerUpCommonTemplate"; iconBgPath = "powerUps_assets/iconCommonTemplate"; break;
                case RARO: cardPath = "powerUps_assets/powerUpRareTemplate"; iconBgPath = "powerUps_assets/iconRareTemplate"; break;
                case ESPECIAL: cardPath = "powerUps_assets/powerUpEspecialTemplate"; iconBgPath = "powerUps_assets/iconEspecialTemplate"; break;
                case EPICO: cardPath = "powerUps_assets/powerUpEpicTemplate"; iconBgPath = "powerUps_assets/iconEpicTemplate"; break;
                case LEGENDARIO: cardPath = "powerUps_assets/powerUpLegendaryTemplate"; iconBgPath = "powerUps_assets/iconLegendaryTemplate"; break;
            }
        }

        if (powerUpElegido instanceof NewWeaponPowerUp) {
            cardPath = "powerUps_assets/powerUpGunTemplate";
            iconBgPath = "powerUps_assets/iconGunTemplate";
        }

        final Table cardGroup = new Table();
        cardGroup.setTouchable(Touchable.enabled);
        cardGroup.setSize(240, 320);
        cardGroup.setTransform(true);
        cardGroup.setOrigin(120, 160);

        Stack layers = new Stack();
        layers.setFillParent(true);

        Table layer1_IconBg = new Table();
        TextureRegion bgRegion = Assets.getRegion("shared", iconBgPath);
        if (bgRegion != null) {
            Image bgIcon = new Image(bgRegion);
            layer1_IconBg.add(bgIcon).width(140).height(140).padBottom(60);
        }

        Table layer2_ItemIcon = new Table();
        String itemIconPath = null;

        if (powerUpElegido instanceof com.tikisadventure.systems.powerUps.WeaponUpgradePowerUp) {
            String baseWeaponName = ((com.tikisadventure.systems.powerUps.WeaponUpgradePowerUp) powerUpElegido).getWeapon().getName();
            itemIconPath = getIconPath(baseWeaponName);
        } else {
            itemIconPath = getIconPath(titulo);
        }

        if (itemIconPath != null) {
            TextureRegion itemRegion = Assets.getRegion("shared", itemIconPath);
            if (itemRegion != null) {
                Image itemImg = new Image(itemRegion);

                if (powerUpElegido instanceof com.tikisadventure.systems.powerUps.WeaponUpgradePowerUp) {
                    Stack iconStack = new Stack();
                    iconStack.add(itemImg);

                    String arrowPath = "powerUps_assets/upgradeArrowRare";
                    switch (powerUpElegido.getRarity()) {
                        case COMUN: arrowPath = "powerUps_assets/upgradeArrowRare"; break;
                        case RARO: arrowPath = "powerUps_assets/upgradeArrowEspecial"; break;
                        case ESPECIAL: arrowPath = "powerUps_assets/upgradeArrowEpic"; break;
                        case EPICO: arrowPath = "powerUps_assets/upgradeArrowLegendary"; break;
                    }

                    TextureRegion arrowReg = Assets.getRegion("shared", arrowPath);
                    if (arrowReg != null) {
                        Image arrowImg = new Image(arrowReg);
                        Table arrowTable = new Table();
                        arrowTable.add(arrowImg).width(70).height(70).bottom().right().padBottom(-35).padRight(-55);
                        iconStack.add(arrowTable);
                    }

                    layer2_ItemIcon.add(iconStack).width(90).height(90).padBottom(60);
                } else {
                    layer2_ItemIcon.add(itemImg).width(90).height(90).padBottom(60);
                }
            }
        }

        TextureRegion frameRegion = Assets.getRegion("shared", cardPath);
        Image layer3_cardFrame = frameRegion != null ? new Image(frameRegion) : new Image();

        Label nameLabel = new Label(titulo, skin, "font-16");
        nameLabel.setAlignment(Align.center);
        nameLabel.setWrap(true);

        Label descLabel = new Label(desc, skin, "font-13");
        descLabel.setAlignment(Align.center);
        descLabel.setWrap(true);

        Label rarityLabel = new Label(rareza, skin, "font-13");
        rarityLabel.setAlignment(Align.center);

        if (powerUpElegido.getRarity() != null) {
            switch (powerUpElegido.getRarity()) {
                case COMUN: rarityLabel.setColor(Color.LIGHT_GRAY); break;
                case RARO: rarityLabel.setColor(Color.GREEN); break;
                case ESPECIAL: rarityLabel.setColor(Color.CYAN); break;
                case EPICO: rarityLabel.setColor(Color.PURPLE); break;
                case LEGENDARIO: rarityLabel.setColor(Color.GOLD); break;
            }
        }

        boolean isNewWeapon = powerUpElegido instanceof NewWeaponPowerUp;

        Table titleLayer = new Table();
        titleLayer.padLeft(20).padRight(20);
        titleLayer.add(nameLabel).top().expand().fillX().padTop(22);

        Table descLayer = new Table();
        descLayer.padLeft(28).padRight(28);
        if (isNewWeapon) {
            descLayer.add(descLabel).bottom().expand().fillX().padBottom(40);
        } else {
            descLayer.add(descLabel).bottom().expand().fillX().padBottom(55);
        }

        Table rarityLayer = new Table();
        if (!isNewWeapon) {
            rarityLayer.add(rarityLabel).bottom().expand().fillX().padBottom(18);
        }

        layers.add(layer1_IconBg);
        layers.add(layer2_ItemIcon);
        layers.add(layer3_cardFrame);
        layers.add(titleLayer);
        layers.add(descLayer);
        if (!isNewWeapon) layers.add(rarityLayer);

        cardGroup.add(layers).expand().fill();

        cardGroup.addListener(new Assets.HoverCursorListener());
        cardGroup.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override
            public void enter(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor fromActor) {
                super.enter(event, x, y, pointer, fromActor);
                if (pointer == -1) {
                    cardGroup.clearActions();
                    cardGroup.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel(
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(1.05f, 1.05f, 0.2f, com.badlogic.gdx.math.Interpolation.fade),
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.forever(
                            com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence(
                                com.badlogic.gdx.scenes.scene2d.actions.Actions.rotateTo(2.0f, 0.8f, com.badlogic.gdx.math.Interpolation.smooth),
                                com.badlogic.gdx.scenes.scene2d.actions.Actions.rotateTo(-2.0f, 0.8f, com.badlogic.gdx.math.Interpolation.smooth)
                            )
                        )
                    ));
                }
            }

            @Override
            public void exit(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, com.badlogic.gdx.scenes.scene2d.Actor toActor) {
                super.exit(event, x, y, pointer, toActor);
                if (pointer == -1) {
                    cardGroup.clearActions();
                    cardGroup.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.parallel(
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(1f, 1f, 0.2f, com.badlogic.gdx.math.Interpolation.fade),
                        com.badlogic.gdx.scenes.scene2d.actions.Actions.rotateTo(0f, 0.2f, com.badlogic.gdx.math.Interpolation.fade)
                    ));
                }
            }

            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                // NUEVO: Verificamos que no estemos procesando ya una carta
                if (isProcessingChoice) return;

                // Bloqueamos clics adicionales
                isProcessingChoice = true;

                powerUpElegido.apply(player);
                if (onChoiceMade != null) {
                    onChoiceMade.run();
                }
            }
        });

        return cardGroup;
    }
}
