package com.tikisadventure.database.core;

//Interfaz con dos métodos: uno para cuando la petición a la BD
//sale bien y otro para cuando falla. Los repositorios la usan
//para devolver el resultado sin bloquear el juego.
public interface AuthCallback {
    void onSuccess(String message);
    void onError(String errorMessage);
}
