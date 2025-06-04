package com.codefathers.model.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Data;

@Data
@Entity(name = "shipping_area")
public class ShippingArea {
    
    @Id
    private UUID id;

    @OneToOne
    @JoinColumn(name = "id")
    private ShippingProvider shippingProvider;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private String states;

}
