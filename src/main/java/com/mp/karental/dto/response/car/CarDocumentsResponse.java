package com.mp.karental.dto.response.car;

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
public class CarDocumentsResponse {

    String id;

    //documents
    String registrationPaperUrl;
    boolean registrationPaperUriIsVerified;
    String certificateOfInspectionUrl;
    boolean certificateOfInspectionUriIsVerified;
    String insuranceUrl;
    boolean insuranceUriIsVerified;

}