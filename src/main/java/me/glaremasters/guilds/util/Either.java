package me.glaremasters.guilds.util;

import java.util.function.Consumer;
import java.util.function.Function;

public class Either<E extends Exception, R> {
    private E error;
    private R result;

    public Either(E error, R result) {
        this.error = error;
        this.result = result;
    }

    public E getError() {
        return error;
    }

    public R getResult() {
        return result;
    }

    public void setError(E error) {
        this.error = error;
    }

    public void setResult(R result) {
        this.result = result;
    }

    public void fold(Consumer<E> right, Consumer<R> result) {
        if (this.error != null) {
            right.accept(error);
        }
        if (this.result != null) {
            result.accept(this.result);
        }
    }

    public void map(Function<E, E> left, Function<R, R> right) {
        this.error = left.apply(this.error);
        this.result = right.apply(this.result);
    }
}
