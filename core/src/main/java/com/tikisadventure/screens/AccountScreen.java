package com.tikisadventure.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.tikisadventure.core.SaveManager;
import com.tikisadventure.database.SupabaseAuth;
import com.tikisadventure.database.AuthCallback;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Align;

public class AccountScreen extends Window {

    private MenuScreen menuScreen;
    private Skin skin;
    private SupabaseAuth authManager;

    public AccountScreen(Skin skin, MenuScreen menuScreen) {
        super("Gestión de Cuenta", skin);
        this.skin = skin;
        this.menuScreen = menuScreen;
        this.authManager = new SupabaseAuth();

        setModal(true);
        setMovable(false);
        padTop(40);

        actualizarInterfaz();
    }

    public void actualizarInterfaz() {
        clearChildren();

        if (menuScreen.isConnected) {
            // --- ESTADO: CONECTADO ---
            Label userLabel = new Label("Usuario: " + menuScreen.username, skin);
            TextButton btnDisconnect = new TextButton("Desconectar", skin);

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

            add(userLabel).pad(15).row();
            add(btnDisconnect).pad(10).width(160).row();

        } else {
            // --- ESTADO: LOCAL ---
            Label localLabel = new Label("Jugando en Local", skin);
            TextButton btnConnect = new TextButton("Conectar", skin);

            btnConnect.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    mostrarOpcionesConexion();
                }
            });

            add(localLabel).pad(15).row();
            add(btnConnect).pad(10).width(160).row();
        }

        TextButton btnCerrar = new TextButton("Cerrar", skin);
        btnCerrar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                setVisible(false);
            }
        });
        add(btnCerrar).padTop(25).width(100);

        pack();
    }

    private void mostrarOpcionesConexion() {
        clearChildren();

        Label infoLabel = new Label("Selecciona una opción", skin);

        TextButton btnLogin = new TextButton("Iniciar Sesión", skin);
        btnLogin.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mostrarLogin(); // Llamamos a la nueva pantalla
            }
        });

        TextButton btnRegister = new TextButton("Crear Cuenta", skin);
        btnRegister.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                mostrarRegistro(); // Llamamos a la nueva pantalla
            }
        });

        TextButton btnVolver = new TextButton("Volver", skin);
        btnVolver.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                actualizarInterfaz();
            }
        });

        add(infoLabel).pad(15).colspan(2).center().row();
        add(btnLogin).pad(10).width(140);
        add(btnRegister).pad(10).width(140).row();
        add(btnVolver).padTop(25).colspan(2).width(100);

        pack();
    }

    private void mostrarLogin() {
        clearChildren();

        Label titulo = new Label("Iniciar Sesión", skin);

        final TextField userField = new TextField("", skin);
        userField.setMessageText("Usuario");

        final TextField passField = new TextField("", skin);
        passField.setMessageText("Contraseña");
        passField.setPasswordMode(true);
        passField.setPasswordCharacter('*');

        final TextButton btnOjo = new TextButton("Ver", skin);
        btnOjo.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                passField.setPasswordMode(!passField.isPasswordMode());
                btnOjo.setText(passField.isPasswordMode() ? "Ver" : "Ocultar");
            }
        });

        Table passTable = new Table();
        passTable.add(passField).width(150);
        passTable.add(btnOjo).padLeft(5).width(60);

        // --- NUEVO: Etiqueta de Error ---
        final Label errorLabel = new Label("", skin);
        errorLabel.setColor(Color.RED); // Texto en rojo
        errorLabel.setWrap(true);       // Permite que el texto baje de línea si es muy largo
        errorLabel.setAlignment(Align.center);

        final TextButton btnAceptar = new TextButton("Aceptar", skin);
        btnAceptar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // 1. Limpiamos cualquier error previo al intentar de nuevo
                errorLabel.setText("");

                final String user = userField.getText();
                final String pass = passField.getText();

                if (user.isEmpty() || pass.isEmpty()) {
                    errorLabel.setText("Rellena todos los campos.");
                    pack(); // Reajusta la ventana
                    return;
                }

                btnAceptar.setDisabled(true);
                btnAceptar.setText("Cargando...");

                menuScreen.getAuthManager().iniciarSesion(user, pass, new com.tikisadventure.database.AuthCallback() {
                    @Override
                    public void onSuccess(String message) {
                        int cloudCoins = Integer.parseInt(message);
                        menuScreen.isConnected = true;
                        menuScreen.username = user;
                        com.tikisadventure.core.SaveManager.setCoins(cloudCoins);
                        com.tikisadventure.core.SaveManager.saveLogin(user, pass);
                        menuScreen.actualizarSpriteCuenta();
                        actualizarInterfaz();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // 2. Mostramos el error en la interfaz
                        errorLabel.setText(errorMessage);
                        btnAceptar.setDisabled(false);
                        btnAceptar.setText("Aceptar");
                        pack(); // Reajusta el tamaño de la ventana por si el texto ocupa más de una línea
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

        // Construcción visual
        add(titulo).pad(10).colspan(2).row();
        add(userField).pad(5).width(215).colspan(2).row();
        add(passTable).pad(5).colspan(2).row();

        // Añadimos el errorLabel justo encima de los botones
        add(errorLabel).width(250).padTop(10).colspan(2).row();

        add(btnAceptar).padTop(10).padRight(5).width(100);
        add(btnVolver).padTop(10).padLeft(5).width(100);

        pack();
    }

    private void mostrarRegistro() {
        clearChildren();

        Label titulo = new Label("Crear Cuenta", skin);

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
        passTable1.add(passField1).width(150);
        passTable1.add(btnOjo).padLeft(5).width(60);

        Table passTable2 = new Table();
        passTable2.add(passField2).width(150);
        passTable2.add().padLeft(5).width(60);

        // --- NUEVO: Etiqueta de Error ---
        final Label errorLabel = new Label("", skin);
        errorLabel.setColor(Color.RED);
        errorLabel.setWrap(true);
        errorLabel.setAlignment(Align.center);

        final TextButton btnAceptar = new TextButton("Aceptar", skin);
        btnAceptar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                errorLabel.setText(""); // Limpiar error previo

                final String user = userField.getText();
                final String pass1 = passField1.getText();
                String pass2 = passField2.getText();

                if (user.isEmpty() || pass1.isEmpty() || pass2.isEmpty()) {
                    errorLabel.setText("Rellena todos los campos.");
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

                menuScreen.getAuthManager().registrarJugador(user, pass1, new com.tikisadventure.database.AuthCallback() {
                    @Override
                    public void onSuccess(String message) {
                        menuScreen.isConnected = true;
                        menuScreen.username = user;
                        com.tikisadventure.core.SaveManager.saveLogin(user, pass1);
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

        // Construcción visual
        add(titulo).pad(10).colspan(2).row();
        add(userField).pad(5).width(215).colspan(2).row();
        add(passTable1).pad(5).colspan(2).row();
        add(passTable2).pad(5).colspan(2).row();

        // Añadimos el errorLabel justo encima de los botones
        add(errorLabel).width(250).padTop(10).colspan(2).row();

        add(btnAceptar).padTop(10).padRight(5).width(100);
        add(btnVolver).padTop(10).padLeft(5).width(100);

        pack();
    }
}
