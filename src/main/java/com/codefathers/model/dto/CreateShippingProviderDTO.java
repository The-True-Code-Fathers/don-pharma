package com.codefathers.model.dto;

import jakarta.validation.constraints.Pattern;

public class CreateShippingProviderDTO {

    @Pattern (
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "Deve conter pelo menos uma letra"
    )
    private String shippingID;


}
