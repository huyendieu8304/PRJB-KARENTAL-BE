package com.mp.karental.constant;

import lombok.Getter;

@Getter
public enum ECarImage {
    JPG("jpg"),
    JPEG("jpeg"),
    PNG("png"),
    GIF("gif");
    private final String extension;

    ECarImage(String extension) {
        this.extension = extension;
    }
}
