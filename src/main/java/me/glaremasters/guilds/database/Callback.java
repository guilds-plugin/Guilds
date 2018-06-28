package me.glaremasters.guilds.database;

/**
 * Created by GlareMasters on 6/28/2018.
 */
public interface Callback<T, E extends Exception> {

    void call(T result, E exception);
}
