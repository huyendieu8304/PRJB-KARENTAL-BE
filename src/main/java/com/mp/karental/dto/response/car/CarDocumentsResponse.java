package com.mp.karental.dto.response.car;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * DTO representing the response containing car document details.
 * This class encapsulates the document URLs along with their verification statuses.
 *
 * <p>It uses Lombok annotations to reduce boilerplate code for getters, setters, and constructors.
 * The {@code @Builder(toBuilder = true)} annotation allows for modifying fields after object creation.</p>
 *
 * @author AnhHP9
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@Schema(description = "Response containing car document details and their verification statuses.")
public class CarDocumentsResponse {

    @Schema(description = "Unique identifier for the car document", example = "123e4567-e89b-12d3-a456-426614174000")
    String id;

    // Documents
    @Schema(description = "URL of the car's registration paper", example = "https://example.com/registration_paper.pdf")
    String registrationPaperUrl;

    @Schema(description = "Indicates whether the registration paper is verified", example = "true")
    boolean registrationPaperUriIsVerified;

    @Schema(description = "URL of the car's certificate of inspection", example = "https://example.com/certificate_of_inspection.pdf")
    String certificateOfInspectionUrl;

    @Schema(description = "Indicates whether the certificate of inspection is verified", example = "true")
    boolean certificateOfInspectionUriIsVerified;

    @Schema(description = "URL of the car's insurance document", example = "https://example.com/insurance.pdf")
    String insuranceUrl;

    @Schema(description = "Indicates whether the insurance document is verified", example = "false")
    boolean insuranceUriIsVerified;
}