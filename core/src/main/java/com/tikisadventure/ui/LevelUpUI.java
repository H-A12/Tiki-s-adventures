package com.tikisadventure.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.tikisadventure.audio.AudioManager;
import com.tikisadventure.audio.AudioType;
import com.tikisadventure.core.Assets;
import com.tikisadventure.entities.player.Player;
import com.tikisadventure.systems.powerUps.PowerUp;
import com.tikisadventure.systems.powerUps.NewWeaponPowerUp;
import com.tikisadventure.systems.PowerUpSystem;
import com.tikisadventure.ui.FontManager;

public class LevelUpUI extends Window {

    private final Runnable onChoiceMade;
    private final Skin skin;
    private Texture texPowerupBg;

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

        texPowerupBg = new Texture(Gdx.files.internal("Menu/MenuMapas/VentanaPowerup.png"));
        setBackground(new TextureRegionDrawable(new TextureRegion(texPowerupBg)));

        setModal(true);
        setMovable(false);
        setTransform(true);
        setScale(1.3f);

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
            Math.round((lastStageWidth - getWidth() * getScaleX()) / 2f),
            Math.round((lastStageHeight - getHeight() * getScaleY()) / 2f)
        );
    }

    public void dispose() {
        if (texPowerupBg != null) texPowerupBg.dispose();
    }

    private void buildCardsUI(Array<PowerUp> opciones) {
        // MODIFICADO: Ahora limpiamos la "mesa" interior, no la ventana entera
        mainContainer.clearChildren();

        Table content = new Table();
        content.pad(80);

        Label title = new Label("¡LEVEL UP!", skin, "font-38");
        content.add(title).padTop(10).padBottom(20).row();

        Table optionsTable = new Table();

        for (PowerUp opcion : opciones) {
            if (opcion == null) continue;
            optionsTable.add(powerUpCardButton(opcion, currentPlayer)).padLeft(15).padRight(15).width(170).height(230);
        }

        content.add(optionsTable).padBottom(15).row();

        // MODIFICADO: Añadimos el contenido a la "mesa"
        mainContainer.add(content);
        pack();
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        if (isVisible() && getStage() != null) {
            float sw = getStage().getWidth();
            float sh = getStage().getHeight();
            if (sw != lastStageWidth || sh != lastStageHeight) {
                centerOnStage(sw, sh);
            }
        }

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
        cardGroup.setSize(170, 230);
        cardGroup.setTransform(true);
        cardGroup.setOrigin(85, 115);

        Stack layers = new Stack();
        layers.setFillParent(true);

        Table layer1_IconBg = new Table();
        TextureRegion bgRegion = Assets.getRegion("shared", iconBgPath);
        if (bgRegion != null) {
            Image bgIcon = new Image(bgRegion);
            layer1_IconBg.add(bgIcon).width(100).height(100).padBottom(40);
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
                        arrowTable.add(arrowImg).width(50).height(50).bottom().right().padBottom(-25).padRight(-39);
                        iconStack.add(arrowTable);
                    }

                    layer2_ItemIcon.add(iconStack).width(64).height(64).padBottom(40);
                } else {
                    layer2_ItemIcon.add(itemImg).width(64).height(64).padBottom(40);
                }
            }
        }

        TextureRegion frameRegion = Assets.getRegion("shared", cardPath);
        Image layer3_cardFrame = frameRegion != null ? new Image(frameRegion) : new Image();

        // MODIFICADO: Aumentamos el ancho máximo a 150f para calcular mejor el tamaño de fuente.
        int descFontSize = calcularFontSize(desc, 150f, 38f);
        Label descLabel = new Label(desc, new Label.LabelStyle(FontManager.getFont(descFontSize), Color.WHITE));
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

        // --- INICIO DE LA NUEVA LÓGICA DEL TÍTULO ---
        Table titleLayer = new Table();
        titleLayer.top(); // Mantenemos el contenedor arriba de la carta

        // 1. Preparamos el texto del título unificado
        String textoTitulo;
        if (powerUpElegido instanceof com.tikisadventure.systems.powerUps.WeaponUpgradePowerUp) {
            String weaponName = ((com.tikisadventure.systems.powerUps.WeaponUpgradePowerUp) powerUpElegido).getWeapon().getName();
            int targetTier = ((com.tikisadventure.systems.powerUps.WeaponUpgradePowerUp) powerUpElegido).getWeapon().getTier() + 1;
            textoTitulo = weaponName + " tier " + targetTier; // Lo juntamos en un solo String
        } else {
            textoTitulo = titulo;
        }

        // 2. Calculamos la fuente (aumenté el límite de altura a 45f para que la función tenga margen)
        int titleFontSize = calcularFontSizeTitulo(textoTitulo, 130f, 45f);

        Label titleLabel = new Label(textoTitulo, new Label.LabelStyle(FontManager.getFont(titleFontSize), Color.WHITE));
        titleLabel.setAlignment(Align.center); // Esto centra el texto horizontal y VERTICALMENTE
        titleLabel.setWrap(true);

        // 3. Le damos una ALTURA FIJA a la celda (height 45).
        // Si tiene 1 línea, flotará en el centro de esos 45px. Si tiene 2, llenará los 45px.
        titleLayer.add(titleLabel).width(130).height(28).padTop(10);
        // --- FIN DE LA NUEVA LÓGICA DEL TÍTULO ---

        Table descLayer = new Table();
        // MODIFICADO: Reducimos padding lateral a 10 para que extienda a los lados.
        descLayer.padLeft(10).padRight(10);

        if (isNewWeapon) {
            // MODIFICADO: Aumentamos padBottom de 35 a 45 para subir el texto.
            descLayer.add(descLabel).bottom().expand().fillX().padBottom(45);
        } else {
            // MODIFICADO: Aumentamos padBottom de 35 a 45 para subir el texto.
            descLayer.add(descLabel).bottom().expand().fillX().padBottom(45);
        }

        Table rarityLayer = new Table();
        if (!isNewWeapon) {
            rarityLayer.add(rarityLabel).bottom().expand().fillX().padBottom(14);
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
                if (isProcessingChoice) return;
                isProcessingChoice = true;

                powerUpElegido.apply(player);
                AudioManager.playSFX(AudioType.POWERUP);
                if (onChoiceMade != null) {
                    onChoiceMade.run();
                }
            }
        });

        return cardGroup;
    }

    private int calcularFontSizeTitulo(String text, float maxWidth, float maxHeight) {
        if (text == null || text.isEmpty()) return 12;
        GlyphLayout layout = new GlyphLayout();
        for (int size = 12; size >= 6; size--) {
            BitmapFont font = FontManager.getFont(size);
            String[] words = text.split("\\s+");
            boolean allWordsFit = true;
            for (String word : words) {
                layout.setText(font, word);
                if (layout.width > maxWidth) {
                    allWordsFit = false;
                    break;
                }
            }
            if (!allWordsFit) continue;

            layout.setText(font, text, Color.WHITE, maxWidth, Align.center, true);

            // MODIFICADO: layout.runs.size cuenta las líneas. Nos aseguramos de que sean 2 como máximo.
            if (layout.height <= maxHeight && layout.runs.size <= 2) {
                return size;
            }
        }
        return 6; // Devuelve el tamaño mínimo si nada encaja en 2 líneas
    }

    private int calcularFontSize(String text, float maxWidth, float maxHeight) {
        if (text == null || text.isEmpty()) return 12;
        GlyphLayout layout = new GlyphLayout();
        for (int size = 12; size >= 6; size--) {
            BitmapFont font = FontManager.getFont(size);
            layout.setText(font, text, Color.WHITE, maxWidth, Align.center, true);
            if (layout.height <= maxHeight) return size;
        }
        return 6;
    }
}
