package com.tikisadventure.database;

public interface AuthCallback {
    void onSuccess(String message);
    void onError(String errorMessage);
}
