package com.mp.karental.constant;

import lombok.Getter;

@Getter
public enum EDocumentFile {
    DOC("doc"),
    DOCX("docx"),
    PDF("pdf"),
    JPEG("jpeg"),
    JPG("jpg"),
    PNG("png");
    private final String extension;

    EDocumentFile(String extension) {
        this.extension = extension;
    }
}
