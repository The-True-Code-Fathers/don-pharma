package com.codefathers.util;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.Getter;

public class ValidationUtil {

    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    @Getter
    private static final Validator validator = factory.getValidator();

    private ValidationUtil() {
        // Utility class – prevent instantiation
    }

    public static void shutdown() {
        factory.close();
    }
}
