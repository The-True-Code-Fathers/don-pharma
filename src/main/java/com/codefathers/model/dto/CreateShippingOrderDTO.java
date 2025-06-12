package com.codefathers.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.codefathers.model.entity.ShippingProvider;
import com.codefathers.model.enums.ShippingServiceStatus;

import com.codefathers.repository.interfaces.ShippingProviderRepository;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CreateShippingOrderDTO {
    @NotNull(message = "O ID do provedor de entrega não pode ser nulo")
    private UUID shippingProviderId;  // Alterado para UUID

    @NotBlank(message = "O estado de destino é obrigatório")
    @Size(min = 2, max = 2, message = "O estado deve ser o código com 2 letras (ex: SP)")
    private String destinationState;

    @NotBlank(message = "A cidade de destino é obrigatória")
    private String destinationCity;

    @NotNull(message = "O peso é obrigatório")
    @DecimalMin(value = "0.01", message = "O peso deve ser maior que zero")
    private BigDecimal weight;

    @NotNull(message = "O status é obrigatório")
    private ShippingServiceStatus status;

    @Min(value = 0, message = "Os dias estimados para entrega não podem ser negativos")
    private Integer estimatedDeliveryDays;

    @FutureOrPresent(message = "A data de entrega deve ser hoje ou no futuro")
    private LocalDate deliveryDate;

    @PastOrPresent(message = "A data de envio não pode ser futura")
    private LocalDate shipmentDate;

    @DecimalMin(value = "0.0", inclusive = false, message = "O custo do frete deve ser positivo")
    private BigDecimal shippingCost;

    // Método auxiliar para obter o provedor (opcional)
    public ShippingProvider getShippingProvider(ShippingProviderRepository repository) {
        if (shippingProviderId == null) {
            return null;
        }
        return repository.searchShippingProviderPerId(shippingProviderId);
    }
}