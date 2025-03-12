package com.mp.karental.dto.response.booking;

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
public class WalletResponse {
    String id;
    long balance;

}
