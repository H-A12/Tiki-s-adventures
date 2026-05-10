package com.tikisadventure.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.tikisadventure.core.SaveManager;
import com.tikisadventure.core.Assets;
import com.tikisadventure.database.core.AuthCallback;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;

public class AccountScreen extends Window {

    private MenuScreen menuScreen;
    private Skin skin;
    private Label.LabelStyle blackLabelStyle;
    private float fixedWidth, fixedHeight;

    public AccountScreen(Skin skin, MenuScreen menuScreen) {
        super("", skin);
        this.skin = skin;
        this.menuScreen = menuScreen;

        Image bgImage = new Image(new Texture(Gdx.files.internal("Menu/VentanaGestionarCuenta.png")));
        setBackground(bgImage.getDrawable());

        TextButton.TextButtonStyle btnStyle = skin.get(TextButton.TextButtonStyle.class);
        btnStyle.pressedOffsetX = 0;
        btnStyle.pressedOffsetY = 0;

        blackLabelStyle = new Label.LabelStyle(skin.get(Label.LabelStyle.class));
        blackLabelStyle.fontColor = Color.BLACK;

        setModal(true);
        setMovable(false);
        pad(45, 40, 30, 40);

        mostrarRegistro();
        pack();
        fixedWidth = getWidth();
        fixedHeight = getHeight();
        actualizarInterfaz();
    }

