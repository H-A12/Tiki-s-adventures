package com.tikisadventure.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.tikisadventure.core.SaveManager;
import com.tikisadventure.core.Assets;
import com.tikisadventure.database.core.AuthCallback;
import com.tikisadventure.localization.LanguageManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;
import com.tikisadventure.ui.button.ButtonFactory;

public class AccountScreen extends Window {

    private MenuScreen menuScreen;
    private Skin skin;
    private Label.LabelStyle blackLabelStyle;
    private float fixedWidth, fixedHeight;
    private Label titleLabel;
    private Table contentHolder;
    private Texture texBotonAlargado;
    private TextButton.TextButtonStyle btnStyleAlargado;

    public AccountScreen(Skin skin, MenuScreen menuScreen) {
        super("", skin);
        this.skin = skin;
        this.menuScreen = menuScreen;

        Image bgImage = new Image(new Texture(Gdx.files.internal("Menu/VentanaGestionarcuenta.png")));
        setBackground(bgImage.getDrawable());

        TextButton.TextButtonStyle btnStyle = skin.get(TextButton.TextButtonStyle.class);
        btnStyle.pressedOffsetX = 0;
        btnStyle.pressedOffsetY = 0;
        Label.LabelStyle font14Style = skin.get("font-14", Label.LabelStyle.class);
        if (font14Style == null) font14Style = skin.get(Label.LabelStyle.class);
        btnStyle.font = font14Style != null ? font14Style.font : null;

        blackLabelStyle = new Label.LabelStyle(font14Style);
        blackLabelStyle.fontColor = Color.BLACK;

        texBotonAlargado = new Texture(Gdx.files.internal("Menu/BotonAlargado.png"));
        NinePatch nueveParches = new NinePatch(texBotonAlargado, 12, 12, 6, 6);
        NinePatchDrawable botonAlargado = new NinePatchDrawable(nueveParches);
        btnStyleAlargado = new TextButton.TextButtonStyle(botonAlargado, botonAlargado, botonAlargado, font14Style != null ? font14Style.font : null);
        btnStyleAlargado.pressedOffsetX = 0;
        btnStyleAlargado.pressedOffsetY = 0;

        setModal(true);
        setMovable(false);
        pad(48, 50, 40, 50);

        // Title en su propia fila, independiente del contenido
        titleLabel = new Label(LanguageManager.t("account.title"), blackLabelStyle);
        titleLabel.setFontScale(1.2f);
        add(titleLabel).left().padLeft(50).padTop(0).expandX().row();

        // Contenedor para intercambiar contenido
        contentHolder = new Table();
        add(contentHolder).expand().fill().row();

        mostrarRegistro();
        pack();

        // Ventana un poco mÃƒÂ¡s ancha de base para que los textos no se desborden
        fixedWidth = Math.max(getWidth(), 550);
        fixedHeight = Math.max(getHeight(), 580);

        actualizarInterfaz();
    }

    public void actualizarInterfaz() {
        contentHolder.clearChildren();

        if (menuScreen.isConnected) {
            Label usuarioLabel = new Label(LanguageManager.t("account.user"), skin, "font-18");
            usuarioLabel.setAlignment(Align.center);
            Label nameLabel = new Label(menuScreen.username, skin, "font-14");
            nameLabel.setAlignment(Align.center);
            nameLabel.setWrap(true);
            TextButton btnDisconnect = new TextButton(LanguageManager.t("account.disconnect"), btnStyleAlargado);
            ButtonFactory.configure(btnDisconnect, () -> {
                menuScreen.isConnected = false;
                menuScreen.username = "";
                SaveManager.clearLogin();
                menuScreen.actualizarSpriteCuenta();
                actualizarInterfaz();
            });

            contentHolder.add(usuarioLabel).colspan(2).padTop(30).padBottom(12).width(280).row();
            contentHolder.add(nameLabel).colspan(2).padBottom(8).width(280).row();
            contentHolder.add(btnDisconnect).colspan(2).pad(8).width(200).row();

        } else {
            Label localLabel = new Label(LanguageManager.t("account.playing.local"), skin, "font-14");
            TextButton btnConnect = new TextButton(LanguageManager.t("account.connect"), btnStyleAlargado);
            ButtonFactory.configure(btnConnect, () -> {
                mostrarOpcionesConexion();
            });

            contentHolder.add(localLabel).colspan(2).pad(15).row();
            contentHolder.add(btnConnect).colspan(2).pad(8).width(200).row();
        }

        TextButton btnCerrar = new TextButton(LanguageManager.t("ui.close"), btnStyleAlargado);
        ButtonFactory.configure(btnCerrar, () -> {
            addAction(Actions.sequence(
                Actions.fadeOut(0.2f),
                Actions.visible(false)
            ));
        });
        contentHolder.add(btnCerrar).colspan(2).padTop(20).width(200);

        pack();
        setSize(fixedWidth, fixedHeight);
    }

