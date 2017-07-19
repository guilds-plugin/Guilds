package me.glaremasters.guilds.database;

public interface Callback<T, E extends Exception> {
    void call(T result, E exception);
}