    public void actualizarInterfaz() {
        clearChildren();

        Label titleLabel = new Label("Gestión de Cuenta", blackLabelStyle);
        add(titleLabel).colspan(2).center().padBottom(10).padTop(2).row();

        if (menuScreen.isConnected) {
            Label userLabel = new Label("Usuario: " + menuScreen.username, skin);
            TextButton btnDisconnect = new TextButton("Desconectar", skin);

            btnDisconnect.addListener(new Assets.HoverCursorListener());
            btnDisconnect.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    menuScreen.isConnected = false;
                    menuScreen.username = "";
                    SaveManager.clearLogin();
                    menuScreen.actualizarSpriteCuenta();
                    actualizarInterfaz();
                }
            });

            add(userLabel).colspan(2).pad(10).row();
            add(btnDisconnect).colspan(2).pad(5).width(110).row();

        } else {
            Label localLabel = new Label("Jugando en Local", skin);
            TextButton btnConnect = new TextButton("Conectar", skin);

            btnConnect.addListener(new Assets.HoverCursorListener());
            btnConnect.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    mostrarOpcionesConexion();
                }
            });

            add(localLabel).colspan(2).pad(10).row();
            add(btnConnect).colspan(2).pad(5).width(95).row();
        }

        TextButton btnCerrar = new TextButton("Cerrar", skin);
        btnCerrar.addListener(new Assets.HoverCursorListener());
        btnCerrar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                addAction(Actions.sequence(
                    Actions.fadeOut(0.2f),
                    Actions.visible(false)
                ));
            }
        });
        add(btnCerrar).colspan(2).padTop(15).width(80);

        pack();
        setSize(fixedWidth, fixedHeight);
    }

    private void mostrarOpcionesConexion() {
        clearChildren();

        Label infoLabel = new Label("Selecciona una opción", blackLabelStyle);

        TextButton btnLogin = new TextButton("Iniciar Sesión", skin);
        btnLogin.addListener(new Assets.HoverCursorListener());
        btnLogin.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mostrarLogin();
            }
        });

        TextButton btnRegister = new TextButton("Crear Cuenta", skin);
        btnRegister.addListener(new Assets.HoverCursorListener());
        btnRegister.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mostrarRegistro();
            }
        });

        TextButton btnVolver = new TextButton("Volver", skin);
        btnVolver.addListener(new Assets.HoverCursorListener());
        btnVolver.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                actualizarInterfaz();
            }
        });

        add(infoLabel).padTop(8).padBottom(14).padLeft(14).padRight(14).colspan(2).center().row();
        add(btnLogin).colspan(2).pad(8).width(135).row();
        add(btnRegister).colspan(2).pad(8).width(120).row();
        add(btnVolver).colspan(2).padTop(18).width(80);

        pack();
        setSize(fixedWidth, fixedHeight);
    }

    private void procesarDatosNube(String loginMessage) {
        // 1. AHORA ROMPEMOS POR "|||" EN VEZ DE POR ","
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

        // --- 2. RECUPERAMOS EL JSON DE ARMAS CUSTOM (Índice 9) ---
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
        // ----------------------------------------------------------

        // Aplicamos el resto del progreso estándar
        SaveManager.aplicarDatosNube(playerId, cloudCoins, cloudScore, moko, zuki);
        SaveManager.aplicarArmasNube(armasNubeArray);
        SaveManager.aplicarMapasNube(mapDesert, mapCave);
        SaveManager.aplicarGadgetsNube(gadgetsNubeArray);
    }

    private void mostrarLogin() {
        clearChildren();
        Label titulo = new Label("Iniciar Sesión", blackLabelStyle);
        titulo.setColor(Color.BLACK);

        final TextField userField = new TextField("", skin);
        userField.setMessageText("Usuario");

        final TextField passField = new TextField("", skin);
        passField.setMessageText("Contraseña");
        passField.setPasswordMode(true);
        passField.setPasswordCharacter('*');

        final TextButton btnOjo = new TextButton("Ver", skin);
        btnOjo.addListener(new Assets.HoverCursorListener());
        btnOjo.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                passField.setPasswordMode(!passField.isPasswordMode());
                btnOjo.setText(passField.isPasswordMode() ? "Ver" : "Ocultar");
            }
        });

        Table passTable = new Table();
        passTable.add(passField).width(130);
        passTable.add(btnOjo).padLeft(5).width(60);

        final Label errorLabel = new Label("", skin);
        errorLabel.setColor(Color.RED);
        errorLabel.setWrap(true);
        errorLabel.setAlignment(Align.center);

        final TextButton btnAceptar = new TextButton("Aceptar", skin);
        btnAceptar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                errorLabel.setText("");

                final String user = userField.getText();
                final String pass = passField.getText();

                if (user.isEmpty() || pass.isEmpty()) {
                    errorLabel.setText("Rellena todos los campos.");
                    pack();
                    return;
                }

                // --- NUEVA VALIDACIÓN DE LONGITUD AL LOGUEAR ---
                if (user.length() < 3 || user.length() > 16) {
                    errorLabel.setText("Nombre incorrecto");
                    pack();
                    return;
                }

                btnAceptar.setDisabled(true);
                btnAceptar.setText("Cargando...");

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
                        btnAceptar.setText("Aceptar");
                        pack();
                    }
                });
            }
        });

        TextButton btnVolver = new TextButton("Volver", skin);
        btnVolver.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mostrarOpcionesConexion();
            }
        });

        add(titulo).padTop(2).padBottom(4).colspan(2).center().row();
        add(userField).pad(2).width(195).colspan(2).row();
        add(passTable).pad(2).colspan(2).row();
        add(errorLabel).width(195).padTop(1).colspan(2).row();
        add(btnAceptar).padTop(1).padRight(5).width(85);
        add(btnVolver).padTop(1).padLeft(5).width(85);

        pack();
        setSize(fixedWidth, fixedHeight);
    }

    private void mostrarRegistro() {
        clearChildren();
        Label titulo = new Label("Crear Cuenta", blackLabelStyle);
        titulo.setColor(Color.BLACK);

        final TextField userField = new TextField("", skin);
        userField.setMessageText("Nuevo Usuario");

        final TextField passField1 = new TextField("", skin);
        passField1.setMessageText("Contraseña");
        passField1.setPasswordMode(true);
        passField1.setPasswordCharacter('*');

        final TextField passField2 = new TextField("", skin);
        passField2.setMessageText("Repetir Contraseña");
        passField2.setPasswordMode(true);
        passField2.setPasswordCharacter('*');

        final TextButton btnOjo = new TextButton("Ver", skin);
        btnOjo.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                boolean isOculto = passField1.isPasswordMode();
                passField1.setPasswordMode(!isOculto);
                passField2.setPasswordMode(!isOculto);
                btnOjo.setText(isOculto ? "Ocultar" : "Ver");
            }
        });

        Table passTable1 = new Table();
        passTable1.add(passField1).width(130);
        passTable1.add(btnOjo).padLeft(5).width(60);

        Table passTable2 = new Table();
        passTable2.add(passField2).width(130);
        passTable2.add().padLeft(5).width(60);

        final Label errorLabel = new Label("", skin);
        errorLabel.setColor(Color.RED);
        errorLabel.setWrap(true);
        errorLabel.setAlignment(Align.center);

        final TextButton btnAceptar = new TextButton("Aceptar", skin);
        btnAceptar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                errorLabel.setText("");

                final String user = userField.getText();
                final String pass1 = passField1.getText();
                String pass2 = passField2.getText();

                if (user.isEmpty() || pass1.isEmpty() || pass2.isEmpty()) {
                    errorLabel.setText("Campos vacios.");
                    pack();
                    return;
                }

                //Validar longitud name al registrarse
                if (user.length() < 3 || user.length() > 16) {
                    errorLabel.setText("El nombre debe tener entre 3 y 16 caracteres.");
                    pack();
                    return;
                }

                if (!pass1.equals(pass2)) {
                    errorLabel.setText("Las contraseñas no coinciden.");
                    pack();
                    return;
                }

                btnAceptar.setDisabled(true);
                btnAceptar.setText("Creando...");

                menuScreen.getAuthManager().registrarJugador(user, pass1, new AuthCallback() {
                    @Override
                    public void onSuccess(String message) {
                        SaveManager.markLocalAsLinked();

                        menuScreen.getAuthManager().iniciarSesion(user, pass1, new AuthCallback() {
                            @Override
                            public void onSuccess(String loginMessage) {
                                procesarDatosNube(loginMessage); // Llama al método centralizado

                                menuScreen.isConnected = true;
                                menuScreen.username = user;
                                SaveManager.saveLogin(user, pass1);

                                menuScreen.actualizarSpriteCuenta();
                                actualizarInterfaz();
                            }

                            @Override
                            public void onError(String error) {
                                errorLabel.setText("Cuenta creada, pero falló el autologin.");
                                btnAceptar.setDisabled(false);
                            }
                        });
                    }

                    @Override
                    public void onError(String errorMessage) {
                        errorLabel.setText(errorMessage);
                        btnAceptar.setDisabled(false);
                        btnAceptar.setText("Aceptar");
                        pack();
                    }
                });
            }
        });

        TextButton btnVolver = new TextButton("Volver", skin);
        btnVolver.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mostrarOpcionesConexion();
            }
        });

        add(titulo).padTop(2).padBottom(4).colspan(2).center().row();
        add(userField).pad(2).width(195).colspan(2).row();
        add(passTable1).pad(2).colspan(2).row();
        add(passTable2).pad(1).colspan(2).row();
        add(errorLabel).width(195).padTop(1).colspan(2).row();
        add(btnAceptar).padTop(1).padRight(5).width(85);
        add(btnVolver).padTop(1).padLeft(5).width(85);

        pack();
        setSize(fixedWidth, fixedHeight);
    }
}
