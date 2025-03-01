package top.mrxiaom.sweetmail.utils;

import org.jetbrains.annotations.Nullable;

public class Result<T> {
    private final T value;
    @Nullable
    private final String error;

    public Result(T value, @Nullable String error) {
        this.value = value;
        this.error = error;
    }

    public T getValue() {
        if (error != null) {
            throw new IllegalStateException(error);
        }
        return value;
    }

    @Nullable
    public String getError() {
        return error;
    }

    public static <T> Result<T> success(T value) {
        return new Result<>(value, null);
    }

    public static <T> Result<T> fail(String exception) {
        return new Result<>(null, exception);
    }
}
