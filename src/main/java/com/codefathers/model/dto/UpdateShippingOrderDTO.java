package com.codefathers.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.codefathers.model.enums.ShippingServiceStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class UpdateShippingOrderDTO {
    
    @NotNull
    private UUID id;
    
    @NotNull
    private UUID shippingProviderId;
    
    @NotNull
    private String destinationState;
    
    @NotNull
    private String destinationCity;
    
    @NotNull
    private BigDecimal weight;
    
    @NotNull
    private ShippingServiceStatus status;
    
    @NotNull
    private Integer estimatedDeliveryDays;
    
    @NotNull
    private LocalDate shipmentDate;
    
    @NotNull
    private LocalDate deliveryDate;
    
    @NotNull
    private BigDecimal shippingCost;

    @NotNull
    private Boolean active;

}
