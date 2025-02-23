package com.mp.karental.validation.validator;

import com.mp.karental.constant.EDocumentFile;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileDocumentValidatorTest {

    private FileDocumentValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new FileDocumentValidator();
    }

    @Test
    void testValidFileExtensions() {
        MultipartFile pdfFile = new MockMultipartFile("file", "document.pdf", "application/pdf", new byte[10]);
        MultipartFile docxFile = new MockMultipartFile("file", "document.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", new byte[10]);

        assertTrue(validator.isValid(pdfFile, context));
        assertTrue(validator.isValid(docxFile, context));
    }

    @Test
    void testInvalidFileExtensions() {
        MultipartFile jpgFile = new MockMultipartFile("file", "image.jpg", "image/jpeg", new byte[10]);
        MultipartFile exeFile = new MockMultipartFile("file", "program.exe", "application/octet-stream", new byte[10]);

        assertTrue(validator.isValid(jpgFile, context));
        assertFalse(validator.isValid(exeFile, context));
    }

    @Test
    void testNullFile() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    void testEmptyFile() {
        MultipartFile emptyFile = new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0]);

        assertTrue(validator.isValid(emptyFile, context));
    }

    @Test
    void testFileWithoutExtension() {
        MultipartFile noExtensionFile = new MockMultipartFile("file", "document", "application/pdf", new byte[10]);

        assertFalse(validator.isValid(noExtensionFile, context));
    }
}