    private void mostrarOpcionesConexion() {
        contentHolder.clearChildren();

        TextButton btnLogin = new TextButton(LanguageManager.t("account.login"), btnStyleAlargado);
        ButtonFactory.configure(btnLogin, () -> mostrarLogin());

        TextButton btnRegister = new TextButton(LanguageManager.t("account.create"), btnStyleAlargado);
        ButtonFactory.configure(btnRegister, () -> mostrarRegistro());

        TextButton btnVolver = new TextButton(LanguageManager.t("shop.back"), btnStyleAlargado);
        ButtonFactory.configure(btnVolver, () -> actualizarInterfaz());

        contentHolder.add(btnLogin).colspan(2).pad(12).width(200).row();
        contentHolder.add(btnRegister).colspan(2).pad(12).width(200).row();
        contentHolder.add(btnVolver).colspan(2).padTop(50).width(140);

        pack();
        setSize(fixedWidth, fixedHeight);
    }

    private void procesarDatosNube(String loginMessage) {
        String[] datosNube = loginMessage.split("\\|\\|\\|", -1);

        long playerId = Long.parseLong(datosNube[0]);
        int cloudCoins = Integer.parseInt(datosNube[1]);
        int cloudScore = Integer.parseInt(datosNube[2]);
        boolean moko = Boolean.parseBoolean(datosNube[3]);
        boolean zuki = Boolean.parseBoolean(datosNube[4]);

        com.badlogic.gdx.utils.Array<String> armasNubeArray = new com.badlogic.gdx.utils.Array<String>();
        if (datosNube.length > 5 && !datosNube[5].isEmpty()) {
            String[] armasList = datosNube[5].split("#");
            for (String armaStr : armasList) {
                armasNubeArray.add(armaStr);
            }
        }

        boolean mapDesert = datosNube.length > 6 ? Boolean.parseBoolean(datosNube[6]) : false;
        boolean mapCave = datosNube.length > 7 ? Boolean.parseBoolean(datosNube[7]) : false;

        com.badlogic.gdx.utils.Array<String> gadgetsNubeArray = new com.badlogic.gdx.utils.Array<>();
        if (datosNube.length > 8 && !datosNube[8].isEmpty()) {
            String[] gadgetsList = datosNube[8].split("#");
            for (String gStr : gadgetsList) {
                gadgetsNubeArray.add(gStr);
            }
        }

        String armasCustomJson = datosNube.length > 9 ? datosNube[9] : "{}";
        if (armasCustomJson == null || armasCustomJson.equals("null") || armasCustomJson.trim().isEmpty()) {
            armasCustomJson = "{}";
        }

        try {
            com.badlogic.gdx.utils.Json jsonTool = new com.badlogic.gdx.utils.Json();
            @SuppressWarnings("unchecked")
            com.badlogic.gdx.utils.ObjectMap<String, com.tikisadventure.core.GameSession.CustomWeaponConfig> mapNube =
                (com.badlogic.gdx.utils.ObjectMap<String, com.tikisadventure.core.GameSession.CustomWeaponConfig>)
                    jsonTool.fromJson(com.badlogic.gdx.utils.ObjectMap.class, com.tikisadventure.core.GameSession.CustomWeaponConfig.class, armasCustomJson);

            if (mapNube != null) {
                com.tikisadventure.core.GameSession.customWeapons = mapNube;
            } else {
                com.tikisadventure.core.GameSession.customWeapons.clear();
            }
            com.tikisadventure.core.GameSession.saveCustomWeapons();
        } catch (Exception e) {
            System.out.println("Error parseando armas custom desde la nube: " + e.getMessage());
            com.tikisadventure.core.GameSession.customWeapons.clear();
        }

        SaveManager.aplicarDatosNube(playerId, cloudCoins, cloudScore, moko, zuki);
        SaveManager.aplicarArmasNube(armasNubeArray);
        SaveManager.aplicarMapasNube(mapDesert, mapCave);
        SaveManager.aplicarGadgetsNube(gadgetsNubeArray);
    }

