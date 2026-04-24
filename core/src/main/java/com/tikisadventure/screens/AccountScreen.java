package com.tikisadventure.screens;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class AccountScreen extends Window {

    private MenuScreen menuScreen;
    private Skin skin;

    public AccountScreen(Skin skin, MenuScreen menuScreen) {
        super("Gestión de Cuenta", skin);
        this.skin = skin;
        this.menuScreen = menuScreen;

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
        userField.setMessageText("Usuario"); // Texto de fondo (Placeholder)

        final TextField passField = new TextField("", skin);
        passField.setMessageText("Contraseña");
        passField.setPasswordMode(true);
        passField.setPasswordCharacter('*');

        // Botón Ojo
        final TextButton btnOjo = new TextButton("Ver", skin);
        // Si quieres usar imágenes:
        // 1. Cambia TextButton por ImageButton
        // 2. En el listener, cambia su style.imageUp por el sprite de ojo abierto/cerrado.
        btnOjo.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                passField.setPasswordMode(!passField.isPasswordMode());
                btnOjo.setText(passField.isPasswordMode() ? "Ver" : "Ocultar");
            }
        });

        // Agrupamos la contraseña y su botón en una sub-tabla para que queden en la misma línea
        Table passTable = new Table();
        passTable.add(passField).width(150);
        passTable.add(btnOjo).padLeft(5).width(60);

        TextButton btnAceptar = new TextButton("Aceptar", skin);
        btnAceptar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String user = userField.getText();
                String pass = passField.getText();

                if (user.isEmpty() || pass.isEmpty()) {
                    System.out.println("Error: Rellena todos los campos");
                    return;
                }

                // TODO: AQUÍ IRÁ LA PETICIÓN LOGIN A SUPABASE
                System.out.println("Intentando login con: " + user);

                // Mock temporal (asumimos éxito)
                menuScreen.isConnected = true;
                menuScreen.username = user;
                menuScreen.actualizarSpriteCuenta();
                actualizarInterfaz(); // Vuelve al menú de cuenta conectada
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
        add(userField).pad(5).width(215).colspan(2).row(); // 150 + 5 + 60 = 215 (para que alinee)
        add(passTable).pad(5).colspan(2).row();

        add(btnAceptar).padTop(15).padRight(5).width(100);
        add(btnVolver).padTop(15).padLeft(5).width(100);

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

        // Un solo botón para mostrar/ocultar ambas contraseñas es mejor para el usuario
        final TextButton btnOjo = new TextButton("Ver", skin);
        btnOjo.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                boolean isOculto = passField1.isPasswordMode(); // Comprobamos el estado actual
                passField1.setPasswordMode(!isOculto);
                passField2.setPasswordMode(!isOculto);
                btnOjo.setText(isOculto ? "Ocultar" : "Ver");
            }
        });

        Table passTable1 = new Table();
        passTable1.add(passField1).width(150);
        passTable1.add(btnOjo).padLeft(5).width(60);

        // La segunda tabla no necesita botón, pero le ponemos un espacio vacío para alinear
        Table passTable2 = new Table();
        passTable2.add(passField2).width(150);
        passTable2.add().padLeft(5).width(60);

        TextButton btnAceptar = new TextButton("Aceptar", skin);
        btnAceptar.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String user = userField.getText();
                String pass1 = passField1.getText();
                String pass2 = passField2.getText();

                if (user.isEmpty() || pass1.isEmpty() || pass2.isEmpty()) {
                    System.out.println("Error: Rellena todos los campos");
                    return;
                }

                if (!pass1.equals(pass2)) {
                    System.out.println("Error: Las contraseñas no coinciden");
                    // Aquí podrías añadir un Label de error en rojo en la interfaz más adelante
                    return;
                }

                // TODO: AQUÍ IRÁ LA PETICIÓN REGISTRO A SUPABASE
                System.out.println("Registrando cuenta para: " + user);

                // Mock temporal (asumimos éxito y logueamos)
                menuScreen.isConnected = true;
                menuScreen.username = user;
                menuScreen.actualizarSpriteCuenta();
                actualizarInterfaz();
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

        add(btnAceptar).padTop(15).padRight(5).width(100);
        add(btnVolver).padTop(15).padLeft(5).width(100);

        pack();
    }
}
