package com.tikisadventure.database.core;

public interface AuthCallback {
    void onSuccess(String message);
    void onError(String errorMessage);
}
