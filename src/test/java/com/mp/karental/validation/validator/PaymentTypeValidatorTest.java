package com.mp.karental.validation.validator;
import com.mp.karental.constant.EPaymentType;
import com.mp.karental.validation.validator.PaymentTypeValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentTypeValidatorTest {

    private PaymentTypeValidator paymentTypeValidator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        paymentTypeValidator = new PaymentTypeValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void testValidPaymentType_Wallet() {
        assertTrue(paymentTypeValidator.isValid(EPaymentType.WALLET, context));
    }

    @Test
    void testValidPaymentType_BankTransfer() {
        assertTrue(paymentTypeValidator.isValid(EPaymentType.BANK_TRANSFER, context));
    }

    @Test
    void testValidPaymentType_Cash() {
        assertTrue(paymentTypeValidator.isValid(EPaymentType.CASH, context));
    }

    @Test
    void testInvalidPaymentType_Null() {
        assertFalse(paymentTypeValidator.isValid(null, context));
    }
}