    private void mostrarLogin() {
        contentHolder.clearChildren();

        Label titulo = new Label(LanguageManager.t("account.login"), blackLabelStyle);

        final TextField userField = new TextField("", skin);
        userField.setMessageText(LanguageManager.t("account.username.hint"));

        final TextField passField = new TextField("", skin);
        passField.setMessageText(LanguageManager.t("account.password.hint"));
        passField.setPasswordMode(true);
        passField.setPasswordCharacter('*');

        final TextButton btnOjo = new TextButton(LanguageManager.t("account.show"), btnStyleAlargado);
        ButtonFactory.configure(btnOjo, () -> {
            passField.setPasswordMode(!passField.isPasswordMode());
            btnOjo.setText(passField.isPasswordMode() ? LanguageManager.t("account.show") : LanguageManager.t("account.hide"));
        });

        Table passTable = new Table();
        // Aumentados los anchos para que no se corten los caracteres agrandados
        passTable.add(passField).width(180);
        passTable.add(btnOjo).padLeft(8).width(85);

        final Label errorLabel = new Label("", skin, "font-13");
        errorLabel.setColor(Color.RED);
        errorLabel.setWrap(true);
        errorLabel.setAlignment(Align.center);

        final TextButton btnAceptar = new TextButton(LanguageManager.t("account.accept"), btnStyleAlargado);
        ButtonFactory.configure(btnAceptar, () -> {
            errorLabel.setText("");

            final String user = userField.getText();
            final String pass = passField.getText();

                if (user.isEmpty() || pass.isEmpty()) {
                    errorLabel.setText(LanguageManager.t("account.error.empty.fields"));
                    pack();
                    setSize(fixedWidth, fixedHeight);
                    return;
                }

                if (user.length() < 3 || user.length() > 16) {
                    errorLabel.setText(LanguageManager.t("account.error.bad.name"));
                    pack();
                    setSize(fixedWidth, fixedHeight);
                    return;
                }

                btnAceptar.setDisabled(true);
                btnAceptar.setText(LanguageManager.t("account.loading"));

                menuScreen.getAuthManager().iniciarSesion(user, pass, new AuthCallback() {
                    @Override
                    public void onSuccess(String message) {
                        procesarDatosNube(message);

                        menuScreen.isConnected = true;
                        menuScreen.username = user;
                        SaveManager.saveLogin(user, pass);

                        menuScreen.actualizarSpriteCuenta();
                        actualizarInterfaz();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        errorLabel.setText(errorMessage);
                        btnAceptar.setDisabled(false);
                        btnAceptar.setText(LanguageManager.t("account.accept"));
                        pack();
                        setSize(fixedWidth, fixedHeight);
                    }
                });
            }
        );

        TextField.TextFieldListener enterListenerLogin = crearEnterListener(btnAceptar);
        userField.setTextFieldListener(enterListenerLogin);
        passField.setTextFieldListener(enterListenerLogin);

        TextButton btnVolver = new TextButton(LanguageManager.t("shop.back"), btnStyleAlargado);
        ButtonFactory.configure(btnVolver, () -> mostrarOpcionesConexion());

        contentHolder.add(titulo).padTop(5).padBottom(8).colspan(2).center().row();
        contentHolder.add(userField).pad(6).width(280).colspan(2).row();
        contentHolder.add(passTable).pad(6).colspan(2).row();
        contentHolder.add(errorLabel).width(280).padTop(4).height(30).colspan(2).row();
        contentHolder.add(btnAceptar).padTop(6).padRight(8).width(200);
        contentHolder.add(btnVolver).padTop(6).padLeft(8).width(200);

        pack();
        setSize(fixedWidth, fixedHeight);
    }

