package com.mp.karental.validation.validator;

import com.mp.karental.validation.ValidProductionYear;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

/**
 * test production year validator
 *
 * QuangPM20
 * version 1.0
 */
class ProductionYearValidatorTest {

    private ProductionYearValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new ProductionYearValidator();
    }

    @Test
    void testValidProductionYears() {
        assertTrue(validator.isValid(1990, context)); // Min year
        assertTrue(validator.isValid(2000, context)); // Middle range
        assertTrue(validator.isValid(2030, context)); // Max year
    }

    @Test
    void testInvalidProductionYears() {
        assertFalse(validator.isValid(1989, context)); // Below min year
        assertFalse(validator.isValid(2031, context)); // Above max year
    }

}
