package com.mp.karental.constant;

import lombok.Getter;

@Getter
public enum EAdditionalFunctions {
    BLUETOOTH("Bluetooth"),
    GPS("GPS"),
    CAMERA("Camera"),
    SUN_ROOF("Sun Roof"),
    CHILD_LOCK("Child Lock"),
    CHILD_SEAT("Child Seat"),
    DVD("DVD"),
    USB("USB");
    private final String name;

    EAdditionalFunctions(String name) {
        this.name = name;
    }
}

