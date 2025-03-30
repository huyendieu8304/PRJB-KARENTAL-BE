package com.mp.karental.dto.response.booking;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Represents the response payload for a wallet.
 * <p>
 * This class encapsulates wallet information,
 * including wallet details.
 * </p>
 * @author QuangPM20
 *
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Schema(name = "response.booking.WalletResponse", description = "Response DTO containing a information of the wallet.")
public class WalletResponse {
    @Schema(example = "123", description = "id of this wallet.")
    String id;
    @Schema(example = "999999", description = "balance of this wallet.")
    long balance;

}
