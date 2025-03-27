package com.mp.karental.validation.validator;

import com.mp.karental.constant.ETransactionType;
import com.mp.karental.validation.ValidTransactionType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.EnumSet;

public class TransactionTypeValidator implements ConstraintValidator<ValidTransactionType, ETransactionType> {

    @Override
    public boolean isValid(ETransactionType type, ConstraintValidatorContext constraintValidatorContext) {
        return type!=null && EnumSet.allOf(ETransactionType.class).contains(type);
    }
}
