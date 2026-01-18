package com.clapter.httpautomator.platform;

import java.util.function.Supplier;

public class DeferredObject<T> {

    private Supplier<T> value;

    public DeferredObject(Supplier<T> supplier) {
        this.value = supplier;
    }

    public Supplier<T> get(){
        return value;
    }

}
