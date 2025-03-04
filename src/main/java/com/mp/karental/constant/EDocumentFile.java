package com.mp.karental.constant;

import lombok.Getter;

/**
 * Represents the document type
 * @author QuangPM20
 *
 * @version 1.0
 */
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
