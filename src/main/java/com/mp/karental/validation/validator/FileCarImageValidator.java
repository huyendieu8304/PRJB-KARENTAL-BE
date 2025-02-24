package com.mp.karental.validation.validator;

import com.mp.karental.constant.ECarImage;
import com.mp.karental.constant.EDocumentFile;
import com.mp.karental.validation.ValidImageCar;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class FileCarImageValidator implements ConstraintValidator<ValidImageCar, MultipartFile> {
    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        if (file == null || file.isEmpty()) {
            return true;  // Skip validation if the file is null or empty (assuming required validation is done with @NotNull)
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }

        // Get the file extension
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();

        // Validate the file extension using the isValidExtension method in the validator
        return isValidExtension(fileExtension);
    }

    private boolean isValidExtension(String extension) {
        for (ECarImage fileType : ECarImage.values()) {
            if (fileType.getExtension().equalsIgnoreCase(extension)) {
                return true;  // Extension is valid
            }
        }
        return false;  // Extension is not valid
    }
}
