package com.codefathers.model.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.codefathers.model.enums.MeasurementUnit;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "product")
public class Product {
    
    @Id
    @Column(length = 50)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private MeasurementUnit measurementUnit;

    @Column(columnDefinition = "text")
    private String description;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
