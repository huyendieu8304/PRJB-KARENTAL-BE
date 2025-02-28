package com.mp.karental.constant;

import lombok.Getter;
/**
 * Represents the image type
 * @author QuangPM20
 *
 * @version 1.0
 */
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
