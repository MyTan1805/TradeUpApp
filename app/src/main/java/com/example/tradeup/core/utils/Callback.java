package com.example.tradeup.core.utils;

public interface Callback<T> {
    void onSuccess(T data);
    void onFailure(Exception e);
}