    private void mostrarRegistro() {
        contentHolder.clearChildren();

        Label titulo = new Label(LanguageManager.t("account.create"), blackLabelStyle);

        final TextField userField = new TextField("", skin);
        userField.setMessageText(LanguageManager.t("account.newuser.hint"));

        final TextField passField1 = new TextField("", skin);
        passField1.setMessageText(LanguageManager.t("account.password.hint"));
        passField1.setPasswordMode(true);
        passField1.setPasswordCharacter('*');

        final TextField passField2 = new TextField("", skin);
        passField2.setMessageText(LanguageManager.t("account.repeat.hint"));
        passField2.setPasswordMode(true);
        passField2.setPasswordCharacter('*');

        final TextButton btnOjo = new TextButton(LanguageManager.t("account.show"), btnStyleAlargado);
        ButtonFactory.configure(btnOjo, () -> {
            boolean isOculto = passField1.isPasswordMode();
            passField1.setPasswordMode(!isOculto);
            passField2.setPasswordMode(!isOculto);
            btnOjo.setText(isOculto ? LanguageManager.t("account.hide") : LanguageManager.t("account.show"));
            }
        );

        Table passTable1 = new Table();
        passTable1.add(passField1).width(180);
        passTable1.add(btnOjo).padLeft(8).width(85);

        Table passTable2 = new Table();
        passTable2.add(passField2).width(180);
        passTable2.add().padLeft(8).width(85);

        final Label errorLabel = new Label("", skin, "font-13");
        errorLabel.setColor(Color.RED);
        errorLabel.setWrap(true);
        errorLabel.setAlignment(Align.center);

        final TextButton btnAceptar = new TextButton(LanguageManager.t("account.accept"), btnStyleAlargado);
        ButtonFactory.configure(btnAceptar, () -> {
            errorLabel.setText("");

            final String user = userField.getText();
            final String pass1 = passField1.getText();
            String pass2 = passField2.getText();

            if (user.isEmpty() || pass1.isEmpty() || pass2.isEmpty()) {
                errorLabel.setText(LanguageManager.t("account.error.empty.fields"));
                pack();
                setSize(fixedWidth, fixedHeight);
                return;
            }

            if (user.length() < 3 || user.length() > 16) {
                errorLabel.setText(LanguageManager.t("account.error.name.length"));
                pack();
                setSize(fixedWidth, fixedHeight);
                return;
            }

            if (!pass1.equals(pass2)) {
                errorLabel.setText(LanguageManager.t("account.error.passwords.mismatch"));
                pack();
                setSize(fixedWidth, fixedHeight);
                return;
            }

            btnAceptar.setDisabled(true);
            btnAceptar.setText(LanguageManager.t("account.creating"));

            menuScreen.getAuthManager().registrarJugador(user, pass1, new AuthCallback() {
                @Override
                public void onSuccess(String message) {
                    SaveManager.markLocalAsLinked();

                    menuScreen.getAuthManager().iniciarSesion(user, pass1, new AuthCallback() {
                        @Override
                        public void onSuccess(String loginMessage) {
                            procesarDatosNube(loginMessage);

                            menuScreen.isConnected = true;
                            menuScreen.username = user;
                            SaveManager.saveLogin(user, pass1);

                            menuScreen.actualizarSpriteCuenta();
                            actualizarInterfaz();
                        }

                        @Override
                        public void onError(String error) {
                            errorLabel.setText(LanguageManager.t("account.error.created.login"));
                            btnAceptar.setDisabled(false);
                        }
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    errorLabel.setText(errorMessage);
                    btnAceptar.setDisabled(false);
                    btnAceptar.setText(LanguageManager.t("account.accept"));
                    pack();
                    setSize(fixedWidth, fixedHeight);
                }
            });
        });

        TextField.TextFieldListener enterListenerReg = crearEnterListener(btnAceptar);
        userField.setTextFieldListener(enterListenerReg);
        passField1.setTextFieldListener(enterListenerReg);
        passField2.setTextFieldListener(enterListenerReg);

        TextButton btnVolver = new TextButton(LanguageManager.t("shop.back"), btnStyleAlargado);
        ButtonFactory.configure(btnVolver, () -> mostrarOpcionesConexion());

        contentHolder.add(titulo).padTop(5).padBottom(8).colspan(2).center().row();
        contentHolder.add(userField).pad(6).width(280).colspan(2).row();
        contentHolder.add(passTable1).pad(6).colspan(2).row();
        contentHolder.add(passTable2).pad(6).colspan(2).row();
        contentHolder.add(errorLabel).width(280).padTop(4).height(30).colspan(2).row();
        contentHolder.add(btnAceptar).padTop(6).padRight(8).width(200);
        contentHolder.add(btnVolver).padTop(6).padLeft(8).width(200);

        pack();
        setSize(fixedWidth, fixedHeight);
    }

    private TextField.TextFieldListener crearEnterListener(final TextButton btn) {
        return new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField textField, char c) {
                if (c == '\n') {
                    InputEvent down = new InputEvent();
                    down.setType(InputEvent.Type.touchDown);
                    down.setButton(0);
                    btn.fire(down);
                    InputEvent up = new InputEvent();
                    up.setType(InputEvent.Type.touchUp);
                    up.setButton(0);
                    btn.fire(up);
                }
            }
        };
    }

    public void dispose() {
        if (texBotonAlargado != null) texBotonAlargado.dispose();
    }
}
