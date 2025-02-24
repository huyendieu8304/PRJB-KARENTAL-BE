package com.mp.karental.dto.response;

import com.mp.karental.entity.Car;
import lombok.Data;
import java.util.List;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;

/**
 * Represents the response for a list of user's cars.
 * <p>
 * This class is used to return a paginated list of cars owned by a user.
 * </p>
 *
 * @author AnhHP9
 *
 * @version 1.0
 */


@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
public class ViewMyCarResponse {
    Page<Car> cars;
}
