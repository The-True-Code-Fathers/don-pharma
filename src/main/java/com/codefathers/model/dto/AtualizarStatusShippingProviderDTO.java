package com.codefathers.model.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtualizarStatusShippingProviderDTO {
    private UUID id;
    private boolean active;
}
