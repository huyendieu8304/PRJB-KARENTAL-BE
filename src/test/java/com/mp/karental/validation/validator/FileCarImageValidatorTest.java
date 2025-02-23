package com.mp.karental.validation.validator;

import com.mp.karental.constant.ECarImage;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileCarImageValidatorTest {

    private FileCarImageValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new FileCarImageValidator();
    }

    @Test
    void testValidFileExtensions() {
        MultipartFile jpgFile = new MockMultipartFile("file", "car.jpg", "image/jpeg", new byte[10]);
        MultipartFile pngFile = new MockMultipartFile("file", "car.png", "image/png", new byte[10]);

        assertTrue(validator.isValid(jpgFile, context));
        assertTrue(validator.isValid(pngFile, context));
    }

    @Test
    void testInvalidFileExtensions() {
        MultipartFile pdfFile = new MockMultipartFile("file", "car.pdf", "application/pdf", new byte[10]);
        MultipartFile txtFile = new MockMultipartFile("file", "car.txt", "text/plain", new byte[10]);

        assertFalse(validator.isValid(pdfFile, context));
        assertFalse(validator.isValid(txtFile, context));
    }

    @Test
    void testNullFile() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    void testEmptyFile() {
        MultipartFile emptyFile = new MockMultipartFile("file", "empty.jpg", "image/jpeg", new byte[0]);

        assertTrue(validator.isValid(emptyFile, context));
    }

    @Test
    void testFileWithoutExtension() {
        MultipartFile noExtensionFile = new MockMultipartFile("file", "car", "image/jpeg", new byte[10]);

        assertFalse(validator.isValid(noExtensionFile, context));
    }
}
