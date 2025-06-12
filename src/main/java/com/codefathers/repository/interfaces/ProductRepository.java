package com.codefathers.repository.interfaces;

import com.codefathers.model.entity.Product;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    void save(Product product);

    void update(Product product);

    void delete(UUID id);

    Optional<Product> findBySKU(String productSku);

    List<Product> listAll();
}
