package com.mp.karental.validation.validator;

import com.mp.karental.constant.ETransactionType;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionTypeValidatorTest {

    private TransactionTypeValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new TransactionTypeValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void testValidTransactionTypes() {
        assertTrue(validator.isValid(ETransactionType.TOP_UP, context), "Expected TOP_UP to be valid");
        assertTrue(validator.isValid(ETransactionType.WITHDRAW, context), "Expected WITHDRAW to be valid");
        assertTrue(validator.isValid(ETransactionType.PAY_DEPOSIT, context), "Expected PAY_DEPOSIT to be valid");
        assertTrue(validator.isValid(ETransactionType.RECEIVE_DEPOSIT, context), "Expected RECEIVE_DEPOSIT to be valid");
        assertTrue(validator.isValid(ETransactionType.REFUND_DEPOSIT, context), "Expected REFUND_DEPOSIT to be valid");
        assertTrue(validator.isValid(ETransactionType.OFFSET_FINAL_PAYMENT, context), "Expected OFFSET_FINAL_PAYMENT to be valid");
    }

    @Test
    void testInvalidTransactionType_Null() {
        assertFalse(validator.isValid(null, context), "Expected null to be invalid");
    }
}
