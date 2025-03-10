package com.mp.karental.constant;

import lombok.Getter;
/**
 * Represents the status of a car in the system
 * @author QuangPM20
 *
 * @version 1.0
 */
@Getter
public enum ECarStatus {
    NOT_VERIFIED,
    VERIFIED, //customer could rent this car
    STOPPED, //car owner no longer lend this car anymore, this can only change to NOT_VERIFIED
}
